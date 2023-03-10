package mahmed.net.spokencallername.Speech;

import mahmed.net.spokencallername.utils.Settings;
import mahmed.net.spokencallername.utils.Utils;
import mahmed.net.spokencallername.workers.VolumeManager;
import android.content.Context;
import android.media.AudioManager;


public class CallSpeechSource implements ISpeechSource
{	
	private static final String TAG="Spoken_CallTextSource";		
	private Settings settings = null;
	private String strPreparedString = "";
	private int count = 0;
	private VolumeManager volMan = null;
	private Context context = null;
	
	public CallSpeechSource(final Context context, String strData, Settings settings)
	{				
		Utils.log(TAG, "Constructed..");		
		this.settings = settings;
		count = settings.getCallerRepeatTimes();
		this.context = context;
		strPreparedString = settings.getCallAlertSpeech();
		strPreparedString= strPreparedString.replace("%", strData);			
	}
	 
	@Override
	public void ready() {			
		volMan = new VolumeManager(settings.getVolume(), VolumeManager.MANAGE_CALL, (AudioManager) context.getSystemService(Context.AUDIO_SERVICE));		
		volMan.setSpeechVol();				
	}	

	@Override
	public String provideTextToSpeak() {		
		return strPreparedString;
	}

	@Override
	public long speechCompleted() {
		
		long next = -1;	
		
		count--;
		
		if(count <= 0)
		{			
			volMan.finshed();
		}
		else
		{
			next = settings.getCallerRepeatInterval();
		}
		
		return next;
	}

	@Override
	public void failed() {			
	}

	@Override
	public void release() {	
		// like speech may not be finished yet
		// and speech completed not yet called and service comes to stop us
		// so better we release it here
		if(volMan != null)
			volMan.finshed();
	} 

 
}