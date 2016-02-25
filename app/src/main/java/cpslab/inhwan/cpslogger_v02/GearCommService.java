package cpslab.inhwan.cpslogger_v02;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class GearCommService extends Service {
    public GearCommService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
