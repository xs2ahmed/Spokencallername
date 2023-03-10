package mahmed.net.spokencallername.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import mahmed.net.spokencallername.utils.Constants;

public class Settings {
	
	private static final String TAG = "Settings";
	
	private final boolean bSayCallerName;	
	private final String strCallAlertSpeech;	
	private int nCallerRepeatCount;
	private int nRepeatInterval;
	
	private final boolean bSaySMSerName;	
	private final boolean bReadSMS;
	private final boolean bNoReadUnknownSms;
	private final boolean bSpeakNumber;
	private final String strSMSAlertSpeech;
	
	
	
	// common settings
	private float fPitch =1;
	private float fSpeedRate = 1;
	private boolean bSuppressSpeechOnRotate = false;
	private final String strUnknownCallerText;
	
	private int volume = 5;

	/** 
	 * TODO default values should point to the same string resources like the preferences
	 * @param context
	 */
	public Settings(final Context context) 
	{
		final SharedPreferences preferences = context.getSharedPreferences(Constants.MY_SHARED_PREFERENCE, Context.MODE_PRIVATE);
			
		//get common settings
	
		fPitch = preferences.getInt("pitch", 1);
		fPitch = 1.0f + (0.05f * fPitch);
		fSpeedRate = preferences.getInt("speed", 1);
		fSpeedRate = 1.0f + (0.05f * fSpeedRate);		
		
		volume = preferences.getInt("speech_volume", 10);
		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		 
		int nMaxIndex = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
		
		volume = (int)(((float)nMaxIndex / 10) * (float)volume);  
		
		
		bSayCallerName = preferences.getBoolean("call_isenabled", true);
		
		//Utils.log(TAG, String.format("Spoken alert value is %b", bSayCallerName));
		
		bSaySMSerName = preferences.getBoolean("sms_isenabled", true);
		bSpeakNumber = preferences.getBoolean("speak_number", false);
		bNoReadUnknownSms = preferences.getBoolean("sms_disable_read_for_unknown", false);		
		nCallerRepeatCount = preferences.getInt("call_repeat_times",3);		
		
		// TODO: put hard coded strings to strings-locale.xml
		// Pick based on what the locale is.
		// This could be a bug on non english mobs if the user gets a call without ever starting the app
		// no preferences may available and in that case these english hard coded strings would cause mess..
		
		strCallAlertSpeech = preferences.getString("call_alert_speech", "call from %");		
		strSMSAlertSpeech = preferences.getString("sms_alert_speech", "you have a new message from %");
		nRepeatInterval = 1000 * preferences.getInt("call_repeat_seconds", 2);		
		
		bReadSMS = preferences.getBoolean("sms_read", false);
		
		// User may put empty setting here.. 
		// might not cause a crash but for test call we should have som 
		// possible issue after I have supported other languages, setting Unknown could be a problem in other languages..
		// Need to set this value based on locale, perhaps sling it into strings and pull from there.
		
		strUnknownCallerText = preferences.getString("unknown_caller_name", "Unknown");
		//if(strUnknownCallerText.length() ==0 )
		//	strUnknownCallerText = "Unknown";
		
		bSuppressSpeechOnRotate = preferences.getBoolean("suppress_speech_rotate", false);
		
		
		/////////////////// Log Settings //////////////////////////////		
		Utils.log(TAG, toString());	 
	}

	public boolean isStartSomething() 
	{
		return (isStartSayCaller() || isStartSaySMS());
	}	
	
	public String getSMSAlertSpeech() 
	{
		return strSMSAlertSpeech;
	}	
	
	public boolean isStartSayCaller() 
	{
		return bSayCallerName;
	}	

	public int getCallerRepeatTimes() 
	{
		
		return nCallerRepeatCount;
	}
	
	/**
	 * Interval in milli seconds between caller name announcements
	 * @return
	 */
	public int getCallerRepeatInterval() 
	{		
		return nRepeatInterval;
	}	
	
	public String getUnknownCallerText()
	{
		return strUnknownCallerText;
	}
	
	// SMS Settings
	
	public boolean isStartSaySMS() 
	{
		return bSaySMSerName;
	}
	public boolean readSMS() 
	{
		return bReadSMS;
	}	
	
	public boolean avoidReadingUnknownSms()
	{
		return bNoReadUnknownSms;
	}
	
	public String getCallAlertSpeech() 
	{
		return strCallAlertSpeech;
	}
	
	public float getPitch() 
	{		
		return fPitch;
	}
	
	public float getSpeed() 
	{		
		return fSpeedRate;
	}
	
	public int getVolume() 
	{		
		return volume;
	}
	
	public boolean suppressSpeechOnRotate()
	{
		return bSuppressSpeechOnRotate;
	}
	
	public String toString()
	{
		return String.format("bSayCallerName:%b strCallAlertSpeech:%s nCallerRepeatCount:%d nRepeatInterval:%d bSaySMSerName:%b bReadSMS:%b bNoReadUnknownSms:%b strSMSAlertSpeech:%s fPitch:%f fSpeedRate:%f bSuppressSpeechOnRotate:%b strUnknownCallerText:%s volume:%d", 
		bSayCallerName, 
		strCallAlertSpeech, 
		nCallerRepeatCount, 
		nRepeatInterval,		
		bSaySMSerName,
		bReadSMS,
		bNoReadUnknownSms,
		strSMSAlertSpeech,	
		fPitch,
		fSpeedRate,
		bSuppressSpeechOnRotate,
		strUnknownCallerText,
		volume);		
	}

	public boolean speakNumber() {
		// TODO Auto-generated method stub
		return bSpeakNumber;
	}
	

	
}