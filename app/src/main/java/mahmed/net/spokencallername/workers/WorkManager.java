package mahmed.net.spokencallername.workers;

import mahmed.net.spokencallername.Speech.CallSpeechSource;
import mahmed.net.spokencallername.Speech.ISpeaker.ISpeakerEndListener;
import mahmed.net.spokencallername.Speech.ISpeechSource;
import mahmed.net.spokencallername.Speech.SmsSpeechSource;
import mahmed.net.spokencallername.Speech.Speaker;
import mahmed.net.spokencallername.motion.MotionListener;
import mahmed.net.spokencallername.motion.MotionSense;
import mahmed.net.spokencallername.utils.Constants;
import mahmed.net.spokencallername.utils.Settings;
import mahmed.net.spokencallername.utils.Utils;
import android.content.Context;


public class WorkManager implements ISpeakerEndListener , MotionListener
{
	
	public interface IWorkCompleteListener
	{
		public void workCompleted();
	}	
	
	Speaker speaker = null;
	ISpeechSource ts = null;
	IWorkCompleteListener workCompleteListener = null;
	MotionSense motionSensor = null;
	
	private static final String TAG="Spoken_WorkManager"; 

	/*
	 * returns 1 if any task was started else 0
	 */
	
	public void start(final Context context, Settings settings, String strName, String strMessage, String strType)
	{
		
		Utils.log(TAG, "started");
		
		// can not use old object have to create new one
		// If we start old thread , exception is thrown that thread was already started
		// I haver verified that a stopped thread can not be started so new thread should be started		
			
		if(strType.equals(Constants.ALERT_TYPE_CALL) || strType.equals(Constants.ALERT_TYPE_CALL_TEST))
		{			
			if(settings.isStartSayCaller())
			{
				Utils.log(TAG, "Speaker and CallThread contructing..");
				this.speaker = new Speaker(context, settings, this);				
				ts = new CallSpeechSource(context, strName,settings);
				speaker.setTextSource(ts);
			}
						
		}
		else if (strType.equals(Constants.ALERT_TYPE_SMS))
		{			
			if(settings.isStartSaySMS())
			{
				Utils.log(TAG, "Speaker and SMSThread contructing..");
				this.speaker = new Speaker(context, settings, this);
				ts = new SmsSpeechSource(context, strName, strMessage, settings);
				speaker.setTextSource(ts);
			}
		}
		else 
		{
			notifyListener();
		}
		
		if(speaker != null && settings.suppressSpeechOnRotate())
		{
			motionSensor = new MotionSense(MotionSense.SENSE_LOW, context, this);
		}
			
	}
	
	public void cleanup()
	{
		if(speaker != null)
		{
			Utils.log(TAG,"Cleanup()");
			speaker.shutdown();		
			ts.release();
			speaker = null;
			
		}
		
		if(motionSensor != null)
		{
			motionSensor.cleanup();
			motionSensor = null;
		}
	}	 

	/**
	 * Construct work manager 
	 * @param listener pass the listener which needs to listen to work done. 
	 * Can be null if caller is not interested in listening
	 */
	public WorkManager(IWorkCompleteListener listener) {
		
		Utils.log(TAG, "Created");
		this.workCompleteListener = listener;
		
	}
		
	private void notifyListener()
	{
		if(workCompleteListener != null)
			workCompleteListener.workCompleted();		
	}

	@Override
	public void end() {
		notifyListener();		
	}

	/**
	 * Should be on main thread
	 */
	@Override
	public void motionDedected() {
		
		//if motion is detected we clean up		
		cleanup();
		//notification is necessary so that service gets out of memory
		notifyListener();		
		
	}
	
	 

}
