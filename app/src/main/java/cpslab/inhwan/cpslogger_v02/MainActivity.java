package cpslab.inhwan.cpslogger_v02;

import android.app.*;
import android.content.*;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.*;
import android.view.*;
import android.widget.*;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends Activity {

    NotificationManager nm;

    PowerManager mPm;
    WakeLock mWakeLock;

    boolean mButton;
    boolean onOff;
    boolean serviceOn;

    KeepGoing kgthread;

    Intent intentLoc;
    Intent intentMov;
    Intent intentWifi;
    Intent intentRec;
    Intent intentSoft;
    Intent intentApp;
    Intent intentNoti;
    Intent intentSig;
    Intent intentPhone;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Notification")
                .setMessage("Check if GPS & WIFI are turned on")
                .setIcon(R.mipmap.ic_launcher)
                .setCancelable(false)
                .setPositiveButton("Done", null)
                .show();

        mWakeLock = null;
        mButton = false;
        onOff = false;

        intentLoc = new Intent(MainActivity.this, LocationService.class);
        intentMov = new Intent(MainActivity.this, MovingService.class);
        intentWifi = new Intent(MainActivity.this, WifiService.class);
		intentRec = new Intent(MainActivity.this, RecordingService.class);
        intentSoft = new Intent(MainActivity.this, SoftSensingService.class);
        intentApp = new Intent(MainActivity.this, RunningAppService.class);
        intentNoti = new Intent(MainActivity.this,NotificationService.class);
        //intentGear = new Intent(MainActivity.this, GearService.class);
        intentSig = new Intent(MainActivity.this,SignalSensingService.class);
        intentPhone = new Intent(MainActivity.this,PhoneStateService.class);

        //Logging for debug. Only valid when error occurred and app is closed. Need to be changed for more precise debugging
        try {
            Calendar calendar = Calendar.getInstance();
            File logfile= new File(Environment.getExternalStorageDirectory()+"/CPSLogger/logfile_" + new SimpleDateFormat("yyyy_MM_dd").format(new Date()) + calendar.get(Calendar.HOUR_OF_DAY) + "h" + calendar.get(Calendar.MINUTE) + "m" + calendar.get(Calendar.SECOND) + "s.txt");
            logfile.createNewFile();
            String cmd = "logcat -d -f "+logfile.getAbsolutePath();
            Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Open setting page for enabling notification listener
        ContentResolver contentResolver = this.getContentResolver();
        String enabledNoti = Settings.Secure.getString(contentResolver,"enabled_notification_listeners");
        if(enabledNoti != null && enabledNoti.contains(this.getPackageName())){
            Log.d("main","Already Set");
        }
        else{
            Intent settingNoti = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(settingNoti);
        }
        String enabledUsage = Settings.Secure.getString(contentResolver,"enabled_notification_listeners");

        kgthread = new KeepGoing();

        //Start Button set-up
        Button btnstart = (Button)findViewById(R.id.start);
        btnstart.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                mButton = true;

                if (onOff == true) {
                    kgthread = new KeepGoing();
                }

                // start the thread
                kgthread.start();
            }
        });

        // End Button set-up
        Button btnend = (Button)findViewById(R.id.stop);
        btnend.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                mButton = false;
                onOff = true; // indicates that the end button is pressed

                // stop services
                stopService(intentLoc);
                stopService(intentMov);
                stopService(intentWifi);
				stopService(intentRec);
                stopService(intentSoft);
                stopService(intentApp);
                stopService(intentSig);
                stopService(intentNoti);
                stopService(intentPhone);
                Intent i = new Intent("cpslab.inhwan.cpslogger_v02.NotificationService");
                i.putExtra("Notification_Event", "QUIT");
                Intent settingNoti = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(settingNoti);

                ContentResolver contentResolver = MainActivity.this.getContentResolver();
                String enabledNoti = Settings.Secure.getString(contentResolver,"enabled_notification_listeners");
                while(enabledNoti != null && enabledNoti.contains(MainActivity.this.getPackageName())){
                    Log.d("main", "Not disabled : " + enabledNoti.contains(MainActivity.this.getPackageName()));
                    try {
                        SystemClock.sleep(1000);
                        enabledNoti = Settings.Secure.getString(contentResolver,"enabled_notification_listeners");
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
                sendBroadcast(i);

                // remove the notification
                nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                nm.cancel(1234);
                // not necessary maybe...
                if(mWakeLock != null) {
                    mWakeLock.release();
                    mWakeLock = null;
                    Log.i("mWakeLock", "off");
                }
            }
        });

        Button btnexit = (Button)findViewById(R.id.exit);
        btnexit.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                MainActivity.this.finishAffinity();
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public void onPause() {
        super.onPause();
    }

    public void onResume() {
        super.onResume();
    }

    private class KeepGoing extends Thread {

        public void run() {

            while(mButton) {
                try {

                    Log.d("mButton", "okok");

                    // start services
                    startService(intentLoc);
                    startService(intentMov);
                    startService(intentWifi);
					startService(intentRec);
                    startService(intentSoft);
                    startService(intentApp);
                    startService(intentNoti);
                    startService(intentSig);
                    startService(intentPhone);

                    serviceOn = true;

                    // notification at status bar
                    nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    PendingIntent intent1 = PendingIntent.getActivity(MainActivity.this, 0, new Intent(MainActivity.this, MainActivity.class), 0);

                    // Create Notification Object
                    Notification notification = new Notification(android.R.drawable.ic_input_add, "Big Brother is Watching U", System.currentTimeMillis());
                    notification.setLatestEventInfo(MainActivity.this, "HistoryLogNew", "Big Brother is Watching U", intent1);
                    nm.notify(1234, notification);

                    // not necessary...maybe
                    if(mWakeLock == null) {
                        mPm = (PowerManager)getSystemService(Context.POWER_SERVICE);
                        mWakeLock = mPm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Wake_Lock");
                        Log.i("mWakeLock", "on");
                        mWakeLock.acquire();
                    }

                    // restart in order to prevent auto quitting
                    sleep(1200000);

                    stopService(intentLoc);
                    stopService(intentMov);
                    stopService(intentWifi);
					stopService(intentRec);
                    stopService(intentSoft);
                    stopService(intentNoti);
                    stopService(intentApp);
                    stopService(intentSig);
                    stopService(intentPhone);

                    serviceOn = false;

                    // this needs to be modified
                    // Stopping the service is not working properly that it continuously turned on after the sleep()

                    // remove the notification
                    nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                    nm.cancel(1234);

                    if(mWakeLock != null) {
                        mWakeLock.release();
                        mWakeLock = null;
                        Log.i("mWakeLock", "off");
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }

}