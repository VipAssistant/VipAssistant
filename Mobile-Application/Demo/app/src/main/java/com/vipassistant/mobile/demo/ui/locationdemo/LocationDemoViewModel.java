package com.vipassistant.mobile.demo.ui.locationdemo;

import androidx.lifecycle.ViewModel;

public class LocationDemoViewModel extends ViewModel {

    private Boolean cachingActivated = false, cachingToastActivated = false;

    public LocationDemoViewModel() {
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