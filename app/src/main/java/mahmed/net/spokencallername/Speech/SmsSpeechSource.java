package mahmed.net.spokencallername.Speech;

import mahmed.net.spokencallername.utils.Settings;
import mahmed.net.spokencallername.utils.Utils;
import mahmed.net.spokencallername.workers.VolumeManager;
import android.content.Context;
import android.media.AudioManager;

/**
 * Speech source implementaion
 * Will speak sender name and message (ifset) 
 * @author Ahmed
 *
 */
public class SmsSpeechSource implements ISpeechSource
{	
	private static final String TAG = "Spoken_SmsSpeechSource";	
	private VolumeManager volMan = null;
	private String strSender = "";
	private String strNotification = "";	
	private String strMessage = "";
	private boolean notified = false;
	private Settings settings = null;
	private Context context = null;	
	
	public SmsSpeechSource(final Context context, String strSender, String strMessage, Settings settings)
	{			
		Utils.log(TAG, "Constructed..");
		this.strSender = strSender;
		strNotification =  settings.getSMSAlertSpeech();
		strNotification = strNotification.replace("%", strSender);
		this.strMessage = strMessage;
		this.settings = settings;
		this.context = context;
		
		//on empty message some tts may never callback uttcomplete
		if(this.strMessage.trim().equals(""))
			this.strMessage = "empty message";
	}	 

	@Override
	public void ready() {		
		volMan = new VolumeManager(settings.getVolume(), VolumeManager.MANAGE_CALL, (AudioManager) context.getSystemService(Context.AUDIO_SERVICE));		
		volMan.setSpeechVol();				
	}
	
	@Override
	public void failed() {	
		
	}

	@Override
	public String provideTextToSpeak() {
		
		Utils.log(TAG, "provideTextToSpeak()..");
		
		String text = "";
		if(!notified)
			text = strNotification;
		else
			text = strMessage;
		
		Utils.log(TAG, "end provideTextToSpeak()");
		return text;
	}

	@Override
	public long speechCompleted() {
		
		Utils.log(TAG, "SpeechComplete()..");
		
		long intervalFoNextText = -1;
		
		if(!notified)
		{
			notified = true;
			
			if(settings.readSMS())
			{	
				
				boolean bAvoidReading = settings.avoidReadingUnknownSms() && strSender.equals(settings.getUnknownCallerText());
				
				if(!bAvoidReading)
				{
					Utils.log(TAG, "Interval set for next speech");
					intervalFoNextText = 1000;
				}			
								
			}		
		}
		else
		{			
			volMan.finshed();			
		}
		
		Utils.log(TAG, "end SpeechComplete()..");
		return intervalFoNextText;
	}	

	@Override
	public void release() {
		Utils.log(TAG, "release()");
		// like speech may not be finished yet
		// and speech completed not yet called and service comes to stop us
		// so better we release it here
		if(volMan != null)
			volMan.finshed();	
	} 
	
}
