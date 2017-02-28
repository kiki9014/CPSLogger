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
    // Step counter is not work in old version. (work in android version after 5.0)

    boolean isStart1, isUnrel1;
    int loopCount1, cntUnrel1;

    boolean isStart2, isUnrel2;
    int loopCount2, cntUnrel2;

    boolean isStart3, isUnrel3;
    int loopCount3, cntUnrel3;

    boolean isStart4, isUnrel4;
    int loopCount4, cntUnrel4;

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

    public void makeToast(String contentStr){

        Toast.makeText(this, contentStr, Toast.LENGTH_SHORT).show();		//toast message
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private class accListener implements SensorEventListener {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
        public void onSensorChanged(SensorEvent event) {
            if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                if(isUnrel1){
                    cntUnrel1++;
                    if(cntUnrel1>100){
                        cntUnrel1 = 0;
                        makeToast("Acc has got 100 unreliable data");
                        Log.d("Magnet", "unreliable value is collected");
                    }
                }
                else{
                    isUnrel1 = true;
                }
            }
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                isUnrel1 = false;
                cntUnrel1 = 0;
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
            if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                if(isUnrel2){
                    cntUnrel2++;
                    if(cntUnrel2>100){
                        cntUnrel2 = 0;
                        makeToast("Gyro has got 100 unreliable data");
                        Log.d("Magnet", "unreliable value is collected");
                    }
                }
                else{
                    isUnrel2 = true;
                }
            }
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                isUnrel2 = false;
                cntUnrel2 = 0;
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
            if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
                if(isUnrel3){
                    cntUnrel3++;
                    if(cntUnrel3>100){
                        cntUnrel3 = 0;
                        makeToast("Mag has got 100 unreliable data");
                        Log.d("Magnet", "unreliable value is collected");
                    }
                }
                else{
                    isUnrel3 = true;
                }
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                isUnrel3 = false;
                cntUnrel3 = 0;
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