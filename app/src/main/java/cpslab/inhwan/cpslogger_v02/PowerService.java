package cpslab.inhwan.cpslogger_v02;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.util.Log;
import android.widget.Toast;

public class PowerService extends Service {
    static String name = "Power";

    Logger powerLogger = new Logger(name);

    boolean fileOpen = false;

    PowerConnectionReceiver batteryReceiver;
    BatteryManager batteryManager;
    ScreenReceiver screenReceiver;
    EarphoneReceiver earphoneReceiver;

    public PowerService() {
    }

    @Override
    public void onCreate(){
        super.onCreate();

        batteryReceiver = new PowerConnectionReceiver();
        screenReceiver = new ScreenReceiver();
        earphoneReceiver = new EarphoneReceiver();

        batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);

        this.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        this.registerReceiver(earphoneReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        this.registerReceiver(screenReceiver, filter);

        fileOpen = true;
    }
    @Override
      public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(this, "PowerService is started", Toast.LENGTH_SHORT).show();		//toast message
//        Log.d(name, "SignalSensingStart");

        if(!fileOpen){
            powerLogger.createFile(name);
            fileOpen = true;
        }

        return START_STICKY;		//Sticky n Unsticky: what is the difference?
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        this.unregisterReceiver(screenReceiver);
        this.unregisterReceiver(batteryReceiver);
        this.unregisterReceiver(earphoneReceiver);

        if(fileOpen){
            powerLogger.closeFile(name);
            fileOpen = false;
        }
//        Log.d(name,"Service Ended");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // To do: Return the communication channel to the service.
        return null;
//        throw new UnsupportedOperationException("Not yet implemented");
    }

    public class PowerConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

            int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = level / (float)scale;
//            Log.d(name, "battery State is changed");

            //isCharging, usbCharge, acCharge
            String batteryState = "battery," + isCharging + "," + usbCharge + "," + acCharge + "," + level + "," + batteryPct;
            if (fileOpen)
                powerLogger.writeData(batteryState);
//            Log.d(name, batteryState);
        }
    }
    public class ScreenReceiver extends BroadcastReceiver {

        boolean wasScreenOn = true;

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                // do whatever you need to do here
                wasScreenOn = false;
//                Log.d(name,"screeen is off");
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                // and do whatever you need to do here
                wasScreenOn = true;
//                Log.d(name,"screen is on");
            }
            String screenState = "screen," + wasScreenOn;
            if (fileOpen)
                powerLogger.writeData(screenState);
        }

    }
    public class EarphoneReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            boolean isPlugged = audioManager.isWiredHeadsetOn();

            Log.d(name,"Headphone is plugged");
            if(fileOpen)
                powerLogger.writeData("headphone," + isPlugged);

        }

    }
}
