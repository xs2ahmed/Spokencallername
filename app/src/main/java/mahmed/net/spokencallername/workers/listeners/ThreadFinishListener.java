package mahmed.net.spokencallername.workers.listeners;

import java.util.EventObject;

public interface ThreadFinishListener
{		
	public class ThreadFinishEvent extends EventObject
	{
	 
		private static final long serialVersionUID = 1L;		 
		public static final int THREAD_INTERRUPTED = 0;
		public static final int THREAD_FINISHED = 1;
		public static final int THREAD_ERROR = 2;
		
		public int nEventID = 0;
		public String strErrorMessage = "";		
		
		public ThreadFinishEvent(Object source, int nEventID, String strMessage)
		{
			super(source);
			this.nEventID = nEventID;
			this.strErrorMessage = strMessage;
			
		}
		
		public int getEventID()
		{
			return nEventID;
		}
		
		public String getErrorMessage()
		{			
			return strErrorMessage;
		}		
		
	}
	
	public void threadFinished(ThreadFinishEvent event);
}