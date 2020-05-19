package com.vipassistant.mobile.demo.ui.constants;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.*;
import androidx.core.content.ContextCompat;
import com.eegeo.mapapi.geometry.LatLng;
import com.eegeo.mapapi.services.routing.RouteDirections;
import com.vipassistant.mobile.demo.R;
import com.vipassistant.mobile.demo.ui.model.Location;
import com.vipassistant.mobile.demo.ui.model.StepInfo;
import com.vipassistant.mobile.demo.ui.utils.AutoCompleteArrayAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Constants {

	/* MapNavigation Constants */
	public static final String demoIndoorMapId = "EIM-71597625-a9b6-4753-b91f-1c0e74fc966d";
	public static final String demoBuildingName = "METU-CENG Block A";
	public static final Location demoIndoorMapEntrance = new Location("Entrance", "Building Entrance", new LatLng(39.891753, 32.783191), .000030, .000007, 1, demoIndoorMapId);
	public static final String mapSceneLink = "https://wrld.mp/4bdda73";
	public static final String markerText = "You Are Here!";
	public static final int mapRefreshMillis = 1000; /* TODO Refresh map per sec */
	public static final double locationLatEps = 0.0001;
	public static final double locationLongEps = 0.0001;
	public static double cameraZoom = 20;
	public static double cameraTilt = 35;
	public static final double PERSON_WALKING_SPEED = 1.4;

	/* Our Indoor Map's Location Container */
	public static ArrayList<Location> allLocations = new ArrayList<Location>() {{
		/* Basement */
		add(new Location("Server Room", "Server Room", new LatLng(39.892068, 32.783152), .000014, .000058, 0, demoIndoorMapId));
		add(new Location("Digital Lab", "Lab", new LatLng(39.891962, 32.783152), .000088, .000038, 0, demoIndoorMapId));
		add(new Location("Stationary", "Stationary", new LatLng(39.891855, 32.783169), .000018, .000035, 0, demoIndoorMapId));
		add(new Location("Basement Back Upstairs", "Stairs", new LatLng(39.892025, 32.783237), .000005, .000023, 0, demoIndoorMapId));
		add(new Location("Basement Hallway", "Hallway", new LatLng(39.891941, 32.783212), .000110, .000011, 0, demoIndoorMapId));
		add(new Location("Z-103", "Staff Room", new LatLng(39.891951, 32.783267), .000019, .000040, 0, demoIndoorMapId));
		add(new Location("Z-102", "Staff Room", new LatLng(39.891911, 32.783274), .000017, .000040, 0, demoIndoorMapId));
		add(new Location("Basement Disabled Restroom/Wc", "Disabled Restroom/Wc", new LatLng(39.891868, 32.783253), .000016, .000017, 0, demoIndoorMapId));
		add(new Location("Basement Woman Restroom/Wc", "Woman Restroom/Wc", new LatLng(39.891868, 32.783292), .000016, .000006, 0, demoIndoorMapId));
		add(new Location("Basement Man Restroom/Wc", "Man Restroom/Wc", new LatLng(39.891876, 32.783309), .000019, .000006, 0, demoIndoorMapId));
		add(new Location("Basement Elevator", "Elevator", new LatLng(39.891856, 32.783269), .000013, .000012, 0, demoIndoorMapId));
		add(new Location("Basement Hall", "Hall", new LatLng(39.891818, 32.783219), .000047, .000090, 0, demoIndoorMapId));
		add(new Location("Study Room", "Study Room", new LatLng(39.891780, 32.783290), .000033, .000038, 0, demoIndoorMapId));
		add(new Location("Vending Machine", "Vending Machine/Automat", new LatLng(39.891797, 32.783194), .000003, .000003, 0, demoIndoorMapId));
		add(new Location("Basement Front Upstairs", "Stairs", new LatLng(39.891780, 32.783169), .000008, .000015, 0, demoIndoorMapId));

		/* Entrance */
		add(demoIndoorMapEntrance);
		add(new Location("Entrance Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 1, demoIndoorMapId));
		add(new Location("Entrance Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 1, demoIndoorMapId));
		add(new Location("Entrance Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 1, demoIndoorMapId));
		add(new Location("Entrance Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 1, demoIndoorMapId));

		/* 1st Floor */
		add(new Location("Room A-105", "Staff Room", new LatLng(39.892068, 32.783152), .000014, .000058, 2, demoIndoorMapId));
		add(new Location("Room A-106", "Staff Room", new LatLng(39.892027, 32.783144), .000015, .000038, 2, demoIndoorMapId));
		add(new Location("Room A-107", "Staff Room", new LatLng(39.891984, 32.783150), .000015, .000038, 2, demoIndoorMapId));
		add(new Location("Room A-108", "Staff Room", new LatLng(39.891902, 32.783159), .000060, .000038, 2, demoIndoorMapId));
		add(new Location("First Floor Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 2, demoIndoorMapId));
		add(new Location("First Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 2, demoIndoorMapId));
		add(new Location("Room A-104", "Staff Room", new LatLng(39.891997, 32.783264), .000018, .000040, 2, demoIndoorMapId));
		add(new Location("Room A-103", "Staff Room", new LatLng(39.891949, 32.783269), .000018, .000040, 2, demoIndoorMapId));
		add(new Location("Room A-102", "Staff Room", new LatLng(39.891909, 32.783274), .000014, .000040, 2, demoIndoorMapId));
		add(new Location("First Floor Woman Restroom/Wc", "Woman Restroom/Wc", new LatLng(39.891868, 32.783253), .000016, .000017, 2, demoIndoorMapId));
		add(new Location("First Floor Tea Room", "Tea Room", new LatLng(39.891875, 32.783303), .000025, .000010, 2, demoIndoorMapId));
		add(new Location("First Floor Elevator", "Elevator", new LatLng(39.891856, 32.783269), .000013, .000012, 2, demoIndoorMapId));
		add(new Location("First Floor Hallway", "Hallway", new LatLng(39.891941, 32.783212), .000110, .000011, 2, demoIndoorMapId));
		add(new Location("First Floor Hall", "Hall", new LatLng(39.891818, 32.783219), .000047, .000090, 2, demoIndoorMapId));
		add(new Location("First Floor Crossover to B Block", "Crossover", new LatLng(39.891843, 32.783413), .000015, .000077, 2, demoIndoorMapId));
		add(new Location("First Floor B Block Entrance", "B Block Entrance", new LatLng(39.891823, 32.783558), .000053, .000062, 2, demoIndoorMapId));
		add(new Location("Room A-101", "Classroom", new LatLng(39.891780, 32.783290), .000033, .000038, 2, demoIndoorMapId));
		add(new Location("First Floor Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 2, demoIndoorMapId));
		add(new Location("First Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 2, demoIndoorMapId));

		/* 1.5 Floor */
		add(new Location("First Half Floor Hall", "Hall", new LatLng(39.891753, 32.783191), .000030, .000007, 3, demoIndoorMapId));
		add(new Location("BMB-4", "Classroom", new LatLng(39.891681, 32.783200), .000050, .000081, 3, demoIndoorMapId));
		add(new Location("First Half Floor Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 3, demoIndoorMapId));
		add(new Location("First Half Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 3, demoIndoorMapId));
		add(new Location("First Half Floor Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 3, demoIndoorMapId));
		add(new Location("First Half Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 3, demoIndoorMapId));

		/* 2nd Floor */
		add(new Location("Room A-205", "Staff Room", new LatLng(39.892068, 32.783152), .000014, .000058, 4, demoIndoorMapId));
		add(new Location("Room A-206", "Staff Room", new LatLng(39.892027, 32.783144), .000015, .000038, 4, demoIndoorMapId));
		add(new Location("Room A-207", "Staff Room", new LatLng(39.891984, 32.783150), .000015, .000038, 4, demoIndoorMapId));
		add(new Location("Room A-208", "Staff Room", new LatLng(39.891902, 32.783159), .000060, .000038, 4, demoIndoorMapId));
		add(new Location("Second Floor Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 4, demoIndoorMapId));
		add(new Location("Second Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 4, demoIndoorMapId));
		add(new Location("Room A-204", "Staff Room", new LatLng(39.891997, 32.783264), .000018, .000040, 4, demoIndoorMapId));
		add(new Location("Room A-203", "Staff Room", new LatLng(39.891949, 32.783269), .000018, .000040, 4, demoIndoorMapId));
		add(new Location("Room A-202", "Staff Room", new LatLng(39.891909, 32.783274), .000014, .000040, 4, demoIndoorMapId));
		add(new Location("Second Floor Man Restroom/Wc", "Man Restroom/Wc", new LatLng(39.891868, 32.783253), .000016, .000017, 4, demoIndoorMapId));
		add(new Location("Second Floor Tea Room", "Tea Room", new LatLng(39.891875, 32.783303), .000025, .000010, 4, demoIndoorMapId));
		add(new Location("Second Floor Elevator", "Elevator", new LatLng(39.891856, 32.783269), .000013, .000012, 4, demoIndoorMapId));
		add(new Location("Second Floor Hallway", "Hallway", new LatLng(39.891941, 32.783212), .000110, .000011, 4, demoIndoorMapId));
		add(new Location("Second Floor Hall", "Hall", new LatLng(39.891818, 32.783219), .000047, .000090, 4, demoIndoorMapId));
		add(new Location("Second Floor Crossover to B Block", "Crossover", new LatLng(39.891843, 32.783413), .000015, .000077, 4, demoIndoorMapId));
		add(new Location("Second Floor B Block Entrance", "B Block Entrance", new LatLng(39.891823, 32.783558), .000053, .000062, 4, demoIndoorMapId));
		add(new Location("Second Floor Resting Room", "Resting Room", new LatLng(39.891780, 32.783290), .000033, .000038, 4, demoIndoorMapId));
		add(new Location("Second Floor Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 4, demoIndoorMapId));
		add(new Location("Second Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 4, demoIndoorMapId));

		/* 2.5 Floor */
		add(new Location("Second Half Floor Hall", "Hall", new LatLng(39.891753, 32.783191), .000030, .000007, 5, demoIndoorMapId));
		add(new Location("Second Half Floor Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 5, demoIndoorMapId));
		add(new Location("Second Half Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 5, demoIndoorMapId));
		add(new Location("Second Half Floor Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 5, demoIndoorMapId));
		add(new Location("Second Half Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 5, demoIndoorMapId));

		/* 3rd Floor */
		add(new Location("BMB-5", "Classroom", new LatLng(39.891681, 32.783200), .000050, .000081, 6, demoIndoorMapId));
		add(new Location("Room A-305", "Staff Room", new LatLng(39.892068, 32.783152), .000014, .000058, 6, demoIndoorMapId));
		add(new Location("Room A-306", "Staff Room", new LatLng(39.892027, 32.783144), .000015, .000038, 6, demoIndoorMapId));
		add(new Location("Room A-307", "Staff Room", new LatLng(39.891984, 32.783150), .000015, .000038, 6, demoIndoorMapId));
		add(new Location("Room A-308", "Staff Room", new LatLng(39.891902, 32.783159), .000060, .000038, 6, demoIndoorMapId));
		add(new Location("Third Floor Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 6, demoIndoorMapId));
		add(new Location("Third Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 6, demoIndoorMapId));
		add(new Location("Room A-304", "Staff Room", new LatLng(39.891997, 32.783264), .000018, .000040, 6, demoIndoorMapId));
		add(new Location("Room A-303", "Staff Room", new LatLng(39.891949, 32.783269), .000018, .000040, 6, demoIndoorMapId));
		add(new Location("Room A-302", "Staff Room", new LatLng(39.891909, 32.783274), .000014, .000040, 6, demoIndoorMapId));
		add(new Location("Third Floor Man Restroom/Wc", "Man Restroom/Wc", new LatLng(39.891868, 32.783253), .000016, .000017, 6, demoIndoorMapId));
		add(new Location("Third Floor Tea Room", "Tea Room", new LatLng(39.891875, 32.783303), .000025, .000010, 6, demoIndoorMapId));
		add(new Location("Third Floor Elevator", "Elevator", new LatLng(39.891856, 32.783269), .000013, .000012, 6, demoIndoorMapId));
		add(new Location("Third Floor Hallway", "Hallway", new LatLng(39.891941, 32.783212), .000110, .000011, 6, demoIndoorMapId));
		add(new Location("Third Floor Hall", "Hall", new LatLng(39.891818, 32.783219), .000047, .000090, 6, demoIndoorMapId));
		add(new Location("Third Floor Crossover to B Block", "Crossover", new LatLng(39.891843, 32.783413), .000015, .000077, 6, demoIndoorMapId));
		add(new Location("Third Floor B Block Entrance", "B Block Entrance", new LatLng(39.891823, 32.783558), .000053, .000062, 6, demoIndoorMapId));
		add(new Location("Room A-301", "Staff Room", new LatLng(39.891780, 32.783290), .000033, .000038, 6, demoIndoorMapId));
		add(new Location("Third Floor Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 6, demoIndoorMapId));
		add(new Location("Third Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 6, demoIndoorMapId));

		/* 3.5 Floor */
		add(new Location("Third Half Floor Hall", "Hall", new LatLng(39.891753, 32.783191), .000030, .000007, 7, demoIndoorMapId));
		add(new Location("Third Half Floor Front Upstairs", "Stairs", new LatLng(39.891784, 32.783203), .000008, .000015, 7, demoIndoorMapId));
		add(new Location("Third Half Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 7, demoIndoorMapId));
		add(new Location("Third Half Floor Back Upstairs", "Stairs", new LatLng(39.892024, 32.783238), .000005, .000023, 7, demoIndoorMapId));
		add(new Location("Third Half Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 7, demoIndoorMapId));

		/* 4th Floor */
		add(new Location("Room A-405", "Staff Room", new LatLng(39.892068, 32.783152), .000014, .000058, 8, demoIndoorMapId));
		add(new Location("Room A-406", "Staff Room", new LatLng(39.892027, 32.783144), .000015, .000038, 8, demoIndoorMapId));
		add(new Location("Room A-407", "Staff Room", new LatLng(39.891984, 32.783150), .000015, .000038, 8, demoIndoorMapId));
		add(new Location("Room A-408", "Staff Room", new LatLng(39.891902, 32.783159), .000060, .000038, 8, demoIndoorMapId));
		add(new Location("Fourth Floor Back Downstairs", "Stairs", new LatLng(39.892041, 32.783237), .000008, .000015, 8, demoIndoorMapId));
		add(new Location("Room A-404", "Staff Room", new LatLng(39.891997, 32.783264), .000018, .000040, 8, demoIndoorMapId));
		add(new Location("Room A-403", "Staff Room", new LatLng(39.891949, 32.783269), .000018, .000040, 8, demoIndoorMapId));
		add(new Location("Room A-402", "Staff Room", new LatLng(39.891909, 32.783274), .000014, .000040, 8, demoIndoorMapId));
		add(new Location("Fourth Floor Man Restroom/Wc", "Man Restroom/Wc", new LatLng(39.891873, 32.783274), .000020, .000039, 8, demoIndoorMapId));
		add(new Location("Fourth Floor Elevator", "Elevator", new LatLng(39.891856, 32.783269), .000013, .000012, 8, demoIndoorMapId));
		add(new Location("Fourth Floor Hallway", "Hallway", new LatLng(39.891941, 32.783212), .000110, .000011, 8, demoIndoorMapId));
		add(new Location("Fourth Floor Hall", "Hall", new LatLng(39.891818, 32.783219), .000047, .000090, 8, demoIndoorMapId));
		add(new Location("Room A-401", "Staff Room", new LatLng(39.891780, 32.783296), .000035, .000047, 8, demoIndoorMapId));
		add(new Location("Fourth Floor Front Downstairs", "Stairs", new LatLng(39.891782, 32.783171), .000008, .000015, 8, demoIndoorMapId));
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
		loadingDialog.setCancelable(false);
		loadingDialog.setCanceledOnTouchOutside(false);
		return loadingDialog;
	}


	public static RelativeLayout buildAutoCompleteTextViewLayout(Context activity, String hint, List<String> content) {
		AutoCompleteArrayAdapter adapter = new AutoCompleteArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, content);
		RelativeLayout.LayoutParams textViewLp = new RelativeLayout.LayoutParams(650, RelativeLayout.LayoutParams.WRAP_CONTENT);
		textViewLp.addRule(RelativeLayout.CENTER_IN_PARENT);
		textViewLp.setMargins(0, 10, 0, 0);

		AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(activity);
		autoCompleteTextView.setThreshold(1);
		autoCompleteTextView.setAdapter(adapter);
		autoCompleteTextView.requestFocus();
		autoCompleteTextView.setLayoutParams(textViewLp);
		autoCompleteTextView.setHint(hint);

		RelativeLayout relativeLayout = new RelativeLayout(activity);
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		relativeLayout.addView(autoCompleteTextView);
		relativeLayout.setLayoutParams(rlp);
		return relativeLayout;
	}

	public static LinearLayout buildEditTextAndAutoTextLayout(Context activity, String hint1, String hint2, List<String> content) {
		LinearLayout.LayoutParams editTextViewLp = new LinearLayout.LayoutParams(650, LinearLayout.LayoutParams.WRAP_CONTENT);
		editTextViewLp.setMargins(0, 10, 0, 0);

		EditText editText = new EditText(activity);
		editText.requestFocus();
		editText.setLayoutParams(editTextViewLp);
		editText.setHint(hint1);

		AutoCompleteArrayAdapter adapter = new AutoCompleteArrayAdapter(activity, android.R.layout.simple_dropdown_item_1line, content);
		LinearLayout.LayoutParams autoTextViewLp = new LinearLayout.LayoutParams(650, LinearLayout.LayoutParams.WRAP_CONTENT);
		autoTextViewLp.setMargins(0, 60, 0, 0);

		AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(activity);
		autoCompleteTextView.setThreshold(1);
		autoCompleteTextView.setAdapter(adapter);
		autoCompleteTextView.setLayoutParams(autoTextViewLp);
		autoCompleteTextView.setHint(hint2);

		LinearLayout linearLayout = new LinearLayout(activity);
		LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		linearLayout.addView(editText);
		linearLayout.addView(autoCompleteTextView);
		linearLayout.setLayoutParams(rlp);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setGravity(Gravity.CENTER);
		return linearLayout;
	}

	public static Double calculateEuclideanDistance(Location op1, Location op2) {
		Double first = Math.pow(op1.getLocation().latitude - op2.getLocation().latitude, 2);
		Double second = Math.pow(op1.getLocation().longitude - op2.getLocation().longitude, 2);
		return Math.sqrt(first + second);
	}

	public static String packLocationDataToSend(Location location) {
		String packMessage;
		if (location.getIndoorMapId() != null) {
			packMessage = String.format("Hi I am sharing my location details with you!\n\n" +
							"Geolocation: %s\n" +
							"Building Name: %s\n" +
							"Floor: %s\n" +
							"Name of the location I am currently in: %s\n" +
							"Type of the location I am currently in: %s\n\n" +
							"Sent from VipAssistant Version 1.0.0",
					String.format("(%s, %s)", location.getLocation().latitude, location.getLocation().longitude),
					String.format("METU-CENG Block A / %s", location.getIndoorMapId()),
					location.getFloor(),
					location.getName(),
					location.getType());
		} else {
			packMessage = String.format("Hi I am sharing my location details with you!\n\n" +
							"Geolocation: %s\n" +
							"Name of the location I am currently in: %s\n" +
							"Type of the location I am currently in: %s\n\n" +
							"Sent from VipAssistant Version 1.0.0",
					String.format("(%s, %s)", location.getLocation().latitude, location.getLocation().longitude),
					location.getName(),
					location.getType());
		}
		return packMessage;
	}

	public static String getETAString(Double plusSeconds) {
		Calendar calendar = Calendar.getInstance();
		Integer hours = calendar.get(Calendar.HOUR_OF_DAY);
		Integer minutes = calendar.get(Calendar.MINUTE);
		Integer seconds = calendar.get(Calendar.SECOND);
		seconds += plusSeconds.intValue();
		if (seconds > 60) {
			seconds -= 60;
			minutes ++;
			if (minutes > 60) {
				minutes = 0;
				hours++;
				if (hours > 24) {
					hours = 0;
				}
			}
		}
		String sHours = hours < 10 ? "0" + hours.toString() : hours.toString();
		String sMins = minutes < 10 ? "0" + minutes.toString() : minutes.toString();
		String sSecs = seconds < 10 ? "0" + seconds.toString() : seconds.toString();
		return String.format("%s:%s:%s", sHours, sMins, sSecs);
	}

	@SuppressLint("DefaultLocale")
	public static String navHelperSideTextBuilder(Double remDistance, Double remTime) {
		String eta = getETAString(remTime);
		return String.format("Remaining\nDistance: %.2f m\nTime: %.2f sec\n\nETA:%s", remDistance, remTime, eta);
	}

	@SuppressLint("DefaultLocale")
	public static String navHelperUpNextTextBuilder(StepInfo stepInfo) {
		String upNext, tempModif;
		if (stepInfo.getDirectionModifier().equals("straight")) {
			tempModif = "go straight";
		} else {
			tempModif = stepInfo.getDirectionModifier();
		}
		switch (stepInfo.getDirectionType()) {
//			case "start":
//				upNext = "Start Walking";
//				break;
			case "arrive":
				upNext = "Arrive Destination Ahead";
				break;
			case "new name":
				upNext = "Ongoing Stairs";
				break;
			case "end of road":
				if (stepInfo.getDirectionModifier().equals("left")) {
					upNext = "end of road turn left";

				} else {
					upNext = "end of road turn right";
				}
				break;
			case "elevator":
				if (stepInfo.getDirectionModifier().equals("go straight")) {
					upNext = "elevator on straight";
				}
				else if (stepInfo.getDirectionModifier().equals("slight left")) {
					upNext = "elevator on slight left";
				} else {
					upNext = "elevator on slight right";
				}
				break;
			case "entrance":
				upNext = "Depart";
				break;
			default:
				upNext = !stepInfo.getDirectionModifier().equals("") ?
						String.format("%s %s", stepInfo.getDirectionType(), tempModif) :
						stepInfo.getDirectionType();
				break;
		}
		upNext = "UP NEXT:\n" + upNext;
		return upNext.toUpperCase();
	}

	public static Drawable navHelperUpNextDrawableBuilder(Context context, String upNext) {
		Drawable returnIcon = null;
		switch (upNext) {
//			case "START WALKING":
//				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_start_walking);
//				break;
			case "UP NEXT:\nARRIVE DESTINATION AHEAD":
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_arrive);
				break;
			case "UP NEXT:\nTURN RIGHT":
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_turn_right);
				break;
			case "UP NEXT:\nTURN LEFT":
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_turn_left);
				break;
			case "UP NEXT:\nDEPART":
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_start_walking);
				break;
			case "UP NEXT:\nCONTINUE LEFT":
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_cont_left);
				break;
			case "UP NEXT:\nCONTINUE RIGHT":
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_cont_right);
				break;
			case "UP NEXT:\nSTAIRS GO STRAIGHT":
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_stairs);
				break;
			case "UP NEXT:\nONGOING STAIRS":
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_stairs);
				break;
			case "UP NEXT:\nEND OF ROAD TURN LEFT":
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_end_left);
				break;
			case "UP NEXT:\nEND OF ROAD TURN RIGHT":
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_end_right);
				break;
			case "UP NEXT:\nELEVATOR ON STRAIGHT":
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_elevator);
				break;
			case "UP NEXT:\nELEVATOR ON SLIGHT LEFT":
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_elevator);
				break;
			case "UP NEXT:\nELEVATOR ON SLIGHT RIGHT":
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_elevator);
				break;
			case "UP NEXT:\nTURN GO STRAIGHT": // todo go straight?
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_go_straight);
				break;
			default:
				Toast.makeText(context, "new upnext: " + upNext, Toast.LENGTH_LONG).show();
				break;
		}
		return returnIcon;
	}

	public static Boolean isTwoLocationEquals(LatLng op1, LatLng op2) {
		return op1.latitude == op2.latitude && op1.longitude == op2.longitude;
	}
}
