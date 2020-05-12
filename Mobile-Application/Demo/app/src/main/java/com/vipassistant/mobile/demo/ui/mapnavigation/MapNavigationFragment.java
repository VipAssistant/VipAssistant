package com.vipassistant.mobile.demo.ui.mapnavigation;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
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
import com.eegeo.mapapi.services.mapscene.MapsceneRequestOptions;
import com.eegeo.mapapi.services.mapscene.MapsceneRequestResponse;
import com.eegeo.mapapi.services.mapscene.MapsceneService;
import com.eegeo.mapapi.services.mapscene.OnMapsceneRequestCompletedListener;
import com.eegeo.mapapi.services.routing.*;
import com.eegeo.mapapi.widgets.RouteView;
import com.eegeo.mapapi.widgets.RouteViewOptions;
import com.vipassistant.mobile.demo.R;
import com.vipassistant.mobile.demo.ui.model.Location;
import com.vipassistant.mobile.demo.ui.service.LocationService;
import com.vipassistant.mobile.demo.ui.utils.AutoCompleteArrayAdapter;

import java.util.*;

import static com.vipassistant.mobile.demo.ui.constants.Constants.*;

public class MapNavigationFragment extends Fragment implements OnMapsceneRequestCompletedListener, OnRoutingQueryCompletedListener {
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
	private Location userLocation;
	private Queue<Location> locationQueue = new LinkedList<>(); // For demo purposes
	private final OnMapsceneRequestCompletedListener mapSceneRequestCompletedListener = this;
	private final OnRoutingQueryCompletedListener routingQueryCompletedListener = this;
	private final OnMarkerClickListener m_markerTappedListener = new MarkerClickListenerImpl();

	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mapNavigationViewModel = ViewModelProviders.of(this).get(MapNavigationViewModel.class);
		root = inflater.inflate(R.layout.fragment_map_nav, container, false);
		ProgressDialog mapLoadingDialog = buildLoadingDialog(getActivity(), "Loading Map Data...");
		mapLoadingDialog.show();

		/* Initialize Map */
		EegeoApi.init(getActivity(), getString(R.string.eegeo_api_key));
		m_mapView = (MapView) root.findViewById(R.id.mapView);
		m_mapView.onCreate(savedInstanceState);

		/* Initialize LocationService */
		this.locationService = new LocationService(allLocations);


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
						mapLoadingDialog.dismiss();
						initializeLocation();
					}
				});

				RelativeLayout uiContainer = (RelativeLayout) root.findViewById(R.id.eegeo_ui_container);
				m_interiorView = new IndoorMapView(m_mapView, uiContainer, m_eegeoMap);

				Button findMeBtn = (Button) root.findViewById(R.id.findMeButton);
				findMeBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						centerCurrentLocation();
					}
				});

				Button searchBtn = (Button) root.findViewById(R.id.searchButton);
				searchBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						displaySearchDialog();
					}
				});

				Button infoBtn = (Button) root.findViewById(R.id.infoButton);
				infoBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
//						displayInfoDialog(); todo
					}
				});

				Button shareSaveBtn = (Button) root.findViewById(R.id.shareAndSaveButton);
				shareSaveBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
