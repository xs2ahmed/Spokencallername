package mahmed.net.spokencallername.utils;


import mahmed.net.spokencallername.R;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Contacts;
import android.provider.Contacts.PeopleColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;

public abstract class Contact 
{
	 private static final String TAG = "Contact";
	private static class DonutContact extends Contact 
	{
		public DonutContact(final String incomingNumber, final Context context) 
		{
			number = incomingNumber;
			this.context = context;

			resolveNumber();
		}

		@Override
		protected void resolveNumber() 
		{
			final Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL, Uri.encode(number));
			final Cursor cur = context.getContentResolver().query(contactUri, new String[] {PeopleColumns.NAME}, null, null, null);
			
			//fix for a reported crash on developer  console
			if(cur == null)
			{
				name = UNKNOWN;
				Utils.log(TAG, String.format("cur was null number %s",number));
				return;
			}

			if (cur.moveToFirst()) 
			{
				if (cur.getString(0) != null) 
				{
					name = cur.getString(0);
				} 
				else 
				{
					name = UNKNOWN;
				}
			} 
			else 
			{
				name = UNKNOWN;
			}
		}
	}

	private static class EclairContact extends Contact
	{
		public EclairContact(final String incomingNumber, final Context context) 
		{
			number = incomingNumber;
			this.context = context;

			resolveNumber();
		}

		@Override
		protected void resolveNumber() 
		{
			final Cursor cur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.NUMBER + " = " + number, null, null);
			
			//fix for a reported crash on developer  console
			if(cur == null)
			{
				name = UNKNOWN;
				Utils.log(TAG, String.format("cur was null number %s",number));
				return;
			}
				
			final int column = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

			if (cur.moveToFirst()) 
			{
				if (cur.getString(column) != null) 
				{
					name = cur.getString(column);
				} 
				else 
				{
					name = UNKNOWN;
				}
			} 
			else 
			{
				name = UNKNOWN;
			}
		}
	}

	private static class SpartanContact extends Contact 
	{
		public SpartanContact(final String incomingNumber, final Context context) 
		{
			number = incomingNumber;
			this.context = context;
			resolveNumber();
		}

		@Override
		protected void resolveNumber() 
		{
			final Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
			final Cursor cur = context.getContentResolver().query(uri, null, null, null, null);

			//fix for a reported crash on developer  console
			if(cur == null)
			{
				name = UNKNOWN;
				Utils.log(TAG, String.format("cur was null number %s",number));
				return;
			}
			
			final int column = cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

			if (cur.moveToFirst()) 
			{
				if (cur.getString(column) != null) 
				{
					name = cur.getString(column);
				}
				else 
				{
					name = UNKNOWN;
				}
			} 
			else
			{
				name = UNKNOWN;
			}
		}
	}

	public static String UNKNOWN = "unknown";

	public static String getCaller(final String incomingNumber, final Context context, final Settings settings) 
	{
		UNKNOWN = context.getResources().getString(R.string.caller_unknown);

		String name = null;
		String text = null;
		Contact contact = null;

		if (incomingNumber.matches("^[+]\\d+||\\d+")) 
		{
			if (Integer.parseInt(Build.VERSION.SDK) == Build.VERSION_CODES.DONUT) 
			{
				contact = new DonutContact(incomingNumber, context);
				text = contact.getName();

				name = text;
			} 
			else
			{
				contact = new EclairContact(incomingNumber, context);
				text = contact.getName();

				if (text.equals(UNKNOWN)) 
				{
					contact = new DonutContact(incomingNumber, context);
					text = contact.getName();

					if (text.equals(UNKNOWN)) 
					{
						contact = new SpartanContact(incomingNumber, context);
						text = contact.getName();
					}
				}

				name = text;
			}
		} 
		else 
		{
			// it isn´t a number, maybe anything special that has a name - read
			// it
			name = incomingNumber;

			if (name.contains("\"")) 
			{
				name = name.substring(name.indexOf('"') + 1, name.length());
				name = name.substring(0, name.indexOf('"'));
			}
		}

		if (name.equals(UNKNOWN)) 
		{		
			if (settings.speakNumber() && incomingNumber != null && incomingNumber.length() != 0) 
			{
				return incomingNumber;
			}
			else
			{
				return settings.getUnknownCallerText();
			}	 
		}		

		if (name.equals("") || name == null) 
		{			
			return "";
		}

		return name;
	}

	protected Context context;

	protected String name;

	// CupcakeContact?

	protected String number;

	private String getName() 
	{
		return name;
	}

	protected abstract void resolveNumber();
}