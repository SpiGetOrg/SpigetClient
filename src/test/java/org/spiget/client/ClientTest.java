package org.spiget.client;

import com.google.gson.JsonObject;
import org.junit.Test;
import org.spiget.client.json.JsonClient;
import org.spiget.client.json.JsonResponse;

import java.io.IOException;

import static org.junit.Assert.*;

public class ClientTest {

	public ClientTest() {
		SpigetClient.config = new JsonObject();
		SpigetClient.userAgent = "Mozilla/5.0 (compatible; TotallyNot5p1g3t; +https://yeleha.co/not5p1g3t)";
		SpigetClient.config.addProperty("request.userAgent", "Spiget-v2-Test GoogleBot");
		SpigetClient.config.addProperty("debug.connections", false);

		JsonClient.userAgent = "Mozilla/5.0 (compatible; TotallyNot5p1g3t; +https://yeleha.co/not5p1g3t)";
	}

	@Test
	public void rootRequestTest() throws IOException, InterruptedException {
		SpigetResponse response = SpigetClient.get("https://spigotmc.org");
		assertEquals(200, response.code);
		System.out.println(response.document);
	}

	@Test
	public void jsonRequestTest() throws IOException, InterruptedException {
		JsonResponse response = JsonClient.get("https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=2");
		assertNotNull(response);
		assertEquals(200, response.code);
		assertNotNull(response.json);
		assertTrue(response.json.isJsonObject());
	}

}
