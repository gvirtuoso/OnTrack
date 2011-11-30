package br.com.oncast.ontrack.client.services.context;

import br.com.drycode.api.web.gwt.dispatchService.client.DispatchCallback;
import br.com.drycode.api.web.gwt.dispatchService.client.DispatchService;
import br.com.oncast.ontrack.client.services.identification.ClientIdentificationProvider;
import br.com.oncast.ontrack.shared.exceptions.business.ProjectNotFoundException;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.services.requestDispatch.ProjectContextRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.ProjectContextResponse;

public class ContextProviderServiceImpl implements ContextProviderService {

	private final ProjectRepresentationProviderImpl projectRepresentationProvider;
	private final ClientIdentificationProvider clientIdentificationProvider;
	private final DispatchService requestDispatchService;

	private ProjectContext projectContext;

	public ContextProviderServiceImpl(final ProjectRepresentationProviderImpl projectRepresentationProvider,
			final ClientIdentificationProvider clientIdentificationProvider, final DispatchService requestDispatchService) {
		this.projectRepresentationProvider = projectRepresentationProvider;
		this.clientIdentificationProvider = clientIdentificationProvider;
		this.requestDispatchService = requestDispatchService;
	}

	@Override
	public ProjectContext getProjectContext(final long projectId) {
		if (isContextAvailable(projectId)) return projectContext;
		throw new RuntimeException("There is no project context avaliable.");
	}

	private void setProjectContext(final ProjectContext projectContext) {
		this.projectContext = projectContext;
		projectRepresentationProvider.setProjectRepresentation(projectContext.getProjectRepresentation());
	}

	@Override
	public boolean isContextAvailable(final long projectId) {
		return (projectContext != null) && (projectContext.getProjectRepresentation().getId() == projectId);
	}

	@Override
	public void loadProjectContext(final long requestedProjectId, final ProjectContextLoadCallback projectContextLoadCallback) {
		requestDispatchService.dispatch(new ProjectContextRequest(clientIdentificationProvider.getClientId(), requestedProjectId),
				new DispatchCallback<ProjectContextResponse>() {

					@Override
					public void onSuccess(final ProjectContextResponse response) {
						setProjectContext(new ProjectContext(response.getProject()));
						projectContextLoadCallback.onProjectContextLoaded();
					}

					@Override
					public void onFailure(final Throwable cause) {
						// TODO +++Treat communication failure.
						if (cause instanceof ProjectNotFoundException) projectContextLoadCallback.onProjectNotFound();
						else projectContextLoadCallback.onUnexpectedFailure(cause);
					}
				});
	}
}