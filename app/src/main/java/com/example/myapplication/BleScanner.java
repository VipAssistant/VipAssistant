package com.example.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import android.os.Handler;
import android.bluetooth.le.*;

public class BleScanner {

    private MainActivity mainActivity;
    private BluetoothAdapter bluetoothAdapter;
    private boolean isScanning;
    private Handler handler;
    private long scanPeriod;
    private int minSignalStrength;

    public BleScanner(MainActivity mainActivity, long scanPeriod, int minSignalStrength){
        this.mainActivity = mainActivity;
        this.scanPeriod = scanPeriod;
        this.minSignalStrength = minSignalStrength;
        handler = new Handler();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) mainActivity.getSystemService(Context.BLUETOOTH_SERVICE);

        bluetoothAdapter =bluetoothManager.getAdapter();
    }

    public boolean getScanningState() {
        return this.isScanning;
    }

    public void start(){
        /* if bluetooth is not open, request from user to open bluetooth */
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mainActivity.startActivityForResult(enableBtIntent, MainActivity.REQUEST_ENABLE_BT);
        }

        else{
            scanLeDevice(true);
        }
    }

    public void stop(){
        scanLeDevice(false);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable && !isScanning) {
            Utilities.toast(mainActivity.getApplicationContext(), "Scanning");

            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utilities.toast(mainActivity.getApplicationContext(), "Finished");

                    isScanning = false;
                    BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                    bluetoothLeScanner.stopScan(bleScanCallback);

                    mainActivity.stopScan();
                }
            }, scanPeriod);

            isScanning = true;
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.startScan(bleScanCallback);
            /* insteead of the above, bluetoothAdapter.startLeScan(uuids, mLeScanCallback) can be used
            * if the beacon list and their uuids are known before
            * */
        }
        else {
            isScanning = false;
            BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            bluetoothLeScanner.stopScan(bleScanCallback);
        }
    }

    private ScanCallback bleScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);

                    if(result.getRssi() > minSignalStrength){
                        mainActivity.addDevice(result.getDevice(), result.getRssi());
                    }
                }
            };
}
