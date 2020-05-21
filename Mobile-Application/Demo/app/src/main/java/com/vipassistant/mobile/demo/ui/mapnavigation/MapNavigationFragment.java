package com.vipassistant.mobile.demo.ui.mapnavigation;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
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
import com.eegeo.mapapi.precaching.PrecacheOperationResult;
import com.eegeo.mapapi.services.mapscene.MapsceneRequestOptions;
import com.eegeo.mapapi.services.mapscene.MapsceneRequestResponse;
import com.eegeo.mapapi.services.mapscene.MapsceneService;
import com.eegeo.mapapi.services.mapscene.OnMapsceneRequestCompletedListener;
import com.eegeo.mapapi.services.routing.*;
import com.eegeo.mapapi.widgets.RouteView;
import com.eegeo.mapapi.widgets.RouteViewOptions;
import com.vipassistant.mobile.demo.R;
import com.vipassistant.mobile.demo.ui.model.Location;
import com.vipassistant.mobile.demo.ui.model.StepInfo;
import com.vipassistant.mobile.demo.ui.service.LocationService;

import java.util.*;

import static android.view.MotionEvent.ACTION_BUTTON_PRESS;
import static com.vipassistant.mobile.demo.ui.constants.Constants.*;
import static com.vipassistant.mobile.demo.ui.constants.Utils.*;

public class MapNavigationFragment extends Fragment implements OnMapsceneRequestCompletedListener, OnRoutingQueryCompletedListener, OnPrecacheOperationCompletedListener {
	private View root;
	private MapNavigationViewModel mapNavigationViewModel;
	private LocationService locationService;
	private RoutingService routingService;
	private MapView m_mapView;
	private EegeoMap m_eegeoMap = null;
	private IndoorMapView m_interiorView = null;
	private Marker outNavigationMarker;
	private BlueSphere m_bluesphere = null;
	private List<RouteView> m_routeViews = new ArrayList<RouteView>();
	private Handler handler = new Handler();
	private Location userLocation, destinationLocation = null;
	private Double userDirection = 180., finalNavBearing = 180.;
	private StepInfo nextStepInfo = null;
	private List<Marker> nav_markers = new ArrayList<>(), see_on_map_markers = new ArrayList<>();
	private Boolean isNavigating = false;
	private Queue<Location> locationQueue = new LinkedList<>(); // For demo purposes
	private Queue<StepInfo> navDirectionQueue = new LinkedList<>(); // For demo purposes
	private final OnMapsceneRequestCompletedListener mapSceneRequestCompletedListener = this;
	private final OnRoutingQueryCompletedListener routingQueryCompletedListener = this;
	private final OnPrecacheOperationCompletedListener precacheOperationCompletedListener = this;
	private final OnMarkerClickListener m_markerTappedListener = new MarkerClickListenerImpl();
	private Integer findMePressed = 0;
	private Button findMeBtn, searchBtn, shareSaveBtn, cancelNavBtn;
	private LinearLayout navigationHelper1, navigationHelper2;
	private TextView navHelperSideText, navHelperUpNextText;
	private Double navRemainingDistance = null, navRemainingTime = null;
	private ImageView navHelperUpNextIcon;
	private LatLng preCacheLocation = null;
	private ProgressDialog mapLoading, navigationRequestLoading, recalculatingRouteLoading;
	private int cachingTimeout = 0;
	private Set<LatLng> cacheLocationSet = new HashSet<>();
	private boolean recalcOngoing = false;
	/* For demo of the recalculating the route feature first set below three to true.
	* Then in the app, first go to the Z-103 and first demo happens, then try to go to the Room A-306
	* which will trigger the second and the third demos. */
	private Boolean incorrectDemo1Activated = false, incorrectDemo2Activated = false, incorrectDemo3Activated = false;

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapNavigationViewModel = ViewModelProviders.of(getActivity()).get(MapNavigationViewModel.class);
		root = inflater.inflate(R.layout.fragment_map_nav, container, false);
		recalculatingRouteLoading = buildLoadingDialog(getActivity(), "Recalculating The Route...");
		navigationRequestLoading = buildLoadingDialog(getActivity(), "Finding Shortest Possible Route For You...");
		mapLoading = buildLoadingDialog(getActivity(), "Loading Map Data...");
		mapLoading.show();

