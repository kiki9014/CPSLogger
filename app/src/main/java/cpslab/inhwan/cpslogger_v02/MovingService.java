package cpslab.inhwan.cpslogger_v02;

/**
 * Created by Inhwan on 2015-10-01.
 */
import java.util.Calendar;
import android.app.*;
import android.content.*;
import android.hardware.*;
import android.os.*;
import android.util.Log;
import android.widget.*;

public class MovingService extends Service {

    SensorManager mSm;
    SensorEventListener accL;
    Sensor accSensor;
    SensorEventListener magL;
    Sensor magSensor;
    SensorEventListener gyrL;
    Sensor gyrSensor;

    boolean isStart1;
    int loopCount1;

    boolean isStart2;
    int loopCount2;

    boolean isStart3;
    int loopCount3;

    String [] acc = new String[3];
    String [] mag = new String[3];
    String [] gyr = new String[3];

    float [] accc = new float[3];
    float [] magg = new float[3];
    float [] gyro = new float[3];

    double [] ac = new double[3];

    String name = "Moving";
    Logger movLogger = new Logger(name);
    boolean fileOpen;

    public void onCreate() {
        super.onCreate();

        mSm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        fileOpen = true;
        //		unregisterRestartAlarm();
    }

    public void onDestroy() {
        super.onDestroy();

        mSm.unregisterListener(accL);
        mSm.unregisterListener(magL);
        mSm.unregisterListener(gyrL);

        if (fileOpen) {
            movLogger.closeFile(name);
            fileOpen = false;
        }

        //		registerRestartAlarm();
        //		Toast.makeText(this, "Mov-Watching is ended", 0).show();		//toast message
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        int delay = SensorManager.SENSOR_DELAY_FASTEST;

        accSensor = mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accL = new accListener();
        magSensor = mSm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magL = new magListener();
        gyrSensor = mSm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyrL = new gyrListener();

        mSm.registerListener(accL, accSensor, delay);
        mSm.registerListener(magL, magSensor, delay);
        mSm.registerListener(gyrL, gyrSensor, delay);

        isStart1 = true; // indicate it is started
        loopCount1 = 1;

        isStart2 = true; // indicate it is started
        loopCount2 = 1;

        isStart3 = true; // indicate it is started
        loopCount3 = 1;

        Toast.makeText(this, "Mov-Watching is started", Toast.LENGTH_SHORT).show();		//toast message

        if(!fileOpen){
            movLogger.createFile(name);
            fileOpen = true;
        }

        return START_STICKY;		//Sticky & Unsticky: what is the difference?
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private class accListener implements SensorEventListener {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        public void onSensorChanged(SensorEvent event) {
            if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {}
            else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accc = event.values;

                acc[0] = Double.toString(accc[0]);
                acc[1] = Double.toString(accc[1]);
                acc[2] = Double.toString(accc[2]);
//                Log.d("acc", acc[0] + ", " + acc[1] + ", " + acc[2]);
                if(fileOpen)
                    movLogger.writeData("acc" +"," + acc[0] + ", " + acc[1] + ", " + acc[2]);

//                lg_Mov.o(acc[0] + ", " + acc[1] + ", " + acc[2]);
            }
        }
    }

    private class gyrListener implements SensorEventListener {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        public void onSensorChanged(SensorEvent event) {
            if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {}
            else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gyro = event.values;
                gyr[0] = Double.toString(gyro[0]);
                gyr[1] = Double.toString(gyro[1]);
                gyr[2] = Double.toString(gyro[2]);
//                Log.d("gyr", gyr[0] + ", " + gyr[1] + ", " + gyr[2]);
                if(fileOpen)
                    movLogger.writeData("gyro," + gyr[0] + ", " + gyr[1] + ", " + gyr[2]);

//                lg_Gyr.o(gyr[0] + ", " + gyr[1] + ", " + gyr[2]);
            }
        }
    }

    private class magListener implements SensorEventListener {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        public void onSensorChanged(SensorEvent event) {
            if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {}
            else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magg = event.values;
                mag[0] = Double.toString(magg[0]);
                mag[1] = Double.toString(magg[1]);
                mag[2] = Double.toString(magg[2]);
//                Log.d("mag", mag[0] + ", " + mag[1] + ", " + mag[2]);
                if(fileOpen)
                    movLogger.writeData("mag," + mag[0] + ", " + mag[1] + ", " + mag[2]);

//                lg_Mag.o(mag[0] + ", " + mag[1] + ", " + mag[2]);
            }
        }
    }


}