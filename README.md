# com.aware.plugin.ntptime
NTPtime is plugin for [AWARE](http://www.awareframework.com/) whitch can be also found in Github [aware-client](https://github.com/denzilferreira/aware-client).
This plugin allows you to easily get current time from NTP server and measure system clocks drift.

Settings:
---------
- **status_plugin_ntptime:** Activate / Deactivate plugin
- **servers_plugin_ntptime:**  url for ntp server
- **interval_plugin_ntptime:** interval in minutes between checks


Broadcasts
----------
- **ACTION_AWARE_PLUGIN_NTPTIME: ** Broadcasted after every time check
  - **drift:** time in ms your clock drifts from actual time

Providers
----------
**URI:** content://com.aware.plugin.ntptime.provider/plugin_ntptime

| Table field      | Field type | Description                   |
| ---------------- |:----------:| -----------------------------:|
| _id              | INTEGER    | primary key autoincrement     |
| timestamp        | REAL       | unix timestamp                |
| device_id        | TEXT       | AWARE device id               |
| drift            | REAL       | clocks drift from ntp time    |
| ntp_time         | REAL       | ntp timestamp in milliseconds |
