package com.vipassistant.mobile.demo.ui.constants;

import android.app.ProgressDialog;
import android.content.Context;
import com.eegeo.mapapi.geometry.LatLng;
import com.vipassistant.mobile.demo.ui.model.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Constants {
	/* Our Indoor Map's Location Container */
	public static final ArrayList<Location> allLocations = new ArrayList<>();

	/* MapNavigation Constants */
	public static final LatLng demoIndoorMapEntrance = new LatLng(39.891756, 32.783188);
	public static final String mapSceneLink = "https://wrld.mp/4bdda73";
	public static final String markerText = "You Are Here!";
	public static final String demoIndoorMapId = "EIM-71597625-a9b6-4753-b91f-1c0e74fc966d";
	public static final int mapRefreshMillis = 1000; /* TODO Refresh map per sec */
	public static final double locationLatEps = 0.000016;
	public static final double locationLongEps = 0.000024;

	/**
	 * Builds and returns a loading dialog with provided content
	 * @param activity
	 * @param loadingMessage
	 * @return
	 */
	public static ProgressDialog buildLoadingDialog(Context activity, String loadingMessage) {
		ProgressDialog loadingDialog = new ProgressDialog(activity);
		loadingDialog.setMessage(loadingMessage);
		loadingDialog.setIndeterminate(false);
		return loadingDialog;
	}
}
