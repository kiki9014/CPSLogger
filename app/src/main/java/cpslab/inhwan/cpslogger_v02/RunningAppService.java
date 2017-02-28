package cpslab.inhwan.cpslogger_v02;

/**
 * Created by Inhwan on 2015-10-01.
 */
import java.io.IOException;
import java.util.*;
import android.app.*;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.*;
import android.os.*;
import android.util.*;
import android.widget.*;

//3rd party library. but it uses linux kernel and will work in continued version upgrade
import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jaredrummler.android.processes.models.AndroidProcess;
import com.jaredrummler.android.processes.models.Stat;

public class RunningAppService extends Service {
    static String name = "App";

    boolean mQuit;

    String pkgbuff;

    Logger appLogger = new Logger(name);

    static boolean fileOpen;

    public void onCreate() {
        super.onCreate();
//		unregisterRestartAlarm();

        fileOpen = true;
    }

    public void onDestroy() {
        super.onDestroy();
        mQuit = true;
//		registerRestartAlarm();
//		Toast.makeText(this, "App-Watching is ended", 0).show();

        if(fileOpen){
            appLogger.closeFile(name);
        }
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        pkgbuff = "0";
        mQuit = false;

        appThread appt = new appThread();
        appt.start();		//start the appThread
        Toast.makeText(this, "App-Watching is started", Toast.LENGTH_SHORT).show(); //modified to Toast.LENGTH_SHORT from 0

        if(!fileOpen){
            appLogger.createFile(name);
            fileOpen = true;
        }

        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private class appThread extends Thread {

        public void run() {

            while(!mQuit) {
                try {

                    List<AndroidAppProcess> processes = AndroidProcesses.getRunningForegroundApps(getApplicationContext());
                    String pkgData = "Forground";
                    for(AndroidAppProcess process : processes){
                        pkgData = pkgData + "," + process.getPackageName();
                    }

                    Log.d("ForeApp", pkgData);
                    appLogger.writeData(pkgData);
                    sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //Not used now
        public String printForegroundTask(Context context) {
            String currentApp = "Null";
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                List<AndroidAppProcess> processes = AndroidProcesses.getRunningForegroundApps(getApplicationContext());
                if (processes != null && processes.size() > 0) {
                    SortedMap<Long, AndroidAppProcess> mySortedMap = new TreeMap<Long, AndroidAppProcess>();
                    for (AndroidAppProcess process : processes) {
                        try{
                            Stat stat = process.stat();
                            mySortedMap.put(stat.priority(), process);
                        }
                        catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    if (mySortedMap != null && !mySortedMap.isEmpty()) {
                        currentApp = mySortedMap.get(mySortedMap.lastKey())
                                .getPackageName();
                    }
                }
            } else {
                ActivityManager am = (ActivityManager) context
                        .getSystemService(Context.ACTIVITY_SERVICE);
                currentApp = am.getRunningTasks(1).get(0).topActivity
                        .getPackageName();

            }
            return currentApp;
        }
    }


}