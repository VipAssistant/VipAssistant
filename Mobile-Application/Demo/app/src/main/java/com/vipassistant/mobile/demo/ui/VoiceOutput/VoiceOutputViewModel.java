package com.vipassistant.mobile.demo.ui.VoiceOutput;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class VoiceOutputViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public VoiceOutputViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Voice Output fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}