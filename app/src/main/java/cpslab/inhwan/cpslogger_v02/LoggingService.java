package cpslab.inhwan.cpslogger_v02;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LoggingService extends Service {
    String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    String fileName, filePath;
    File file;
    FileOutputStream fileOutputStream;
    OutputStreamWriter outputStreamWriter;

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_mm_dd");
        fileName = "CPSLogger_" + simpleDateFormat.format(date) + ".csv";
        filePath = baseDir + File.pathSeparator + "CPSLogger" + File.separator+fileName;

        file = new File(filePath);

        try{
            fileOutputStream = new FileOutputStream(file);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        }
        catch (Exception e){
            Log.e("Logging", e.toString());
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        try{
            outputStreamWriter.close();
            fileOutputStream.close();
        }
        catch (Exception e){
            Log.e("Logging", e.toString());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
