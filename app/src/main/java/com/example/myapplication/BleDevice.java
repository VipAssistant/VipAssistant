package com.example.myapplication;

import android.bluetooth.BluetoothDevice;

public class BleDevice {

    private BluetoothDevice bleDevice;
    private int rssiValue;

    /**
     * setter method of the bleDevice field
     * @param bleDevice
     */
    public BleDevice(BluetoothDevice bleDevice){
        this.bleDevice = bleDevice;
    }

    public String getMacAddress(){
        return bleDevice.getAddress();
    }

    public String getBleName(){
        return bleDevice.getName();
    }

    public int getRssiValue() {
        return rssiValue;
    }

    public void setRssiValue(int rssiValue) {
        this.rssiValue = rssiValue;
    }
}
