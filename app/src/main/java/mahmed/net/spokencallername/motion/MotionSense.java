package mahmed.net.spokencallername.motion;

import java.util.List;

import mahmed.net.spokencallername.utils.Utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MotionSense implements SensorEventListener 
{
	public static final String TAG = "MotionSense";
	
	public static final int SENSE_LOW = 1;
	public static final int SENSE_NORMAL = 2;
	public static final int SENSE_SENSITIVE = 3;
	
	
	
	private int nSensitivity = 1;
	private float factor = 8;
	
	private boolean bStartValsCaptured = false;
	
	float startx = 0; 
	float starty = 0; 
	float startz = 0;
	
	private MotionListener listener = null; 
	private SensorManager  sm = null;
	
	public MotionSense(int nSenseLevel, Context context, MotionListener listener)
	{
		nSensitivity = nSenseLevel;
		factor = 8/nSensitivity;
		
		  sm =  (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	       
	       List<Sensor> sensorList = sm.getSensorList(Sensor.TYPE_ACCELEROMETER); 
	       
	       if(sensorList != null && sensorList.size() > 0 )
	       {	       
	    	   sm.registerListener(this, sensorList.get(0), SensorManager.SENSOR_DELAY_NORMAL);
	    	   this.listener = listener; 
	       } 
	       else
	       {
	    	   Utils.log(TAG, "No accelerometer sensor..");
	       }
	}
	
	public void cleanup()
	{
		sm.unregisterListener(this);
		listener = null;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Callback on main thread..
	 */
	@Override
	public void onSensorChanged(SensorEvent event) 
	{	
		if(listener == null)
			return;
		
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		
		if(!bStartValsCaptured)
		{
			startx = x;
			starty = y;
			startz = z;
			
			bStartValsCaptured = true;
			return;
		}
		else
		{			
			if(Math.abs (startz - z)  > factor || Math.abs (startx - x)  > factor || Math.abs (starty - y)  > factor)
			{
				listener.motionDedected();
				cleanup();				
			}
		}
		
	}	
	


}
