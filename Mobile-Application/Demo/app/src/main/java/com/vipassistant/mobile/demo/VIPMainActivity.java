package com.vipassistant.mobile.demo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.androidadvance.topsnackbar.TSnackbar;
import com.eegeo.indoors.IndoorMapView;
import com.eegeo.mapapi.EegeoApi;
import com.eegeo.mapapi.EegeoMap;
import com.eegeo.mapapi.MapView;
import com.eegeo.mapapi.bluesphere.BlueSphere;
import com.eegeo.mapapi.camera.CameraAnimationOptions;
import com.eegeo.mapapi.camera.CameraPosition;
import com.eegeo.mapapi.camera.CameraUpdateFactory;
import com.eegeo.mapapi.geometry.LatLng;
import com.eegeo.mapapi.map.OnInitialStreamingCompleteListener;
import com.eegeo.mapapi.map.OnMapReadyCallback;
import com.eegeo.mapapi.markers.Marker;
import com.eegeo.mapapi.markers.MarkerOptions;
import com.eegeo.mapapi.services.mapscene.MapsceneRequestOptions;
import com.eegeo.mapapi.services.mapscene.MapsceneRequestResponse;
import com.eegeo.mapapi.services.mapscene.MapsceneService;
import com.eegeo.mapapi.services.mapscene.OnMapsceneRequestCompletedListener;
import com.eegeo.mapapi.services.routing.*;
import com.eegeo.mapapi.widgets.RouteView;
import com.eegeo.mapapi.widgets.RouteViewOptions;
import com.vipassistant.mobile.demo.ui.model.Directive;
import com.vipassistant.mobile.demo.ui.model.Location;
import com.vipassistant.mobile.demo.ui.model.StepInfo;
import com.vipassistant.mobile.demo.ui.service.LocationService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.vipassistant.mobile.demo.ui.constants.Constants.*;
import static com.vipassistant.mobile.demo.ui.utils.Utils.*;

public class VIPMainActivity extends AppCompatActivity implements OnMapsceneRequestCompletedListener, OnRoutingQueryCompletedListener {

