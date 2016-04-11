package cpslab.inhwan.cpslogger_v02;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

public class PhoneStateService extends Service {

    static String name = "Phone";

    Logger phoneLogger = new Logger(name);

    static boolean fileOpen;

    broadcastReceiver phoneBR;

    @Override
    public void onCreate(){
        super.onCreate();

        fileOpen = true;

        phoneBR = new broadcastReceiver();
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction("cpslab.inhwan.cpslogger_v02.PhoneStateLogging");
        registerReceiver(phoneBR, iFilter);
    }

    @Override
    public void onDestroy(){
        if(fileOpen){
            phoneLogger.closeFile(name);
            fileOpen = false;
        }

        unregisterReceiver(phoneBR);

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(this, "Phone-watching is started", Toast.LENGTH_SHORT).show(); //modified to Toast.LENGTH_SHORT from 0

        if(!fileOpen){
            phoneLogger.createFile(name);
            fileOpen = true;
        }

        return START_STICKY;
    }

    public class broadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            String data = intent.getStringExtra("data2Log");

            if(fileOpen){
                phoneLogger.writeData(data);
            }
        }
    }
}
