package br.com.oncast.ontrack.client.ui.places.report;

import br.com.oncast.ontrack.client.services.ClientServices;
import br.com.oncast.ontrack.client.ui.places.planning.PlanningPlace;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.release.exceptions.ReleaseNotFoundException;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.RootPanel;

public class ReportActivity extends AbstractActivity {

	private static final ClientServices SERVICE_PROVIDER = ClientServices.get();

	private final UUID requestedProjectId;
	private final UUID requestedReleaseId;

	public ReportActivity(final ReportPlace place) {
		this.requestedProjectId = place.getRequestedProjectId();
		this.requestedReleaseId = place.getRequestedReleaseId();
	}

	@Override
	public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
		try {
			final ProjectContext projectContext = SERVICE_PROVIDER.contextProvider().getProjectContext(requestedProjectId);
			final Release release = projectContext.findRelease(requestedReleaseId);

			final ReportPanel view = new ReportPanel(projectContext, release);

			SERVICE_PROVIDER.alerting().setAlertingParentWidget(view.getAlertingContainer());

			final Element body = RootPanel.getBodyElement();
			body.getStyle().setOverflow(Overflow.VISIBLE);
			body.getStyle().setProperty("height", "auto");
			body.getParentElement().getStyle().setOverflow(Overflow.AUTO);
			panel.setWidget(view);
		}
		catch (final ReleaseNotFoundException e) {
			exitToPlanningPlace();
		}
	}

	private void exitToPlanningPlace() {
		SERVICE_PROVIDER.placeController().goTo(new PlanningPlace(requestedProjectId));
	}

	@Override
	public void onStop() {
		final Element body = RootPanel.getBodyElement();
		body.getStyle().clearOverflow();
		body.getStyle().clearHeight();
		body.getParentElement().getStyle().clearOverflow();
	}
}
