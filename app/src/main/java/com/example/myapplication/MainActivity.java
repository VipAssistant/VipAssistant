package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final int REQUEST_ENABLE_BT = 1;

    private HashMap<String, BleDevice> bleDeviceHashMap;
    private ArrayList<BleDevice> bleDeviceArrayList;
    private BleDevicesListAdapter adapter;

    private Button btn_Scan;
    private BleScanner bleScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* check if device supports Bluetooth low energy or not
        * if not supported, finish the app immediately.
        * */
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utilities.toast(getApplicationContext(), "BLE not supported");
            finish();
        }

        bleScanner = new BleScanner(this, 7500, -75);

        bleDeviceHashMap = new HashMap<>();
        bleDeviceArrayList = new ArrayList<>();

        adapter = new BleDevicesListAdapter(this, R.layout.device_item, bleDeviceArrayList);

        ListView listView = new ListView(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        btn_Scan = findViewById(R.id.btn_scan);
        ((ScrollView) findViewById(R.id.scrollView)).addView(listView);
        findViewById(R.id.btn_scan).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() { super.onResume(); }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScan();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
            }
            else if (resultCode == RESULT_CANCELED) {
                Utilities.toast(getApplicationContext(), "Please turn on Bluetooth");
            }
        }
    }


    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_scan:
                Utilities.toast(getApplicationContext(), "Scan Button Pressed");

                if (!bleScanner.getScanningState()) {
                    startScan();
                }
                else {
                    stopScan();
                }

                break;
            default:
                break;
        }
    }

    public void addDevice(BluetoothDevice device, int rssi) {

        String address = device.getAddress();
        if (!bleDeviceHashMap.containsKey(address)) {
            BleDevice bleDevice = new BleDevice(device);
            bleDevice.setRssiValue(rssi);

            bleDeviceHashMap.put(address, bleDevice);
            bleDeviceArrayList.add(bleDevice);
        }
        else {
            bleDeviceHashMap.get(address).setRssiValue(rssi);
        }

        adapter.notifyDataSetChanged();
    }

    public void startScan(){
        btn_Scan.setText("Scanning");

        bleDeviceArrayList.clear();
        bleDeviceHashMap.clear();

        adapter.notifyDataSetChanged();

        bleScanner.start();
    }

    public void stopScan() {
        btn_Scan.setText("Scan");

        bleScanner.stop();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
