package helper.gsm.com.gsmhelpertool;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.time.LocalDateTime;
import java.util.Date;

import static android.content.Context.RECEIVER_NOT_EXPORTED;
import static android.content.Context.RECEIVER_EXPORTED;

public class MainActivity extends AppCompatActivity {
    private final static Integer NOT_SUPPORTED_VERSION = 8;
    private SMSSender smsSender;
    private MySMSReceiver mySMSReceiver;
    private IntentFilter intentFilter;
    private IntentFilter intentFilterRec;
    private IntentFilter smsLogFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.ACCESS_WIFI_STATE,
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.ACCESS_NETWORK_STATE,
                        android.Manifest.permission.RECEIVE_SMS,
                        android.Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS},
                1);

        smsSender = new SMSSender();
        intentFilter = new IntentFilter("helper.gsm.com.gsmhelpertool");
        smsLogFilter = new IntentFilter(getString(R.string.log_receiver));

        mySMSReceiver = new MySMSReceiver();
        intentFilterRec = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");

        Button b1 = (Button) findViewById(R.id.button);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.editText);
                String phone = editText.getText().toString();
                
                if (phone == null || phone.isEmpty()) {
                    android.util.Log.e("MainActivity", "Phone number is empty");
                    return;
                }
                
                // Get message from editTextLog or use a default message
                String message = "Test message from GSM Helper Tool";
                
                android.util.Log.d("MainActivity", "Sending SMS to: " + phone + " with message: " + message);
                
                Intent intent = new Intent();
                intent.setAction("helper.gsm.com.gsmhelpertool");
                intent.putExtra("phone", phone);
                intent.putExtra("message", message);
                
                try {
                    sendBroadcast(intent);
                    android.util.Log.d("MainActivity", "Broadcast sent for sending SMS");
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Error sending broadcast", e);
                }
            }
        });


        // For listening for incoming SMS (system broadcast)
        // System broadcasts like SMS_RECEIVED require RECEIVER_EXPORTED
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mySMSReceiver, intentFilterRec, RECEIVER_EXPORTED);
        } else {
            registerReceiver(mySMSReceiver, intentFilterRec);
        }

        // For internal logging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(smsLogBroadcastReceiver, smsLogFilter, RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(smsLogBroadcastReceiver, smsLogFilter);
        }

        // for sending to the SmsSender
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(smsSender, intentFilter, RECEIVER_EXPORTED);
                Log.d("MainActivity", "Registered SMSSender receiver with RECEIVER_EXPORTED");
            } else {
                registerReceiver(smsSender, intentFilter);
                Log.d("MainActivity", "Registered SMSSender receiver without flag");
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Error registering SMSSender receiver", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsSender);
        unregisterReceiver(mySMSReceiver);
        unregisterReceiver(smsLogBroadcastReceiver);
    }

    private BroadcastReceiver smsLogBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // internet lost alert dialog method call from here...

            try {
                final Bundle bundle = intent.getExtras();
                if (bundle == null) return;
                
                String phone = bundle.getString("phone");
                String message = bundle.getString("message");

                String logMessage = "Received SMS from "
                        + phone
                        + " with content: ["
                        + message
                        + "]";

                // Use runOnUiThread to safely access UI elements
                if (MainActivity.this != null && !MainActivity.this.isFinishing()) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                EditText editTextLog = (EditText) MainActivity.this.findViewById(R.id.editTextLog);
                                if (editTextLog != null) {
                                    editTextLog.append(
                                            new Date().toString()
                                                    + ": "
                                                    + logMessage
                                                    + "\n\n"
                                    );
                                }
                            } catch (Exception e) {
                                Log.e("MainActivity", "Error updating log", e);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Error in smsLogBroadcastReceiver", e);
            }
        }
    };
}
