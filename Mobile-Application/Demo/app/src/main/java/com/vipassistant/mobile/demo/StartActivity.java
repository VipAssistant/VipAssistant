package com.vipassistant.mobile.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import gr.net.maroulis.library.EasySplashScreen;

import java.io.IOException;

import static com.vipassistant.mobile.demo.ui.utils.Utils.readAndLoadWorldCitiesData;
import static com.vipassistant.mobile.demo.ui.utils.Utils.readSavedLocations;


public class StartActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EasySplashScreen config = new EasySplashScreen(StartActivity.this)
				.withFullScreen()
				.withTargetActivity(MainActivity.class)
				.withSplashTimeOut(3000)
				.withBackgroundColor(Color.parseColor("#192028"))
				.withHeaderText("")
				.withFooterText("VipAssistant© Version 1.0.0")
				.withBeforeLogoText("Welcome to")
				.withAfterLogoText("Your Eyes in Buildings")
				.withLogo(R.drawable.ic_logo1_transparent);
		config.getHeaderTextView().setTextColor(Color.WHITE);
		config.getFooterTextView().setTextColor(Color.WHITE);
		config.getFooterTextView().setPadding(0,0,0,20);
		config.getBeforeLogoTextView().setTextColor(Color.WHITE);
		config.getBeforeLogoTextView().setPadding(0,0,0,50);
		config.getAfterLogoTextView().setTextColor(Color.WHITE);
		config.getAfterLogoTextView().setPadding(0,60,0,0);
		View easySplashScreen = config.create();
		setContentView(easySplashScreen);

		/* Initialize Outdoor Locations once and for all and read saved locations if there is any */
		try {
			readAndLoadWorldCitiesData(this);
			readSavedLocations(this);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
