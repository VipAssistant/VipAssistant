package com.vipassistant.mobile.demo.ui.mapnavigation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapNavigationViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public MapNavigationViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Map Navigation fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}