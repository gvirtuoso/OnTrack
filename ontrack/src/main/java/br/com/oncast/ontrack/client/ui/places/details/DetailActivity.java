package br.com.oncast.ontrack.client.ui.places.details;

import br.com.oncast.ontrack.client.services.ClientServices;
import br.com.oncast.ontrack.client.services.metrics.TimeTrackingEvent;
import br.com.oncast.ontrack.client.services.places.ApplicationPlaceController;
import br.com.oncast.ontrack.client.ui.components.details.DetailsPanel;
import br.com.oncast.ontrack.client.ui.generalwidgets.PopupConfig;
import br.com.oncast.ontrack.client.ui.generalwidgets.PopupConfig.PopupCloseListener;
import br.com.oncast.ontrack.client.ui.generalwidgets.PopupConfig.PopupOpenListener;
import br.com.oncast.ontrack.client.ui.keyeventhandler.ShortcutService;
import br.com.oncast.ontrack.client.ui.places.UndoRedoShortCutMapping;
import br.com.oncast.ontrack.shared.model.kanban.exception.KanbanColumnNotFoundException;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.exceptions.ReleaseNotFoundException;
import br.com.oncast.ontrack.shared.model.scope.exceptions.ScopeNotFoundException;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class DetailActivity extends AbstractActivity {

	private DetailsPanel detailPanel;
	private HandlerRegistration register;
	private final DetailPlace place;

	public DetailActivity(final DetailPlace place) {
		this.place = place;
	}

	public void start() {
		if (!setDetailPanel(place)) return;
		final TimeTrackingEvent timeTracking = ClientServices.get().metrics().startPlaceLoad(place);
		if (!place.hasLoadedPlace()) register = ShortcutService.register(detailPanel, ClientServices.get().actionExecution(), UndoRedoShortCutMapping.values());

		PopupConfig.configPopup().popup(this.detailPanel).onClose(new PopupCloseListener() {
			@Override
			public void onHasClosed() {
				getApplicationPlaceController().goTo(place.getDestinationPlace());
				detailPanel.unregisterActionExecutionListener();
			}

		}).onOpen(new PopupOpenListener() {
			@Override
			public void onWillOpen() {
				detailPanel.registerActionExecutionListener();
				timeTracking.end();
			}
		}).focusMode(true).pop();
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		start();
	}

	@Override
	public void onStop() {
		if (register != null) register.removeHandler();
	}

	private boolean setDetailPanel(final DetailPlace place) {
		final ClientServices services = ClientServices.get();
		final ProjectContext context = services.contextProvider().getProjectContext(place.getRequestedProjectId());

		detailPanel = null;
		try {
			detailPanel = DetailsPanel.forScope(context.findScope(place.getSubjectId()));
		} catch (final ScopeNotFoundException e) {
			try {
				detailPanel = DetailsPanel.forRelease(context.findRelease(place.getSubjectId()));
			} catch (final ReleaseNotFoundException e1) {
				try {
					detailPanel = DetailsPanel.forKanbanColumn(context.findKanbanColumn(place.getSubjectId()));
				} catch (final KanbanColumnNotFoundException e2) {
					services.alerting().showError(services.messages().errorShowingDetails());
					getApplicationPlaceController().goTo(place.getDestinationPlace());
					return false;
				}
			}
		}
		return true;
	}

	private ApplicationPlaceController getApplicationPlaceController() {
		return ClientServices.get().placeController();
	}

}
