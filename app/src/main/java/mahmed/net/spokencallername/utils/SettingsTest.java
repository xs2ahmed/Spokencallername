package mahmed.net.spokencallername.utils;

import android.content.Context;

/**
 * Only for testing the speech as we need to repeat it once only for test
 * @author Ahmed
 *
 */
public class SettingsTest extends Settings 
{

	public SettingsTest(Context context) {
		super(context);
		
	}

	@Override
	public int getCallerRepeatTimes() 
	{	
		return 1;		
	}	

}
