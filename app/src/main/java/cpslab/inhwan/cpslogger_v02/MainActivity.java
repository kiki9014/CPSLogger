package cpslab.inhwan.cpslogger_v02;

import android.app.*;
import android.content.*;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.util.*;
import android.view.*;
import android.widget.*;

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
//    Intent intentApp;

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
//    	serviceOn = false;

        intentLoc = new Intent(MainActivity.this, LocationService.class);
        intentMov = new Intent(MainActivity.this, MovingService.class);
        intentWifi = new Intent(MainActivity.this, WifiService.class);
//		intentRec = new Intent(MainActivity.this, RecordingService.class);
        intentSoft = new Intent(MainActivity.this, SoftSensingService.class);
//        intentApp = new Intent(MainActivity.this, RunningAppService.class);

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
//				stopService(intentRec);
                stopService(intentSoft);
//                stopService(intentApp);

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
//					startService(intentRec);
                    startService(intentSoft);
//                    startService(intentApp);

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
//					stopService(intentRec);
                    stopService(intentSoft);
//                    stopService(intentApp);

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