//						displayShareSaveDialog(); todo dialogta eski iconlarini kullan
					}
				});
			}
		});
		return root;
	}

	private void initializeLocation() {
		/* Initialize Location Queue first with indoor map entrance Location */
		locationQueue.add(demoIndoorMapEntrance);
		/* Then initialize related variables */
		this.userLocation = computeCurrentLocation();
		this.outNavigationMarker = m_eegeoMap.addMarker(new MarkerOptions().position(userLocation.getLocation()).labelText(markerText));
		this.m_bluesphere = m_eegeoMap.getBlueSphere();
		this.m_bluesphere.setEnabled(true);
		this.m_bluesphere.setPosition(userLocation.getLocation());
		this.m_bluesphere.setIndoorMap(userLocation.getIndoorMapId(), userLocation.getFloor());
		this.m_bluesphere.setBearing(180); // TODO DIRECTION

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
		this.m_bluesphere.setBearing(180); // TODO DIRECTION get from newlocation..
	}

	/**
	 * Method that is called periodically to update Map Fragment
	 * updates user location etc.
	 * updates existing routes ? !!!!!!
	 * direction
	 * updates geoloc nav helper etc?
	 */
	private void updateMapPeriodically() {
		updateLocation(computeCurrentLocation());
		// TODO USER MARKER I TAKIP ET
		// TODO nav helper - finish navigation and stuff.. if queue len = 1 and routeviews exist
	}

	private void centerCurrentLocation() {
		CameraPosition position = new CameraPosition.Builder()
				.target(userLocation.getLocation())
				.indoor(userLocation.getIndoorMapId(), userLocation.getFloor())
				.zoom(19)
				.bearing(270)
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
		alertDialogBuilder.setPositiveButton("Find Me A ...!", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				List<String> allLocationTypes = locationService.getAllLocationTypes();
				AlertDialog.Builder innerDialogBuilder = new AlertDialog.Builder(getActivity());
				innerDialogBuilder.setIcon(android.R.drawable.ic_menu_search);
				innerDialogBuilder.setTitle("What do you want us to find for you?");
				AutoCompleteArrayAdapter locNamesAdapter = new AutoCompleteArrayAdapter(getContext(), android.R.layout.simple_dropdown_item_1line, allLocationTypes);
				AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(getContext());
				autoCompleteTextView.setThreshold(1);
				autoCompleteTextView.setAdapter(locNamesAdapter);
				innerDialogBuilder.setView(autoCompleteTextView);
				innerDialogBuilder.setPositiveButton("Find!", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String locType = autoCompleteTextView.getText().toString();
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
										if (location.getIndoorMapId() != null) {
											m_eegeoMap.addMarker(new MarkerOptions() // todo styling
													.position(location.getLocation())
													.indoor(location.getIndoorMapId(), location.getFloor())
													.labelText(location.getName()));
										} else {
											m_eegeoMap.addMarker(new MarkerOptions()
													.position(location.getLocation())
													.labelText(location.getName()));
										}
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
				AlertDialog.Builder innerDialogBuilder = new AlertDialog.Builder(getActivity());
				innerDialogBuilder.setIcon(android.R.drawable.ic_menu_search);
				innerDialogBuilder.setTitle("Where do you want to go?");
				AutoCompleteArrayAdapter locNamesAdapter = new AutoCompleteArrayAdapter(getContext(), android.R.layout.simple_dropdown_item_1line, allLocationNames);
				AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(getContext());
				autoCompleteTextView.setThreshold(1);
				autoCompleteTextView.setAdapter(locNamesAdapter);
				innerDialogBuilder.setView(autoCompleteTextView);
				innerDialogBuilder.setPositiveButton("Get Directions", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String locName = autoCompleteTextView.getText().toString();
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

	/**
	 * Method that requests routes via RoutingService
	 * Its results are utilized in onRoutingQueryCompleted() since this class implements routingQueryCompletedListener
	 *
	 * @param location
	 */
	private void requestNavigationForLocation(Location location) {
		routingService.findRoutes(new RoutingQueryOptions()
				.addIndoorWaypoint(userLocation.getLocation(), userLocation.getFloor())
				.addIndoorWaypoint(location.getLocation(), location.getFloor())
				.onRoutingQueryCompletedListener(routingQueryCompletedListener));
	}

	@Override
	public void onMapsceneRequestCompleted(MapsceneRequestResponse response) {
		if (response.succeeded()) {
			String message = "Mapscene '" + response.getMapscene().name + "' loaded"; // TODO: may remove later.
			Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(getActivity(), "Failed to load mapscene", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onRoutingQueryCompleted(RoutingQuery query, RoutingQueryResponse response) {
		if (response.succeeded()) {
			Double routeDuration = .0, routeDistance = .0;
			ArrayList<Location> queueRoutes = new ArrayList<>();
			for (Route route : response.getResults()) {
				routeDistance += route.distance;
				routeDuration += route.duration;
				RouteViewOptions options = new RouteViewOptions() // todo styling etc
						.color(Color.argb(128, 255, 0, 0))
						.width(8.0f);
				RouteView routeView = new RouteView(m_eegeoMap, route, options);
				m_routeViews.add(routeView);
				for (RouteSection routeSection : route.sections) {
					for (RouteStep routeStep : routeSection.steps) {
						int floor = routeStep.indoorFloorId;
						String indoorId = routeStep.indoorId;
						for (LatLng path : routeStep.path) {
							queueRoutes.add(new Location("path", "path", path, .0, .0, floor, indoorId));
						}
					}
				}
			}
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
			dialogBuilder.setIcon(android.R.drawable.ic_dialog_map);
			dialogBuilder.setTitle("Successfully Found Shortest Route!");
			dialogBuilder.setMessage(String.format("Shortest route to destination point is displayed on the background.\n\n" +
					"Distance: %f meters\n" +
					"ETA: %f seconds", routeDistance, routeDuration));
			dialogBuilder.setPositiveButton("Start!", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					locationQueue.addAll(queueRoutes);
					// TODO: DISPLAY NAV HELPER...
					dialog.dismiss();
				}
			});
			dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			dialogBuilder.show().show();
		} else {
			Toast.makeText(getActivity(), "Failed to find routes to destination point!", Toast.LENGTH_LONG).show();
		}
	}

	private class MarkerClickListenerImpl implements OnMarkerClickListener {
		public void onMarkerClick(Marker marker) {
			if (marker.getTitle() != "You Are Here!") {
				Context context = MapNavigationFragment.this.getActivity();
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
				dialogBuilder.setIcon(android.R.drawable.ic_dialog_map);
				dialogBuilder.setTitle("Do you want to navigate to this location?");
				dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						requestNavigationForLocation(new Location("dest", "dest", marker.getPosition(),
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
		}

		m_mapView.onDestroy();
	}
}