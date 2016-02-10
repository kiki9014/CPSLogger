package cpslab.inhwan.cpslogger_v02;

import android.content.*;
import android.telephony.*;
import android.util.*;

public class ServiceReceiver extends BroadcastReceiver  {

private static final String TAG = "my";

@Override
public void onReceive(Context context, Intent intent) {
 Log.i(TAG, "ServiceReceiver->onReceive();");
 MyPhoneStateListener phoneListener = new MyPhoneStateListener();
 TelephonyManager telephony = (TelephonyManager)
                              context.getSystemService(Context.TELEPHONY_SERVICE);
 telephony.listen(phoneListener, PhoneStateListener.LISTEN_SERVICE_STATE);
 telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
 Intent testActivityIntent = new Intent(context,MainActivity.class);
 testActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                testActivityIntent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                context.startActivity(testActivityIntent);
	}
}