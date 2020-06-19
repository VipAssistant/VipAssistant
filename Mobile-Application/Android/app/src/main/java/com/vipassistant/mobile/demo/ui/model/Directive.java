package com.vipassistant.mobile.demo.ui.model;

public class Directive {
	private String stringToOutput;
	private Integer durationInMillis;

	public Directive() {
	}

	public Directive(String stringToOutput, Integer durationInMillis) {
		this.stringToOutput = stringToOutput;
		this.durationInMillis = durationInMillis;
	}

	public String getStringToOutput() {
		return stringToOutput;
	}

	public void setStringToOutput(String stringToOutput) {
		this.stringToOutput = stringToOutput;
	}

	public Integer getDurationInMillis() {
		return durationInMillis;
	}

	public void setDurationInMillis(Integer durationInMillis) {
		this.durationInMillis = durationInMillis;
	}
}
