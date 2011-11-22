package br.com.oncast.ontrack.client.services.context;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import br.com.oncast.ontrack.client.services.requestDispatch.DispatchCallback;
import br.com.oncast.ontrack.client.services.requestDispatch.RequestDispatchService;
import br.com.oncast.ontrack.client.services.serverPush.ServerPushClientService;
import br.com.oncast.ontrack.shared.exceptions.business.UnableToCreateProjectRepresentation;
import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;
import br.com.oncast.ontrack.shared.services.context.NewProjectCreatedEventHandler;
import br.com.oncast.ontrack.shared.services.context.ProjectCreatedEvent;
import br.com.oncast.ontrack.shared.services.requestDispatch.ProjectCreationRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.ProjectListRequest;

public class ProjectRepresentationProviderImpl implements ProjectRepresentationProvider {

	private final RequestDispatchService dispatchService;
	private final Set<ProjectListChangeListener> projectListChangeListeners = new HashSet<ProjectListChangeListener>();
	private final Set<ProjectRepresentation> availableProjectRepresentations = new HashSet<ProjectRepresentation>();
	private ProjectRepresentation currentProjectRepresentation;

	public ProjectRepresentationProviderImpl(final RequestDispatchService dispatchService, final ServerPushClientService serverPushClientService) {
		this.dispatchService = dispatchService;

		serverPushClientService.registerServerEventHandler(ProjectCreatedEvent.class, new NewProjectCreatedEventHandler() {

			@Override
			public void onEvent(final ProjectCreatedEvent event) {
				final ProjectRepresentation newProjectRepresentation = event.getProjectRepresentation();
				if (availableProjectRepresentations.contains(newProjectRepresentation)) return;
				availableProjectRepresentations.add(newProjectRepresentation);
				notifyListenersForCurrentProjectListChange();
			}
		});

		dispatchService.dispatch(new ProjectListRequest(), new DispatchCallback<List<ProjectRepresentation>>() {

			@Override
			public void onRequestCompletition(final List<ProjectRepresentation> response) {
				availableProjectRepresentations.addAll(response);
				notifyListenersForCurrentProjectListChange();
			}

			@Override
			public void onFailure(final Throwable caught) {
				// FIXME Treat fatal error. COuld not load project list...
			}
		});
	}

	@Override
	public ProjectRepresentation getCurrentProjectRepresentation() {
		if (currentProjectRepresentation == null) throw new RuntimeException("There is no project representation set yet.");
		return currentProjectRepresentation;
	}

	protected void setProjectRepresentation(final ProjectRepresentation projectRepresentation) {
		this.currentProjectRepresentation = projectRepresentation;
	}

	@Override
	public void createNewProject(final String projectName, final ProjectCreationListener projectCreationListener) {
		dispatchService.dispatch(new ProjectCreationRequest(projectName), new DispatchCallback<ProjectRepresentation>() {

			@Override
			public void onRequestCompletition(final ProjectRepresentation projectRepresentation) {
				projectCreationListener.onProjectCreated(projectRepresentation);
			}

			@Override
			public void onFailure(final Throwable caught) {
				if (caught instanceof UnableToCreateProjectRepresentation) projectCreationListener.onProjectCreationFailure();
				else projectCreationListener.onUnexpectedFailure();
			}
		});
	}

	@Override
	public void registerProjectListChangeListener(final ProjectListChangeListener projectListChangeListener) {
		if (projectListChangeListeners.contains(projectListChangeListener)) return;
		projectListChangeListeners.add(projectListChangeListener);
		notifyListenerForCurrentProjectListChange(projectListChangeListener);
	}

	private void notifyListenersForCurrentProjectListChange() {
		for (final ProjectListChangeListener listener : projectListChangeListeners)
			notifyListenerForCurrentProjectListChange(listener);
	}

	private void notifyListenerForCurrentProjectListChange(final ProjectListChangeListener projectListChangeListener) {
		projectListChangeListener.onProjectListChanged(availableProjectRepresentations);
	}
}