package cpslab.inhwan.cpslogger_v02;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SignalSensingService extends Service {
    public SignalSensingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
