package com.vipassistant.mobile.demo.ui.constants;

import android.app.ProgressDialog;
import android.content.Context;
import com.eegeo.mapapi.geometry.LatLng;
import com.vipassistant.mobile.demo.ui.model.Location;

import java.util.ArrayList;

public class Constants {
	/* MapNavigation Constants */
	public static final String demoIndoorMapId = "EIM-71597625-a9b6-4753-b91f-1c0e74fc966d";
	public static final Location demoIndoorMapEntrance = new Location("Entrance", "entrance", new LatLng(39.891756, 32.783188), .0, .0, 1, demoIndoorMapId);
	public static final String mapSceneLink = "https://wrld.mp/4bdda73";
	public static final String markerText = "You Are Here!";
	public static final int mapRefreshMillis = 1000; /* TODO Refresh map per sec */
	public static final double locationLatEps = 0.000016;
	public static final double locationLongEps = 0.000024;

	/* Our Indoor Map's Location Container */
	public static final ArrayList<Location> allLocations = new ArrayList<>();

	static {
		allLocations
				.add(new Location("1st Floor WC", "wc", new LatLng(39.891883, 32.783255), .000030, .000009, 2, demoIndoorMapId));
	}

	/**
	 * Builds and returns a loading dialog with provided content
	 *
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

	public static Double calculateEuclideanDistance(Location op1, Location op2) {
		Double first = Math.pow(op1.getLocation().latitude - op2.getLocation().latitude, 2);
		Double second = Math.pow(op1.getLocation().longitude - op2.getLocation().longitude, 2);
		return Math.sqrt(first + second);
	}
}
