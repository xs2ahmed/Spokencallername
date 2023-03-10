package mahmed.net.spokencallername.Speech;


public interface ISpeaker {

	public void setTextSource(ISpeechSource ts);	
	public void shutdown();
	
	public interface ISpeakerEndListener
	{
		public void end();
	}
	
}
