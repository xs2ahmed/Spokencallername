package mahmed.net.spokencallername.utils;



import mahmed.net.spokencallername.R;
import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;

/** 
 * This class was introduced to avoid repeated code in the App
 * Usage from within an activity
  
   	onCreate()
   	{
   		// 1: create
	  	ttsChecker = new TTSChecker(this);
	  	
	  	// 2:request to check
		ttsChecker.startCheck();		
	}
	
	
	// 3: Override onActivityResult in the Activity
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
	// 4: Forward call to checker..
		 if(ttsChecker != null)
			 ttsChecker.ttsCallBack(requestCode, resultCode, data);
	}

 * @author Ahmed
 *
 */
public class TTSChecker
{

	public static final int TTS_CHECK_REQ = 7;
	public String message = "";
	
	private Activity sourceActivity = null;
	
	public TTSChecker(Activity checkingActivity)
	{	
		sourceActivity = checkingActivity;				
	}
	
	public boolean startCheck()
	{
		if(sourceActivity != null)
		{
			// check for TTS installed
			final Intent checkIntent = new Intent();
			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			sourceActivity.startActivityForResult(checkIntent, TTS_CHECK_REQ);
			return true;
		}
		return false;
	}

	public boolean ttsCallBack(int requestCode, int resultCode, Intent data) 
	{
		if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) 
		{
			ttsPassed();
			message = "TTS is present on your system";
			return true;
		} 
		else if(resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_VOLUME)
		{			
			message = "Media/disk is mounted elsewhere, speech might not work";
			return false;
		}
		else
		{
			message = "TTS not installed";
			ttsFailed(resultCode, data);
			return false;			
		}		
	}
	
	protected void ttsPassed()
	{
		
	}
	
	protected void ttsFailed(int resultCode, Intent data)
	{	
		Utils.messageT(sourceActivity, sourceActivity.getString(R.string.tts_absent_message));
		
		// missing data, install it
		final Intent installIntent = new Intent();
		installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
		if(sourceActivity!=null)
			sourceActivity.startActivity(installIntent);
	}

 
}
