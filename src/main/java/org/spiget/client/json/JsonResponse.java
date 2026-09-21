package org.spiget.client.json;

import com.google.gson.JsonElement;

public class JsonResponse {

	public int code;
	/**
	 * Parsed body, or <code>null</code> if the body was empty or not valid json (e.g. an error page)
	 */
	public JsonElement json;

	public JsonResponse(int code, JsonElement json) {
		this.code = code;
		this.json = json;
	}
}
