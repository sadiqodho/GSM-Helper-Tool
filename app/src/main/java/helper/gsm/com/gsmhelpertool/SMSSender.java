package helper.gsm.com.gsmhelpertool;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;

import java.util.ArrayList;

public class SMSSender extends BroadcastReceiver {
    SmsManager smsManager;
    String SENT = "SMS_SENT";
    String DELIVERED = "SMS_DELIVERED";

    @Override
    public void onReceive(Context context, Intent intent) {
        smsManager = SmsManager.getDefault();
        Bundle bundle = intent.getExtras();
        String phone = bundle.getString("phone");
        String message = bundle.getString("message");
        if (phone != null && message != null){

            ArrayList<String> parts = smsManager.divideMessage(message);
            int numParts = parts.size();

            ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
            try {
                for (int i = 0; i < numParts; i++) {
                    sentIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0));
                    deliveryIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0));
                }

                smsManager.sendMultipartTextMessage( phone,null, parts, sentIntents, deliveryIntents);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
