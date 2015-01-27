package com.aware.plugin.ntptime;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aware.plugin.ntptime.Plugin;
import com.aware.plugin.ntptime.R;
import com.aware.Aware;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	/**
	 * State
	 */
	public static final String STATUS_PLUGIN_NTPTIME = "status_plugin_ntptime";
	
	/**
	 * Server settings 
	 */
	public static final String SERVERS_PLUGIN_NTPTIME = "servers_plugin_ntptime";
	
	public static final String INTERVAL_PLUGIN_NTPTIME = "interval_plugin_ntptime";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		syncSettings();
	}
	
	private void syncSettings() {
		CheckBoxPreference check = (CheckBoxPreference) findPreference(STATUS_PLUGIN_NTPTIME);
		check.setChecked(Aware.getSetting(getApplicationContext(), STATUS_PLUGIN_NTPTIME).equals("true"));
		
		
		ListPreference servers = (ListPreference) findPreference(SERVERS_PLUGIN_NTPTIME);
		servers.setSummary( Aware.getSetting(getApplicationContext(), SERVERS_PLUGIN_NTPTIME) );

        EditTextPreference interval = (EditTextPreference) findPreference(INTERVAL_PLUGIN_NTPTIME);
		interval.setSummary( Aware.getSetting(getApplicationContext(), INTERVAL_PLUGIN_NTPTIME) );
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	};

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference preference = (Preference) findPreference(key);
		if( preference.getKey().equals(STATUS_PLUGIN_NTPTIME)) {
			boolean is_active = sharedPreferences.getBoolean(key, false);
			Aware.setSetting(getApplicationContext(), key, is_active);
			if( is_active ) {
				Aware.startPlugin(getApplicationContext(), getPackageName());
			} else {
				Aware.stopPlugin(getApplicationContext(), getPackageName());
			}	
		}
		else if( preference.getKey().equals(SERVERS_PLUGIN_NTPTIME)) {
			preference.setSummary(sharedPreferences.getString(key, "time.nist.gov"));
			Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "time.nist.gov"));
		}
		else if( preference.getKey().equals(INTERVAL_PLUGIN_NTPTIME)) {
			preference.setSummary(sharedPreferences.getString(key, "15"));
			Aware.setSetting(getApplicationContext(), key, sharedPreferences.getString(key, "15"));
		}
		Intent apply = new Intent(Aware.ACTION_AWARE_REFRESH);
		sendBroadcast(apply);
	}
}
