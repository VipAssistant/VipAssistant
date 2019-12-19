package com.vipassistant.mobile.demo.ui.VoiceInput;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class VoiceInputViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public VoiceInputViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}