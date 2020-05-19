package com.vipassistant.mobile.demo.ui.mapnavigation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MapNavigationViewModel extends ViewModel {

    private Boolean cachingActivated = true, cachingToastActivated = false;

    public MapNavigationViewModel() {
    }

    public Boolean getCachingActivated() {
        return cachingActivated;
    }

    public void setCachingActivated(Boolean cachingActivated) {
        this.cachingActivated = cachingActivated;
    }

    public Boolean getCachingToastActivated() {
        return cachingToastActivated;
    }

    public void setCachingToastActivated(Boolean cachingToastActivated) {
        this.cachingToastActivated = cachingToastActivated;
    }

}