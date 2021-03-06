package br.com.oncast.ontrack.client.ui.places;

import br.com.oncast.ontrack.client.services.ClientServices;
import br.com.oncast.ontrack.client.ui.places.details.DetailPlace;
import br.com.oncast.ontrack.client.ui.places.login.LoginPlace;
import br.com.oncast.ontrack.client.ui.places.metrics.OnTrackMetricsPlace;
import br.com.oncast.ontrack.client.ui.places.organization.OrganizationPlace;
import br.com.oncast.ontrack.client.ui.places.planning.PlanningPlace;
import br.com.oncast.ontrack.client.ui.places.progress.ProgressPlace;
import br.com.oncast.ontrack.client.ui.places.projectCreation.ProjectCreationPlace;
import br.com.oncast.ontrack.client.ui.places.projectSelection.ProjectSelectionPlace;
import br.com.oncast.ontrack.client.ui.places.report.ReportPlace;
import br.com.oncast.ontrack.client.ui.places.timesheet.TimesheetPlace;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

public class AppActivityMapper implements ActivityMapper {

	private final ClientServices services;
	private final AppActivityFactory activityFactory;

	public AppActivityMapper(final ClientServices serviceProvider) {
		this.services = serviceProvider;
		this.activityFactory = new AppActivityFactory();
	}

	@Override
	public Activity getActivity(final Place place) {
		if (!services.serverPush().isConnected()) return activityFactory.getConnectServerPushLoadingActivity(place);

		if (place instanceof LoginPlace) return activityFactory.getLoginActivity((LoginPlace) place);

		if (!services.authentication().isUserAvailable()) return activityFactory.getUserInformationLoadingActivity(place);

		if (place instanceof ProjectDependentPlace) {
			final ProjectDependentPlace projectDependentPlace = (ProjectDependentPlace) place;
			final UUID requestedProjectId = projectDependentPlace.getRequestedProjectId();

			if (requestedProjectId == null || !requestedProjectId.isValid()) return activityFactory.getProjectSelectionActivity();
			if (!services.contextProvider().isContextAvailable(requestedProjectId)) return activityFactory
					.getContextLoadingActivity(projectDependentPlace);
		}

		if (place instanceof DetailPlace) {
			activityFactory.getDetailActivity((DetailPlace) place).start();
			return null;
		}

		if (place instanceof TimesheetPlace) {
			activityFactory.getTimesheetActivity((TimesheetPlace) place).start();
			return null;
		}

		if (place instanceof PlanningPlace) return activityFactory.getPlanningActivity((PlanningPlace) place);
		if (place instanceof ProjectSelectionPlace) return activityFactory.getProjectSelectionActivity();
		if (place instanceof ProjectCreationPlace) return activityFactory.getProjectCreationPlace((ProjectCreationPlace) place);
		if (place instanceof ProgressPlace) return activityFactory.getProgressActivity((ProgressPlace) place);
		if (place instanceof OrganizationPlace) return activityFactory.getOrganizationActivity((OrganizationPlace) place);
		if (place instanceof OnTrackMetricsPlace) return activityFactory.getOnTrackStatisticsActivity();
		if (place instanceof ReportPlace) return activityFactory.getReportActivity((ReportPlace) place);

		return null;
	}
}
