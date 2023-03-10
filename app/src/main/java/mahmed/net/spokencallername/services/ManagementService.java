package mahmed.net.spokencallername.services;


//import junit.framework.Assert;
import mahmed.net.spokencallername.R;
import mahmed.net.spokencallername.utils.Constants;
import mahmed.net.spokencallername.utils.Contact;
import mahmed.net.spokencallername.utils.Settings;
import mahmed.net.spokencallername.utils.SettingsTest;
import mahmed.net.spokencallername.utils.Utils;
import mahmed.net.spokencallername.workers.WorkManager;
import mahmed.net.spokencallername.workers.WorkManager.IWorkCompleteListener;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;


public class ManagementService extends Service implements IWorkCompleteListener {
	private class ServiceStatus {
		private static final int UNDEFINED = 0;
		private static final int CREATED = 1;
		private static final int STARTED = 2;

		private int nCurrent = UNDEFINED;
		private String strServingType = "";

		public ServiceStatus(int nStatus, String strType) {
			this.nCurrent = nStatus;
			this.strServingType = strType;
		}
	}

	ServiceStatus status = null;
	WorkManager workManager = null;
	Settings settings = null;
	private static final String TAG = "MGTSERVICE";
	int nData = -1;

	@Override
	public void onCreate() {
		super.onCreate();

		Utils.log(TAG, "onCreate..");
		// Only one instance of settings be created..
		// Avoid creating settings inside sub component.. instead pass this on..
		settings = new Settings(this);

		//not yet running...
		status = new ServiceStatus(ServiceStatus.CREATED, "");

		if (!settings.isStartSomething()) {
			stopSelf();
			Utils.log(TAG, "OnCreate() Nothing to do..  stopSelf()..");
			return;
		}


		//This was introduced to fix broken  voice on ICS.
		// Where there is a call ICS cpu goes high and it would not give 
		// much cpu time to my process , sometimes it would even kill it.
		// can check the adb shell top -m 10 processes to check this behaviour
		// startForeground() prevent chances of it being killed.


		//Intent is required to create notification on task bar for the foreGround service
		// In our case this notification will start service and send stop param in extra 

		/*
		Intent intent = new Intent(this, ManagementService.class);
		intent.putExtra(Constants.KEY_STOP_SERVICE, true);
		PendingIntent stopServiceIntent = PendingIntent.getService(this, 123, intent, 0);
		
		final Notification notification = new Notification(R.drawable.ic_launcher_foreground, null, 0);

		
		String notifTitle = getString(R.string.notification_title);
		String notifMessage = getString(R.string.notification_message);

		
		//notification.setLatestEventInfo(this, notifTitle, notifMessage, stopServiceIntent);
		startForeground(17, notification);

		 */

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			startMyOwnForeground();
		else
			startForeground(17, new Notification());


		workManager = new WorkManager(this);
	}

	@Override
	public void onDestroy() {
		Utils.log(TAG, "onDestroy..");

		if (workManager != null)
			workManager.cleanup();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Utils.log(TAG, "onStartCommand..");

		boolean stopMe = intent.getBooleanExtra(Constants.KEY_STOP_SERVICE, false);

		if (stopMe) {
			Utils.log(TAG, "stop intent received now stopping");
			stopSelf();
			return START_NOT_STICKY;
		}

		if (!settings.isStartSomething()) {
			workManager = null;
			// no need to call stop self as it was called on on create()
			return START_NOT_STICKY;
		}


		if (status.nCurrent == ServiceStatus.STARTED) {
			//already running to so something else, so do nothing in this case
			// this situation can occur if for example service was started because to 
			// announce a message and while message is still announced a call comes in ..
			// the call message would be ignored.

			// This is possibly a minor bug which is very less likely to occur
			if (status.strServingType.equals(Constants.ALERT_TYPE_SMS)) {
				//sms being announced and call came in ..
				// so stop work
				Utils.log(TAG, "SMS was announced and call came in..");
				workManager.cleanup();
				stopSelf();
				// here the worker will callback service and it will stop itself, call announcement will not be made
				// which is a bug but a small one..
			}
			return START_NOT_STICKY;
		}

		String strNumber = intent.getStringExtra(Constants.KEY_ALERT_INCOMING_NUMBER);
		String strType = intent.getStringExtra(Constants.KEY_ALERT_TYPE);
		String strSMSContent = intent.getStringExtra(Constants.KEY_ALERT_SMSCONTENT);

		//Assert.assertNotNull(strNumber);

		Utils.log(TAG, strNumber);
		Utils.log(TAG, strType);

		String strCaller = Contact.getCaller(strNumber, this, settings);

		if (strType.equals(Constants.ALERT_TYPE_CALL_TEST)) {
			settings = new SettingsTest(this);
		}
		//strSMSContent will valid for sms only
		workManager.start(this, settings, strCaller, strSMSContent, strType);

		status.nCurrent = (ServiceStatus.STARTED);
		status.strServingType = strType;

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void workCompleted() {
		Utils.log(TAG, "workCompleted()");
		stopSelf();
	}

	/**
	 * Meant for external components to start this service
	 *
	 * @param c             context of the Activity / App
	 * @param strNumber     phone number
	 * @param strAlertType  Altert Type
	 * @param strSMSContent sms message for sms type else empty
	 */
	public static void startService(Context c, String strNumber, String strAlertType, String strSMSContent) {
		//prepare to start service
		final Intent serviceIntent = new Intent(c, ManagementService.class);

		serviceIntent.putExtra(Constants.KEY_ALERT_INCOMING_NUMBER, strNumber);
		serviceIntent.putExtra(Constants.KEY_ALERT_TYPE, strAlertType);
		serviceIntent.putExtra(Constants.KEY_ALERT_SMSCONTENT, strSMSContent);

		c.startService(serviceIntent);
		Utils.log(TAG, "service start requested");
	}

	/**
	 * Called by external components to stop this service
	 *
	 * @param c
	 */
	public static void stopService(Context c) {
		final Intent serviceIntent = new Intent(c, ManagementService.class);
		c.stopService(serviceIntent);
		Utils.log(TAG, "service stop requested");
	}

	private void startMyOwnForeground() {
		String NOTIFICATION_CHANNEL_ID = "mahmed.net.spokencallername";
		String channelName = "My Background Service";
		NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
		chan.setLightColor(Color.BLUE);
		chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		assert manager != null;
		manager.createNotificationChannel(chan);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
		Notification notification = notificationBuilder.setOngoing(true)
				.setSmallIcon(R.drawable.ic_launcher_foreground)
				.setContentTitle("App is running in background")
				.setPriority(NotificationManager.IMPORTANCE_HIGH)
				.setCategory(Notification.CATEGORY_SERVICE)
				.build();
		startForeground(2, notification);

	}
}
