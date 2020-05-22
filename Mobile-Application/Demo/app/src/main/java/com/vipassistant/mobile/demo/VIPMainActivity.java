package com.vipassistant.mobile.demo;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
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
import com.eegeo.mapapi.markers.OnMarkerClickListener;
import com.eegeo.mapapi.precaching.OnPrecacheOperationCompletedListener;
import com.eegeo.mapapi.services.mapscene.MapsceneRequestOptions;
import com.eegeo.mapapi.services.mapscene.MapsceneRequestResponse;
import com.eegeo.mapapi.services.mapscene.MapsceneService;
import com.eegeo.mapapi.services.mapscene.OnMapsceneRequestCompletedListener;
import com.eegeo.mapapi.services.routing.*;
import com.eegeo.mapapi.widgets.RouteView;
import com.eegeo.mapapi.widgets.RouteViewOptions;
import com.google.android.material.navigation.NavigationView;
import com.vipassistant.mobile.demo.ui.mapnavigation.MapNavigationViewModel;
import com.vipassistant.mobile.demo.ui.model.Location;
import com.vipassistant.mobile.demo.ui.model.StepInfo;
import com.vipassistant.mobile.demo.ui.service.LocationService;
import com.vipassistant.mobile.demo.ui.vipnavigate.VipNavigationFragment;
import com.vipassistant.mobile.demo.ui.vipnavigate.VipNavigationViewModel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import static android.view.MotionEvent.ACTION_BUTTON_PRESS;
import static com.vipassistant.mobile.demo.ui.constants.Constants.*;
import static com.vipassistant.mobile.demo.ui.utils.Utils.*;
import static com.vipassistant.mobile.demo.ui.utils.Utils.buildLoadingDialog;

public class VIPMainActivity extends AppCompatActivity implements OnMapsceneRequestCompletedListener, OnRoutingQueryCompletedListener {

	private MapView m_mapView;
	private EegeoMap m_eegeoMap = null;
	private IndoorMapView m_interiorView = null;
	private LocationService locationService;
	private RoutingService routingService;
	private ProgressDialog mapLoading, navigationRequestLoading, recalculatingRouteLoading;
	private Marker outNavigationMarker;
	private BlueSphere m_bluesphere = null;
	private List<RouteView> m_routeViews = new ArrayList<RouteView>();
	private Handler handler = new Handler();
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

	// todo: add voice out/in to specified places

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_vipnavigation);

		/* Dont let phone go sleep while the app is running */
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		/* Initialize Outdoor Locations once and for all and read saved locations if there is any */
		try {
			readAndLoadWorldCitiesData(this);
			readSavedLocations(this);
		} catch (IOException e) {
			e.printStackTrace();
		}

		recalculatingRouteLoading = buildLoadingDialog(this, "Recalculating The Route...");
		navigationRequestLoading = buildLoadingDialog(this, "Finding Shortest Possible Route For You...");
		mapLoading = buildLoadingDialog(this, "Loading Map Data...");
		// todo getting ready vo
		mapLoading.show();

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
						// todo now ready vo + nasil kullanacagini soyleyen bi initial vo
						mapLoading.dismiss();
						initializeLocationAndSetCamera();
					}
				});

				RelativeLayout uiContainer = (RelativeLayout) findViewById(R.id.eegeo_ui_container);
				m_interiorView = new IndoorMapView(m_mapView, uiContainer, m_eegeoMap);
			}
		});
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
		this.handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				updateMapPeriodically();
				handler.postDelayed(this, mapRefreshMillis);
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
		}
		else {
			initialLoadWait++;
		}

		if (isNavigating && locationQueue.size() == 1) {
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
			// todo finished vo
			Toast.makeText(this, "You've successfully arrived your destination!", Toast.LENGTH_LONG).show();
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
						upNext + String.format(" in %.2f m", nextStepInfo.getStepDistance()) : upNext;
				navHelperUpNextText.setText(upNext);
			}
			navHelperSideText.setText(navHelperSideTextBuilder(navRemainingDistance, navRemainingTime));
			// todo nav status vo
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
			// todo voiceout
			Toast.makeText(this, "Failed to load mapscene", Toast.LENGTH_LONG).show();
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
			// todo voiceout
			Toast.makeText(this, "Failed to find routes to destination point!", Toast.LENGTH_LONG).show();
		}
	}

	public void cancelNavigation() {
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
		// todo cancelled vo
		Toast.makeText(this, "Cancelled your current navigation", Toast.LENGTH_LONG).show();
	}

	public void whereAmI() {
		// todo vo using the values in userLocation and userDirection
	}

	public void saveLocation() {
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

	public void shareLocation() {
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(Intent.EXTRA_TEXT, packLocationDataToSend(userLocation));
		startActivity(Intent.createChooser(share, "Share My Location!"));
		// todo creates intent...?
	}

	public void findMeA() {
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

	public void searchLocation() {
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

	public void reportMySurroundings() {
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
