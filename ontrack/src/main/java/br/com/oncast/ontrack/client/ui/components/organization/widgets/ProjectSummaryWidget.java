package br.com.oncast.ontrack.client.ui.components.organization.widgets;

import java.util.HashSet;
import java.util.Set;

import br.com.oncast.ontrack.client.WidgetVisibilityEnsurer;
import br.com.oncast.ontrack.client.WidgetVisibilityEnsurer.ContainerAlignment;
import br.com.oncast.ontrack.client.WidgetVisibilityEnsurer.Orientation;
import br.com.oncast.ontrack.client.services.ClientServiceProvider;
import br.com.oncast.ontrack.client.services.context.ProjectContextLoadCallback;
import br.com.oncast.ontrack.client.ui.components.annotations.widgets.ReleaseDetailWidget;
import br.com.oncast.ontrack.client.ui.components.releasepanel.widgets.chart.ReleaseChart;
import br.com.oncast.ontrack.client.ui.components.releasepanel.widgets.chart.ReleaseChartDataProvider;
import br.com.oncast.ontrack.client.ui.events.ReleaseSelectionEvent;
import br.com.oncast.ontrack.client.ui.events.ReleaseSelectionEventHandler;
import br.com.oncast.ontrack.client.ui.events.ScopeSelectionEvent;
import br.com.oncast.ontrack.client.ui.events.ScopeSelectionEventHandler;
import br.com.oncast.ontrack.client.ui.generalwidgets.AnimatedContainer;
import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidget;
import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidgetContainer;
import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidgetFactory;
import br.com.oncast.ontrack.client.ui.places.planning.PlanningPlace;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class ProjectSummaryWidget extends Composite implements ModelWidget<ProjectRepresentation> {

	private static final int MARGIN_LEFT = 10;

	private static final ProjectSummaryWidgetMessages messages = GWT.create(ProjectSummaryWidgetMessages.class);

	private static ProjectSummaryWidgetUiBinder uiBinder = GWT.create(ProjectSummaryWidgetUiBinder.class);

	interface ProjectSummaryWidgetUiBinder extends UiBinder<Widget, ProjectSummaryWidget> {}

	interface ProjectSummaryWidgetStyle extends CssResource {
		String containerStateOpen();

		String containerStateClosed();
	}

	@UiField
	ProjectSummaryWidgetStyle style;

	@UiField
	Label name;

	@UiField
	ReleaseDetailWidget releaseDetail;

	@UiField
	FocusPanel releaseContainer;

	@UiField(provided = true)
	ModelWidgetContainer<Release, ReleaseSummaryWidget> releases;

	@UiField
	Label scopesListTitle;

	@UiField(provided = true)
	ModelWidgetContainer<Scope, ScopeSummaryWidget> scopesList;

	@UiField
	SimplePanel chartPanel;

	@UiField
	DeckPanel loadingDeck;

	@UiField
	FocusPanel containerStateToggleButton;

	private ProjectRepresentation projectRepresentation;

	private ProjectContext project;

	private final Set<HandlerRegistration> selectionEventHandlers;

	private ReleaseSummaryWidget selectedWidget;

	private ReleaseChart chart;

	private boolean hasStartedUp = false;

	private ReleaseEffortBasedHorizontalPanel releasesHorizontalPanel;

	public ProjectSummaryWidget(final ProjectRepresentation project) {
		this.projectRepresentation = project;
		selectionEventHandlers = new HashSet<HandlerRegistration>();

		createReleasesContainer();
		createScopesList();

		initWidget(uiBinder.createAndBindUi(this));

		name.setText(project.getName());
		setContainerState(false);
	}

	private void setupSelectionEventHandlers() {
		if (!selectionEventHandlers.isEmpty()) return;

		final EventBus eventBus = ClientServiceProvider.getInstance().getEventBus();
		selectionEventHandlers.add(eventBus.addHandler(ReleaseSelectionEvent.getType(), new ReleaseSelectionEventHandler() {
			@Override
			public void onReleaseSelection(final ReleaseSelectionEvent event) {
				if (!getProjectId().equals(event.getProjectId())) return;

				setSelected(event.getRelease());
			}
		}));

		selectionEventHandlers.add(eventBus.addHandler(ScopeSelectionEvent.getType(), new ScopeSelectionEventHandler() {
			@Override
			public void onScopeSelectionRequest(final ScopeSelectionEvent event) {
				final Scope s = event.getTargetScope();
				if (!getProjectId().equals(event.getProjectId()) || !s.getProgress().isDone()) return;

				if (chart != null) chart.highlight(s.getProgress().getEndDay());
			}
		}));
	}

	private void removeSelectionEventHandler() {
		for (final HandlerRegistration reg : selectionEventHandlers) {
			reg.removeHandler();
		}
		selectionEventHandlers.clear();
	}

	private void createReleasesContainer() {
		releases = new ModelWidgetContainer<Release, ReleaseSummaryWidget>(new ModelWidgetFactory<Release, ReleaseSummaryWidget>() {
			@Override
			public ReleaseSummaryWidget createWidget(final Release modelBean) {
				return new ReleaseSummaryWidget(getProjectId(), modelBean);
			}

		}, new AnimatedContainer(releasesHorizontalPanel = new ReleaseEffortBasedHorizontalPanel()));
	}

	private void createScopesList() {
		scopesList = new ModelWidgetContainer<Scope, ScopeSummaryWidget>(new ModelWidgetFactory<Scope, ScopeSummaryWidget>() {
			@Override
			public ScopeSummaryWidget createWidget(final Scope modelBean) {
				return new ScopeSummaryWidget(modelBean, getProjectId());
			}
		});
	}

	@UiHandler("containerStateToggleButton")
	void onNameClicked(final ClickEvent e) {
		setContainerState(!getContainerState());
	}

	@UiHandler("releaseContainer")
	void onReleaseContainerClicked(final ClickEvent e) {
		if (selectedWidget != null) selectedWidget.setSelected(false);
		updateDetails(getProjectRelease());
	}

	@UiHandler("planningLink")
	void onPlanningLinkClicked(final ClickEvent e) {
		ClientServiceProvider.getInstance().getApplicationPlaceController().goTo(new PlanningPlace(projectRepresentation));
	}

	@Override
	public boolean update() {
		name.setText(projectRepresentation.getName());
		releases.update(getProjectRelease().getChildren());
		return false;
	}

	private void updateDetails(final Release release) {
		releaseDetail.setSubject(release);

		scopesListTitle.setText(release.isLeaf() ? messages.scope() : messages.unplannedScope());
		scopesList.update(release.getScopeList());
		final ClientServiceProvider serviceProvider = ClientServiceProvider.getInstance();
		final ReleaseChartDataProvider dataProvider = new ReleaseChartDataProvider(release, serviceProvider.getReleaseEstimatorProvider().get(),
				serviceProvider.getActionExecutionService());

		chart = new ReleaseChart(false);
		chartPanel.setWidget(chart);
		chart.setRelease(release, dataProvider);
		chart.updateData();
	}

	private void setSelected(final Release release) {
		if (selectedWidget != null && selectedWidget.getModelObject().equals(release)) return;

		if (selectedWidget != null) selectedWidget.setSelected(false);

		selectedWidget = getWidget(release);
		if (selectedWidget != null) selectedWidget.setSelected(true);

		updateDetails(release);
	}

	private void ensureSelectedWidgetIsVisible() {
		if (selectedWidget == null) return;

		selectedWidget.setHierarchicalContainerState(true);
		final Element element = selectedWidget.asWidget().getElement();
		final Element container = releaseContainer.getElement();

		WidgetVisibilityEnsurer.ensureVisible(element, container, Orientation.HORIZONTAL, ContainerAlignment.BEGIN, MARGIN_LEFT);
		WidgetVisibilityEnsurer.ensureVisible(element, container, Orientation.VERTICAL, ContainerAlignment.CENTER, 0);
	}

	private ReleaseSummaryWidget getWidget(final Release release) {
		final ReleaseSummaryWidget w = releases.getWidgetFor(release);
		if (release.isRoot() || w != null) return w;

		final ReleaseSummaryWidget parentWidget = getWidget(release.getParent());
		return parentWidget == null ? null : parentWidget.getChildWidgetFor(release);
	}

	public void setContainerState(final boolean b) {
		loadingDeck.setVisible(b);
		containerStateToggleButton.setStyleName(style.containerStateOpen(), b);
		containerStateToggleButton.setStyleName(style.containerStateClosed(), !b);

		if (b) {
			setupSelectionEventHandlers();
			setup();
		}
		else removeSelectionEventHandler();
	}

	private void setup() {
		if (hasStartedUp) return;

		loadingDeck.showWidget(0);
		ClientServiceProvider.getInstance().getContextProviderService().loadProjectContext(projectRepresentation.getId(), new ProjectContextLoadCallback() {
			@Override
			public void onUnexpectedFailure(final Throwable cause) {
				ClientServiceProvider.getInstance().getClientAlertingService().showError(cause.getLocalizedMessage());
			}

			@Override
			public void onProjectNotFound() {
				ClientServiceProvider.getInstance().getClientAlertingService().showError(messages.projectNotFound());
			}

			@Override
			public void onProjectContextLoaded() {
				project = ClientServiceProvider.getCurrentProjectContext();
				releasesHorizontalPanel.setRelease(getProjectRelease());
				update();
				setSelected(getCurrentRelease());
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						ensureSelectedWidgetIsVisible();
					}
				});
				chart.updateSize();
				hasStartedUp = true;
				loadingDeck.showWidget(1);
			}
		});
	}

	private boolean getContainerState() {
		return loadingDeck.isVisible();
	}

	private Release getCurrentRelease() {
		for (final Release r : getProjectRelease().getAllReleasesInTemporalOrder()) {
			if (r.isDone()) continue;

			for (final Scope s : r.getScopeList()) {
				if (s.getProgress().isUnderWork()) return r;
			}
		}
		return getFirstNotCompleteRelease();
	}

	private Release getFirstNotCompleteRelease() {
		for (final Release r : getProjectRelease().getAllReleasesInTemporalOrder()) {
			if (!r.isDone()) return r;
		}
		return getProjectRelease();
	}

	private Release getProjectRelease() {
		return project.getProjectRelease();
	}

	private UUID getProjectId() {
		return projectRepresentation.getId();
	}

	@Override
	public ProjectRepresentation getModelObject() {
		return projectRepresentation;
	}

	public void setProjectRepresentation(final ProjectRepresentation projectRepresentation) {
		this.projectRepresentation = projectRepresentation;
		update();
	}
}
