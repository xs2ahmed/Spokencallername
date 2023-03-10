package mahmed.net.spokencallername;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceFragmentCompat;

import mahmed.net.spokencallername.utils.Constants;

import static android.Manifest.permission.FOREGROUND_SERVICE;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;






public class SettingsActivity extends AppCompatActivity {

    private static final int FOREGROUND_SERVICE_CODE = 10;
    private static final int READ_CALL_LOG_CODE = 11;
    private static final int READ_PHONE_STATE_CODE = 12;
    private static final int RECEIVE_SMS_CODE = 13;
    private static final int READ_CONTACTS_CODE = 14;
    private static final int INTERNET_CODE = 15;
    private static final int WRITE_EXTERNAL_STOR_CODE = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public void onResume() {
        super.onResume();


        if(ContextCompat.checkSelfPermission(this, "android.permission.FOREGROUND_SERVICE")
                != PackageManager.PERMISSION_GRANTED)
        {
            checkPermission(FOREGROUND_SERVICE, FOREGROUND_SERVICE_CODE);
        }

        if(ContextCompat.checkSelfPermission(this, FOREGROUND_SERVICE)
                != PackageManager.PERMISSION_GRANTED)
        {
            checkPermission(FOREGROUND_SERVICE, FOREGROUND_SERVICE_CODE);
        }
        if(ContextCompat.checkSelfPermission(this, READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED)
        {
            checkPermission(READ_CALL_LOG, READ_CALL_LOG_CODE);
        }
        if(ContextCompat.checkSelfPermission(this, RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED)
        {
            checkPermission(READ_CALL_LOG, RECEIVE_SMS_CODE);
        }







    }
    // Function to check and request permission
    public void checkPermission(String permission, int requestCode)
    {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] { permission }, requestCode);
        }
        else {
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    // This function is called when user accept or decline the permission.
// Request Code is used to check which permission called this function.
// This request code is provided when user is prompt for permission.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                            String[] permissions,
                                            int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == FOREGROUND_SERVICE_CODE) {

            // Checking whether user granted the permission or not.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // Showing the toast message
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == READ_CALL_LOG_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }





    public static class SettingsFragment extends PreferenceFragmentCompat implements Constants {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName(MY_SHARED_PREFERENCE);
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}