package org.spiget.client;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.jsoup.Jsoup;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;

@Log4j2
public class PuppeteerClient extends SpigetClient {

   public static String DIR_NAME;
    public static Path DIR;

    public static SpigetResponse get(String url) throws IOException, InterruptedException {
        try {
            try (FileWriter writer = new FileWriter(DIR_NAME+"toload.txt")) {
                writer.write(url);
            }
            log.info("Wrote toload");

            try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
                final WatchKey watchKey = DIR.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                for (int i = 0; i < 100; i++) {
                    final WatchKey wk = watchService.take();
//                    log.info(wk.toString());
                    for (WatchEvent event : wk.pollEvents()) {
                        Path changed = ((Path) event.context());
//                        log.info(changed.toString());
                        if (changed.endsWith("page.html")) {
                            log.info("page file changed");
                            return new SpigetResponse(new HashMap<>(), Jsoup.parse(new String(Files.readAllBytes(Paths.get(DIR_NAME+"page.html")))), 200);
                        }
                    }
                    boolean valid = wk.reset();
                    if (!valid) {
                        log.info("unregistered watchkey");
                    }
                    Thread.sleep(600);
                }
                log.error("Failed to get page");
            }

            return new SpigetResponse(new HashMap<>(), null, 500);
        } catch (Throwable throwable) {
            log.log(Level.ERROR, throwable);
            throw new RuntimeException(throwable);
        }
    }

}
