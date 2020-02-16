package com.vipassistant.mobile.demo.ui.Integration.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class IntegrationViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public IntegrationViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("Welcome to the Vip Assistant Demo Application");
    }

    public LiveData<String> getText() {
        return mText;
    }
}