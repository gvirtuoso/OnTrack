package br.com.oncast.ontrack.server.services.session;

import br.com.oncast.ontrack.server.business.ServerServiceProvider;
import br.com.oncast.ontrack.server.services.log.RequestLogger;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class SessionFilter implements Filter {

	private static final ServerServiceProvider SERVER_SERVICE_PROVIDER = ServerServiceProvider.getInstance();
	private static final SessionManager SESSION_MANAGER = SERVER_SERVICE_PROVIDER.getSessionManager();
	private static final RequestLogger REQUEST_LOGGER = new RequestLogger();

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		final HttpServletRequest httpServletRequest = (HttpServletRequest) request;

		REQUEST_LOGGER.log(httpServletRequest);
		SESSION_MANAGER.configureCurrentHttpSession(httpServletRequest);

		chain.doFilter(request, response);
	}

	@Override
	public void init(final FilterConfig arg0) throws ServletException {}

	@Override
	public void destroy() {}
}
