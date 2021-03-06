package br.com.oncast.ontrack.server.services.log;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class RequestLogger {

	private static final Logger LOGGER = Logger.getLogger(RequestLogger.class);

	public void log(final HttpServletRequest request) {
		LOGGER.debug(new RequestLogBuilder(request).appendClientIpAddress().appendRequestedURI().appendUserAgent().toString());
	}

}
