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
						voiceOutputQueue.add(new Directive("Welcome to VipAssistant's visually impaired mode, currently getting map information ready for you.", 5000));
						voiceOutputQueue.add(new Directive("You can click anywhere on the screen to interact with the system via giving voice commands.", 5000));
						voiceOutputQueue.add(new Directive("For example, click and say 'Help' to hear available commands.", 3000));
						voiceOutput(voiceOutputQueue.remove());
						voiceOutputHandler.postDelayed(new Runnable() {
							@Override
							public void run() {
								if (!voiceOutputQueue.isEmpty()) {
									voiceOutput(voiceOutputQueue.remove());
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
					if (!isNavigating) { // TODO: eger navige etmiyorsa voice queue sunu sil
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
					// TODO: CHECK FOR DEDICATED INPUTS VIA BOOLEAN FIELDS IN CLASS
					//			if (gettingblainput) {
					//
					//			} else {
					//				matchVoiceCommand(res);
					//			}
					voiceRecognizer.stopListening();
					voiceRecognizer.destroy();
					processingCommandLoading.show();
					matchVoiceCommand(voiceResults);
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
		LinearLayout micLayout = (LinearLayout) findViewById(R.id.mic_layout);
		for (String inp : res) {
			inp = inp.toLowerCase();
			if (inp.contains("help")) {
				Toast.makeText(this, "Received 'Help' Command, Processing...", Toast.LENGTH_LONG).show();
				outputHelp();
				matched = true;
				break;
			} else if (inp.contains("where am i")) {
				Toast.makeText(this, "Received 'Where Am I' Command, Processing...", Toast.LENGTH_LONG).show();
				whereAmI();
				matched = true;
				break;
			} else if (inp.contains("report my surroundings")) {
				Toast.makeText(this, "Received 'Report My Surroundings' Command, Processing...", Toast.LENGTH_LONG).show();
				reportMySurroundings();
				matched = true;
				break;
			} else if (inp.contains("search location")) {
				Toast.makeText(this, "Received 'Search Location' Command, Processing...", Toast.LENGTH_LONG).show();
				searchLocation();
				matched = true;
				break;
			} else if (inp.contains("find me a location")) {
				Toast.makeText(this, "Received 'Find Me a Location' Command, Processing...", Toast.LENGTH_LONG).show();
				findMeALocation();
				matched = true;
				break;
			} else if (inp.contains("save current location")) {
				Toast.makeText(this, "Received 'Save Current Location' Command, Processing...", Toast.LENGTH_LONG).show();
				saveLocation();
				matched = true;
				break;
			} else if (inp.contains("share current location")) {
				Toast.makeText(this, "Received 'Share Current Location' Command, Processing...", Toast.LENGTH_LONG).show();
				shareLocation();
				matched = true;
				break;
			} else if (inp.contains("switch to non-vip mode")) {
				Toast.makeText(this, "Received 'Switch to non-VIP Mode' Command, Processing...", Toast.LENGTH_LONG).show();
				switchToNormalMode();
				matched = true;
				break;
			}
		}
		if (!matched) {
			unknownCommandVoiceOutput();
		}
		processingCommandLoading.dismiss();
		micLayout.setVisibility(View.INVISIBLE);
		listeningCommand = false;
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
				locationHandler.postDelayed(this, mapRefreshMillis);
			}
		}, mapRefreshMillis);
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
				String upNext = navHelperUpNextTextBuilder(nextStepInfo);
				navHelperUpNextIcon.setImageDrawable(navHelperUpNextDrawableBuilder(this, upNext));
				upNext = !upNext.equals("UP NEXT:\nARRIVE DESTINATION AHEAD") &&
						!upNext.equals("UP NEXT:\nELEVATOR ON SLIGHT RIGHT") &&
						!upNext.equals("UP NEXT:\nELEVATOR ON STRAIGHT") &&
						!upNext.equals("UP NEXT:\nELEVATOR ON SLIGHT LEFT") ?
						upNext + String.format(" in %.2f meters", nextStepInfo.getStepDistance()) : upNext;
				navHelperUpNextText.setText(upNext);
			}
			navHelperSideText.setText(navHelperSideTextBuilder(navRemainingDistance, navRemainingTime));

			Directive directiveVoice = new Directive(navHelperUpNextText.getText().toString(), 2000);
			voiceOutputQueue.add(directiveVoice);
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
									queueRoutes.add(new Location("path", "path", path, .000011, .000011, floor, indoorId));
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

			// todo: voiceout found vb vb - start/cancel

//			if(cancel) {
//				isNavigating = false;
//				destinationLocation = null;
//				for (Marker marker : nav_markers) {
//					m_eegeoMap.removeMarker(marker);
//				}
//				for (RouteView routeView : m_routeViews) {
//					routeView.removeFromMap();
//				}
//			} else below
//
			isNavigating = true;
			MarkerOptions markerOptions = new MarkerOptions()
					.position(destinationLocation.getLocation())
					.indoor(destinationLocation.getIndoorMapId(), destinationLocation.getFloor())
					.iconKey("dir_enter_map");
			nav_markers.add(m_eegeoMap.addMarker(markerOptions));
			locationQueue.addAll(queueRoutes);
			navDirectionQueue.addAll(navDirQueueDirections);
			nextStepInfo = navDirectionQueue.remove();
			finalNavBearing = navDirQueueDirections.get(navDirQueueDirections.size() - 1).getDirectionBearingAfter();
			userDirection = nextStepInfo.getDirectionBearingBefore() - 180;
			navigationHelper1.setVisibility(View.VISIBLE);
			navigationHelper2.setVisibility(View.VISIBLE);
			navHelperSideText.setText(navHelperSideTextBuilder(navRemainingDistance, navRemainingTime));
			String upNext = navHelperUpNextTextBuilder(nextStepInfo);
			navHelperUpNextIcon.setImageDrawable(navHelperUpNextDrawableBuilder(this, upNext));
			upNext = !upNext.equals("UP NEXT:\nARRIVE DESTINATION") ?
					upNext + String.format(" in %.2f m", nextStepInfo.getStepDistance()) : upNext + " AHEAD";
			navHelperUpNextText.setText(upNext);
		} else {
			// todo voiceout failing
			Toast.makeText(this, "Failed to find routes to destination point!", Toast.LENGTH_LONG).show();
		}
	}

	private void cancelNavigation() {
		String cancelledVoiceOutput = String.format("Cancelled your current navigation to %s", destinationLocation.getName());
		// todo cancelled vo

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

	private void whereAmI() {
		// todo vo using the values in userLocation and userDirection
	}

	private void saveLocation() {
		List<String> allLocationTypes = locationService.getAllLocationTypes();
		// todo vo + vin for name and type of the location
//		LinearLayout relativeLayout = buildEditTextAndAutoTextLayout(getContext(),
//				"Name of your location", "Type of your location", allLocationTypes);
//		String locName = ((EditText) relativeLayout.getChildAt(0)).getText().toString();
//		String locType = ((AutoCompleteTextView) relativeLayout.getChildAt(1)).getText().toString();

//		Location newLocation = new Location(locName, locType, userLocation.getLocation(),
//				userLocation.getLocEpsLat(), userLocation.getLocEpsLong(),
//				userLocation.getFloor(), userLocation.getIndoorMapId());
//		allLocations.add(newLocation);
//		saveLocationToLocal(newLocation);
		// todo saved vo
		Toast.makeText(this, "Your Current Location is Saved Successfully", Toast.LENGTH_LONG).show();
	}

	private void shareLocation() {
		// todo vo + vin for who to send the location
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage("05428927877", null, packLocationDataToSend(userLocation), null, null);
		// todo shared your location with ....
	}

	private void findMeALocation() {
		List<String> allLocationTypes = locationService.getAllLocationTypes();
		// todo vo + vin for type of the location -- What do you want us to find for you?
//		RelativeLayout autoCompleteTextViewLayout = buildAutoCompleteTextViewLayout(getContext(), "Enter Location Type", allLocationTypes);
//		String locType = ((AutoCompleteTextView) autoCompleteTextViewLayout.getChildAt(0)).getText().toString();
//		List<Location> queryResult = locationService.findByType(locType);
//		if (queryResult != null && !queryResult.isEmpty()) {
//			/* Find Closest via Euclidean Formula */
//			Location closest = queryResult.get(0);
//			Double minDistance = calculateEuclideanDistance(userLocation, closest);
//			for (Location loc : queryResult) {
//				Double tempDistance = calculateEuclideanDistance(userLocation, loc);
//				if (minDistance > tempDistance) {
//					if (!(userLocation.getFloor() != loc.getFloor() && userLocation.getFloor() == closest.getFloor())) {
//						closest = loc;
//						minDistance = tempDistance;
//					}
//				} else if (userLocation.getFloor() == loc.getFloor() && userLocation.getFloor() != closest.getFloor()) {
//					closest = loc;
//					minDistance = tempDistance;
//				}
//			}
//			// todo vo the query result and tell you are getting directions for the closest one
//			requestNavigationForLocation(closest);
//		} else {
//			// todo vo
//			Toast.makeText(this, String.format("There does not exist any locations with type %s in this building.", locType), Toast.LENGTH_LONG).show();
//		}
	}

	private void searchLocation() {
		List<String> allLocationNames = locationService.getAllIndoorLocationNames();
		// todo vo + vin for name of the location -- Where do you want to go inside the building?
//		RelativeLayout autoCompleteTextViewLayout = buildAutoCompleteTextViewLayout(getContext(), "Enter Location Name", allLocationNames);
//		String locName = ((AutoCompleteTextView) autoCompleteTextViewLayout.getChildAt(0)).getText().toString();
//		Optional<Location> queryResult = locationService.findByIndoorName(locName);
//		if (queryResult.isPresent()) {
//			// todo vo the query result and tell you are getting directions for the location right now
//			requestNavigationForLocation(queryResult.get());
//		} else {
//			// todo vo
//			Toast.makeText(this, String.format("There does not exist any locations with name %s in this building.", locName), Toast.LENGTH_LONG).show();
//		}
	}

	private void reportMySurroundings() {
		List<Location> allNearbyLocations = locationService.findByFloorAndLocation(userLocation.getFloor(), userLocation.getLocation());
		List<String> nearbyLocationNames = locationService.convertToLocationNames(allNearbyLocations);
		String innerTitle = String.format("You are in floor %d of building %s", userLocation.getFloor(), demoBuildingName);
		String innerMessage = String.format("Name of the location you're in: %s\n" +
						"Type of the location you're in: %s\n" +
						"Your Geolocation is: (%f, %f)\n\n" +
						"Below is a list locations that are adjacent to you:",
				userLocation.getName(), userLocation.getType(),
				userLocation.getLocation().latitude, userLocation.getLocation().longitude);
		// todo vo above
	}

	private void switchToNormalMode() {
		// todo vo that you are switching to non-vip
		voiceOutputQueue.clear();
		mTTS.stop();
		Intent myIntent = new Intent(VIPMainActivity.this, MainActivity.class);
		VIPMainActivity.this.startActivity(myIntent);
	}

	private void outputHelp() {
		// todo vo list of commands available
		List<Directive> helpDirectives = new ArrayList<Directive>() {{
			add(new Directive("In this mode you can Navigate yourself in buildings by searching locations by their name, or even better, by letting VipAssistant find you a nearby location that you specified.", 9000));
			add(new Directive("VipAssistant will guide you through the navigation, you can also cancel your navigation anytime", 5000));
			add(new Directive("Or You can ask where your location is and surroundings report,", 3500));
			add(new Directive("In addition you can save or share your current location.", 3500));
			add(new Directive("To learn where you are right now give: 'Where am I' command", 3500));
			add(new Directive("To get a surroundings report give: 'Report my surroundings' command", 4000));
			add(new Directive("To search for locations by their name give: 'Search location' command", 4000));
			add(new Directive("To ask VipAssistant to find you a location give: 'Find me a location' command", 4500));
			add(new Directive("To save your current location give: 'Save current location' command", 4000));
			add(new Directive("To share your current location give: 'Share current location' command", 4000));
			add(new Directive("To switch to non-VIP mode give: 'Switch to non-VIP mode' command", 3500));
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
