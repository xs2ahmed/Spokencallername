package mahmed.net.spokencallername.Speech;

/**
 * This interface should be implemented by classes 
 * which intended to provide the Text to be spoken
 * The implementer is unaware of TTS code
 * @author Ahmed
 *
 */
public interface ISpeechSource {

	/**
	 * TTs is initialized, I am ready now
	 * You can call speaker.requestSpeech(time)
	 * to ask for speech request
	 * @param speaker
	 */
	public void ready();
	/**
	 * TTs may have failed this method is called instead of ready
	 * The source should unregister from the Speaker here
	 * and not call other methods on speaker
	 * @param speaker
	 */
	public void failed();	
	/**
	 * In response to speechRequest this method is called 
	 * when time expires.
	 * 
	 * @return The Text that should be spoken
	 */
	public String provideTextToSpeak();	
	/**
	 * This is called when Speech provided is spoken
	 * by TTS
	 * @return milliseconds after which next text will be provided or -1 if no more speech text
	 */
	public long speechCompleted();
	/**
	 * It is preferable that the component
	 * instantiating the SpeechSource and associating it with ISpeech should release it
	 * Rather than the ISpeaker doing it itself
	 * When ISpeaker finishes it tells the listener that it's done, 
	 * so it can do its job there
	 * Just like we create a stream on socket, 
	 * closing stream should not close the socket ?
	 * Free any resources
	 */
	public void release();
	
	
}