	private static final int RCS_INPUT = 1000;
	private MapView m_mapView;
	private EegeoMap m_eegeoMap = null;
	private IndoorMapView m_interiorView = null;
	private LocationService locationService;
	private RoutingService routingService;
	private ProgressDialog mapLoading, navigationRequestLoading, processingCommandLoading;
	private Marker outNavigationMarker;
	private BlueSphere m_bluesphere = null;
	private List<RouteView> m_routeViews = new ArrayList<RouteView>();
	private Handler locationHandler = new Handler(), voiceOutputHandler = new Handler();
	private Location userLocation, destinationLocation = null;
	private Double userDirection = 180., finalNavBearing = 180.;
	private StepInfo nextStepInfo = null;
	private List<Marker> nav_markers = new ArrayList<>();
	private Boolean isNavigating = false;
	private Queue<Location> locationQueue = new LinkedList<>(); // For demo purposes
	private Queue<StepInfo> navDirectionQueue = new LinkedList<>(); // For demo purposes
	private final OnMapsceneRequestCompletedListener mapSceneRequestCompletedListener = this;
	private final OnRoutingQueryCompletedListener routingQueryCompletedListener = this;
	private LinearLayout navigationHelper1, navigationHelper2;
	private TextView navHelperSideText, navHelperUpNextText;
	private Double navRemainingDistance = null, navRemainingTime = null;
	private ImageView navHelperUpNextIcon;
	private int initialLoadWait = 0;
	private TextToSpeech mTTS;
	private Integer voiceOutputHandlerRefreshDuration = 1000;
	private Queue<Directive> voiceOutputQueue = new LinkedList<>();
	private TSnackbar snackbar = null;
	private Boolean listeningCommand = false;
	private Intent voiceIntent;
	private SpeechRecognizer voiceRecognizer;
	private RecognitionListener voiceListener;
	private Double navRouteDuration, navRouteDistance;
	private ArrayList<Location> navReqQueueRoutes;
	private ArrayList<StepInfo> navReqNavDirQueueDirections;
	private String saveLocationName;
	private Boolean listeningForNavStart = false, listeningForSearchLocation = false, listeningForFindMeLocation = false,
			listeningForSaveLocation1 = false, listeningForSaveLocation2 = false, listeningForShareLocation = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_vipnavigation);

		/* Dont let phone go sleep while the app is running */
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				if (status == TextToSpeech.SUCCESS) {
					int result = mTTS.setLanguage(Locale.ENGLISH);
					if (result == TextToSpeech.LANG_MISSING_DATA
							|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
						Log.e("VoiceOutput - TTS", "Language not supported");
					} else {
						voiceOutputQueue.add(new Directive("Welcome to Vip Assistant's visually impaired mode.", 3000));
						voiceOutputQueue.add(new Directive("You can click anywhere on the screen to interact with the system via giving voice commands.", 5000));
						voiceOutputQueue.add(new Directive("For example, click and say 'Help' to hear available commands.", 3000));
						voiceOutput(voiceOutputQueue.remove());
						voiceOutputHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								if (!voiceOutputQueue.isEmpty()) {
									Directive directiveToSay = voiceOutputQueue.remove();
									if (directiveToSay.getStringToOutput().equals("Do you want to proceed or cancel navigation?")) {
										listeningForNavStart = true;
									} else if (directiveToSay.getStringToOutput().equals("Where do you want to go?")) {
										listeningForSearchLocation = true;
									} else if (directiveToSay.getStringToOutput().equals("What do you want us to find for you?")) {
										listeningForFindMeLocation = true;
									} else if (directiveToSay.getStringToOutput().equals("Name of the location to save?")) {
										listeningForSaveLocation1 = true;
									} else if (directiveToSay.getStringToOutput().equals("Type of the location to save?")) {
										listeningForSaveLocation2 = true;
									} else if (directiveToSay.getStringToOutput().equals("Name of the person you want to share your location with?")) {
										listeningForShareLocation = true;
									}
									voiceOutput(directiveToSay);
								}
								voiceOutputHandler.postDelayed(this, voiceOutputHandlerRefreshDuration); // refreshing speak queue for every the specified length of directives second
							}
						}, voiceOutputHandlerRefreshDuration);
					}
				} else {
					Log.e("VoiceOutput - TTS", "TTS Initialization failed");
				}
			}
		});

		mapLoading = buildLoadingDialog(this, "Loading Map Data...");
		mapLoading.show();

		processingCommandLoading = buildLoadingDialog(this, "Processing the Command...");
		navigationRequestLoading = buildLoadingDialog(this, "Finding Shortest Possible Route For You...");

		/* Initialize Map */
		EegeoApi.init(this, getString(R.string.eegeo_api_key));
		m_mapView = (MapView) findViewById(R.id.mapView);
		m_mapView.onCreate(savedInstanceState);

		this.locationService = new LocationService(allLocations, allOutdoorLocations);

		/* Inflate Navigation Helper */
		navigationHelper1 = (LinearLayout) findViewById(R.id.nav_helper_1);
		navHelperSideText = (TextView) findViewById(R.id.nav_helper_side_text);
		navigationHelper2 = (LinearLayout) findViewById(R.id.nav_helper_2);
		navHelperUpNextText = (TextView) findViewById(R.id.nav_up_next_text);
		navHelperUpNextIcon = (ImageView) findViewById(R.id.nav_helper_up_next_icon);

		m_mapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(final EegeoMap map) {
				m_eegeoMap = map;
				routingService = map.createRoutingService();

				MapsceneService mapsceneService = map.createMapsceneService();
				mapsceneService.requestMapscene(
						new MapsceneRequestOptions(mapSceneLink)
								.onMapsceneRequestCompletedListener(mapSceneRequestCompletedListener)
				);

				map.addInitialStreamingCompleteListener(new OnInitialStreamingCompleteListener() {
					@Override
					public void onInitialStreamingComplete() {
						mapLoading.dismiss();
						initializeLocationAndSetCamera();
						startVIPmessageBlinking();
					}
				});

				RelativeLayout uiContainer = (RelativeLayout) findViewById(R.id.eegeo_ui_container);
				m_interiorView = new IndoorMapView(m_mapView, uiContainer, m_eegeoMap);
			}
		});

		RelativeLayout uiContainer = (RelativeLayout) findViewById(R.id.eegeo_ui_container);
		uiContainer.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (!listeningCommand) {
					listeningCommand = true;
					if (!isNavigating) {
						mTTS.speak(null, TextToSpeech.QUEUE_FLUSH, null);
						voiceOutputHandlerRefreshDuration = 1000;
						voiceOutputQueue.clear();
					}
					snackbar.dismiss();
					mTTS.stop();
					listenForVoiceInput();
				} else {
					LinearLayout micLayout = (LinearLayout) findViewById(R.id.mic_layout);
					listeningCommand = false;
					processingCommandLoading.dismiss();
					voiceRecognizer.stopListening();
					voiceRecognizer.destroy();
					micLayout.setVisibility(View.INVISIBLE);
				}
				return false;
			}
		});
	}

	private void listenForVoiceInput() {
		LinearLayout micLayout = (LinearLayout) findViewById(R.id.mic_layout);
		micLayout.setVisibility(View.VISIBLE);
		voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
		voiceRecognizer = SpeechRecognizer.createSpeechRecognizer(this.getApplicationContext());
		voiceListener = new RecognitionListener() {
			@Override
			public void onResults(Bundle results) {
				ArrayList<String> voiceResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
				if (voiceResults == null) {
					System.out.println("No voice results");
				} else {
					voiceRecognizer.stopListening();
					voiceRecognizer.destroy();
					listeningCommand = false;

					if (listeningForNavStart) {
						listeningForNavStart = false;
						boolean resulted = false;
						for (String voice : voiceResults) {
							voice = voice.toLowerCase();
							if (voice.contains("proceed")) {
								startNavigation();
								resulted = true;
								break;
							} else if (voice.contains("cancel")) {
								break;
							}
						}
						if (!resulted) {
							Directive dir1 = new Directive("Ok, cancelling your navigation request", 3000);
							voiceOutputQueue.add(dir1);
							isNavigating = false;
							destinationLocation = null;
							for (Marker marker : nav_markers) {
								m_eegeoMap.removeMarker(marker);
							}
							for (RouteView routeView : m_routeViews) {
								routeView.removeFromMap();
							}
						}
					} else if (listeningForSearchLocation) {
						listeningForSearchLocation = false;
						searchLocation(voiceResults);
					} else if (listeningForFindMeLocation) {
						listeningForFindMeLocation = false;
						findMeALocation(voiceResults);
					} else if (listeningForSaveLocation1) {
						listeningForSaveLocation1 = false;
						saveLocationName = voiceResults.get(0);
						Directive dir = new Directive("Type of the location to save?", 2000);
						voiceOutputQueue.add(dir);
					} else if (listeningForSaveLocation2) {
						listeningForSaveLocation2 = false;
						saveLocation(voiceResults.get(0));
					} else if (listeningForShareLocation) {
						listeningForShareLocation = false;
						shareLocation();
					} else {
						processingCommandLoading.show();
						matchVoiceCommand(voiceResults);
					}
				}
			}

			@Override
			public void onReadyForSpeech(Bundle params) {
				System.out.println("Ready for speech");
			}

			/**
			 *  ERROR_NETWORK_TIMEOUT = 1;
			 *  ERROR_NETWORK = 2;
			 *  ERROR_AUDIO = 3;
			 *  ERROR_SERVER = 4;
			 *  ERROR_CLIENT = 5;
			 *  ERROR_SPEECH_TIMEOUT = 6;
			 *  ERROR_NO_MATCH = 7;
			 *  ERROR_RECOGNIZER_BUSY = 8;
			 *  ERROR_INSUFFICIENT_PERMISSIONS = 9;
			 *
			 * @param error code is defined in SpeechRecognizer
			 */
			@Override
			public void onError(int error) {
				System.err.println("Error listening for speech: " + error);
			}

			@Override
			public void onBeginningOfSpeech() {
				System.out.println("Speech starting");
			}

			@Override
			public void onBufferReceived(byte[] buffer) {
				// Auto-generated method stub

			}

			@Override
			public void onEndOfSpeech() {
				// Auto-generated method stub
				LinearLayout micLayout = (LinearLayout) findViewById(R.id.mic_layout);
				micLayout.setVisibility(View.INVISIBLE);

			}

			@Override
			public void onEvent(int eventType, Bundle params) {
				// Auto-generated method stub

			}

			@Override
			public void onPartialResults(Bundle partialResults) {
				// Auto-generated method stub

			}

			@Override
			public void onRmsChanged(float rmsdB) {
				// Auto-generated method stub

			}
		};
		voiceRecognizer.setRecognitionListener(voiceListener);
		voiceRecognizer.startListening(voiceIntent);
	}

	private void matchVoiceCommand(ArrayList<String> res) {
		boolean matched = false;
		for (String inp : res) {
			inp = inp.toLowerCase();
			if (inp.contains("help") || inp.contains("help please")) {
				Toast.makeText(this, "Received 'Help' Command, Processing...", Toast.LENGTH_SHORT).show();
				outputHelp();
				matched = true;
				break;
			} else if (inp.contains("where am i") || inp.contains("where am i please")) {
				Toast.makeText(this, "Received 'Where Am I' Command, Processing...", Toast.LENGTH_SHORT).show();
				whereAmI();
				matched = true;
				break;
			} else if (inp.contains("report my surroundings") || inp.contains("report my surroundings please")) {
				Toast.makeText(this, "Received 'Report My Surroundings' Command, Processing...", Toast.LENGTH_SHORT).show();
				reportMySurroundings();
				matched = true;
				break;
			} else if (inp.contains("search location") || inp.contains("search location please")) {
				Toast.makeText(this, "Received 'Search Location' Command, Processing...", Toast.LENGTH_SHORT).show();
				Directive dir = new Directive("Where do you want to go?", 2000);
				voiceOutputQueue.add(dir);
				matched = true;
				break;
			} else if (inp.contains("find me a location") || inp.contains("find me a location please")) {
				Toast.makeText(this, "Received 'Find Me a Location' Command, Processing...", Toast.LENGTH_SHORT).show();
				Directive dir = new Directive("What do you want us to find for you?", 2500);
				voiceOutputQueue.add(dir);
				matched = true;
				break;
			} else if (inp.contains("save current location") || inp.contains("save current location please")) {
				Toast.makeText(this, "Received 'Save Current Location' Command, Processing...", Toast.LENGTH_SHORT).show();
				Directive dir = new Directive("Name of the location to save?", 2000);
				voiceOutputQueue.add(dir);
				matched = true;
				break;
			} else if (inp.contains("share current location") || inp.contains("share current location please")) {
				Toast.makeText(this, "Received 'Share Current Location' Command, Processing...", Toast.LENGTH_SHORT).show();
				Directive dir = new Directive("Name of the person you want to share your location with?", 3500);
				voiceOutputQueue.add(dir);
				matched = true;
				break;
			} else if (inp.contains("switch to non-vip mode") || inp.contains("switch to non-vip mode please")) {
				Toast.makeText(this, "Received 'Switch to non-VIP Mode' Command, Processing...", Toast.LENGTH_SHORT).show();
				switchToNormalMode();
				matched = true;
				break;
			} else if (inp.contains("what time is it") || inp.contains("what time is it please")) {
				Toast.makeText(this, "Received 'What Time is it?' Command, Processing...", Toast.LENGTH_SHORT).show();
				whatTimeIsIt();
				matched = true;
				break;
			} else if (inp.contains("date")) {
				Toast.makeText(this, "Received 'What is the Date?' Command, Processing...", Toast.LENGTH_SHORT).show();
				whatDateIsIt();
				matched = true;
				break;
			} else if (inp.contains("indoor")) {
				Toast.makeText(this, "Received 'How is the Weather in Indoors' Command, Processing...", Toast.LENGTH_SHORT).show();
				weatherIndoor();
				matched = true;
				break;
			} else if (inp.contains("outdoor")) {
				Toast.makeText(this, "Received 'How is the Weather in Outdoors' Command, Processing...", Toast.LENGTH_SHORT).show();
				weatherOutdoor();
				matched = true;
				break;
			} else if (inp.contains("cancel")) {
				Toast.makeText(this, "Received 'Cancel Current Navigation' Command, Processing...", Toast.LENGTH_SHORT).show();
				cancelNavigation();
				matched = true;
				break;
			}
		}
		if (!matched) {
			unknownCommandVoiceOutput();
		}
		processingCommandLoading.dismiss();
	}

	private void weatherOutdoor() {
		String msg = "Outdoor weather is: Partly cloudy, temperature is: 21 Celsius, Humidity is: 32%, Precipitation: 20%";
		Directive dir = new Directive(msg, 7500);
		voiceOutputQueue.add(dir);
	}

	private void weatherIndoor() {
		String msg = "Indoor temperature is: 27 Celsius, Humidity is: 42%";
		Directive dir = new Directive(msg, 4000);
		voiceOutputQueue.add(dir);
	}

	private void whatDateIsIt() {
		Date date = new Date();
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul"));
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		Integer day = cal.get(Calendar.DAY_OF_MONTH);
		String dayStr = day.toString();
		String monthStr = getMonthStr(month);
		if (day == 25)
			dayStr = "Twenty Fifth";
		String msg = String.format("Today is: %s of %s %s", dayStr, monthStr, year);
		Directive dir = new Directive(msg, 4000);
		voiceOutputQueue.add(dir);
	}

	private void whatTimeIsIt() {
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		String msg = String.format("Current time is: %s", dateFormat.format(date));
		Directive dir = new Directive(msg, 4000);
		voiceOutputQueue.add(dir);
	}

	private void unknownCommandVoiceOutput() {
		Directive dir1 = new Directive("Sorry I could not match your voice with any available commands", 3000);
		Directive dir2 = new Directive("You can try to check available commands list by giving 'Help' command", 3500);
		voiceOutputQueue.add(dir1);
		voiceOutputQueue.add(dir2);
	}

	/**
	 * Create a snackbar + also voice output given for given line
	 *
	 * @param outputString
	 */
	private void voiceOutput(Directive outputDirective) {
		voiceOutputHandlerRefreshDuration = outputDirective.getDurationInMillis();
		View view = findViewById(android.R.id.content).getRootView();
		displaySnackbar(view, outputDirective.getStringToOutput(), outputDirective.getDurationInMillis());
		speak(outputDirective.getStringToOutput());
	}

	private void displaySnackbar(View view, String line, int duration) {
		snackbar = TSnackbar.make(view, line, TSnackbar.LENGTH_LONG);
		snackbar.setIconLeft(R.drawable.ic_record_voice_over_w_24dp, 32);
		View snackbarView = snackbar.getView();
		snackbarView.setBackgroundColor(Color.parseColor("#1a1b29"));
		snackbar.setDuration(duration);
		TextView textView = (TextView) snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
		textView.setTextColor(Color.parseColor("#ffffff"));
		textView.setMaxLines(5);
		textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
		snackbar.show();
	}

	private void speak(String line) {
		/* QUEUE_ADD means new speeches are appended to the queue to be said after current
		 * also could've used QUEUE_FLUSH which means new speech cancels ongoing one */
		if (isNavigating) {
			mTTS.setSpeechRate(2);
		} else {
			mTTS.setSpeechRate(1);
		}
		mTTS.speak(line, TextToSpeech.QUEUE_ADD, null);
	}

	private void startVIPmessageBlinking() {
		LinearLayout bLayout = (LinearLayout) findViewById(R.id.blink_layout);
		Animation anim = new AlphaAnimation(0.0f, 1.0f);
		anim.setDuration(1000);
		anim.setStartOffset(20);
		anim.setRepeatMode(Animation.REVERSE);
		anim.setRepeatCount(Animation.INFINITE);
		bLayout.startAnimation(anim);
	}

	private void initializeLocationAndSetCamera() {
		/* Initialize Location Queue first with indoor map entrance Location */
		locationQueue.add(navigateInitialLocation);
		/* Then initialize related variables */
		this.userLocation = computeCurrentLocation();
		this.outNavigationMarker = m_eegeoMap.addMarker(
				new MarkerOptions()
						.position(userLocation.getLocation())
						.iconKey("my_location")
						.labelText(markerText));
		this.m_bluesphere = m_eegeoMap.getBlueSphere();
		this.m_bluesphere.setEnabled(true);
		this.m_bluesphere.setPosition(userLocation.getLocation());
		this.m_bluesphere.setIndoorMap(userLocation.getIndoorMapId(), userLocation.getFloor());
		this.m_bluesphere.setBearing(userDirection);

		CameraPosition position = new CameraPosition.Builder()
				.target(userLocation.getLocation())
				.indoor(userLocation.getIndoorMapId(), userLocation.getFloor())
				.zoom(cameraZoom)
				.tilt(cameraTilt)
				.bearing(userDirection - 180)
				.build();
		CameraAnimationOptions animationOptions = new CameraAnimationOptions.Builder()
				.build();
		m_eegeoMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), animationOptions);

		/* Also now set-up Handler for periodic Map refreshing */
		this.locationHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				updateMapPeriodically();
				locationHandler.postDelayed(this, 2500);
			}
		}, 2500);
	}

	/**
	 * Computes the new location of user by BLE Infrastructure in real time
	 *
	 * @return LatLng -- user's new location
	 */
	private Location computeCurrentLocation() {
		/* ..BLE Calculation.. */
		if (locationQueue.size() != 1) {
			/* If queue does not contain only 1 element then pop the head */
			return locationQueue.remove();
		} else {
			/* Else do not pop since user stays still */
			return locationQueue.element();
		}
	}

	/**
	 * Method that is called periodically to update user's location with its markers
	 * inside map. Utilizes computeCurrentLocation() for resolving new location
	 */
	private void updateLocation(Location newLocation) {
		this.userLocation = newLocation;
		this.outNavigationMarker.setPosition(newLocation.getLocation());
		this.m_bluesphere.setPosition(userLocation.getLocation());
		this.m_bluesphere.setIndoorMap(userLocation.getIndoorMapId(), userLocation.getFloor());
		this.m_bluesphere.setBearing(userDirection);
	}

	/**
	 * Method that is called periodically to update Map Fragment
	 */
	private void updateMapPeriodically() {
		updateLocation(computeCurrentLocation());
		if (initialLoadWait >= 3) {
			CameraPosition position = new CameraPosition.Builder()
					.target(userLocation.getLocation())
					.indoor(userLocation.getIndoorMapId(), userLocation.getFloor())
					.zoom(cameraZoom)
					.tilt(cameraTilt)
					.bearing(userDirection - 180)
					.build();
			CameraAnimationOptions animationOptions = new CameraAnimationOptions.Builder()
					.build();
			m_eegeoMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), animationOptions);
		} else {
			initialLoadWait++;
		}

		if (isNavigating && locationQueue.size() == 1) {
			String finishedNavigatingVoiceOutput = String.format("You've successfully arrived %s", destinationLocation.getName());
			Directive finishedNavigationDirective = new Directive(finishedNavigatingVoiceOutput, 2000);
			voiceOutputQueue.add(finishedNavigationDirective);

			userDirection = finalNavBearing;
			isNavigating = false;
			destinationLocation = null;
			nextStepInfo = null;
			navRemainingTime = .0;
			navRemainingDistance = .0;
			/* Remove all routes */
			for (Marker marker : nav_markers) {
				m_eegeoMap.removeMarker(marker);
			}
			for (RouteView routeView : m_routeViews) {
				routeView.removeFromMap();
			}
			navigationHelper1.setVisibility(View.INVISIBLE);
			navigationHelper2.setVisibility(View.INVISIBLE);

			Toast.makeText(this, finishedNavigatingVoiceOutput, Toast.LENGTH_LONG).show();
		} else if (isNavigating) {
			/* Update Navigation Helper and etc. */
			navRemainingDistance = navRemainingDistance - PERSON_WALKING_SPEED >= 0 ? navRemainingDistance - PERSON_WALKING_SPEED : 0;
			navRemainingTime = navRemainingTime - mapRefreshMillis / 1000 >= 0 ? navRemainingTime - mapRefreshMillis / 1000 : 0;
			if (!navDirectionQueue.isEmpty() && userLocation.getLocation().equals(nextStepInfo.getDirectionLocation())) {
				nextStepInfo = navDirectionQueue.remove();
				userDirection = nextStepInfo.getDirectionBearingBefore() - 180;
				m_bluesphere.setBearing(userDirection);
				String upNext = navHelperUpNextTextBuilder(nextStepInfo);
				navHelperUpNextIcon.setImageDrawable(navHelperUpNextDrawableBuilder(this, upNext));
				upNext = !upNext.equals("UP NEXT:\nARRIVE DESTINATION AHEAD") &&
						!upNext.equals("UP NEXT:\nELEVATOR ON SLIGHT RIGHT") &&
						!upNext.equals("UP NEXT:\nELEVATOR ON STRAIGHT") &&
						!upNext.equals("UP NEXT:\nELEVATOR ON SLIGHT LEFT") ?
						upNext + String.format(" in %.2f meters", nextStepInfo.getStepDistance()) : upNext;
				navHelperUpNextText.setText(upNext);
				Directive directiveVoice = new Directive(navHelperUpNextText.getText().toString().replace("\n", " "), 2500);
				voiceOutputQueue.add(directiveVoice);
			}
			navHelperSideText.setText(navHelperSideTextBuilder(navRemainingDistance, navRemainingTime));
		}
	}


	/**
	 * Method that requests routes via RoutingService
	 * Its results are utilized in onRoutingQueryCompleted() since this class implements routingQueryCompletedListener
	 *
	 * @param location
	 */
	private void requestNavigationForLocation(Location location) {
		navigationRequestLoading.show();
		destinationLocation = location;
		routingService.findRoutes(new RoutingQueryOptions()
				.addIndoorWaypoint(userLocation.getLocation(), userLocation.getFloor())
				.addIndoorWaypoint(location.getLocation(), location.getFloor())
				.onRoutingQueryCompletedListener(routingQueryCompletedListener));
	}

	@Override
	public void onMapsceneRequestCompleted(MapsceneRequestResponse response) {
		if (!response.succeeded()) {
			Directive failedDirective = new Directive("Failed to load map data, system problem.", 2000);
			voiceOutputQueue.add(failedDirective);
			Toast.makeText(this, failedDirective.getStringToOutput(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onRoutingQueryCompleted(RoutingQuery query, RoutingQueryResponse response) {
		navigationRequestLoading.dismiss();
		if (response.succeeded()) {
			Double routeDuration = .0, routeDistance = .0;
			ArrayList<Location> queueRoutes = new ArrayList<>();
			ArrayList<StepInfo> navDirQueueDirections = new ArrayList<>();
			for (Route route : response.getResults()) {
				routeDistance += route.distance;
				routeDuration += route.duration;
				RouteViewOptions options = new RouteViewOptions()
						.color(Color.argb(168, 255, 0, 0))
						.width(15.0f);
				RouteView routeView = new RouteView(m_eegeoMap, route, options);
				m_routeViews.add(routeView);
				for (RouteSection routeSection : route.sections) {
					for (RouteStep routeStep : routeSection.steps) {
						int floor = routeStep.indoorFloorId;
						String indoorId = routeStep.indoorId;
						for (LatLng path : routeStep.path) {
							if (checkShouldGo(path, floor)) {
								if (path == routeStep.path.get(routeStep.path.size() - 1) &&
										routeStep == routeSection.steps.get(routeSection.steps.size() - 1) &&
										routeSection == route.sections.get(route.sections.size() - 1) &&
										route == response.getResults().get(response.getResults().size() - 1)) {
									queueRoutes.add(new Location("path", "path", path, .000011, .000011, floor, indoorId));
									queueRoutes.add(destinationLocation);
								} else {
									queueRoutes.add(new Location("First Floor Hall", "Path in Hall", path, .000011, .000011, floor, indoorId));
									if (path == routeStep.path.get(0)) {
										/* Add marker */
										MarkerOptions markerOptions = new MarkerOptions().position(path).iconKey("dir_route_start");
										if (routeStep.isIndoors) {
											markerOptions.indoor(indoorId, floor);
										}
										Marker marker = m_eegeoMap.addMarker(markerOptions);
										nav_markers.add(marker);
									}
								}
							}
						}
						if (checkShouldGo(routeStep.directions.location, floor)) {
							navDirQueueDirections.add(new StepInfo(routeStep.directions.type,
									routeStep.directions.modifier,
									routeStep.directions.location,
									routeStep.directions.bearingBefore,
									routeStep.directions.bearingAfter,
									routeStep.duration,
									routeStep.distance));
						} else {
							routeDistance -= routeStep.distance;
							routeDuration -= routeStep.duration;
						}
					}
				}
			}
			routeDuration = routeDistance / PERSON_WALKING_SPEED;
			navRouteDuration = routeDuration;
			navRouteDistance = routeDistance;
			navReqQueueRoutes = queueRoutes;
			navReqNavDirQueueDirections = navDirQueueDirections;
			String eta = getETAString(navRouteDuration);
			Directive dir2 = new Directive(String.format("Distance of the route: %.2f meters", navRouteDistance), 3000);
			Directive dir3 = new Directive(String.format("Duration of the route: %.2f seconds", navRouteDuration), 3000);
			Directive dir4 = new Directive(String.format("Estimated time of arrival: %s", eta), 3500);
			Directive dir5 = new Directive("Do you want to proceed or cancel navigation?", 3000);
			voiceOutputQueue.add(dir2);
			voiceOutputQueue.add(dir3);
			voiceOutputQueue.add(dir4);
			voiceOutputQueue.add(dir5);
		} else {
			String msg = String.format("Failed to find routes for %s", destinationLocation.getName());
			Directive dir = new Directive(msg, 3000);
			voiceOutputQueue.add(dir);
			isNavigating = false;
			destinationLocation = null;
			for (Marker marker : nav_markers) {
				m_eegeoMap.removeMarker(marker);
			}
			for (RouteView routeView : m_routeViews) {
				routeView.removeFromMap();
			}
			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
		}
	}

	private void startNavigation() {
		isNavigating = true;
		navRemainingDistance = navRouteDistance;
		navRemainingTime = navRouteDuration;
		MarkerOptions markerOptions = new MarkerOptions()
				.position(destinationLocation.getLocation())
				.indoor(destinationLocation.getIndoorMapId(), destinationLocation.getFloor())
				.iconKey("dir_enter_map");
		nav_markers.add(m_eegeoMap.addMarker(markerOptions));
		locationQueue.addAll(navReqQueueRoutes);
		navDirectionQueue.addAll(navReqNavDirQueueDirections);
		nextStepInfo = navDirectionQueue.remove();
		finalNavBearing = navReqNavDirQueueDirections.get(navReqNavDirQueueDirections.size() - 1).getDirectionBearingAfter();
		userDirection = nextStepInfo.getDirectionBearingBefore() - 180;
		navigationHelper1.setVisibility(View.VISIBLE);
		navigationHelper2.setVisibility(View.VISIBLE);
		navHelperSideText.setText(navHelperSideTextBuilder(navRemainingDistance, navRemainingTime));
		String upNext = navHelperUpNextTextBuilder(nextStepInfo);
		navHelperUpNextIcon.setImageDrawable(navHelperUpNextDrawableBuilder(this, upNext));
		upNext = !upNext.equals("UP NEXT:\nARRIVE DESTINATION") ?
				upNext + String.format(" in %.2f m", nextStepInfo.getStepDistance()) : upNext + " AHEAD";
		navHelperUpNextText.setText(upNext);
		Directive navInitVoice = new Directive(navHelperUpNextText.getText().toString().replace("\n", " "), 2500);
		voiceOutputQueue.add(navInitVoice);
	}

	private void cancelNavigation() {
		String cancelledVoiceOutput = String.format("Cancelled your current navigation to %s", destinationLocation.getName());
		Directive cancVoice = new Directive(cancelledVoiceOutput, 3000);
		voiceOutputQueue.add(cancVoice);

		/* Refresh location queue */
		locationQueue = new LinkedList<>(Arrays.asList(userLocation));
		navDirectionQueue = new LinkedList<>();
		isNavigating = false;
		destinationLocation = null;
		nextStepInfo = null;
		navRemainingTime = .0;
		navRemainingDistance = .0;
		/* Remove all routes */
		for (Marker marker : nav_markers) {
			m_eegeoMap.removeMarker(marker);
		}
		for (RouteView routeView : m_routeViews) {
			routeView.removeFromMap();
		}
		navigationHelper1.setVisibility(View.INVISIBLE);
		navigationHelper2.setVisibility(View.INVISIBLE);

		Toast.makeText(this, cancelledVoiceOutput, Toast.LENGTH_LONG).show();
	}

	private void reportMySurroundings() {
		List<Location> allNearbyLocations = locationService.findByFloorAndLocation(userLocation.getFloor(), userLocation.getLocation());
		List<String> nearbyLocationNames = locationService.convertToLocationNames(allNearbyLocations);
		String msg1 = "Following locations are located nearby your surroundings:";
		String msg2 = "That was the end of the nearby locations. You can try 'Where Am I?' command to get a detailed information about your current location.";
		String msg3 = "To navigate anyone of the nearby locations, you can try searching it by its name or by its type.";

		List<Directive> dirList = new ArrayList<>();
		dirList.add(new Directive(msg1,3250));
		for (String loc : nearbyLocationNames) {
			dirList.add(new Directive(loc, 1750));
		}
		dirList.add(new Directive(msg2, 8000));
		dirList.add(new Directive(msg3, 6000));
		voiceOutputQueue.addAll(dirList);
	}


	private void whereAmI() {
		String msg1 = String.format("You are in floor %d of building %s", userLocation.getFloor(), demoBuildingName);
		String msg2 = String.format("Name of the location you're in: %s", userLocation.getName());
		String msg3 = String.format("Type of the location you're in: %s", userLocation.getType());
		String msg4 = String.format("Your Geolocation is: (%.2f, %.2f)", userLocation.getLocation().latitude, userLocation.getLocation().longitude);
		String msg5 = String.format("You can try 'Report My Surroundings' command to get an additional insight about the locations nearby to you", userLocation.getLocation().latitude, userLocation.getLocation().longitude);
		List<Directive> dirList = new ArrayList<>();
		dirList.add(new Directive(msg1,3250));
		dirList.add(new Directive(msg2,3000));
		dirList.add(new Directive(msg3,3000));
		dirList.add(new Directive(msg4,4500));
		dirList.add(new Directive(msg5,7500));
		voiceOutputQueue.addAll(dirList);
	}

	private void saveLocation(String locType) {
		if (saveLocationName.contains("spot")) {
			saveLocationName = "My Favourite Spot in A Block";
			locType = "My Location Collection";
		}
		Location newLocation = new Location(saveLocationName, locType, userLocation.getLocation(),
				userLocation.getLocEpsLat(), userLocation.getLocEpsLong(),
				userLocation.getFloor(), userLocation.getIndoorMapId());
		allLocations.add(newLocation);
		saveLocationToLocal(newLocation);
		String msg = String.format("Your Current Location is Saved Successfully with name: %s and with type: %s.", saveLocationName, locType);
		String additionalMsg = "Now you can search this location by any of the search commands accordingly";
		voiceOutputQueue.add(new Directive(msg,8500));
		voiceOutputQueue.add(new Directive(additionalMsg,6500));
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	private void shareLocation() {
		// TODO: not works sometimes.
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage("05428927877", null, packLocationDataToSend(userLocation), null, null);
		String msg = "Your Current Location is Shared Successfully with person: Yavuz Selim Yesilyurt through direct SMS.";
		voiceOutputQueue.add(new Directive(msg,8500));
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	private void findMeALocation(ArrayList<String> voiceResults) {
		boolean found = false;
		for (String voice : voiceResults) {
			voice = voice.toLowerCase();
			if (voice.contains("restroom")) {
				Location loc = new Location("First Floor Woman Restroom/Wc", "Woman Restroom/Wc", new LatLng(39.891868, 32.783253), .000016, .000017, 2, demoIndoorMapId);
				Directive dir = new Directive(String.format("Found 2 Woman Restrooms. The closest Woman Restroom is %s. Getting directions and calculating shortest path for %s.", loc.getName(), loc.getName()), 12000);
				voiceOutputQueue.add(dir);
				requestNavigationForLocation(loc);
				found = true;
				break;
			} else {
				List<Location> res = locationService.findByTypeVoice(voice);
				if (!res.isEmpty()) {
					/* Find Closest via Euclidean Formula */
					Location closest = res.get(0);
					Double minDistance = calculateEuclideanDistance(userLocation, closest);
					for (Location loc : res) {
						Double tempDistance = calculateEuclideanDistance(userLocation, loc);
						if (minDistance > tempDistance) {
							if (Math.abs(userLocation.getFloor() - loc.getFloor()) <= Math.abs(userLocation.getFloor() - closest.getFloor())) {
								closest = loc;
								minDistance = tempDistance;
							}
						} else if (Math.abs(userLocation.getFloor() - loc.getFloor()) < Math.abs(userLocation.getFloor() - closest.getFloor())) {
							closest = loc;
							minDistance = tempDistance;
						}
					}
					Directive dir;
					if (res.size() != 1)
						dir = new Directive(String.format("Found %s %ss. The closest %s is %s. Getting directions and calculating shortest path for %s.", res.size(), voice, voice, closest.getName(), closest.getName()), 10000);
					else
						dir = new Directive(String.format("Found %s %s. The closest %s is %s. Getting directions and calculating shortest path for %s.", res.size(), voice, voice, closest.getName(), closest.getName()), 10000);
					voiceOutputQueue.add(dir);
					requestNavigationForLocation(closest);
					found = true;
					break;
				}
			}
		}
		if (!found) {
			String msg = String.format("Sorry, I could not match your voice with any location types in this building.");
			Directive dir = new Directive(msg, 3000);
			voiceOutputQueue.add(dir);
		}
	}

	private void searchLocation(ArrayList<String> voiceResults) {
		boolean found = false;
		for (String voice : voiceResults) {
			voice = voice.toLowerCase();
			if (voice.contains("5")) {
				Optional<Location> dest = locationService.findByIndoorName("BMB-5");
				if (dest.isPresent()) {
					Directive dir = new Directive(String.format("Getting directions and calculating shortest path for %s", dest.get().getName()), 4000);
					voiceOutputQueue.add(dir);
					requestNavigationForLocation(dest.get());
					found = true;
				}
				break;
			} else if (voice.contains("101")) {
				Optional<Location> dest = locationService.findByIndoorName("Room A-101");
				if (dest.isPresent()) {
					Directive dir = new Directive(String.format("Getting directions and calculating shortest path for %s", dest.get().getName()), 4000);
					voiceOutputQueue.add(dir);
					requestNavigationForLocation(dest.get());
					found = true;
				}
				break;
			} else {
				Optional<String> queryResult = locationService.findByIndoorNameVoice(voice);
				if (queryResult.isPresent()) {
					String destStr = queryResult.get();
					if (destStr.equalsIgnoreCase("bmb-4"))
						destStr = "bmb-5";
					Optional<Location> dest = locationService.findByIndoorName(destStr);
					if (dest.isPresent()) {
						Directive dir = new Directive(String.format("Getting directions and calculating shortest path for %s", dest.get().getName()), 4000);
						voiceOutputQueue.add(dir);
						requestNavigationForLocation(dest.get());
						found = true;
					}
					break;
				}
			}
		}
		if (!found) {
			String msg = String.format("Sorry, I could not match your voice with any locations in this building.");
			Directive dir = new Directive(msg, 3000);
			voiceOutputQueue.add(dir);
		}
	}

	private void switchToNormalMode() {
		voiceOutputQueue.clear();
		mTTS.stop();
		Intent myIntent = new Intent(VIPMainActivity.this, MainActivity.class);
		VIPMainActivity.this.startActivity(myIntent);
	}

	private void outputHelp() {
		List<Directive> helpDirectives = new ArrayList<Directive>() {{
			add(new Directive("In this mode you can Navigate yourself in buildings by searching locations by their name, or even better, by letting Vip Assistant find you a nearby location that you specified.", 9500));
			add(new Directive("Vip Assistant will guide you through the navigation, you can also cancel your navigation anytime", 5000));
			add(new Directive("To search for locations by their name give: 'Search Location' command", 4200));
			add(new Directive("To ask Vip Assistant to find you a location give: 'Find me a Location' command", 4500));
			add(new Directive("Or You can ask where your location is and surroundings report using 'Where Am I' and 'Report My Surroundings' commands respectively.", 7000));
			add(new Directive("In addition, you can save or share your current location using 'Save Current Location' and 'Share Current Location' commands respectively.", 7200));
			add(new Directive("Furthermore, You can ask the current time and date to Vip Assistant using 'What Time is it' and 'What is the Date' commands, respectively.", 7200));
			add(new Directive("To learn about outdoor weather or indoor air conditions you can try: 'How is the Weather in Outdoors' and 'How is the Weather in Indoors' commands, respectively.", 8000));
			add(new Directive("To switch to non VIP mode you can give: 'Switch to non VIP mode' command", 4000));
			add(new Directive("You can listen this helper by giving: 'Help' command again", 3500));
		}};
		voiceOutputQueue.addAll(helpDirectives);
	}

	private void saveLocationToLocal(Location location) {
		String content = String.format("%s,%s,%s,%s,%s,%s,%s,%s", location.getName(),
				location.getType(), location.getLocation().latitude, location.getLocation().longitude,
				location.getLocEpsLat(), location.getLocEpsLong(), location.getFloor(), location.getIndoorMapId());
		try {
			FileOutputStream outputStream = this.getApplicationContext().openFileOutput("saved_locations.txt", Context.MODE_PRIVATE);
			outputStream.write(content.getBytes());
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		m_mapView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		m_mapView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (m_eegeoMap != null) {
			for (RouteView routeView : m_routeViews) {
				routeView.removeFromMap();
			}
			for (Marker navMarker : nav_markers) {
				m_eegeoMap.removeMarker(navMarker);
			}
		}
		m_mapView.onDestroy();
	}
}
