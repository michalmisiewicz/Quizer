package android.proz.misiewicz.elkowanie;

import android.content.SharedPreferences;
import android.media.MediaCodec;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import java.util.regex.Pattern;

/**
 * Created by Michal on 18-04-2017.
 */

public class SettingsActivity extends PreferenceActivity
                              implements SharedPreferences.OnSharedPreferenceChangeListener
{

    private String ip_pattern = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_main);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if(key.equals(getString(R.string.pref_addr_key)))
        {
            Preference address_preference = findPreference(key);
            Pattern pattern = Pattern.compile(ip_pattern);

            String newValue = sharedPreferences.getString(key, "");
            if(pattern.matcher(newValue).matches())
            {
                address_preference.setSummary(newValue);
                ServerConnection.getInstance().setServerAddress(newValue);
            }
        }
    }
}
