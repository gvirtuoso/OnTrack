package br.com.oncast.ontrack.client.services;

import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionService;
import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionServiceImpl;
import br.com.oncast.ontrack.client.services.actionSync.ActionSyncService;
import br.com.oncast.ontrack.client.services.authentication.AuthenticationService;
import br.com.oncast.ontrack.client.services.authentication.AuthenticationServiceImpl;
import br.com.oncast.ontrack.client.services.authentication.UserProviderService;
import br.com.oncast.ontrack.client.services.context.ContextProviderService;
import br.com.oncast.ontrack.client.services.context.ContextProviderServiceImpl;
import br.com.oncast.ontrack.client.services.errorHandling.ErrorTreatmentService;
import br.com.oncast.ontrack.client.services.errorHandling.ErrorTreatmentServiceImpl;
import br.com.oncast.ontrack.client.services.identification.ClientIdentificationProvider;
import br.com.oncast.ontrack.client.services.places.ApplicationPlaceController;
import br.com.oncast.ontrack.client.services.requestDispatch.RequestDispatchService;
import br.com.oncast.ontrack.client.services.requestDispatch.RequestDispatchServiceImpl;
import br.com.oncast.ontrack.client.services.serverPush.ServerPushClientService;
import br.com.oncast.ontrack.client.services.serverPush.ServerPushClientServiceImpl;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;

// TODO Create interfaces for each service and return them instead of the direct reference of its implementations (so that the rest of the application only
// reference the interfaces, making the code more testable).
public class ClientServiceProvider {

	private AuthenticationService authenticationService;
	private UserProviderService userProviderService;
	private ActionSyncService actionSyncService;
	private ActionExecutionService actionExecutionService;
	private ContextProviderService contextProviderService;
	private RequestDispatchService requestDispatchService;
	private ServerPushClientService serverPushClientService;
	private ClientIdentificationProvider clientIdentificationProvider;
	private ApplicationPlaceController placeController;
	private ErrorTreatmentService errorTreatmentService;
	private EventBus eventBus;

	public AuthenticationService getAuthenticationService() {
		if (authenticationService != null) return authenticationService;
		return authenticationService = new AuthenticationServiceImpl(requestDispatchService, getUserProviderService());
	}

	private UserProviderService getUserProviderService() {
		if (userProviderService != null) return userProviderService;
		return userProviderService = new UserProviderService();
	}

	public ApplicationPlaceController getApplicationPlaceController() {
		if (placeController != null) return placeController;
		return placeController = new ApplicationPlaceController(getEventBus());
	}

	public ActionExecutionService getActionExecutionService() {
		if (actionExecutionService != null) return actionExecutionService;
		return actionExecutionService = new ActionExecutionServiceImpl(getContextProviderService(), getErrorTreatmentService());
	}

	public ContextProviderService getContextProviderService() {
		if (contextProviderService != null) return contextProviderService;
		return contextProviderService = new ContextProviderServiceImpl();
	}

	public RequestDispatchService getRequestDispatchService() {
		if (requestDispatchService != null) return requestDispatchService;
		return requestDispatchService = new RequestDispatchServiceImpl();
	}

	public ActionSyncService getActionSyncService() {
		if (actionSyncService != null) return actionSyncService;
		return actionSyncService = new ActionSyncService(getRequestDispatchService(), getServerPushClientService(), getActionExecutionService(),
				getClientIdentificationProvider(), getErrorTreatmentService());
	}

	private ErrorTreatmentService getErrorTreatmentService() {
		if (errorTreatmentService != null) return errorTreatmentService;
		return errorTreatmentService = new ErrorTreatmentServiceImpl();
	}

	private ClientIdentificationProvider getClientIdentificationProvider() {
		if (clientIdentificationProvider != null) return clientIdentificationProvider;
		return clientIdentificationProvider = new ClientIdentificationProvider();
	}

	private ServerPushClientService getServerPushClientService() {
		if (serverPushClientService != null) return serverPushClientService;
		return serverPushClientService = new ServerPushClientServiceImpl(getErrorTreatmentService());
	}

	private EventBus getEventBus() {
		if (eventBus != null) return eventBus;
		return eventBus = new SimpleEventBus();
	}
}
