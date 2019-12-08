package org.spiget.client;

import com.google.gson.JsonObject;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ClientTest {

	public ClientTest() {
		SpigetClient.config = new JsonObject();
		SpigetClient.userAgent = "Spiget-v2-Test GoogleBot";
		SpigetClient.config.addProperty("request.userAgent", "Spiget-v2-Test GoogleBot");
		SpigetClient.config.addProperty("debug.connections", false);
	}

	@Test
	public void rootRequestTest() throws IOException, InterruptedException {
		SpigetResponse response = SpigetClient.get("https://spigotmc.org");
		assertEquals(200, response.code);
		System.out.println(response.document);
	}

}
