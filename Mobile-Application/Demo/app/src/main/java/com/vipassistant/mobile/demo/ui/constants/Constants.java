package com.vipassistant.mobile.demo.ui.constants;

import android.app.ProgressDialog;
import android.content.Context;
import com.eegeo.mapapi.geometry.LatLng;
import com.vipassistant.mobile.demo.ui.model.Location;

import java.util.ArrayList;

public class Constants {
	/* MapNavigation Constants */
	public static final String demoIndoorMapId = "EIM-71597625-a9b6-4753-b91f-1c0e74fc966d";
	public static final String demoBuildingName = "METU-CENG Block A";
	public static final Location demoIndoorMapEntrance = new Location("Entrance", "Building Entrance", new LatLng(39.891753, 32.783191), .000030, .000007, 1, demoIndoorMapId);
	public static final String mapSceneLink = "https://wrld.mp/4bdda73";
	public static final String markerText = "You Are Here!";
	public static final int mapRefreshMillis = 1000; /* TODO Refresh map per sec */
	public static final double locationLatEps = 0.000016;
	public static final double locationLongEps = 0.000024;

	/* Our Indoor Map's Location Container */
	public static final ArrayList<Location> allLocations = new ArrayList<Location>() {{
		/* Basement */
		add(new Location("Server Room", "Room", new LatLng(39.892068, 32.783152), .000014, .000058, 0, demoIndoorMapId));
		add(new Location("Digital Lab", "Lab", new LatLng(39.891960, 32.783153), .000088, .000038, 0, demoIndoorMapId));
		add(new Location("Stationary", "Stationary", new LatLng(39.891855, 32.783169), .000018, .000035, 0, demoIndoorMapId));
		add(new Location("Basement Back Upstairs", "Stairs", new LatLng(39.892025, 32.783237), .000005, .000023, 0, demoIndoorMapId));
		add(new Location("Basement Hallway", "Hallway", new LatLng(39.891941, 32.783212), .000110, .000011, 0, demoIndoorMapId));
		add(new Location("Z-103", "Room", new LatLng(39.891951, 32.783267), .000019, .000040, 0, demoIndoorMapId));
		add(new Location("Z-102", "Room", new LatLng(39.891911, 32.783274), .000017, .000040, 0, demoIndoorMapId));
		add(new Location("Basement Disabled Restroom/Wc", "Disabled Restroom/Wc", new LatLng(39.891868, 32.783253), .000016, .000017, 0, demoIndoorMapId));
		add(new Location("Basement Woman Restroom/Wc", "Woman Restroom/Wc", new LatLng(39.891868, 32.783292), .000016, .000006, 0, demoIndoorMapId));
		add(new Location("Basement Man Restroom/Wc", "Man Restroom/Wc", new LatLng(39.891876, 32.783309), .000019, .000006, 0, demoIndoorMapId));
		add(new Location("Basement Elevator", "Elevator", new LatLng(39.891856, 32.783269), .000013, .000012, 0, demoIndoorMapId));
		add(new Location("Basement Hall", "Hall", new LatLng(39.891818, 32.783219), .000047, .000090, 0, demoIndoorMapId));
		add(new Location("Study Room", "Room", new LatLng(39.891780, 32.783290), .000033, .000038, 0, demoIndoorMapId));
		add(new Location("Vending Machine", "Vending Machine/Automat", new LatLng(39.891797, 32.783194), .000003, .000003, 0, demoIndoorMapId));
		add(new Location("Basement Front Upstairs", "Stairs", new LatLng(39.891780, 32.783169), .000008, .000015, 0, demoIndoorMapId));

		/* Entrance */
		add(demoIndoorMapEntrance);
		add(new Location("Entrance Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 1, demoIndoorMapId));
		add(new Location("Entrance Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 1, demoIndoorMapId));
		add(new Location("Entrance Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 1, demoIndoorMapId));
		add(new Location("Entrance Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 1, demoIndoorMapId));

		/* 1st Floor */
		add(new Location("Room A-105", "Room", new LatLng(39.892068, 32.783152), .000014, .000058, 2, demoIndoorMapId));
		add(new Location("Room A-106", "Room", new LatLng(39.892027, 32.783144), .000015, .000038, 2, demoIndoorMapId));
		add(new Location("Room A-107", "Room", new LatLng(39.891984, 32.783150), .000015, .000038, 2, demoIndoorMapId));
		add(new Location("Room A-108", "Room", new LatLng(39.891902, 32.783159), .000060, .000038, 2, demoIndoorMapId));
		add(new Location("First Floor Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 2, demoIndoorMapId));
		add(new Location("First Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 2, demoIndoorMapId));
		add(new Location("Room A-104", "Room", new LatLng(39.891997, 32.783264), .000018, .000040, 2, demoIndoorMapId));
		add(new Location("Room A-103", "Room", new LatLng(39.891949, 32.783269), .000018, .000040, 2, demoIndoorMapId));
		add(new Location("Room A-102", "Room", new LatLng(39.891909, 32.783274), .000014, .000040, 2, demoIndoorMapId));
		add(new Location("First Floor Woman Restroom/Wc", "Woman Restroom/Wc", new LatLng(39.891868, 32.783253), .000016, .000017, 2, demoIndoorMapId));
		add(new Location("Tea Room", "Room", new LatLng(39.891875, 32.783303), .000025, .000010, 2, demoIndoorMapId));
		add(new Location("First Floor Elevator", "Elevator", new LatLng(39.891856, 32.783269), .000013, .000012, 2, demoIndoorMapId));
		add(new Location("First Floor Hallway", "Hallway", new LatLng(39.891941, 32.783212), .000110, .000011, 2, demoIndoorMapId));
		add(new Location("First Floor Hall", "Hall", new LatLng(39.891818, 32.783219), .000047, .000090, 2, demoIndoorMapId));
		add(new Location("First Floor Crossover to B Block", "Crossover", new LatLng(39.891843, 32.783413), .000015, .000077, 2, demoIndoorMapId));
		add(new Location("First Floor B Block Entrance", "Block Entrance", new LatLng(39.891823, 32.783558), .000053, .000062, 2, demoIndoorMapId));
		add(new Location("Room A-101", "Room", new LatLng(39.891780, 32.783290), .000033, .000038, 2, demoIndoorMapId));
		add(new Location("First Floor Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 2, demoIndoorMapId));
		add(new Location("First Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 2, demoIndoorMapId));

		// TODO finalize remanining location data
	}};

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
