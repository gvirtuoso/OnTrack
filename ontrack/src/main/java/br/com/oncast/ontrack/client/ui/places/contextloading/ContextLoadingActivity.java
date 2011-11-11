package br.com.oncast.ontrack.client.ui.places.contextloading;

import br.com.oncast.ontrack.client.services.context.ContextProviderService;
import br.com.oncast.ontrack.client.services.context.ProjectRepresentationProvider;
import br.com.oncast.ontrack.client.services.places.ApplicationPlaceController;
import br.com.oncast.ontrack.client.services.requestDispatch.DispatchCallback;
import br.com.oncast.ontrack.client.services.requestDispatch.RequestDispatchService;
import br.com.oncast.ontrack.client.ui.places.ProjectDependentPlace;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.services.requestDispatch.ProjectContextRequest;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ContextLoadingActivity extends AbstractActivity {

	private final ApplicationPlaceController placeController;
	private final RequestDispatchService requestDispatchService;
	private final ContextProviderService contextProviderService;
	private final ProjectDependentPlace projectDependentPlace;
	private final ProjectRepresentationProvider projectRepresentationProvider;

	public ContextLoadingActivity(final ContextProviderService contextProviderService, final ApplicationPlaceController placeController,
			final RequestDispatchService requestDispatchService, final ProjectRepresentationProvider projectRepresentationProvider,
			final ProjectDependentPlace destinationPlace) {

		this.contextProviderService = contextProviderService;
		this.placeController = placeController;
		this.requestDispatchService = requestDispatchService;
		this.projectRepresentationProvider = projectRepresentationProvider;
		this.projectDependentPlace = destinationPlace;
	}

	// TODO +Show animations and change the view according to the communication state.
	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		if (contextProviderService.isContextAvailable(projectDependentPlace.getRequestedProjectId())) placeController.goTo(projectDependentPlace);

		final ContextLoadingView view = new ContextLoadingPanel();
		panel.setWidget(view);

		// TODO Display 'loading' UI indicator.
		requestDispatchService.dispatch(new ProjectContextRequest(projectDependentPlace.getRequestedProjectId()), new DispatchCallback<ProjectContext>() {

			@Override
			public void onFailure(final Throwable cause) {
				// TODO Hide 'loading' UI indicator.
				// TODO +++Treat communication failure.
				Window.alert("Error! Could not load project: " + cause.toString());
				cause.printStackTrace();
			}

			@Override
			public void onRequestCompletition(final ProjectContext projectContext) {
				// TODO Hide 'loading' UI indicator.
				contextProviderService.setProjectContext(projectContext);
				projectRepresentationProvider.setProjectRepresentation(projectContext.getProjectRepresentation());
				placeController.goTo(projectDependentPlace);
			}
		});
	}
}