package cpslab.inhwan.cpslogger_v02;

/**
 * Created by Inhwan on 2015-10-01.
 */
import android.app.*;
import android.content.*;
import android.location.*;
import android.os.*;
import android.widget.*;
import android.util.*;

public class LocationService extends Service {

    LocationManager mLocMan;
    String mProvider;
    LocationListener locL;
    String name = "Location";
    Logger locLogger = new Logger(name);

    boolean fileOpen;

    public void onCreate() {
        super.onCreate();
        mLocMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mProvider = mLocMan.getBestProvider(new Criteria(), true);
        locL = new locListener();

        fileOpen = true;
    }

    public void onDestroy() {
        super.onDestroy();
        mLocMan.removeUpdates(locL);
        if (fileOpen) {
            locLogger.closeFile(name);
            fileOpen = false;
        }
    }

    public int onStartCommand (Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        mLocMan.requestLocationUpdates(mProvider, 30000, 1, locL);
        mLocMan.addNmeaListener(m_nmea_listener);
        Toast.makeText(this, "Loc-Watching is started", Toast.LENGTH_SHORT).show();

        if(!fileOpen){
            locLogger.createFile(name);
            fileOpen = true;
        }

        return START_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    private class locListener implements LocationListener {
        public void onLocationChanged(Location location) {

            if(fileOpen)
                locLogger.writeData("GPS," + location.getLatitude() + "," + location.getLongitude() + "," + location.getAltitude());


        }
        public void onProviderDisabled(String provider) {
        }
        public void onProviderEnabled(String provider) {
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }

    GpsStatus.NmeaListener m_nmea_listener = new GpsStatus.NmeaListener(){			//GPS
        public void onNmeaReceived(long timestamp, String nmea) {

            if(nmea.startsWith("$GPGGA")) {
                String str_temp[] = nmea.split(",");
                if(fileOpen)
                    locLogger.writeData("SAT,"+ str_temp[7]);
            }

        }
    };


}