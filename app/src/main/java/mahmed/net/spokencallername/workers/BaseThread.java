package mahmed.net.spokencallername.workers;

import java.util.ArrayList;

//import junit.framework.Assert;
import mahmed.net.spokencallername.Speech.Speaker;
import mahmed.net.spokencallername.motion.MotionListener;
import mahmed.net.spokencallername.motion.MotionSense;
import mahmed.net.spokencallername.utils.Settings;
import mahmed.net.spokencallername.workers.listeners.ThreadFinishListener;
import android.content.Context;
import android.util.Log;

class BaseThread extends Thread
{
	Context context = null;		
	String strSpeakData = "";		
	String strExtraData = "";
	Speaker speaker = null;
	Settings settings = null;
	ArrayList<ThreadFinishListener> threadFinishListeners = new ArrayList<ThreadFinishListener>();
	private MotionSense motionSense = null;
	private static final String COMPONENT = "Spoken_BaseThread";
	
	public BaseThread(String name, final Context context, String strData, String strExtra, final Settings settings, final Speaker speaker)
	{
		super(name);
		strData.trim();			
		//Assert.assertNotNull(context);
		//ssert.assertNotNull(strData);
		
				
		this.context = context;
		this.strSpeakData = strData;
		this.strExtraData = strExtra;
		this.settings = settings;
		this.speaker = speaker;
	}	
	
	public void addThreadFinishListerner(ThreadFinishListener listener)
	{
	//	Assert.assertNotNull(listener);
	//	Assert.assertFalse(threadFinishListeners.contains(listener));
		
		threadFinishListeners.add(listener);
	}
	
	protected void notifyListeners(ThreadFinishListener.ThreadFinishEvent event)
	{
		for (ThreadFinishListener listener : threadFinishListeners) 
		{
			listener.threadFinished(event);
		}
	}
	
	protected boolean startMotionSense(MotionListener listener)
	{
		if(!settings.suppressSpeechOnRotate())
			return false;
		
		if(motionSense == null)
		{
			Log.d(COMPONENT, "motion sense started");
			motionSense = new MotionSense(MotionSense.SENSE_LOW, context, listener);
			return true;
		}
		else
		{			
			return false;
		}		
	}
	
	protected boolean stopMotionSense(MotionListener listener)
	{
		if(motionSense != null)
		{
			Log.d(COMPONENT, "stopping motion sense ...");
			motionSense.cleanup();
			motionSense = null;
			return true;
		}
		else
		{
			return false;
		}		
	}
	
	
   
}