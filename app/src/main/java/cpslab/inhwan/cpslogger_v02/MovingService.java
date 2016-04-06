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
    SensorEventListener stepL;
    Sensor stepSensor;

    boolean isStart1;
    int loopCount1;

    boolean isStart2;
    int loopCount2;

    boolean isStart3;
    int loopCount3;

    boolean isStart4;
    int loopCount4;

    String [] acc = new String[3];
    String [] mag = new String[3];
    String [] gyr = new String[3];
    String stp;

    float [] accc = new float[3];
    float [] magg = new float[3];
    float [] gyro = new float[3];
    float step;

    double [] ac = new double[3];

    String nameAcc = "Acc";
    String nameGyro = "Gyro";
    String nameMag = "Mag";
    String nameStep = "Step";
    Logger accLogger = new Logger(nameAcc);
    Logger gyroLogger = new Logger(nameGyro);
    Logger magLogger = new Logger(nameMag);
    Logger stepLogger = new Logger(nameStep);
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
        mSm.unregisterListener(stepL);

        if (fileOpen) {
            accLogger.closeFile(nameAcc);
            gyroLogger.closeFile(nameGyro);
            magLogger.closeFile(nameMag);
            stepLogger.closeFile(nameStep);
            fileOpen = false;
        }

        //		registerRestartAlarm();
        //		Toast.makeText(this, "Mov-Watching is ended", 0).show();		//toast message
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        int delay = SensorManager.SENSOR_DELAY_FASTEST;
        int delay2 = (Integer) SensorManager.SENSOR_DELAY_NORMAL;

        accSensor = mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accL = new accListener();
        magSensor = mSm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        magL = new magListener();
        gyrSensor = mSm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        gyrL = new gyrListener();
        stepSensor = mSm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        stepL = new stepListener();


        mSm.registerListener(accL, accSensor, delay);
        mSm.registerListener(magL, magSensor, delay);
        mSm.registerListener(gyrL, gyrSensor, delay);
        mSm.registerListener(stepL,stepSensor, delay2);

        isStart1 = true; // indicate it is started
        loopCount1 = 1;

        isStart2 = true; // indicate it is started
        loopCount2 = 1;

        isStart3 = true; // indicate it is started
        loopCount3 = 1;

        isStart4 = true;
        loopCount4 = 1;

        Toast.makeText(this, "Mov-Watching is started", Toast.LENGTH_SHORT).show();		//toast message

        if(!fileOpen){
            accLogger.createFile(nameAcc);
            gyroLogger.createFile(nameGyro);
            magLogger.createFile(nameMag);
            stepLogger.createFile(nameStep);
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
                    accLogger.writeData("1" +"," + acc[0] + ", " + acc[1] + ", " + acc[2]);

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
                    gyroLogger.writeData("2," + gyr[0] + ", " + gyr[1] + ", " + gyr[2]);

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
                    magLogger.writeData("3," + mag[0] + ", " + mag[1] + ", " + mag[2]);

//                lg_Mag.o(mag[0] + ", " + mag[1] + ", " + mag[2]);
            }
        }
    }


    private class stepListener implements SensorEventListener {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        public void onSensorChanged(SensorEvent event) {
          if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                step = event.values[0];
                stp = Double.toString(step);
                Log.d("step", stp);
                if(fileOpen)
                    stepLogger.writeData("4," + stp);

//                lg_Gyr.o(gyr[0] + ", " + gyr[1] + ", " + gyr[2]);
            }
        }
    }
}