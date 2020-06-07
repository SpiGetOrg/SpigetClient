package org.spiget.client;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;

@Log4j2
public class HtmlUnitClient extends SpigetClient {

	static final String COOKIE_HOST        = ".spigotmc.org";
	static final long   CLOUDFLARE_TIMEOUT = 5100;

	protected static WebClient webClient;

	protected static WebClient getClient() {
		if (webClient != null) { return webClient; }
		webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getBrowserVersion().setUserAgent(userAgent);

		// Javascript to pass cloudflare's security challenge
		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.setJavaScriptTimeout(7000);

		webClient.getOptions().setTimeout(6000);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setRedirectEnabled(true);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		webClient.getOptions().setUseInsecureSSL(true);
		webClient.getOptions().setPopupBlockerEnabled(false);
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);

		return webClient;
	}

	public static void disposeClient() {
		if (webClient != null) { webClient.close(); }
		webClient = null;
	}

	public static SpigetResponse get(String url) throws IOException, InterruptedException {
		WebClient client = getClient();
		WebRequest request = new WebRequest(new URL(url), HttpMethod.GET);

		Page page = getPage(client, request);
		if (page instanceof HtmlPage) {
			HtmlPage htmlPage = (HtmlPage) page;
			page = waitForCloudflare(client, request, page, htmlPage.asXml());
			storeCookies(client);

			if (page instanceof HtmlPage) {
				return new SpigetResponse(cookies, Jsoup.parse(((HtmlPage) page).asXml()), page.getWebResponse().getStatusCode());
			}
		}

		return null;
	}

	public static SpigetDownload download(String url) throws IOException, InterruptedException {
		WebClient client = getClient();
		WebRequest request = new WebRequest(new URL(url), HttpMethod.GET);

		Page page = getPage(client, request);
		if (page instanceof HtmlPage) {
			HtmlPage htmlPage = (HtmlPage) page;
			page = waitForCloudflare(client, request, page, htmlPage.asXml());
			storeCookies(client);
		}

		Page enclosedPage = client.getCurrentWindow().getEnclosedPage();
		return new SpigetDownload(enclosedPage.getUrl().toString(), enclosedPage.getWebResponse().getContentAsStream(), page.getWebResponse().getStatusCode(), !(page instanceof HtmlPage));
	}

	static void applyCookies(WebClient client) {
		client.getCookieManager().clearCookies();
		for (Map.Entry<String, String> cookie : cookies.entrySet()) {
			client.getCookieManager().addCookie(new Cookie(COOKIE_HOST, cookie.getKey(), cookie.getValue()));
		}
	}

	static void storeCookies(WebClient client) {
		for (Cookie cookie : client.getCookieManager().getCookies()) {
			cookies.put(cookie.getName(), cookie.getValue());
		}
	}

	static Page getPage(WebClient client, WebRequest request) throws IOException {
		applyCookies(client);
		Page page = client.getPage(request);
		storeCookies(client);
		return page;
	}

	static Page waitForCloudflare(WebClient client, WebRequest request, Page page, String xml) throws IOException, InterruptedException {
		if (xml.contains("Checking your browser") || xml.contains("checking_browser")) {
			bypassCloudflare = true;

			log.info("Waiting for Cloudflare...");

			try {
				FileWriter fileWriter = new FileWriter(new File("cfpage.html"));
				fileWriter.write(xml);
				fileWriter.flush();
				fileWriter.close();
			} catch (Exception ignored) {
			}

			// Give the client time to complete the Javascript challenge
			Thread.sleep(CLOUDFLARE_TIMEOUT);

			page = client.getPage(request);
			if (page instanceof UnexpectedPage) {
				log.warn("Got UnexpectedPage with status " + page.getWebResponse().getStatusMessage());
			}
		} else {
			bypassCloudflare = false;
		}
		return page;
	}

}
