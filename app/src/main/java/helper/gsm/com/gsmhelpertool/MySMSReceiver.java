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

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                String phoneNumber = "";
                String message = "";
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    message += currentMessage.getDisplayMessageBody();
                } // end for loop

                try{
                    Intent intent2 =new Intent();
                    intent2.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    intent2.setAction("com.gsmmodem");
                    intent2.putExtra("phone", phoneNumber);
                    intent2.putExtra("message", message);
                    context.sendBroadcast(intent2);

                    Intent logIntent = new Intent();
                    logIntent.setAction(String.valueOf(R.string.log_receiver));
                    logIntent.putExtra("phone", phoneNumber);
                    logIntent.putExtra("message", message);
                    context.sendBroadcast(logIntent);
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" +e);

        }
    }
}
