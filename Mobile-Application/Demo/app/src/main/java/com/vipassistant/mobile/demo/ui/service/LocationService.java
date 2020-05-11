package com.vipassistant.mobile.demo.ui.service;

import com.eegeo.mapapi.geometry.LatLng;

import java.util.HashMap;
import java.util.Map;

public class LocationService {
	/* We keep Location Information in our custom Indoor Map in 3 different containers
	 * First one is a HashTable of locations (locationName:location) for free map queries
	 * Second one is a HashTable of HashTables (locationType:location) for "Find Me A.." types of queries
	 * And the third one is a HashTable of HashTables (floor:location) again for "Report My Surroundings" types of queries
	 */
	private final Map<String, LatLng> allLocations;
	private final Map<String, HashMap<String, LatLng>> typedLocations;
	private final Map<Integer, HashMap<String, LatLng>> floorLocations;

	public LocationService(HashMap<String, LatLng> allLocations,
						   HashMap<String, HashMap<String, LatLng>> typedLocations,
						   HashMap<Integer, HashMap<String, LatLng>> floorLocations) {
		this.allLocations = allLocations;
		this.typedLocations = typedLocations;
		this.floorLocations = floorLocations;
	}

	public Map<String, LatLng> getAllLocations() {
		return allLocations;
	}

	public Map<String, HashMap<String, LatLng>> getTypedLocations() {
		return typedLocations;
	}

	public Map<Integer, HashMap<String, LatLng>> getFloorLocations() {
		return floorLocations;
	}
}
