package cpslab.inhwan.cpslogger_v02;

/**
 * Created by Inhwan on 2015-10-01.
 */
import java.util.*;

import android.app.*;
import android.content.*;
import android.net.wifi.*;
import android.os.*;
import android.util.Base64;
import android.util.Log;
import android.widget.*;

public class WifiService extends Service {

    boolean mQuit;

    WifiManager mWifiMan;
    StringBuilder sb = new StringBuilder();
    StringBuilder sbOrigin = new StringBuilder();

    String name = "Wifi";
    Logger wifiLogger = new Logger(name);
    boolean fileOpen;

    public void onCreate() {
        super.onCreate();
        mWifiMan = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        fileOpen = true;
    }

    public void onDestroy() {
        super.onDestroy();
        mQuit = true;

        if(fileOpen){
            wifiLogger.closeFile(name);
            fileOpen = false;
        }
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        mQuit = false;
        wifiThread wifit = new wifiThread();
        wifit.start();		//start the wifiThread
        Toast.makeText(this, "Wifi-Watching is started", Toast.LENGTH_SHORT).show();

        if(!fileOpen){
            wifiLogger.createFile(name);
            fileOpen = true;
        }

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
                    sbOrigin = new StringBuilder();
                    sb.append(configs.size());
                    for(int i = 0; i < configs.size(); i++) {
                        String BSSID = configs.get(i).BSSID;    //MAC address
                        String SSID = configs.get(i).SSID;      //AP name
                        String cap = configs.get(i).capabilities;
                        int freq = configs.get(i).frequency;    //2.4GHz or 5.0GHz(24XXHz or 50XXHz)
                        int level = configs.get(i).level;       //signal length. Negative value. Larger is stronger

                        sb.append(Base64.encodeToString((BSSID + "," + SSID + "," + cap + "," + freq + "," + level).getBytes(),Base64.NO_WRAP));
                        sbOrigin.append(BSSID + "," + SSID + "," + cap + "," + freq + "," + level + "\n");

                        if(i < configs.size() - 1)
                            sb.append(",");
                    }
                    wifiLogger.writeData(sb.toString());
                    sleep(30000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}