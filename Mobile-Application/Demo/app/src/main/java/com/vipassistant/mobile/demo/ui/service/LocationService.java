package com.vipassistant.mobile.demo.ui.service;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.eegeo.mapapi.geometry.LatLng;
import com.vipassistant.mobile.demo.ui.constants.Constants;
import com.vipassistant.mobile.demo.ui.model.Location;

import java.util.*;
import java.util.stream.Collectors;

import static com.vipassistant.mobile.demo.ui.constants.Constants.locationLatEps;
import static com.vipassistant.mobile.demo.ui.constants.Constants.locationLongEps;

public class LocationService {
	/* We keep Location Information in our custom Indoor Map in 1 container
	 * namely in an ArrayList and perform all our queries with stream operations on that list */
	private final List<Location> allLocations;

	public LocationService(ArrayList<Location> allLocations) {
		this.allLocations = allLocations;
	}

	public List<Location> getAllLocations() {
		return allLocations;
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public Optional<Location> getLocation(LatLng location) {
		return allLocations.stream()
				.filter(loc -> loc.getLocation().equals(location))
				.findAny();
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public List<Location> findByName(String name) {
		return allLocations.stream()
					.filter(loc -> loc.getName().equals(name))
					.collect(Collectors.toList());
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public List<Location> findByType(String type) {
		return allLocations.stream()
					.filter(loc -> loc.getType().equals(type))
					.collect(Collectors.toList());
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public List<Location> findByFloorAndLocation(Integer floor, LatLng location) {
		return allLocations.stream()
					.filter(loc -> loc.getFloor().equals(floor))
					.filter(loc -> Math.abs(loc.getLocation().latitude + loc.getLocEpsLat() - location.latitude) <= locationLatEps &&
								   Math.abs(loc.getLocation().longitude + loc.getLocEpsLong() - location.longitude) <= locationLongEps)
					.collect(Collectors.toList());
	}
}
