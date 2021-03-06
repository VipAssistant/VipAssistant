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
	private List<Location> worldLocations;

	public LocationService(List<Location> allLocations,
						   List<Location> worldLocations) {
		this.allLocations = allLocations;
		this.worldLocations = worldLocations;
	}

	public List<Location> getAllLocations() {
		return allLocations;
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public List<String> getAllIndoorLocationNames() {
		return convertToLocationNames(allLocations);
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public List<String> getAllOutdoorLocationNames() {
		return convertToLocationNames(worldLocations);
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public List<String> getAllLocationTypes() {
		return convertToLocationTypes(allLocations);
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public List<String> convertToLocationNames(List<Location> locations) {
		return locations.stream()
				.map(location -> location.getName())
				.collect(Collectors.toList());
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public List<String> convertToLocationTypes(List<Location> locations) {
		Set<String> locationTypes = locations.stream()
					.map(location -> location.getType())
					.collect(Collectors.toSet());
		return new ArrayList<>(locationTypes);
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public Optional<Location> getLocation(LatLng location) {
		return allLocations.stream()
				.filter(loc -> loc.getLocation().equals(location))
				.findAny();
	}

	/**
	 * Name is unique for each location inside the map
	 * @param name
	 * @return
	 */
	@RequiresApi(api = Build.VERSION_CODES.N)
	public Optional<Location> findByIndoorName(String name) {
		return allLocations.stream()
					.filter(loc -> loc.getName().equalsIgnoreCase(name))
					.findFirst();
	}

	/**
	 * Name is unique for each location inside the map
	 * @param name
	 * @return
	 */
	@RequiresApi(api = Build.VERSION_CODES.N)
	public Optional<String> findByIndoorNameVoice(String name) {
		return allLocations.stream()
				.map(loc -> loc.getName().toLowerCase())
				.filter(loc -> loc.contains(name))
				.findFirst();
	}

	/**
	 * Name is unique for each location inside the map
	 * @param name
	 * @return
	 */
	@RequiresApi(api = Build.VERSION_CODES.N)
	public Optional<Location> findByOutdoorName(String name) {
		return worldLocations.stream()
				.filter(loc -> loc.getName().equals(name))
				.findFirst();
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public List<Location> findByType(String type) {
		return allLocations.stream()
					.filter(loc -> loc.getType().equals(type))
					.collect(Collectors.toList());
	}

	/**
	 * Name is unique for each location inside the map
	 * @param name
	 * @return
	 */
	@RequiresApi(api = Build.VERSION_CODES.N)
	public List<Location> findByTypeVoice(String name) {
		return allLocations.stream()
				.filter(loc -> loc.getType().toLowerCase().contains(name))
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
