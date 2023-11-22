package helper.gsm.com.gsmhelpertool;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import helper.gsm.com.gsmhelpertool.contracts.SettingsContract;
import helper.gsm.com.gsmhelpertool.helpers.DatabaseHelper;
import helper.gsm.com.gsmhelpertool.services.ForegroundService;
import helper.gsm.com.gsmhelpertool.services.LogMessageReceiver;
import helper.gsm.com.gsmhelpertool.utils.Utilities;

public class MainActivity extends AppCompatActivity {

    private LogMessageReceiver logMessageReceiver;
    private DatabaseHelper databaseHelper;
    private SQLiteDatabase db;

    private final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        public void run() {
            boolean isChecked = Utilities.getSettingsBooleanColumn(db, SettingsContract.COLUMN_GSM_MODEM_STATUS);
            if(Utilities.isPackageInstalled(getApplicationContext().getPackageManager()) && isChecked){
                String packageName = "com.gsmmodem";
                Intent gsmModemIntent = new Intent();
                gsmModemIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                gsmModemIntent.setAction(packageName);
                gsmModemIntent.putExtra("current_status", Utilities.getCurrentDateTime());
                getApplicationContext().sendBroadcast(gsmModemIntent);
                Log.v("GSMHelperTool", "Main Activity: " + Utilities.getCurrentDateTime());
            }
            handler.postDelayed(this, 10000 * 5);// Repeat the activity every minute
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseHelper = new DatabaseHelper(getApplicationContext());
        db = databaseHelper.getWritableDatabase();

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{android.Manifest.permission.ACCESS_WIFI_STATE,
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.ACCESS_NETWORK_STATE,
                        android.Manifest.permission.RECEIVE_SMS,
                        android.Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS},
                1);

        IntentFilter smsLogFilter = new IntentFilter(String.valueOf(R.string.log_receiver));

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

        EditText editTextLog = findViewById(R.id.editTextLog);
        logMessageReceiver = new LogMessageReceiver(editTextLog);

        // For internal logging
        registerReceiver(logMessageReceiver, smsLogFilter);
        startService(new Intent(this, ForegroundService.class));

        try{
            ToggleButton toggleButton = findViewById(R.id.updateToGSMModemApp);
            toggleButton.setChecked(Utilities.getSettingsBooleanColumn(db, SettingsContract.COLUMN_GSM_MODEM_STATUS));

            if (toggleButton.isChecked()){
                handler.post(runnable);
            }

            toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    ContentValues contentValues = new ContentValues();
                    if (isChecked) {
                        handler.post(runnable);
                    } else {
                        handler.removeCallbacks(runnable);
                    }

                    contentValues.put(SettingsContract.COLUMN_GSM_MODEM_STATUS, isChecked);
                    db.update(SettingsContract.TABLE_NAME, contentValues, null, null);
            });
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(logMessageReceiver);
    }
}
