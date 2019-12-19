package com.vipassistant.mobile.demo.ui.BLEScanner;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BLEScannerViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public BLEScannerViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is tools fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}