package br.com.oncast.ontrack.server.services.setup;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.zschech.gwt.comet.server.impl.AsyncServlet;
import br.com.drycode.api.web.gwt.dispatchService.server.DispatchServiceServlet;
import br.com.drycode.api.web.gwt.dispatchService.shared.DispatchServiceException;
import br.com.oncast.ontrack.client.services.feedback.SendFeedbackRequest;
import br.com.oncast.ontrack.client.services.migration.GenerateExportXmlRequest;
import br.com.oncast.ontrack.server.business.DefaultUserExistenceAssurer;
import br.com.oncast.ontrack.server.business.ServerServiceProvider;
import br.com.oncast.ontrack.server.services.authentication.AuthenticationVerificationAspectFilter;
import br.com.oncast.ontrack.server.services.requestDispatch.AnnotatedSubjectIdsRequestHandler;
import br.com.oncast.ontrack.server.services.requestDispatch.AuthenticationRequestHandler;
import br.com.oncast.ontrack.server.services.requestDispatch.ChangePasswordRequestHandler;
import br.com.oncast.ontrack.server.services.requestDispatch.CurrentUserInformationRequestHandler;
import br.com.oncast.ontrack.server.services.requestDispatch.DeAuthenticationRequestHandler;
import br.com.oncast.ontrack.server.services.requestDispatch.GenerateExportXmlRequestHandler;
import br.com.oncast.ontrack.server.services.requestDispatch.ModelActionSyncRequestHandler;
import br.com.oncast.ontrack.server.services.requestDispatch.ProjectAuthorizationRequestHandler;
import br.com.oncast.ontrack.server.services.requestDispatch.ProjectContextRequestHandler;
import br.com.oncast.ontrack.server.services.requestDispatch.ProjectCreationQuotaRequestHandler;
import br.com.oncast.ontrack.server.services.requestDispatch.ProjectCreationRequestHandler;
import br.com.oncast.ontrack.server.services.requestDispatch.ProjectListRequestHandler;
import br.com.oncast.ontrack.server.services.requestDispatch.SendFeedbackRequestHandler;
import br.com.oncast.ontrack.server.services.serverPush.ServerPushServerService;
import br.com.oncast.ontrack.shared.services.requestDispatch.AnnotatedSubjectIdsRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.AuthenticationRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.ChangePasswordRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.CurrentUserInformationRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.DeAuthenticationRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.ModelActionSyncRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.ProjectAuthorizationRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.ProjectContextRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.ProjectCreationQuotaRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.ProjectCreationRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.ProjectListRequest;

public class ApplicationContextListener implements ServletContextListener {

	private static final ServerServiceProvider SERVICE_PROVIDER = ServerServiceProvider.getInstance();

	@Override
	public void contextInitialized(final ServletContextEvent event) {
		setupDispatchHandlers();
		setupAuthenticationAspectIntoDispatchService();
		setupBusinessLogic(event);
		setupServerPush(event);
		assureDefaultUserIsPresent();
	}

	private void setupAuthenticationAspectIntoDispatchService() {
		DispatchServiceServlet.registerRequestFilter(new AuthenticationVerificationAspectFilter(SERVICE_PROVIDER.getAuthenticationManager()));
	}

	// TODO +++ Maybe change this registration to Annotation based one;
	private void setupDispatchHandlers() {
		try {
			DispatchServiceServlet.registerRequestHandler(ModelActionSyncRequest.class, new ModelActionSyncRequestHandler());
			DispatchServiceServlet.registerRequestHandler(ProjectContextRequest.class, new ProjectContextRequestHandler());
			DispatchServiceServlet.registerRequestHandler(ProjectCreationRequest.class, new ProjectCreationRequestHandler());
			DispatchServiceServlet.registerRequestHandler(ProjectAuthorizationRequest.class, new ProjectAuthorizationRequestHandler());
			DispatchServiceServlet.registerRequestHandler(ProjectListRequest.class, new ProjectListRequestHandler());
			DispatchServiceServlet.registerRequestHandler(AuthenticationRequest.class, new AuthenticationRequestHandler());
			DispatchServiceServlet.registerRequestHandler(DeAuthenticationRequest.class, new DeAuthenticationRequestHandler());
			DispatchServiceServlet.registerRequestHandler(ChangePasswordRequest.class, new ChangePasswordRequestHandler());
			DispatchServiceServlet.registerRequestHandler(CurrentUserInformationRequest.class, new CurrentUserInformationRequestHandler());
			DispatchServiceServlet.registerRequestHandler(ProjectCreationQuotaRequest.class, new ProjectCreationQuotaRequestHandler());
			DispatchServiceServlet.registerRequestHandler(SendFeedbackRequest.class, new SendFeedbackRequestHandler());
			DispatchServiceServlet.registerRequestHandler(GenerateExportXmlRequest.class, new GenerateExportXmlRequestHandler());
			DispatchServiceServlet.registerRequestHandler(AnnotatedSubjectIdsRequest.class, new AnnotatedSubjectIdsRequestHandler());
		}
		catch (final DispatchServiceException e) {
			throw new RuntimeException("The application is misconfigured.", e);
		}
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
		SERVICE_PROVIDER.getBusinessLogic();
	}

	/**
	 * Assure the default user is present when the application starts.
	 */
	private void assureDefaultUserIsPresent() {
		DefaultUserExistenceAssurer.verify();
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
