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
        android.util.Log.d("SMSSender", "Received broadcast");
        try {
            smsManager = SmsManager.getDefault();
            
            if (intent == null) {
                android.util.Log.e("SMSSender", "Intent is null");
                return;
            }
            
            Bundle bundle = intent.getExtras();
            if (bundle == null) {
                android.util.Log.e("SMSSender", "Bundle is null");
                return;
            }
            
            String phone = bundle.getString("phone");
            String message = bundle.getString("message");
            
            android.util.Log.d("SMSSender", "Phone: " + phone + ", Message: " + message);
            
            if (phone != null && message != null && !phone.isEmpty() && !message.isEmpty()){

                ArrayList<String> parts = smsManager.divideMessage(message);
                int numParts = parts.size();

                android.util.Log.d("SMSSender", "Message divided into " + numParts + " parts");

                ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>();
                ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>();
                try {
                    for (int i = 0; i < numParts; i++) {
                        sentIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(SENT), PendingIntent.FLAG_IMMUTABLE));
                        deliveryIntents.add(PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), PendingIntent.FLAG_IMMUTABLE));
                    }

                    smsManager.sendMultipartTextMessage(phone, null, parts, sentIntents, deliveryIntents);
                    android.util.Log.d("SMSSender", "SMS sent successfully");
                }catch (Exception ex){
                    android.util.Log.e("SMSSender", "Error sending SMS", ex);
                }
            } else {
                android.util.Log.e("SMSSender", "Phone or message is null or empty");
            }
        } catch (Exception e) {
            android.util.Log.e("SMSSender", "Error in onReceive", e);
        }
    }
}
