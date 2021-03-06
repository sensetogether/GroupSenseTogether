// V1

package fr.inria.yifan.mysensor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import fr.inria.yifan.mysensor.Sensing.CrowdSensor;
import fr.inria.yifan.mysensor.Transmission.FilesIOHelper;

import static java.lang.System.currentTimeMillis;

/*
 * This activity provides functions monitoring battery consumption
 */

@SuppressLint("Registered")
public class BatteryUsage extends AppCompatActivity {

    // Parameters for sensing sampling
    public static final int SAMPLE_NUMBER = 300;

    // Email destination for the sensing data
    public static final String DST_MAIL_ADDRESS = "yifan.du@polytechnique.edu";
    public static final int SAMPLE_DELAY = 1000;
    private static final String TAG = "Battery activity";

    private final Object mLock; // Thread locker
    private boolean isGetSenseRun; // Running flag

    // Declare all related views in UI
    private Button mStartButton;
    private Button mStopButton;
    private Switch mSwitchLog;
    private Switch mSwitchMail;
    private ArrayAdapter<String> mAdapterSensing;

    private FilesIOHelper mFilesIOHelper; // File helper
    private ArrayList<String> mSensingData; // Sensing data

    private BatteryManager mBatteryManager;
    private CrowdSensor mCrowdSensor;

    // Constructor initializes locker
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BatteryUsage() {
        mLock = new Object();
    }

    // Initially bind all views
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void bindViews() {
        TextView mWelcomeView = findViewById(R.id.welcome_view);
        mWelcomeView.setText(R.string.hint_sensing);
        mStartButton = findViewById(R.id.start_button);
        mStopButton = findViewById(R.id.stop_button);
        mStopButton.setVisibility(View.INVISIBLE);
        mSwitchLog = findViewById(R.id.switch_log);
        mSwitchMail = findViewById(R.id.switch_mail);
        mSwitchMail.setVisibility(View.INVISIBLE);

        // Build an adapter to feed the list with the content of a string array
        mSensingData = new ArrayList<>();
        mAdapterSensing = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mSensingData);
        // Then attache the adapter to the list view
        ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(mAdapterSensing);

        mStartButton.setOnClickListener(view -> {
            mAdapterSensing.clear();
            //mAdapterSensing.add("0 Timestamp, 1 Latitude, Longitude, 2 Temperature (C), 3 Light density (lx), " +
            //"4 Pressure (hPa), 5 Humidity (%), 6 Sound level (dBA)");
            startRecord();
            mStartButton.setVisibility(View.INVISIBLE);
            mStopButton.setVisibility(View.VISIBLE);
        });
        mStopButton.setOnClickListener(view -> {
            stopRecord();
            mStartButton.setVisibility(View.VISIBLE);
            mStopButton.setVisibility(View.INVISIBLE);
        });
        mSwitchLog.setOnClickListener(view -> {
            if (mSwitchLog.isChecked()) {
                mSwitchMail.setVisibility(View.VISIBLE);
            } else {
                mSwitchMail.setVisibility(View.INVISIBLE);
            }
        });
    }

    // Main activity initialization
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensing);
        bindViews();

        mCrowdSensor = new CrowdSensor(this) {
            @Override
            public void onWorkFinished(JSONObject result) {
                Log.e(TAG, "Work finished: " + result);
            }
        };

        mFilesIOHelper = new FilesIOHelper(this);
        // Get the battery manager
        mBatteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
    }

    // Stop thread when exit!
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSwitchLog.isChecked()) {
            try {
                mFilesIOHelper.autoSave(arrayToString(mSensingData));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        isGetSenseRun = false;
    }

    // Start the sensing thread
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void startRecord() {
        isGetSenseRun = true;
        // Service set "Location", "Temperature", "Light", "Pressure", "Humidity", "Noise"
        //mCrowdSensor.startServices(Arrays.asList("Location", "Temperature", "Light", "Pressure", "Humidity", "Noise"));
        mCrowdSensor.startServices(Arrays.asList("Temperature", "Light", "Pressure", "Humidity", "Noise"));

        new Thread(() -> {
            int count = 0;
            // Record the battery when start
            float mStartBattery = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000f;
            // Test thread
            while (isGetSenseRun && count < SAMPLE_NUMBER) {
                // Sampling time delay
                synchronized (mLock) {
                    try {
                        mLock.wait((long) SAMPLE_DELAY);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                JSONObject result = mCrowdSensor.getCurrentResult();
                runOnUiThread(() -> mAdapterSensing.add(result.toString()));
                // Upload to the cloud
                //CrowdSensor.doProxyUpload(result);
                count++;
            }
            float currentBattery = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000f;
            mAdapterSensing.add("Power energy consumed in mA: " + (mStartBattery - currentBattery));
        }).start();
    }

    // Stop the sensing
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("SetTextI18n")
    private void stopRecord() {
        isGetSenseRun = false;
        mCrowdSensor.stopServices();

        if (mSwitchLog.isChecked()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            final EditText editName = new EditText(this);
            editName.setText(Build.MODEL + "_" + currentTimeMillis());
            dialog.setTitle("Enter file name: ");
            dialog.setView(editName);
            dialog.setPositiveButton("OK", (dialogInterface, i) -> {
                //Log.d(TAG, "Now is " + time);
                try {
                    String filename = editName.getText() + ".csv";
                    mFilesIOHelper.saveFile(filename, arrayToString(mSensingData));
                    if (mSwitchMail.isChecked()) {
                        Log.d(TAG, "File path is : " + mFilesIOHelper.getFileUri(filename));
                        mFilesIOHelper.sendFile(DST_MAIL_ADDRESS, getString(R.string.email_title), mFilesIOHelper.getFileUri(filename));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            dialog.setNegativeButton("Cancel", (dialogInterface, i) -> {
                //Pass
            });
            dialog.show();
        }
    }

    // Go to the context activity
    public void goContext(View view) {
        Intent goToContext = new Intent();
        goToContext.setClass(this, ContextActivity.class);
        startActivity(goToContext);
        finish();
    }

    // Go to the service activity
    public void goService(View view) {
        Intent goToService = new Intent();
        goToService.setClass(this, ServiceActivity.class);
        startActivity(goToService);
        finish();
    }

    // Convert string array to single string
    private String arrayToString(ArrayList<String> array) {
        StringBuilder content = new StringBuilder();
        for (String line : array) {
            content.append(line).append("\n");
        }
        return content.toString();
    }
}
