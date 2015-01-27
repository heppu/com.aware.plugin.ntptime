package com.aware.plugin.ntptime;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.aware.plugin.ntptime.Provider.NTPtime_Data;
import com.aware.utils.IContextCard;

public class ContextCard implements IContextCard {

    public ContextCard(){};

    public View getContextCard( Context context ) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View card = inflater.inflate(R.layout.card, null);
	
		TextView time_drift = (TextView) card.findViewById(R.id.time_drift);
		TextView ntp_time = (TextView) card.findViewById(R.id.ntp_time);
		
		Cursor latest_drift = context.getContentResolver().query( NTPtime_Data.CONTENT_URI, null, null, null, NTPtime_Data.TIMESTAMP + " DESC LIMIT 1" );
		if( latest_drift != null && latest_drift.moveToFirst() ) {
			long time = latest_drift.getLong(latest_drift.getColumnIndex(NTPtime_Data.DRIFT));
			String time_string = "System Clock Drift: "+Long.toString(time)+"ms";
			SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss.SSS"); 
			String ntpTimeString = formatter.format(new Date(time));
			time_drift.setText(time_string);
			ntp_time.setText("NTP time: "+ntpTimeString);
			
		}
		
		if( latest_drift != null && ! latest_drift.isClosed() ) latest_drift.close();
		
		return card;
	}
}
