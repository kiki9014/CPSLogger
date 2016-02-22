package cpslab.inhwan.cpslogger_v02;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class LoggingService extends Service {
    String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    String currentDate;
    Map<String, File> files;
    Map<String, FileOutputStream> fileOutputStreams;
    Map<String, OutputStreamWriter> outputStreamWriters;

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_mm_dd");
        currentDate = simpleDateFormat.format(date);

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    public void createFile(String name){
        String fileName = "CPSLogger_" + name + "_" + currentDate + ".csv";
        String filePath = baseDir + File.pathSeparator + "CPSLogger" + File.separator+fileName;

        File file = new File(filePath);

        try{
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            fileOutputStreams.put(name,fileOutputStream);
            outputStreamWriters.put(name,outputStreamWriter);
        }
        catch (Exception e){
            Log.e("Logging",e.toString());
        }
    }

    public void writeData(String name, String data){
        SimpleDateFormat format = new SimpleDateFormat("kk:mm:ss.SSS");
        String timeStamp = format.format(new Date());

        try{
            OutputStreamWriter osw = outputStreamWriters.get(name);
            osw.write(timeStamp + "," + data);
        }
        catch (Exception e){
            Log.e("Logging", e.toString());
        }
    }

    public void closeFile(String name){
        try{
            FileOutputStream fos = fileOutputStreams.get(name);
            OutputStreamWriter osw = outputStreamWriters.get(name);

            osw.close();
            fos.close();
        }
        catch (Exception e){
            Log.e("Logging", e.toString());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class saveReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent){
            
        }
    }
}
