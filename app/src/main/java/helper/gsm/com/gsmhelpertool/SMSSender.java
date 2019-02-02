package helper.gsm.com.gsmhelpertool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;

public class SMSSender extends BroadcastReceiver {
    SmsManager smsManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        smsManager = SmsManager.getDefault();
        Bundle bundle = intent.getExtras();
        String phone = bundle.getString("phone");
        String message = bundle.getString("message");
        if (phone != null && message != null){
            smsManager.sendTextMessage(phone,
                    null,message,null,null);
        }
    }
}
