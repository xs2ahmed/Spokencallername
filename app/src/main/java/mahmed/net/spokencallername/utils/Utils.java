package mahmed.net.spokencallername.utils;
import mahmed.net.spokencallername.services.ManagementService;
//import junit.framework.Assert;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class Utils {
 
	/**
	 * internal method to test broadcast received
	 */
	public static final void messageT(Context context, String strMessage)
	{		
	//	Assert.assertNotNull(context);
		
		CharSequence text = strMessage;
		int duration = Toast.LENGTH_LONG;
		Toast toast = Toast.makeText(context, text, duration);
		toast.show();				
	}
	
	public static final void log(String strComponent, String strMessage)
	{			
			if(Log.isLoggable(strComponent, Log.INFO))
			{
				// Appeding packagename will help me get them out of logcat.
				Log.i(String.format("%s_%s", Constants.APP_PREFIX, strComponent), strMessage)	;
			}
	}
	
	public static String collectPlatformInfo(Context cont)
	{
		//PackageInfo pInfo = cont.getPackageManager().getPackageInfo(cont.getPackageName(), 0);
		//environemtn
		StringBuffer buffer = new StringBuffer();
		buffer.append(String.format("PRODUCT: %s \n", Build.PRODUCT)); 
		buffer.append(String.format("BRAND: %s \n", Build.BRAND));
		buffer.append(String.format("MODEL: %s \n", Build.MODEL));
		buffer.append(String.format("MANUFACTURER: %s \n", Build.MANUFACTURER));
		buffer.append(String.format("DEVICE: %s \n", Build.DEVICE));
		buffer.append(String.format("Build.VERSION.SDK_INT: %d \n", Build.VERSION.SDK_INT));
		buffer.append(String.format("Build.VERSION.SDK: %s \n", Build.VERSION.SDK));
		buffer.append(String.format("ServiceRunning: %b \n", isServiceRunning(cont)));
		buffer.append(String.format("Settings: %s \n", new Settings(cont).toString()));
		
		
		return buffer.toString();
		 
	}
	
	public static boolean isServiceRunning(Context c)
	{		
	    ActivityManager manager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	if(service.pid == android.os.Process.myPid())
	    	{
		        if (ManagementService.class.getName().equals(service.service.getClassName())) {
		            return true;
		        }
	    	}
	    }
	    return false;		
	}
	

	
	
	
	
}
