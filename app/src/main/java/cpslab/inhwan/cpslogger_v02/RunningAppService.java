package cpslab.inhwan.cpslogger_v02;

/**
 * Created by Inhwan on 2015-10-01.
 */
import java.util.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import android.widget.*;

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
                    ActivityManager activityManager = (ActivityManager)getSystemService( ACTIVITY_SERVICE );
                    List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();

                    List<ActivityManager.RunningTaskInfo> taskInfos = activityManager.getRunningTasks(1);

                    ComponentName topActivity = taskInfos.get(0).topActivity;
                    String pkgName = topActivity.getPackageName();
//                    Log.i("pkgName", pkgName);

                    if(pkgName.compareTo(pkgbuff) != 0 ) {
//                        lg_App.o("\n"+"No. of Running Program: "+procInfos.size()+"\n"+"Top Activity: "+pkgName+"\n");
                        pkgbuff = pkgName;
                        Log.i("pkgbuff", pkgbuff);
                        Log.i("pkgNo", Double.toString(procInfos.size()));
                        String pkgData = "top," + pkgbuff;
                        appLogger.writeData(pkgData);
                    }
                    else {}

                    //	for(int i = 0; i < procInfos.size(); i++)
                    //	{
                    //		tv.setText(tv.getText().toString()+procInfos.get(i).pid+", "+procInfos.get(i).processName+"\n");
                    //	}
                    sleep(100000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}