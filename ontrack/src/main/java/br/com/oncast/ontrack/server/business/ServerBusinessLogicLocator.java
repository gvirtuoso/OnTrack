package br.com.oncast.ontrack.server.business;

import br.com.oncast.ontrack.server.services.ServerServiceProvider;

public class ServerBusinessLogicLocator {

	private static ServerBusinessLogicLocator instance;
	private BusinessLogic businessLogic;
	private final ServerServiceProvider serviceProvider;

	public static ServerBusinessLogicLocator getInstance() {
		if (instance != null) return instance;
		return instance = new ServerBusinessLogicLocator();
	}

	private ServerBusinessLogicLocator() {
		serviceProvider = ServerServiceProvider.getInstance();
	}

	public BusinessLogic getBusinessLogic() {
		if (businessLogic != null) return businessLogic;
		synchronized (this) {
			if (businessLogic != null) return businessLogic;
			return businessLogic = new BusinessLogicImpl(serviceProvider.getPersistenceService(), serviceProvider.getActionBroadcastService());
		}
	}

}