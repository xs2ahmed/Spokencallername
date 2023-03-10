
package mahmed.net.spokencallername.Speech;
import java.util.HashMap;
import java.util.Locale;

import mahmed.net.spokencallername.utils.Settings;
import mahmed.net.spokencallername.utils.Utils;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;


public class Speaker implements ISpeaker, OnInitListener 
{
	private class SpeechFinishedListener implements TextToSpeech.OnUtteranceCompletedListener
	{
		@Override
		public void onUtteranceCompleted(final String utteranceId) 
		{
			if (utteranceId.equals("mahmed.net.apps")) 
			{				
				Utils.log(TAG, String.format("speech is complete.. threadid:%d", Thread.currentThread().getId()));
				h.sendMessage(h.obtainMessage(SPEECH_COMPLETE));
			}
		}
	}
	
	public static final int ENGINE_READY = 0;
	public static final int ENGINE_FAILED = 1;
	public static final int REQUEST_SPEECH = 2;	
	public static final int SPEECH_COMPLETE = 3;
	
	
	private ISpeechSource ts = null;
	private ISpeakerEndListener endListener = null;	
	//private final HashMap<String, String> params;
	private  Bundle params = null;
	private Handler h = null;	
	
	private final TextToSpeech tts;	
	private final String TAG = "SPEAKER";
	private Settings settings = null;
	private Context context = null;
	private boolean disposed = false;

	public Speaker(final Context context, final Settings settings, ISpeakerEndListener endListener) 
	{			
		this.endListener = endListener;
		this.settings = settings;

		//params = new HashMap<String, String>();
		//params.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
		//params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "mahmed.net.apps");

		params = new Bundle();
		params.putString(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
		params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "mahmed.net.apps");


		this.context = context;
		
		// Why synchronize ?
		// In case of testing call settings, 
		// Speaker object gets created on a non UI thread, 
		// onInit gets called on main thread always..
		// SO there is is possibility of onInit being called before TextToSpeech returns.
		// and there could be a null pointer exception..
		
		 

		
		h = new Handler(new Handler.Callback() {
			
			@Override
			public boolean handleMessage(Message msg) {
				
				if(msg.what == ENGINE_READY)
				{
					ts.ready();
					h.sendMessage(h.obtainMessage(REQUEST_SPEECH));
				}
				else if(msg.what == ENGINE_FAILED)
				{
					ts.failed();
					shutdown();									
					Speaker.this.endListener.end();
				}
				else if(msg.what == REQUEST_SPEECH)
				{	
					String text = ts.provideTextToSpeak();
					tts.speak(text, TextToSpeech.QUEUE_ADD, params , "sd");
				}
				else if(msg.what == SPEECH_COMPLETE)
				{
					long nextText = ts.speechCompleted();
					if(nextText == -1)
					{
						shutdown();					
						ts = null;
						Speaker.this.endListener.end();
					}
					else
					{
						h.sendMessageDelayed(h.obtainMessage(REQUEST_SPEECH), nextText);		
					}
				}							
					
				return false;
			}
		});

		tts = new TextToSpeech(context, this);
	
		Utils.log(TAG, "Created TextToSpeech..");
	}

	@Override
	public void onInit(final int status) 
	{	
		/**
		 * Reason for this check, 
		 * If onInit is delayed in such a way that service stop request comes, 
		 * which in turn calls clean up , we nullify handler var/ stop tts etc
		 * But then later onInit comes in.. same thread though
		 * But perhaps the thread which actually speaker in android API
		 * pumps the message on the main thread
		 * Remember this message was not removed when we clear our handler's pending messages
		 * Since the handler used by TTS internally to pump onInit/speechcomplete is ofcourse different than ours
		 * I used a var disposed and return here is already disposed
		 * By the way this can be reproduced occasionaly if we quickly terminate call as asson as it arrives or attend
		 * also via several clicks in test call, where is starts stops, 
		 * but this code seems to fix it
		 */
		if(disposed)
			return;
		
		if(status != TextToSpeech.SUCCESS)
		{
			h.sendMessage(h.obtainMessage(ENGINE_FAILED));
		}
		else
		{
			boolean error = false;
			
			Utils.log(TAG, String.format("TTS onInit.... threadid:%d", Thread.currentThread().getId()));

			if(tts == null)
			{
				throw new RuntimeException(Utils.collectPlatformInfo(context));
			}

			if(TextToSpeech.ERROR == tts.setOnUtteranceCompletedListener(new SpeechFinishedListener()))
			{
				Utils.log(TAG, "Error tts setUt");
				error = true;
			}

			int code = tts.setLanguage(Locale.getDefault());

			if(code == TextToSpeech.LANG_NOT_SUPPORTED || code ==TextToSpeech.LANG_MISSING_DATA)
			{
				Utils.log(TAG, String.format("Error settingLang on TTS code %d", code));	
				error = true;
			}

			if(TextToSpeech.ERROR == tts.setSpeechRate(settings.getSpeed()))
			{
				//this error is not a fatal, we can continue
				Utils.log(TAG, "Error tts setSPeechrate");	
				error = true;
			}

			if(TextToSpeech.ERROR == tts.setPitch(settings.getPitch()))
			{	
				//this error is not a fatal, we can continue
				Utils.log(TAG, "Error tts setPitch");				
			}
			
			if(!error)
			{
				h.sendMessage(h.obtainMessage(ENGINE_READY));
			}
			else
			{					
				h.sendMessage(h.obtainMessage(ENGINE_FAILED));
			}
				
		}

	}	
	
	@Override
	public void setTextSource(ISpeechSource ts) {
		this.ts = ts;		
	}
	
	public void shutdown() 
	{
		Utils.log(TAG, "Shutting down TTS..");
		tts.stop();
		tts.shutdown();
		
		if(h != null)
		{
			h.removeCallbacksAndMessages(null);			
			h = null;
		}
		
		disposed = true;
	}  
	
}