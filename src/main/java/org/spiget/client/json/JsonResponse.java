package org.spiget.client.json;

import com.google.gson.JsonElement;

public class JsonResponse {

	public int code;
	public JsonElement json;

	public JsonResponse(int code, JsonElement json) {
		this.code = code;
		this.json = json;
	}
}
