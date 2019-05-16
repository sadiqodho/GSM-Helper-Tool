package helper.gsm.com.gsmhelpertool;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private final static Integer NOT_SUPPORTED_VERSION = 8;
    private SMSSender smsSender;
    private MySMSReceiver mySMSReceiver;
    private IntentFilter intentFilter;
    private IntentFilter intentFilterRec;

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

        registerReceiver(smsSender, intentFilter);
        registerReceiver(mySMSReceiver, intentFilterRec);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsSender);
        unregisterReceiver(mySMSReceiver);
    }
}
