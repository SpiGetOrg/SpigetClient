package org.spiget.client;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.InputStream;

@Data
@AllArgsConstructor
public class SpigetDownload {

	private String      url;
	private InputStream inputStream;
	private int         code;
	private boolean     available;

}
