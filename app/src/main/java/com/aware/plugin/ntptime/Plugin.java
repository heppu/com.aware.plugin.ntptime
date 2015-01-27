package com.aware.plugin.ntptime;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.plugin.ntptime.Provider.NTPtime_Data;
import com.aware.utils.Aware_Plugin;

public class Plugin extends Aware_Plugin {
	
	/**
	 * Shared context: new NTPtime data is available
	 */
	public static final String ACTION_AWARE_PLUGIN_NTPTIME = "ACTION_AWARE_PLUGIN_NTPTIME";
	
	public static final String EXTRA_DRIFT = "drift";
	
	public static int temp_interval = 0;
	private static String sDrift;
	private static ContextProducer sContextProducer;

	public NtpAlarm alarm = new NtpAlarm();
	
	@Override
	public void onCreate() {
		super.onCreate();
        TAG = "AWARE::NTPtime";
        DEBUG = Aware.getSetting(getApplicationContext(), Aware_Preferences.DEBUG_FLAG).equals("true");

        Intent aware = new Intent(this, Aware.class);
        startService(aware);

		Aware.setSetting(getApplicationContext(), Settings.STATUS_PLUGIN_NTPTIME, true);
		
		if( Aware.getSetting(getApplicationContext(), Settings.SERVERS_PLUGIN_NTPTIME).length() == 0 ) {
			Aware.setSetting(getApplicationContext(), Settings.SERVERS_PLUGIN_NTPTIME, "time.nist.gov");
		}
		
		if( Aware.getSetting(getApplicationContext(), Settings.INTERVAL_PLUGIN_NTPTIME).length() == 0 ) {
			Aware.setSetting(getApplicationContext(), Settings.INTERVAL_PLUGIN_NTPTIME, "15");
		}

		Intent apply = new Intent(Aware.ACTION_AWARE_REFRESH);
		sendBroadcast(apply);
		
		CONTEXT_PRODUCER = new ContextProducer() {
			@Override
			public void onContext() {
				Intent mNTPtime = new Intent(ACTION_AWARE_PLUGIN_NTPTIME);
				mNTPtime.putExtra(EXTRA_DRIFT, sDrift.toString());
				sendBroadcast(mNTPtime);
			}
		};
		sContextProducer = CONTEXT_PRODUCER;
		
		DATABASE_TABLES = Provider.DATABASE_TABLES;
		TABLES_FIELDS = Provider.TABLES_FIELDS;
		CONTEXT_URIS = new Uri[]{ NTPtime_Data.CONTENT_URI };

        getDrift(this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int interval =  Integer.parseInt(Aware.getSetting(getApplicationContext(), Settings.INTERVAL_PLUGIN_NTPTIME));
		if(interval!=temp_interval){
            temp_interval = interval;
			alarm.CancelAlarm(Plugin.this);
			alarm.SetAlarm(Plugin.this, interval);
		}
		
		return START_STICKY;
	}
	
	protected static void getDrift(Context context) {
		Intent NTPtime_Service = new Intent(context, ntptime_Service.class);
		context.startService(NTPtime_Service);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		alarm.CancelAlarm(Plugin.this);
		Aware.setSetting(getApplicationContext(), Aware_Preferences.STATUS_LOCATION_NETWORK, false);
		Aware.setSetting(getApplicationContext(), Settings.STATUS_PLUGIN_NTPTIME, false);
		Intent apply = new Intent(Aware.ACTION_AWARE_REFRESH);
		sendBroadcast(apply);
	}
	
	/**
	 * Background service that will connect to NTP server and fetch and store current ntp time and clocks drift.
	 * @author hkoski
	 */
	public static class ntptime_Service extends IntentService {
		public ntptime_Service() {
			super("AWARE NTPtime");
		}

		@Override
		protected void onHandleIntent(Intent intent) {
            SntpClient client = new SntpClient();
			String server = Aware.getSetting(getApplicationContext(), "servers_plugin_ntptime");

			if (client.requestTime(server, 5000)) {
				
				long ntpTime = client.getNtpTime();
				long offset = client.getClockOffset();
				
				ContentValues time_data = new ContentValues();
				
				time_data.put(NTPtime_Data.TIMESTAMP, System.currentTimeMillis());
				time_data.put(NTPtime_Data.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
				time_data.put(NTPtime_Data.DRIFT, offset);
				time_data.put(NTPtime_Data.NTP_TIME, ntpTime);
				
				getContentResolver().insert(NTPtime_Data.CONTENT_URI, time_data);
				
				sDrift = new BigDecimal(offset).toPlainString();
				sContextProducer.onContext();
				if( DEBUG) Log.d(TAG, time_data.toString());
			}	
		}
	}
}