		/* Initialize Map */
		EegeoApi.init(getActivity(), getString(R.string.eegeo_api_key));
		m_mapView = (MapView) root.findViewById(R.id.mapView);
		m_mapView.onCreate(savedInstanceState);

		/* Initialize LocationService */
		this.locationService = new LocationService(allLocations);

		/* Inflate Navigation Helper */
		navigationHelper1 = (LinearLayout) root.findViewById(R.id.nav_helper_1);
		navHelperSideText = (TextView) root.findViewById(R.id.nav_helper_side_text);
		navigationHelper2 = (LinearLayout) root.findViewById(R.id.nav_helper_2);
		navHelperUpNextText = (TextView) root.findViewById(R.id.nav_up_next_text);
		navHelperUpNextIcon = (ImageView) root.findViewById(R.id.nav_helper_up_next_icon);

		m_mapView.getMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(final EegeoMap map) {
				m_eegeoMap = map;
				routingService = map.createRoutingService();
				m_eegeoMap.addMarkerClickListener(m_markerTappedListener);

				MapsceneService mapsceneService = map.createMapsceneService();
				mapsceneService.requestMapscene(
						new MapsceneRequestOptions(mapSceneLink)
								.onMapsceneRequestCompletedListener(mapSceneRequestCompletedListener)
				);

				map.addInitialStreamingCompleteListener(new OnInitialStreamingCompleteListener() {
					@Override
					public void onInitialStreamingComplete() {
						mapLoading.dismiss();
						initializeLocation();
					}
				});

				RelativeLayout uiContainer = (RelativeLayout) root.findViewById(R.id.eegeo_ui_container);
				m_interiorView = new IndoorMapView(m_mapView, uiContainer, m_eegeoMap);

				findMeBtn = (Button) root.findViewById(R.id.findMeButton);
				findMeBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						centerCurrentLocation();
					}
				});

				searchBtn = (Button) root.findViewById(R.id.searchButton);
				searchBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						displaySearchDialog();
					}
				});

				cancelNavBtn = (Button) root.findViewById(R.id.cancelNavButton);
				cancelNavBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						displayCancelNavigationDialog();
					}
				});

				shareSaveBtn = (Button) root.findViewById(R.id.shareAndSaveButton);
				shareSaveBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						displayShareSaveDialog();
					}
				});
			}
		});

		RelativeLayout uiContainer = (RelativeLayout) root.findViewById(R.id.eegeo_ui_container);
		uiContainer.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() != ACTION_BUTTON_PRESS) {
					findMeBtn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.nav_find_me_1));
					findMePressed = 0; // TODO CHECK
				}
				return false;
			}
		});
		return root;
	}

	private void initializeLocation() {
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
	 * updates user location etc.
	 * updates existing routes ? !!!!!!
	 * updates geoloc nav helper etc?
	 */
	private void updateMapPeriodically() {
		updateLocation(computeCurrentLocation());

		if (mapNavigationViewModel.getCachingActivated() && cachingTimeout >= 3) {
			cacheCurrentCameraLocation(m_eegeoMap.getCameraPosition().target);
			cachingTimeout = 0;
		}
		cachingTimeout++;

		if (this.findMePressed == 3) {
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
		} else if (this.findMePressed != 0) {
			this.findMePressed++;
		}

		if ((incorrectDemo1Activated || incorrectDemo2Activated || incorrectDemo3Activated) &&
				userLocation.getType().equals("stop")) {
			userLocation.setType("not-stop-anymore");
			navDirectionQueue.clear();
			recalcOngoing = false;
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
			if (incorrectDemo1Activated)
				requestNavigationForLocation(locationService.findByName("Z-103").get());
			else
				requestNavigationForLocation(locationService.findByName("Room A-306").get());
		} else {
			if (isNavigating && locationQueue.size() == 1 &&
					((recalcOngoing && !(!incorrectDemo1Activated && !incorrectDemo2Activated && incorrectDemo3Activated))
							|| !(incorrectDemo1Activated || incorrectDemo2Activated || incorrectDemo3Activated))) {
				recalcOngoing = false;
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
				cancelNavBtn.setVisibility(View.INVISIBLE);
				searchBtn.setVisibility(View.VISIBLE);
				Toast.makeText(getContext(), "You've successfully arrived your destination!", Toast.LENGTH_LONG).show();
			} else if (isNavigating) {
				/* Update Navigation Helper and etc. */
				navRemainingDistance = navRemainingDistance - PERSON_WALKING_SPEED >= 0 ? navRemainingDistance - PERSON_WALKING_SPEED : 0;
				navRemainingTime = navRemainingTime - mapRefreshMillis / 1000 >= 0 ? navRemainingTime - mapRefreshMillis / 1000 : 0;
				if (!navDirectionQueue.isEmpty() && userLocation.getLocation().equals(nextStepInfo.getDirectionLocation())) {
					nextStepInfo = navDirectionQueue.remove();
					userDirection = nextStepInfo.getDirectionBearingBefore() - 180;
					String upNext = navHelperUpNextTextBuilder(nextStepInfo);
					navHelperUpNextIcon.setImageDrawable(navHelperUpNextDrawableBuilder(getActivity(), upNext));
					upNext = !upNext.equals("UP NEXT:\nARRIVE DESTINATION AHEAD") &&
							!upNext.equals("UP NEXT:\nELEVATOR ON SLIGHT RIGHT") &&
							!upNext.equals("UP NEXT:\nELEVATOR ON STRAIGHT") &&
							!upNext.equals("UP NEXT:\nELEVATOR ON SLIGHT LEFT") ?
							upNext + String.format(" in %.2f m", nextStepInfo.getStepDistance()) : upNext;
					navHelperUpNextText.setText(upNext);
				}
				navHelperSideText.setText(navHelperSideTextBuilder(navRemainingDistance, navRemainingTime));
			}

			if (locationQueue.size() == 2) {
				if ((incorrectDemo1Activated || incorrectDemo2Activated)) {
					userDirection = 270.;
				} else if (incorrectDemo3Activated) {
					userDirection = 90.;
				}
			}
		}
	}

	private void centerCurrentLocation() {
		findMePressed = 1;
		findMeBtn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.nav_find_me_2));
		CameraPosition currentCamPosition = m_eegeoMap.getCameraPosition();
		Double zoomValue = currentCamPosition.targetIndoorMapId.equals("") ? 19 : cameraZoom;
		CameraPosition position = new CameraPosition.Builder()
				.target(userLocation.getLocation())
				.indoor(userLocation.getIndoorMapId(), userLocation.getFloor())
				.zoom(zoomValue)
				.tilt(cameraTilt)
				.bearing(userDirection - 180)
				.build();
		CameraAnimationOptions animationOptions = new CameraAnimationOptions.Builder()
				.build();
		m_eegeoMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), animationOptions);
		Toast.makeText(getActivity(), "Centered Your Location", Toast.LENGTH_LONG).show();
	}

	private void displaySearchDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setIcon(android.R.drawable.ic_menu_search);
		alertDialogBuilder.setTitle("Search in Map");
		alertDialogBuilder.setMessage("You can query the system in 3 different ways!");
		alertDialogBuilder.setPositiveButton("Find Me A ... inside the building!", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				List<String> allLocationTypes = locationService.getAllLocationTypes();
				RelativeLayout autoCompleteTextViewLayout = buildAutoCompleteTextViewLayout(getContext(), "Enter Location Type", allLocationTypes);
				InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.toggleSoftInputFromWindow(root.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
				AlertDialog.Builder innerDialogBuilder = new AlertDialog.Builder(getActivity());
				innerDialogBuilder.setIcon(android.R.drawable.ic_menu_search);
				innerDialogBuilder.setTitle("What do you want us to find for you?");
				innerDialogBuilder.setView(autoCompleteTextViewLayout);
				innerDialogBuilder.setPositiveButton("Find!", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(autoCompleteTextViewLayout.getWindowToken(), 0);

						String locType = ((AutoCompleteTextView) autoCompleteTextViewLayout.getChildAt(0)).getText().toString();
						List<Location> queryResult = locationService.findByType(locType);
						if (queryResult != null && !queryResult.isEmpty()) {
							AlertDialog.Builder secondInnerDialogBuilder = new AlertDialog.Builder(getActivity());
							innerDialogBuilder.setIcon(android.R.drawable.ic_dialog_map);
							secondInnerDialogBuilder.setTitle(String.format("Found %d %s", queryResult.size(), locType));
							secondInnerDialogBuilder.setMessage("You can either see them on the map or directly request navigation for the closest one." +
									" If you choose to see them on the map, you can still request navigation to location by clicking to the marker.");
							secondInnerDialogBuilder.setPositiveButton("See them on the Map", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									/* Create clickable marker for each result in the map */
									for (Location location : queryResult) {
										Marker locMarker;
										if (location.getIndoorMapId() != null) {
											locMarker = m_eegeoMap.addMarker(new MarkerOptions()
													.position(location.getLocation())
													.indoor(location.getIndoorMapId(), location.getFloor())
													.iconKey("dir_route_end")
													.labelText(location.getName()));
										} else {
											locMarker = m_eegeoMap.addMarker(new MarkerOptions()
													.position(location.getLocation())
													.iconKey("dir_route_end")
													.labelText(location.getName()));
										}
										see_on_map_markers.add(locMarker);
									}
									dialog.dismiss();
								}
							});
							secondInnerDialogBuilder.setNegativeButton("Request Navigation for the Closest", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									/* Find Closest via Euclidean Formula */
									Location closest = queryResult.get(0);
									Double minDistance = calculateEuclideanDistance(userLocation, closest);
									for (Location loc : queryResult) {
										Double tempDistance = calculateEuclideanDistance(userLocation, loc);
										if (minDistance > tempDistance) {
											if (!(userLocation.getFloor() != loc.getFloor() && userLocation.getFloor() == closest.getFloor())) {
												closest = loc;
												minDistance = tempDistance;
											}
										} else if (userLocation.getFloor() == loc.getFloor() && userLocation.getFloor() != closest.getFloor()) {
											closest = loc;
											minDistance = tempDistance;
										}
									}
									requestNavigationForLocation(closest);
									dialog.dismiss();
								}
							});
							secondInnerDialogBuilder.show().show();
						} else {
							Toast.makeText(getActivity(), String.format("There does not exist any locations with type %s in this building.", locType), Toast.LENGTH_LONG).show();
						}
						dialog.dismiss();
					}
				});
				innerDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(autoCompleteTextViewLayout.getWindowToken(), 0);
						dialog.dismiss();
					}
				});
				innerDialogBuilder.show().show();
				dialog.dismiss();
			}
		});
		alertDialogBuilder.setNegativeButton("Free Map Search", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				List<String> allLocationNames = locationService.getAllLocationNames();
				RelativeLayout autoCompleteTextViewLayout = buildAutoCompleteTextViewLayout(getContext(), "Enter Location Name", allLocationNames);
				InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.toggleSoftInputFromWindow(root.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
				AlertDialog.Builder innerDialogBuilder = new AlertDialog.Builder(getActivity());
				innerDialogBuilder.setIcon(android.R.drawable.ic_menu_search);
				innerDialogBuilder.setTitle("Where do you want to go?");
				innerDialogBuilder.setView(autoCompleteTextViewLayout);
				innerDialogBuilder.setPositiveButton("Get Directions", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(autoCompleteTextViewLayout.getWindowToken(), 0);

						String locName = ((AutoCompleteTextView) autoCompleteTextViewLayout.getChildAt(0)).getText().toString();
						Optional<Location> queryResult = locationService.findByName(locName);
						if (queryResult.isPresent()) {
							requestNavigationForLocation(queryResult.get());
						} else {
							Toast.makeText(getActivity(), String.format("There does not exist any locations with name %s in this building.", locName), Toast.LENGTH_LONG).show();
						}
						dialog.dismiss();
					}
				});
				innerDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(autoCompleteTextViewLayout.getWindowToken(), 0);
						dialog.dismiss();
					}
				});
				innerDialogBuilder.show().show();
				dialog.dismiss();
			}
		});
		alertDialogBuilder.setNeutralButton("Report My Surroundings", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				List<Location> allNearbyLocations = locationService.findByFloorAndLocation(userLocation.getFloor(), userLocation.getLocation());
				List<String> nearbyLocationNames = locationService.convertToLocationNames(allNearbyLocations);
				AlertDialog.Builder innerDialogBuilder = new AlertDialog.Builder(getActivity());
				String innerTitle = String.format("You are in floor %d of building %s", userLocation.getFloor(), demoBuildingName);
				String innerMessage = String.format("Name of the location you're in: %s\n" +
								"Type of the location you're in: %s\n" +
								"Your Geolocation is: (%f, %f)\n\n" +
								"Below is a list locations that are adjacent to you:",
						userLocation.getName(), userLocation.getType(),
						userLocation.getLocation().latitude, userLocation.getLocation().longitude);
				innerDialogBuilder.setIcon(android.R.drawable.ic_menu_mylocation);
				innerDialogBuilder.setTitle(innerTitle);
				innerDialogBuilder.setMessage(innerMessage);
				ArrayAdapter<String> locNamesAdapter = new ArrayAdapter<String>(getContext(),
						android.R.layout.simple_spinner_item, nearbyLocationNames);
				ListView listView = new ListView(getContext());
				listView.setAdapter(locNamesAdapter);
				innerDialogBuilder.setView(listView);
				innerDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				innerDialogBuilder.show().show();
				dialog.dismiss();
			}
		});

		AlertDialog dialog = alertDialogBuilder.show();

		/*	Push dialog buttons to the left */
		Button btn1 = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		Button btn2 = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
		Button btn3 = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btn1.getLayoutParams();
		layoutParams.gravity = Gravity.LEFT;
		btn1.setLayoutParams(layoutParams);
		btn2.setLayoutParams(layoutParams);
		btn3.setLayoutParams(layoutParams);
		dialog.show();
	}

	@Override
	public void onMapsceneRequestCompleted(MapsceneRequestResponse response) {
		if (response.succeeded()) {
//			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
//			alertDialogBuilder.setIcon(android.R.drawable.ic_menu_search);
//			alertDialogBuilder.setTitle("Welcome non-VIP Navigation Module!");
//			alertDialogBuilder.setPositiveButton("Find Me A ...!", new DialogInterface.OnClickListener() {
		} else {
			Toast.makeText(getActivity(), "Failed to load mapscene", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Method that requests routes via RoutingService
	 * Its results are utilized in onRoutingQueryCompleted() since this class implements routingQueryCompletedListener
	 *
	 * @param location
	 */
	private void requestNavigationForLocation(Location location) {
		if ((incorrectDemo1Activated || incorrectDemo2Activated || incorrectDemo3Activated) && isNavigating) {
			recalculatingRouteLoading.show();
		} else {
			navigationRequestLoading.show();
		}
		destinationLocation = location;
		routingService.findRoutes(new RoutingQueryOptions()
				.addIndoorWaypoint(userLocation.getLocation(), userLocation.getFloor())
				.addIndoorWaypoint(location.getLocation(), location.getFloor())
				.onRoutingQueryCompletedListener(routingQueryCompletedListener));
	}

	@Override
	public void onRoutingQueryCompleted(RoutingQuery query, RoutingQueryResponse response) {
		if ((incorrectDemo1Activated || incorrectDemo2Activated || incorrectDemo3Activated) && isNavigating) {
			recalculatingRouteLoading.dismiss();
		} else {
			navigationRequestLoading.dismiss();
		}

		if (response.succeeded()) {
			if ((incorrectDemo1Activated || incorrectDemo2Activated || incorrectDemo3Activated) && isNavigating) {
				recalculatingTheRoute(response.getResults());
			} else {
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
							navDirQueueDirections.add(new StepInfo(routeStep.directions.type,
									routeStep.directions.modifier,
									routeStep.directions.location,
									routeStep.directions.bearingBefore,
									routeStep.directions.bearingAfter,
									routeStep.duration,
									routeStep.distance));
						}
					}
				}
				routeDuration = routeDistance / PERSON_WALKING_SPEED; // TODO: check
				String eta = getETAString(routeDuration);
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
				dialogBuilder.setIcon(android.R.drawable.ic_dialog_map);
				dialogBuilder.setTitle("Successfully Found Shortest Route!");
				dialogBuilder.setMessage(String.format("Shortest route to destination point is displayed on the background.\n\n" +
						"Distance: %.2f meters\n" +
						"Duration: %.2f seconds\n" +
						"ETA: %s", routeDistance, routeDuration, eta));
				Double finalRouteDistance = routeDistance;
				Double finalRouteDuration = routeDuration;
				dialogBuilder.setPositiveButton("Start!", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						isNavigating = true;
						searchBtn.setVisibility(View.INVISIBLE);
						cancelNavBtn.setVisibility(View.VISIBLE);
						navRemainingDistance = finalRouteDistance;
						navRemainingTime = finalRouteDuration;
						MarkerOptions markerOptions = new MarkerOptions()
								.position(destinationLocation.getLocation())
								.indoor(destinationLocation.getIndoorMapId(), destinationLocation.getFloor())
								.iconKey("dir_enter_map");
						nav_markers.add(m_eegeoMap.addMarker(markerOptions));
						recalculationDemoFix(queueRoutes);
						locationQueue.addAll(queueRoutes);
						navDirectionQueue.addAll(navDirQueueDirections);
						nextStepInfo = navDirectionQueue.remove();
						finalNavBearing = navDirQueueDirections.get(navDirQueueDirections.size() - 1).getDirectionBearingAfter();
						userDirection = nextStepInfo.getDirectionBearingBefore() - 180;
						navigationHelper1.setVisibility(View.VISIBLE);
						navigationHelper2.setVisibility(View.VISIBLE);
						navHelperSideText.setText(navHelperSideTextBuilder(navRemainingDistance, navRemainingTime));
						String upNext = navHelperUpNextTextBuilder(nextStepInfo);
						navHelperUpNextIcon.setImageDrawable(navHelperUpNextDrawableBuilder(getActivity(), upNext));
						upNext = !upNext.equals("UP NEXT:\nARRIVE DESTINATION") ?
								upNext + String.format(" in %.2f m", nextStepInfo.getStepDistance()) : upNext + " AHEAD";
						navHelperUpNextText.setText(upNext);
						dialog.dismiss();
					}
				});
				dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						isNavigating = false;
						destinationLocation = null;
						for (Marker marker : nav_markers) {
							m_eegeoMap.removeMarker(marker);
						}
						for (RouteView routeView : m_routeViews) {
							routeView.removeFromMap();
						}
						dialog.dismiss();
					}
				});
				dialogBuilder.show().show();
			}
		} else {
			Toast.makeText(getActivity(), "Failed to find routes to destination point!", Toast.LENGTH_LONG).show();
		}
	}

	private void recalculatingTheRoute(List<Route> results) {
		recalcOngoing = true;
		Double routeDuration = .0, routeDistance = .0;
		ArrayList<Location> queueRoutes = new ArrayList<>();
		ArrayList<StepInfo> navDirQueueDirections = new ArrayList<>();
		for (Route route : results) {
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
						if (path == routeStep.path.get(routeStep.path.size() - 1) &&
								routeStep == routeSection.steps.get(routeSection.steps.size() - 1) &&
								routeSection == route.sections.get(route.sections.size() - 1) &&
								route == results.get(results.size() - 1)) {
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
					navDirQueueDirections.add(new StepInfo(routeStep.directions.type,
							routeStep.directions.modifier,
							routeStep.directions.location,
							routeStep.directions.bearingBefore,
							routeStep.directions.bearingAfter,
							routeStep.duration,
							routeStep.distance));
				}
			}
		}
		routeDuration = routeDistance / PERSON_WALKING_SPEED;
		isNavigating = true;
		navRemainingDistance = routeDistance;
		navRemainingTime = routeDuration;
		MarkerOptions markerOptions = new MarkerOptions()
				.position(destinationLocation.getLocation())
				.indoor(destinationLocation.getIndoorMapId(), destinationLocation.getFloor())
				.iconKey("dir_enter_map");
		nav_markers.add(m_eegeoMap.addMarker(markerOptions));

		if (incorrectDemo1Activated) {
			incorrectDemo1Activated = false;
		}
		else if (incorrectDemo2Activated) {
			incorrectDemo2Activated = false;
			recalculationDemoFix(queueRoutes);
		}
		else if (incorrectDemo3Activated) {
			incorrectDemo3Activated = false;
		}

		locationQueue.addAll(queueRoutes);
		navDirectionQueue.addAll(navDirQueueDirections);
		nextStepInfo = navDirectionQueue.remove();
		finalNavBearing = navDirQueueDirections.get(navDirQueueDirections.size() - 1).getDirectionBearingAfter();
		userDirection = nextStepInfo.getDirectionBearingBefore() - 180;
		navigationHelper1.setVisibility(View.VISIBLE);
		navigationHelper2.setVisibility(View.VISIBLE);
		navHelperSideText.setText(navHelperSideTextBuilder(navRemainingDistance, navRemainingTime));
		String upNext = navHelperUpNextTextBuilder(nextStepInfo);
		navHelperUpNextIcon.setImageDrawable(navHelperUpNextDrawableBuilder(getActivity(), upNext));
		upNext = !upNext.equals("UP NEXT:\nARRIVE DESTINATION") ?
				upNext + String.format(" in %.2f m", nextStepInfo.getStepDistance()) : upNext + " AHEAD";
		navHelperUpNextText.setText(upNext);
	}

	private ArrayList<Location> recalculationDemoFix(ArrayList<Location> queueRoutes) {
		if (incorrectDemo1Activated) {
			// z-103
			queueRoutes.subList(13, queueRoutes.size()).clear();
			queueRoutes.add(new Location("Inc Demo-1 path", "path", new LatLng(39.891833, 32.783265), .000011, .000011, 0, demoIndoorMapId));
			queueRoutes.add(new Location("Inc Demo-1 stop", "stop", new LatLng(39.891835, 32.783302), .000011, .000011, 0, demoIndoorMapId));
			return queueRoutes;
		} else if (incorrectDemo2Activated) {
			// a-306
			queueRoutes.subList(35, queueRoutes.size()).clear();
			queueRoutes.add(new Location("Inc Demo-2 path", "path", new LatLng(39.891836, 32.783302), .000011, .000011, 6, demoIndoorMapId));
			queueRoutes.add(new Location("Inc Demo-2 stop", "stop", new LatLng(39.891843, 32.783386), .000011, .000011, 6, demoIndoorMapId));
			return queueRoutes;
		} else if (incorrectDemo3Activated) {
			// a-306 again
			queueRoutes.subList(4, queueRoutes.size()).clear();
			queueRoutes.add(new Location("Inc Demo-3 path", "path", new LatLng(39.891825, 32.783187), .000011, .000011, 6, demoIndoorMapId));
			queueRoutes.add(new Location("Inc Demo-3 stop", "stop", new LatLng(39.891820, 32.783142), .000011, .000011, 6, demoIndoorMapId));
			return queueRoutes;
		} else {
			return queueRoutes;
		}
	}

	private void displayShareSaveDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setIcon(R.drawable.nav_locs);
		alertDialogBuilder.setTitle("Location Pinned");
		alertDialogBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				List<String> allLocationTypes = locationService.getAllLocationTypes();
				LinearLayout relativeLayout = buildEditTextAndAutoTextLayout(getContext(),
						"Name of your location", "Type of your location", allLocationTypes);
				InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				inputMethodManager.toggleSoftInputFromWindow(root.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);

				AlertDialog.Builder innerDialogBuilder = new AlertDialog.Builder(getActivity());
				innerDialogBuilder.setIcon(android.R.drawable.ic_menu_save);
				innerDialogBuilder.setTitle("Save this location");
				innerDialogBuilder.setView(relativeLayout);
				innerDialogBuilder.setPositiveButton("Save!", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(relativeLayout.getWindowToken(), 0);
						String locName = ((EditText) relativeLayout.getChildAt(0)).getText().toString();
						String locType = ((AutoCompleteTextView) relativeLayout.getChildAt(1)).getText().toString();
						allLocations.add(new Location(locName, locType, userLocation.getLocation(),
								userLocation.getLocEpsLat(), userLocation.getLocEpsLong(),
								userLocation.getFloor(), userLocation.getIndoorMapId()));
						// TODO: also send to server..
						Toast.makeText(getActivity(), "Your Current Location is Saved Successfully", Toast.LENGTH_LONG).show();
						dialog.dismiss();
					}
				});
				innerDialogBuilder.setNegativeButton("Cancel!", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(relativeLayout.getWindowToken(), 0);
						dialog.dismiss();
					}
				});
				innerDialogBuilder.show().show();
				dialog.dismiss();
			}
		});
		alertDialogBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alertDialogBuilder.setNegativeButton("Share", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("text/plain");
				share.putExtra(Intent.EXTRA_TEXT, packLocationDataToSend(userLocation));
				startActivity(Intent.createChooser(share, "Share My Location!"));
				dialog.dismiss();
			}
		});
		alertDialogBuilder.show().show();
	}

	private void displayCancelNavigationDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
		alertDialogBuilder.setIcon(R.drawable.nav_cancel);
		alertDialogBuilder.setTitle("Cancel Navigation?");
		alertDialogBuilder.setPositiveButton("Yes, Please", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
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
				cancelNavBtn.setVisibility(View.INVISIBLE);
				searchBtn.setVisibility(View.VISIBLE);
				Toast.makeText(getContext(), "Cancelled your current navigation", Toast.LENGTH_LONG).show();
				dialog.dismiss();
			}
		});
		alertDialogBuilder.setNeutralButton("No, Go Back", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alertDialogBuilder.show().show();
	}

	/**
	 * Begin an operation to precache a spherical area of the map. This allows that area to load faster in future.
	 *
	 * @param location
	 */
	public void cacheCurrentCameraLocation(LatLng location) {
		if (!cacheLocationSet.contains(location)) {
			cacheLocationSet.add(location);
			this.preCacheLocation = location;
			// Precache a 3000 meter radius around this point
			m_eegeoMap.precache(
					location,
					3000.0,
					precacheOperationCompletedListener);
		}
	}

	@Override
	public void onPrecacheOperationCompleted(PrecacheOperationResult precacheOperationResult) {
		if (mapNavigationViewModel.getCachingToastActivated()) {
			String toastMessage;
			if (precacheOperationResult.succeeded()) {
				toastMessage = String.format("Successfully cached a radius of 3km around (%.2f, %.2f).\n" +
								"Now this area will load faster than ever!",
						preCacheLocation.latitude, preCacheLocation.longitude);
			} else {
				toastMessage = String.format("Could not cache 3km radius around (%.2f, %.2f)!",
						preCacheLocation.latitude, preCacheLocation.longitude);
			}
			Toast.makeText(getContext(), toastMessage, Toast.LENGTH_LONG).show();
		}
	}

	private class MarkerClickListenerImpl implements OnMarkerClickListener {
		public void onMarkerClick(Marker marker) {
			if (marker.getTitle() != "You Are Here!") {
				Context context = MapNavigationFragment.this.getActivity();
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
				dialogBuilder.setIcon(android.R.drawable.ic_dialog_map);
				dialogBuilder.setTitle(String.format("Do you want to navigate to %s?", marker.getTitle()));
				dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						for (Marker remMarker : see_on_map_markers) {
							m_eegeoMap.removeMarker(remMarker);
						}
						requestNavigationForLocation(new Location(marker.getTitle(), "Destination", marker.getPosition(),
								.0, .0, marker.getIndoorFloorId(), marker.getIndoorMapId()));
						dialog.dismiss();
					}
				});
				dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
				dialogBuilder.show().show();
			}
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
			for (Marker remMarker : see_on_map_markers) {
				m_eegeoMap.removeMarker(remMarker);
			}
		}
		m_mapView.onDestroy();
	}
}