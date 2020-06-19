package com.vipassistant.mobile.demo.ui.utils;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.widget.*;
import androidx.core.content.ContextCompat;
import com.eegeo.mapapi.geometry.LatLng;
import com.eegeo.mapapi.geometry.WeightedLatLngAlt;
import com.vipassistant.mobile.demo.R;
import com.vipassistant.mobile.demo.ui.constants.Constants;
import com.vipassistant.mobile.demo.ui.model.Beacon;
import com.vipassistant.mobile.demo.ui.model.Location;
import com.vipassistant.mobile.demo.ui.model.StepInfo;
import com.vipassistant.mobile.demo.ui.utils.AutoCompleteArrayAdapter;

import java.io.*;
import java.util.*;

import static com.vipassistant.mobile.demo.ui.constants.Constants.*;

public class Utils {
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
			minutes++;
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
				} else if (stepInfo.getDirectionModifier().equals("slight left")) {
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
			case "UP NEXT:\nDEPART GO STRAIGHT":
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
			case "UP NEXT:\nTURN GO STRAIGHT":
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_go_straight);
				break;
			default:
				returnIcon = ContextCompat.getDrawable(context, R.drawable.ic_go_straight);
				break;
		}
		return returnIcon;
	}

	public static List<WeightedLatLngAlt> generateRandomData(int peopleCount, LatLng sw, LatLng ne) {
		Random random = new Random();
		List<WeightedLatLngAlt> points = new ArrayList<>();

		for (int i = 0; i < peopleCount; ++i) {
			double lat = random.nextDouble() * (ne.latitude - sw.latitude) + sw.latitude;
			double lng = random.nextDouble() * (ne.longitude - sw.longitude) + sw.longitude;
			points.add(new WeightedLatLngAlt(lat, lng, random.nextInt(12)));
		}

		return points;
	}

	public static void readAndLoadWorldCitiesData(Context context) throws IOException {
		String file = "worldcities.csv";
		try(BufferedReader br = new BufferedReader(new InputStreamReader(context.getAssets().open(file)))) {
			String line = "";
			while ((line = br.readLine()) != null) {
				String[] lineContent = (line.split(","));
				String name = lineContent[0].replace("\"", "");
				String lat = lineContent[2].replace("\"", "");
				String lon = lineContent[3].replace("\"", "");
				allOutdoorLocations.add(new Location(name, "City", new LatLng(Double.parseDouble(lat), Double.parseDouble(lon))));
			}
		} catch (FileNotFoundException e) {
			System.err.println("worldcities.csv is not found!");
		}
	}

	public static void readSavedLocations(Context context) throws IOException {
		try {
			FileInputStream inputStream = context.getApplicationContext().openFileInput("saved_locations.txt");
			Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] lineContent = (line.split(","));
				allLocations.add(new Location(lineContent[0], lineContent[1],
						new LatLng(Double.parseDouble(lineContent[2]), Double.parseDouble(lineContent[3])),
						Double.parseDouble(lineContent[4]), Double.parseDouble(lineContent[5]),
						Integer.parseInt(lineContent[6]), lineContent[7]));
			}
			inputStream.close();
		} catch (FileNotFoundException e) {
			System.out.println("Not found any saved locations, skipping...");
		}
	}

	public static boolean checkShouldGo(LatLng latLng, int floor) {
		for (LatLng notGoLatLng : criticalLatLngs) {
			if (notGoLatLng.equals(latLng) && floor == 0)
				return false;
		}
		return true;
	}

	public static void updateBeaconRSSI(Beacon beacon) {
		Random rand = new Random();
		int max = 45, min = 10;
		Integer rssi = (rand.nextInt(max + 1 - min) + min) - 100;
		beacon.setRssiValue(rssi);
	}

	public static String getMonthStr(int month) {
		switch (month) {
			case 0:
				return "January";
			case 1:
				return "February";
			case 2:
				return "March";
			case 3:
				return "April";
			case 4:
				return "May";
			case 5:
				return "June";
			case 6:
				return "July";
			case 7:
				return "August";
			case 8:
				return "September";
			case 9:
				return "October";
			case 10:
				return "November";
			case 11:
				return "December";
			default:
				return "Unknown Month";
		}
	}

	/**
	 * A static helper method for generic determination of String to respond
	 * to caller method that is calling the request method
	 *
	 * @param httpCode
	 * @return String
	 */
	public static String resolveHttpCodeResponse(Integer httpCode) {
		switch (httpCode) {
			case 400:
				return Constants.HTTP_400;
			case 401:
				return Constants.HTTP_401;
			case 404:
				return Constants.HTTP_404;
			case 500:
				return Constants.HTTP_500;
			default: {
				Log.w(String.valueOf(Log.WARN), String.format("Got HTTP %s from login request", httpCode));
				return "Oops! Something went wrong - " + httpCode;
			}
		}
	}
}
