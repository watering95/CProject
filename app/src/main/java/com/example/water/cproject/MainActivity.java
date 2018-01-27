package com.example.water.cproject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBLEScanner;

    private Machine mMachine;

    private boolean mScanning;
    private boolean mfindGenuino;
    private TextView mDeviceName;
    private TextView mDeviceAddress;
    private TextView mDeviceStatus;
    private TextView mMotorSpeed;
    private TextView mMotorState;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    private String mServiceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMachine = new Machine(this);
        mHandler = new Handler();
        mDeviceName = findViewById(R.id.deviceName);
        mDeviceAddress  = findViewById(R.id.deviceAddress);
        mDeviceStatus = findViewById(R.id.deviceStatus);
        mMotorSpeed = findViewById(R.id.motorSpeed);
        mMotorState = findViewById(R.id.motorState);

        mMotorSpeed.setText("0");
        mMotorState.setText("Stop");

        SeekBar sbSpeed = findViewById(R.id.seekBarSpeed);
        SeekBar sbSpeedLeft = findViewById(R.id.seekBarLeftSpeed);
        SeekBar sbSpeedRight = findViewById(R.id.seekBarRightSpeed);

        sbSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mMachine.setRunSpeed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress;
                progress = mMachine.getRunSpeed();
                mMotorSpeed.setText(String.valueOf(progress));
            }
        });
        sbSpeedLeft.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mMachine.setLeftSpeed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress;
            }
        });
        sbSpeedRight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mMachine.setRightSpeed(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress;
            }
        });

        //BLE Permission
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionResult = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);

            if(permissionResult != PackageManager.PERMISSION_GRANTED) {
                if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("Need Permission").setMessage("This Function need Permission \"COARSE LOCATION\". Continue?")
                            .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
                                    }
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "Cancel Function", Toast.LENGTH_SHORT).show();
                        }
                    }).create().show();
                } else {
                    requestPermissions(new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 1000);
                }
            } else {
            }
        } else {
        }

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // Checks if Bluetooth LE Scanner is available.
        if (mBLEScanner == null) {
            Toast.makeText(this, R.string.ble_scanner_not_find, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1000:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMachine.bindService();
    }

    public void onClick(View v) {
        if(!mMachine.mGenuino.getBLE().getConnectState()) return;
        switch(v.getId()) {
            case R.id.buttonRun:
                mMachine.action(mMachine.MACHINE_FORWARD);
                mMotorState.setText("Run");
                break;
            case R.id.buttonStop:
                mMachine.action(mMachine.MACHINE_STOP);
                mMotorState.setText("Stop");
                break;
            case R.id.buttonRight:
                mMachine.action(mMachine.MACHINE_RIGHT);
                mMotorState.setText("Right");
                break;
            case R.id.buttonLeft:
                mMachine.action(mMachine.MACHINE_LEFT);
                mMotorState.setText("Left");
                break;
            case R.id.buttonBack:
                mMachine.action(mMachine.MACHINE_BACKWARD);
                mMotorState.setText("Back");
                break;
            default:
                mMachine.action(mMachine.MACHINE_STOP);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mMachine.mGenuino.getBLE().getConnectState()) {
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            if(mfindGenuino) {
                menu.findItem(R.id.menu_scan).setVisible(false);
                menu.findItem(R.id.menu_connect).setVisible(true);
                menu.findItem(R.id.menu_disconnect).setVisible(false);
                menu.findItem(R.id.menu_stop).setVisible(true);
                menu.findItem(R.id.menu_refresh).setActionView(null);
            } else {
                if (mScanning) {
                    menu.findItem(R.id.menu_scan).setVisible(false);
                    menu.findItem(R.id.menu_connect).setVisible(false);
                    menu.findItem(R.id.menu_disconnect).setVisible(false);
                    menu.findItem(R.id.menu_stop).setVisible(true);
                    menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    menu.findItem(R.id.menu_scan).setVisible(true);
                    menu.findItem(R.id.menu_connect).setVisible(false);
                    menu.findItem(R.id.menu_disconnect).setVisible(false);
                    menu.findItem(R.id.menu_stop).setVisible(false);
                    menu.findItem(R.id.menu_refresh).setActionView(null);
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                scanBLEDevice(true);
                break;
            case R.id.menu_stop:
                mMachine.commDisconnect();
                scanBLEDevice(false);
                break;
            case R.id.menu_connect:
                mMachine.commConnect(mServiceAddress);
                break;
            case R.id.menu_disconnect:
                mMachine.commDisconnect();
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMachine.registerGattReceiver(mGattUpdateReceiver);
        mMachine.commConnect(mServiceAddress);

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBluetoothAdapter.isEnabled()) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        scanBLEDevice(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanBLEDevice(false);
    }

    private void scanBLEDevice(final boolean enable) {

        ScanSettings settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
        List<ScanFilter> filters = new ArrayList<>();
        String DEFAULT_BLE_ADDRESS = "98:4F:EE:10:7F:E5";
        ScanFilter filter = new ScanFilter.Builder().setDeviceAddress(DEFAULT_BLE_ADDRESS).build();
        filters.add(filter);

        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBLEScanner.stopScan(mScanCallback);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBLEScanner.startScan(filters, settings, mScanCallback);
            updateConnectionState(R.string.ble_scanning);
            invalidateOptionsMenu();
        } else {
            mScanning = false;
            mfindGenuino = false;
            mBLEScanner.stopScan(mScanCallback);
            updateConnectionState(R.string.ble_stopscan);
            mDeviceName.setText("No Device");
            mDeviceAddress.setText("No Device");
            mServiceAddress = null;
            invalidateOptionsMenu();
        }
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Toast.makeText(MainActivity.this, String.valueOf(errorCode), Toast.LENGTH_SHORT).show();
            invalidateOptionsMenu();
        }

        private void processResult(final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!mfindGenuino) {
                        mMachine.mGenuino.getBLE().setName(result.getDevice().getName());
                        mDeviceName.setText(mMachine.mGenuino.getBLE().getName());
                        mMachine.mGenuino.getBLE().setAddress(result.getDevice().getAddress());
                        mServiceAddress = mMachine.mGenuino.getBLE().getAddress();
                        mDeviceAddress.setText(mServiceAddress);
                        updateConnectionState(R.string.ble_scan_finish);
                        mfindGenuino = true;
                        invalidateOptionsMenu();
                    }
                }
            });
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                mMachine.mGenuino.getBLE().setConnect();
                updateConnectionState(R.string.ble_connected);
                invalidateOptionsMenu();
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mMachine.mGenuino.getBLE().setDisconnect();
                updateConnectionState(R.string.ble_disconnected);
                invalidateOptionsMenu();
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Select Genuino services and characteristics on the user interface.
                mMachine.selectMachineGattServices();
            } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {

            }
        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceStatus.setText(resourceId);
            }
        });
    }
}
