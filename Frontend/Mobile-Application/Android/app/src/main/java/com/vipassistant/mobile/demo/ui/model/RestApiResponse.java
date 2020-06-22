package com.vipassistant.mobile.demo.ui.model;

public class RestApiResponse {
	private Object data;
	private String message;
	private String result;

	public RestApiResponse() {
	}

	public RestApiResponse(Object data, String message, String result) {
		this.data = data;
		this.message = message;
		this.result = result;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
}
