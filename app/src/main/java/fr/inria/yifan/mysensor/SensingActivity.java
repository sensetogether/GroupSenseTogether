package fr.inria.yifan.mysensor;

import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.ArrayList;

import fr.inria.yifan.mysensor.Support.ContextHelper;
import fr.inria.yifan.mysensor.Support.FilesIOHelper;
import fr.inria.yifan.mysensor.Support.SensorsHelper;

import static fr.inria.yifan.mysensor.Support.Configuration.ENABLE_REQUEST_LOCATION;
import static fr.inria.yifan.mysensor.Support.Configuration.SAMPLE_WINDOW_IN_MS;

/*
* This activity provides functions including showing sensor and logging sensing data.
*/

public class SensingActivity extends AppCompatActivity {

    private static final String TAG = "Sensing activity";

    // Thread locker and running flag
    private final Object mLock;
    private boolean isGetSenseRun;
    private int mSenseRound;

    // Declare all related views
    private TextView mWelcomeView;
    private Button mStartButton;
    private Button mStopButton;
    private Switch mSwitchLog;
    private ArrayAdapter<String> mAdapterSensing;

    // File helper and string data
    private FilesIOHelper mFilesIOHelper;
    private ArrayList<String> mSensingData;

    // Sensors helper for sensor and context
    private SensorsHelper mSensorHelper;
    private ContextHelper mContextHelper;

    // Constructor initializes locker
    public SensingActivity() {
        mLock = new Object();
    }

    // Initially bind all views
    private void bindViews() {
        mStartButton = findViewById(R.id.start_button);
        mStopButton = findViewById(R.id.stop_button);
        mStopButton.setVisibility(View.INVISIBLE);
        mSwitchLog = findViewById(R.id.switch_log);
        mWelcomeView = findViewById(R.id.welcome_view);
        mWelcomeView.setText(R.string.hint_sensing);

        // Build an adapter to feed the list with the content of an array of strings
        mSensingData = new ArrayList<>();
        mAdapterSensing = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mSensingData);

        // Attache the adapter to the list view
        ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(mAdapterSensing);

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAdapterSensing.clear();
                mAdapterSensing.add("Timestamp, daytime, light density (lx), magnetic strength (μT), " +
                        "GSM RSSI (dBm), GPS accuracy (m), GPS speed (m/s), proximity (bit), " +
                        "sound level (dB), temperature (C), pressure (hPa), humidity (%)");
                startRecord();
                mStartButton.setVisibility(View.INVISIBLE);
                mStopButton.setVisibility(View.VISIBLE);
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopRecord();
                mStartButton.setVisibility(View.VISIBLE);
                mStopButton.setVisibility(View.INVISIBLE);
            }
        });
    }

    // Main activity initialization
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensing);
        bindViews();
        mSensorHelper = new SensorsHelper(this);
        mFilesIOHelper = new FilesIOHelper(this);
        mContextHelper = new ContextHelper(this);
    }

    // Resume the sensing service
    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorHelper != null) {
            mSensorHelper.startService();
        }
        if (mContextHelper != null) {
            mContextHelper.startService();
        }
    }

    // Stop thread when exit!
    @Override
    protected void onPause() {
        isGetSenseRun = false;
        super.onPause();
        if (mSensorHelper != null) {
            mSensorHelper.stopService();
        }
        if (mContextHelper != null) {
            mContextHelper.stopService();
        }
    }

    // Start the sensing thread
    private void startRecord() {
        if (isGetSenseRun) {
            Log.e(TAG, "Still in sensing and recording");
            return;
        }
        mSensorHelper.startService();
        isGetSenseRun = true;
        mSenseRound = 0;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isGetSenseRun) {
                    // Sampling time delay
                    synchronized (mLock) {
                        try {
                            mLock.wait(SAMPLE_WINDOW_IN_MS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapterSensing.add(System.currentTimeMillis() + ", " +
                                    mContextHelper.isDaytime() + ", " +
                                    mSensorHelper.getLightDensity() + ", " +
                                    mSensorHelper.getMagnet() + ", " +
                                    mContextHelper.getRssiDbm() + ", " +
                                    mContextHelper.getGPSAccuracy() + ", " +
                                    mContextHelper.getGPSSpeed() + ", " +
                                    mSensorHelper.getProximity() + ", " +
                                    mSensorHelper.getSoundLevel() + ", " +
                                    mSensorHelper.getTemperature() + ", " +
                                    mSensorHelper.getPressure() + ", " +
                                    mSensorHelper.getHumidity());
                            mSenseRound += 1;
                            mWelcomeView.setText(String.valueOf(mSenseRound));
                            //Log.d(TAG, String.valueOf(mSenseRound));
                        }
                    });
                }
            }
        }).start();
    }

    // Stop the sound sensing
    private void stopRecord() {
        mSensorHelper.stopService();
        isGetSenseRun = false;
        if (mSwitchLog.isChecked()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            final EditText editName = new EditText(this);
            editName.setText(String.valueOf(System.currentTimeMillis()));
            dialog.setTitle("Enter file name: ");
            dialog.setView(editName);
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    StringBuilder content = new StringBuilder();
                    for (String line : mSensingData) {
                        content.append(line).append("\n");
                    }
                    //Log.d(TAG, "Now is " + time);
                    try {
                        mFilesIOHelper.saveFile(String.valueOf(editName.getText()), content.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            dialog.show();
        }
    }

    // Go to the detection activity
    public void goDetection(View view) {
        Intent goToDetection = new Intent();
        goToDetection.setClass(this, DetectionActivity.class);
        startActivity(goToDetection);
        finish();
    }

    // Go to the network activity
    public void goNetwork(View view) {
        Intent goToNetwork = new Intent();
        goToNetwork.setClass(this, NetworkActivity.class);
        startActivity(goToNetwork);
        finish();
    }

    // Callback for user enabling GPS switch
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ENABLE_REQUEST_LOCATION: {
                mContextHelper.startService();
            }
        }
    }

}
