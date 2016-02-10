package cpslab.inhwan.cpslogger_v02;

/**
 * Created by Inhwan on 2015-10-01.
 */
import java.util.*;

import android.app.*;
import android.content.*;
import android.net.wifi.*;
import android.os.*;
import android.util.Log;
import android.widget.*;

public class WifiService extends Service {

    boolean mQuit;

    WifiManager mWifiMan;
    StringBuilder sb = new StringBuilder();

    public void onCreate() {
        super.onCreate();
        mWifiMan = (WifiManager)getSystemService(Context.WIFI_SERVICE);
//		unregisterRestartAlarm();
    }

    public void onDestroy() {
        super.onDestroy();
        mQuit = true;
//		registerRestartAlarm();
//		Toast.makeText(this, "Wifi-Watching is ended", 0).show();
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        mQuit = false;
        wifiThread wifit = new wifiThread();
        wifit.start();		//start the wifiThread
        Toast.makeText(this, "Wifi-Watching is started", Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private class wifiThread extends Thread {

        public void run() {

            while(!mQuit) {
                try {
                    mWifiMan.startScan();
                    List<ScanResult> configs = mWifiMan.getScanResults();
                    sb = new StringBuilder();
                    for(int i = 0; i < configs.size(); i++) {
                        String line = (configs.get(i)).toString();
                        String seg[] = line.split(",");

                        sb.append(seg[0] + ", " + seg[1] +", " + seg[3] + "\n");

//                        sb.append(new Integer(i+1).toString() + "::");
//                        sb.append(seg[0] + "\n" + seg[1] + "\n" + seg[3]);
//                        sb.append("\n\n");
                    }
                    Log.d("Wifi Connection Info: ", sb + "\n");
                    sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}