package mahmed.net.spokencallername;

//import junit.framework.Assert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import mahmed.net.spokencallername.services.*;
import mahmed.net.spokencallername.utils.Constants;
import mahmed.net.spokencallername.utils.Settings;
import mahmed.net.spokencallername.utils.Utils;

public class CallListener extends BroadcastReceiver {

	Context m_context;
	
	private static boolean onCall = false;	
	
	private static final String TAG = "CallListener";
	
	/**
	 * Called on application thread
	 */
	
	@Override
	public void onReceive(Context context, Intent intent) 
	{ 	
		m_context = context;
		
		Utils.log(TAG, String.format("onReceive..: %s/%s", intent.toString(),  intent.getStringExtra(TelephonyManager.EXTRA_STATE)));
		
		AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);	
		
		//Silent or on vibration then we don't do anything..
		if(am.getRingerMode() != AudioManager.RINGER_MODE_NORMAL)
		{
			Utils.log(TAG, "Silent mode..bye");
			ManagementService.stopService(m_context);
			return;
		}		
		
		String strAction = intent.getAction();
		
		//Assert.assertNotNull(strAction);
		
		Settings settings = new Settings(m_context);
		if(strAction.equals(android.telephony.TelephonyManager.ACTION_PHONE_STATE_CHANGED) && settings.isStartSayCaller())
		{			
			handleCallStateChanged(context, intent);					
		}
		
		else if(strAction.equals("android.provider.Telephony.SMS_RECEIVED") && settings.isStartSaySMS())
		{	
			Utils.log(TAG, "handleSmsReceived() .. invoked");
			handleSmsReceived(context, intent);
		}
		else 
		{
			Utils.log(TAG, String.format("invalid broadcast %s received", strAction));			
		}
		
		Utils.log(TAG, "end onReceive.."); 
		
	}
	
	/*
	 * Docs
    Broadcast intent action indicating that the call state (cellular) on the device has changed. 

	The EXTRA_STATE extra indicates the new call state. If the new state is RINGING, a second extra EXTRA_INCOMING_NUMBER provides the incoming phone number as a String. 

	Requires the READ_PHONE_STATE permission. 

	This was a sticky broadcast in version 1.0, but it is no longer sticky. Instead, use getCallState() to synchronously query the current call state.

	See Also
	EXTRA_STATE
	EXTRA_INCOMING_NUMBER
	getCallState()
	*/ 
	 	
	private void handleCallStateChanged(Context context, Intent intent)
	{
		Utils.log(TAG, "handleCallStateChanged()..");
		
				
		String newCallState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
		
		Utils.log(TAG, newCallState);
		
		if(newCallState.equals(TelephonyManager.EXTRA_STATE_RINGING))
		{
			if(onCall)
			{
				Utils.log(TAG, "Already on call..calling stopService()");
				ManagementService.stopService(m_context);
				return;
			}		
			
			String strPhoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
			
			if(strPhoneNumber != null)
			{
				Utils.log(TAG, "phone number is available startService() will be called");
				ManagementService.startService(m_context, strPhoneNumber, Constants.ALERT_TYPE_CALL,"");
			}
			
							
			
		}
		else if (newCallState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))
		{
			Utils.log(TAG, "off hook, stopping service");
			
			onCall = true; 
			ManagementService.stopService(m_context);
		}
		else if (newCallState.equals(TelephonyManager.EXTRA_STATE_IDLE))
		{
			Utils.log(TAG, "idle, stopping service");
			
			onCall = false;
			ManagementService.stopService(m_context);
		}
	}
		
	private void handleSmsReceived(Context context, Intent intent)
	{
		// if ringing etc or on call we don't wanna run the service at that time
		if (((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getCallState() != TelephonyManager.CALL_STATE_IDLE) 
			return;
		
		Utils.log(TAG, "handling sms received");
		
		final Bundle bundle = intent.getExtras();
		final Object[] pdusObj = (Object[]) bundle.get("pdus");

		
		Utils.log(TAG, String.format("pdu length is %d",pdusObj.length));
		
		final SmsMessage[] messages = new SmsMessage[pdusObj.length];
		
		String strNumber = "";
		
		StringBuffer strBufferSMSBody = new StringBuffer("");
		
		for (int i = 0; i < pdusObj.length; i++) 
		{
			messages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
			
			// Bug fix: check because in the Developer console there were some reports indicating a null pointer exception
			// calling getOriginatingAddress(); on the object
			
			if(messages[i] == null)
			{
				Utils.log(TAG, "message was null...");
				continue;
			}
			
			String strAddress = messages[i].getOriginatingAddress();
			if(strAddress == null)
			{
				Utils.log(TAG, "sms orignator was null");
				strAddress = "";
			}
			
			if(strNumber.equals(""))
			{			
				strNumber = strAddress;
			}
			
			if(strNumber.equals(strAddress))
				strBufferSMSBody.append(messages[i].getMessageBody());			
		}
		
		ManagementService.startService(m_context, strNumber, Constants.ALERT_TYPE_SMS, strBufferSMSBody.toString());
	}
}
