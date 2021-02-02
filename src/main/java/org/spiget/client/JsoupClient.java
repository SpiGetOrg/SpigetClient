package org.spiget.client;

import lombok.extern.log4j.Log4j2;
import org.inventivetalent.metrics.MetricDataBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

@Log4j2
public class JsoupClient extends SpigetClient {

    public static SpigetResponse get(String url) throws IOException, InterruptedException {
        if (SpigetClient.requestsMetric == null) {
            SpigetClient.initMetrics();
        }
        MetricDataBuilder m = SpigetClient.requestsMetric
                .tag("project", SpigetClient.project)
                .tag("cfbypass", String.valueOf(bypassCloudflare))
                .tag("type", "get")
                .tag("client", "jsoup");

        Connection connection = Jsoup.connect(url).method(Connection.Method.GET).userAgent(userAgent);
        connection.cookies(cookies);
        connection.followRedirects(true);
        connection.ignoreHttpErrors(true);
        connection.ignoreContentType(true);
        connection.timeout(5000);

        Connection.Response response = connection.execute();
        Document document = response.parse();

        String docString = document.toString();
        if (docString.contains("checking_browser")) {
            // We've hit cloudflare -> enable bypass and try again
            bypassCloudflare = true;
            m.tag("code", String.valueOf(response.statusCode()))
                    .tag("state", "fail")
                    .inc();
            return SpigetClient.get(url);
        }
        if (response.statusCode() > 500) {
            log.warn("Got status code " + response.statusCode());
            m.tag("code", String.valueOf(response.statusCode()))
                    .tag("state", "fail")
                    .inc();
            // We've hit cloudflare but also got a bad status code -> enable bypass, wait a few seconds and try again
            Thread.sleep(1000);
            bypassCloudflare = true;
            return SpigetClient.get(url);
        }

        // Request was successful
        m.tag("code", String.valueOf(response.statusCode()))
                .tag("state", "success")
                .inc();
        cookies.putAll(response.cookies());
        return new SpigetResponse(cookies, document, response.statusCode());
    }

    public static SpigetDownload download(String url) throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

}
