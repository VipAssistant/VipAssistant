package com.vipassistant.mobile.demo.ui.constants;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.widget.*;
import androidx.core.content.ContextCompat;
import com.eegeo.mapapi.geometry.LatLng;
import com.eegeo.mapapi.geometry.WeightedLatLngAlt;
import com.vipassistant.mobile.demo.R;
import com.vipassistant.mobile.demo.ui.model.Location;
import com.vipassistant.mobile.demo.ui.model.StepInfo;
import com.vipassistant.mobile.demo.ui.utils.AutoCompleteArrayAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

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

	public static List<WeightedLatLngAlt> generateRandomData(int peopleCount, LatLng sw, LatLng ne) {
		Random random = new Random();
		List<WeightedLatLngAlt> points = new ArrayList<>();

		for (int i = 0; i < peopleCount; ++i) {
			double lat = random.nextDouble() * (ne.latitude - sw.latitude) + sw.latitude;
			double lng = random.nextDouble() * (ne.longitude - sw.longitude) + sw.longitude;
			points.add(new WeightedLatLngAlt(lat, lng, random.nextInt(12))); // TODO: intensity value!!!
		}

		return points;
	}
}
