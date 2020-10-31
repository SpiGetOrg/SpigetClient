package org.spiget.client;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Log4j2
public class PuppeteerClient2 extends SpigetClient {

    public static List<String> HOSTS = new ArrayList<>();
    public static Gson GSON = new Gson();

    static String getHost() {
        return HOSTS.get(ThreadLocalRandom.current().nextInt(HOSTS.size()));
    }

    public static SpigetResponse get(String url) throws IOException, InterruptedException {
        String fullUrl = getHost() + "/" + URLEncoder.encode(url, StandardCharsets.UTF_8.name());
        log.info(fullUrl);
        Connection connection = Jsoup.connect(fullUrl).method(Connection.Method.GET);
        connection.followRedirects(true);
        connection.ignoreHttpErrors(true);
        connection.ignoreContentType(true);
        connection.timeout(5000);

        Connection.Response response = connection.execute();
        JsonObject json = GSON.fromJson(response.body(), JsonObject.class);
        String url1 = json.get("url").getAsString();
        if (!Objects.equals(url, url1)) {
            log.warn("Requested URL didn't match returned URL (" + url + " != " + url1 + ")");
            return new SpigetResponse(new HashMap<>(), null, 500);
        }
        int status = json.get("status").getAsInt();
        String encodedContent = json.get("content").getAsString();
        String content = new String(Base64.getDecoder().decode(encodedContent.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);

        return new SpigetResponse(cookies, Jsoup.parse(content), status);
    }

    static HashMap<String, String> parseCookies(String jsonString) {
        HashMap<String, String> map = new HashMap<>();
        try {
            JsonObject cookieJson = new JsonParser().parse(jsonString).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : cookieJson.entrySet()) {
                map.put(entry.getKey(), entry.getValue().getAsString());
            }
        } catch (Exception e) {
            log.log(Level.WARN, e);
        }
        return map;
    }

}
