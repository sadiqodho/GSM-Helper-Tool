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
        smsLogFilter = new IntentFilter(String.valueOf(R.string.log_receiver));

        mySMSReceiver = new MySMSReceiver();
        intentFilterRec = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");

        Button b1 = (Button) findViewById(R.id.button);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = (EditText) findViewById(R.id.editText);
                String phone = editText.getText().toString();
                Intent intent = new Intent();
                intent.setAction("helper.gsm.com.gsmhelpertool");
                intent.putExtra("phone", phone);
                intent.putExtra("message", phone);
                sendBroadcast(intent);
            }
        });


        // For listening for incoming SMS
        registerReceiver(mySMSReceiver, intentFilterRec);

        // For internal logging
        registerReceiver(smsLogBroadcastReceiver, smsLogFilter);

        // for sending to the SmsSender
        registerReceiver(smsSender, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsSender);
        unregisterReceiver(mySMSReceiver);
        unregisterReceiver(smsLogBroadcastReceiver);
    }

    BroadcastReceiver smsLogBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // internet lost alert dialog method call from here...

            try {
                final Bundle bundle = intent.getExtras();
                String phone = bundle.getString("phone");
                String message = bundle.getString("message");

                String logMessage = "Received SMS from "
                        + phone
                        + " with content: ["
                        + message
                        + "]";

                EditText editTextLog = (EditText) findViewById(R.id.editTextLog);
                editTextLog.append(
                        new Date().toString()
                                + ": "
                                + logMessage
                                + "\n\n"
                );
            } catch (Exception e) {
                Log.d(String.valueOf(R.string.log_receiver), "Error: " + e.getLocalizedMessage());
            }
        }
    };
}
