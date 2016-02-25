package cpslab.inhwan.cpslogger_v02;

/**
 * Created by Hyunjun on 2/22/2016.
 */

import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
    String currentDate;
    File file;
    FileOutputStream fileOutputStream;
    OutputStreamWriter outputStreamWriter;

    Logger(String name){
        Date date = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");
        currentDate = simpleDateFormat.format(date);

        createFile(name);
    }

    public void createFile(String name){
        String fileName = "CPSLogger_" + name + "_" + currentDate + ".txt";
        String directory = baseDir + File.separator+"CPSLogger" + File.separator + name;
        Log.d("Logger",name+" Logger File will create soon");

        File dir = new File(directory);
        if(!dir.exists()){
            dir.mkdirs();
            Log.i(name, "Directory is created");
        }
        else
            Log.i(name, "Directory is already exist");

        file = new File(dir,fileName);

        try{
            fileOutputStream = new FileOutputStream(file);
            outputStreamWriter = new OutputStreamWriter(fileOutputStream);
        }
        catch (Exception e){
            Log.e("Logging", e.toString());
        }
    }

    public void writeData(String name, String data){
        SimpleDateFormat format = new SimpleDateFormat("kk:mm:ss.SSS");
        String timeStamp = format.format(new Date());

        try{
            outputStreamWriter.write(timeStamp + "," + data);
        }
        catch (Exception e){
            Log.e("Logging", e.toString());
        }
    }

    public void closeFile(String name){
        try{
            outputStreamWriter.close();
            fileOutputStream.close();
        }
        catch (Exception e){
            Log.e("Logging", e.toString());
        }
    }
}
