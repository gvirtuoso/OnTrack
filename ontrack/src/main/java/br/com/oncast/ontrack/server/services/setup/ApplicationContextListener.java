package br.com.oncast.ontrack.server.services.setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.zschech.gwt.comet.server.impl.AsyncServlet;
import br.com.oncast.ontrack.server.business.ServerBusinessLogicLocator;
import br.com.oncast.ontrack.server.services.serverPush.ServerPushServerService;

public class ApplicationContextListener implements ServletContextListener {

	@Override
	public void contextInitialized(final ServletContextEvent event) {
		setupBusinessLogic(event);
		setupServerPush(event);
	}

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
		shutdownServerPush(event);
	}

	/**
	 * Sets up the business logic and - with it - all direct server services.
	 * Needed to give a chance to services like {@link ServerPushServerService} to register their listeners on the necessary application input classes (Servlets
	 * for instance).
	 * 
	 * @param event the servlet context event.
	 */
	private void setupBusinessLogic(final ServletContextEvent event) {
		ServerBusinessLogicLocator.getInstance().getBusinessLogic();
	}

	/**
	 * Initializes comet related resources associated with an ApplicationContext.
	 * Without this the comet related resources will be initialized when first used and will not be shutdown cleanly.
	 * 
	 * @param event the servlet context event.
	 */
	private void setupServerPush(final ServletContextEvent event) {
		AsyncServlet.initialize(event.getServletContext());
	}

	/**
	 * Shuts down comet related resources associated with an ApplicationContext.
	 * Without this the comet related resources will not be shutdown cleanly.
	 * 
	 * @param event the servlet context event.
	 */
	private void shutdownServerPush(final ServletContextEvent event) {
		AsyncServlet.destroy(event.getServletContext());
	}
}