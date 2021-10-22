package org.spiget.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.sentry.Sentry;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.inventivetalent.metrics.Metric;
import org.inventivetalent.metrics.Metrics;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public abstract class SpigetClient {

	public static JsonObject config;

	public static final String BASE_URL = "https://www.spigotmc.org/";
	public static String userAgent;

	public static Metrics metrics;
	public static Metric requestsMetric;
	public static String project = "default";

	public static boolean             bypassCloudflare = true;
	public static Map<String, String> cookies          = new HashMap<>();

	public static void initMetrics() {
		if (metrics != null) {
			requestsMetric = metrics.metric("spiget", "spigot_requests");
		}
	}

	public static SpigetResponse get(String url) throws IOException, InterruptedException {
		if (requestsMetric == null) {
			initMetrics();
		}
		if (config.get("debug.connections").getAsBoolean()) {
			log.debug("GET " + url);
		}
		if (config.has("request.delay")) {
			Thread.sleep(config.get("request.delay").getAsInt());
		}
//		SpigetResponse response = PuppeteerClient2.get(url);
//		userAgent = PuppeteerClient.getUserAgent();
//		cookies.clear();
//		cookies.putAll(response.getCookies());
//		return response;
		if (bypassCloudflare) {
			return HtmlUnitClient.get(url);
		} else {
			return JsoupClient.get(url);
		}
	}

	public static SpigetDownload download(String url) throws IOException, InterruptedException {
		if (config.get("debug.connections").getAsBoolean()) {
			log.debug("DOWNLOAD " + url);
		}
		if (config.has("request.delay")) {
			Thread.sleep(config.get("request.delay").getAsInt());
		}
		return HtmlUnitClient.download(url);
	}

	public static void loadCookiesFromFile() throws IOException {
		try {
			JsonObject cookieJson = new JsonParser().parse(new FileReader(config.get("request.cookieFile").getAsString())).getAsJsonObject();
			for (Map.Entry<String, JsonElement> entry : cookieJson.entrySet()) {
				cookies.put(entry.getKey(), entry.getValue().getAsString());
			}
		} catch (Exception e) {
			Sentry.captureException(e);
			log.log(Level.WARN, e);
		}
	}

	public static void saveCookiesToFile() throws IOException {
		JsonObject cookieJson = new JsonObject();
		Map.Entry<String, String> lastCfChlCookie = null;
		for (Map.Entry<String, String> entry : cookies.entrySet()) {
			if (entry.getKey().startsWith("cf_chl_seq_")) {
				lastCfChlCookie = entry;
			} else {
				cookieJson.addProperty(entry.getKey(), entry.getValue());
			}
		}
		if (lastCfChlCookie != null) {
			cookieJson.addProperty(lastCfChlCookie.getKey(), lastCfChlCookie.getValue());
		}
		try (Writer writer = new FileWriter(config.get("request.cookieFile").getAsString())) {
			new Gson().toJson(cookieJson, writer);
		}
	}

}
