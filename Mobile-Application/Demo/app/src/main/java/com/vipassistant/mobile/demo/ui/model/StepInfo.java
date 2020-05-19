package com.vipassistant.mobile.demo.ui.model;

import com.eegeo.mapapi.geometry.LatLng;
import com.eegeo.mapapi.services.routing.RouteDirections;

public class StepInfo {
	private String directionType;
	private String directionModifier;
	private LatLng directionLocation;
	private Double directionBearingBefore;
	private Double directionBearingAfter;
	private Double stepDuration;
	private Double stepDistance;

	public StepInfo() {
	}

	public StepInfo(String directionType,
					String directionModifier,
					LatLng directionLocation,
					Double directionBearingBefore,
					Double directionBearingAfter,
					Double stepDuration,
					Double stepDistance) {
		this.directionType = directionType;
		this.directionModifier = directionModifier;
		this.directionLocation = directionLocation;
		this.directionBearingBefore = directionBearingBefore;
		this.directionBearingAfter = directionBearingAfter;
		this.stepDuration = stepDuration;
		this.stepDistance = stepDistance;
	}

	public String getDirectionType() {
		return directionType;
	}

	public void setDirectionType(String directionType) {
		this.directionType = directionType;
	}

	public String getDirectionModifier() {
		return directionModifier;
	}

	public void setDirectionModifier(String directionModifier) {
		this.directionModifier = directionModifier;
	}

	public LatLng getDirectionLocation() {
		return directionLocation;
	}

	public void setDirectionLocation(LatLng directionLocation) {
		this.directionLocation = directionLocation;
	}

	public Double getDirectionBearingBefore() {
		return directionBearingBefore;
	}

	public void setDirectionBearingBefore(Double directionBearingBefore) {
		this.directionBearingBefore = directionBearingBefore;
	}

	public Double getDirectionBearingAfter() {
		return directionBearingAfter;
	}

	public void setDirectionBearingAfter(Double directionBearingAfter) {
		this.directionBearingAfter = directionBearingAfter;
	}

	public Double getStepDuration() {
		return stepDuration;
	}

	public void setStepDuration(Double stepDuration) {
		this.stepDuration = stepDuration;
	}

	public Double getStepDistance() {
		return stepDistance;
	}

	public void setStepDistance(Double stepDistance) {
		this.stepDistance = stepDistance;
	}
}
