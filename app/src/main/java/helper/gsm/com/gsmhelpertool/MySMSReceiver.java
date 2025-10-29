package helper.gsm.com.gsmhelpertool;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class MySMSReceiver extends BroadcastReceiver {
    final SmsManager sms = SmsManager.getDefault();
    /**
     * Database properties
     */
    private Context context;
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d("SmsReceiver", "=== MySMSReceiver: onReceive called ===");

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                
                // Check if pdusObj is null or empty
                if (pdusObj == null || pdusObj.length == 0) {
                    Log.e("SmsReceiver", "pdusObj is null or empty");
                    return;
                }
                
                String phoneNumber = "";
                String message = "";
                for (int i = 0; i < pdusObj.length; i++) {
                    if (pdusObj[i] != null) {
                        SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                        if (currentMessage != null) {
                            phoneNumber = currentMessage.getDisplayOriginatingAddress();
                            message += currentMessage.getDisplayMessageBody();
                        }
                    }
                } // end for loop

                Log.d("SmsReceiver", "Received SMS from: " + phoneNumber + " with message: " + message);

                // Only proceed if we have valid phone number and message
                if (phoneNumber != null && !phoneNumber.isEmpty() && message != null) {
                    try{
                        Log.d("SmsReceiver", "Sending broadcast to com.gsmmodem app with phone: " + phoneNumber);
                        
                        Intent intent2 = new Intent();
                        intent2.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                        intent2.setAction("com.gsmmodem");
                        intent2.putExtra("phone", phoneNumber);
                        intent2.putExtra("message", message);
                        
                        // For Android 12+ (API 31+), add explicit package
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            intent2.setPackage("com.gsmmodem");
                        }
                        
                        context.sendBroadcast(intent2);
                        Log.d("SmsReceiver", "Broadcast sent to com.gsmmodem successfully");

                        Intent logIntent = new Intent();
                        if (context != null && context.getResources() != null) {
                            logIntent.setAction(context.getResources().getString(R.string.log_receiver));
                            logIntent.putExtra("phone", phoneNumber);
                            logIntent.putExtra("message", message);
                            context.sendBroadcast(logIntent);
                            Log.d("SmsReceiver", "Log broadcast sent to internal receiver");
                        }
                    }catch (Exception ex){
                        Log.e("SmsReceiver", "Error sending broadcast", ex);
                    }
                } else {
                    Log.e("SmsReceiver", "Phone number or message is empty");
                }
            } else {
                Log.e("SmsReceiver", "Bundle is null");
            }

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception in smsReceiver", e);

        }
    }
}
