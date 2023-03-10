package mahmed.net.spokencallername.workers;

import mahmed.net.spokencallername.utils.Utils;

import android.media.AudioManager;
/**
 * Class to manage volume when speech is to be played...
 * Assuming that Speech is on ALARM stream..
 * @author Ahmed
 *
 */
public class VolumeManager 
{
	private static final String TAG = "VolumeManager";
	public static final int MANAGE_SMS = 1;
	public static final int MANAGE_CALL= 2;
	
	
	private int manage = 1;
	private AudioManager am = null;
	
	
	
	//Call only stuff
			
	private int volNotifInitial = 3;    //initial alaram
	
	
	int speechVolume = 5; 
	
	public VolumeManager(int speechvolume, int manage, AudioManager audioMan)
	{
		this.manage = manage;
		this.am = audioMan;
		
		this.speechVolume = speechvolume;	
			
		volNotifInitial = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
		Utils.log(TAG, String.format("Starting notif volume is = %d",volNotifInitial));
		 	
	}
	
	/**
	 * Sets appropriate volume for Speech,
	 * Call only once.. 
	 */
	public void setSpeechVol()
	{					
		setVolume(AudioManager.STREAM_NOTIFICATION, speechVolume);
	}	 
	
	/**
	 * Must be call when the speech has finished 
	 * and no more speech to be played..
	 * This will restore original volumes
	 */
	public void finshed()
	{		
		setVolume(AudioManager.STREAM_NOTIFICATION, volNotifInitial);		 				
	}
	
	private void setVolume(int stream, int newVol)
	{	
		Utils.log(TAG, String.format("Setting volume of stream %d %d", stream, newVol));
	 
		int currentVol = am.getStreamVolume(stream);			
		
		if(currentVol == newVol)
			return;
		
		am.setStreamVolume(stream, newVol, 0);
		 
	}
	
}
