package br.com.oncast.ontrack.client.ui.components.organization.widgets;

import br.com.oncast.ontrack.client.services.ClientServices;
import br.com.oncast.ontrack.client.ui.events.ReleaseSelectionEvent;
import br.com.oncast.ontrack.client.ui.generalwidgets.AnimatedContainer;
import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidget;
import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidgetContainer;
import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidgetFactory;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class ReleaseSummaryWidget extends Composite implements ModelWidget<Release> {

	interface ReleaseSummaryWidgetUiBinder extends UiBinder<Widget, ReleaseSummaryWidget> {}

	private static final int MIN_WIDTH = 80;

	private static final ReleaseSummaryWidgetMessages messages = GWT.create(ReleaseSummaryWidgetMessages.class);

	private static ReleaseSummaryWidgetUiBinder uiBinder = GWT.create(ReleaseSummaryWidgetUiBinder.class);

	interface ReleaseSummaryWidgetStyle extends CssResource {

		String rootSelected();

		String headerSelected();

		String headerUnplanned();

	}

	@UiField
	ReleaseSummaryWidgetStyle style;

	@UiField
	HTMLPanel root;

	@UiField
	FocusPanel containerStateToggleButton;

	@UiField
	FocusPanel header;

	@UiField
	HTMLPanel content;

	@UiField
	Label title;

	@UiField
	SimplePanel progressBar;

	@UiField(provided = true)
	ModelWidgetContainer<Release, ReleaseSummaryWidget> childReleases;

	private final Release release;

	private final ReleaseSummaryWidget parentReleaseWidget;

	private boolean containerState;

	private final UUID projectId;

	public ReleaseSummaryWidget(final UUID projectId, final Release release, final ReleaseSummaryWidget parentReleaseWidget) {
		this.projectId = projectId;
		this.release = release;
		containerState = !release.isDone();
		this.parentReleaseWidget = parentReleaseWidget;

		createChildReleasesContainer();
		initWidget(uiBinder.createAndBindUi(this));

		update();
		setContainerState(containerState);
	}

	public ReleaseSummaryWidget(final UUID projectId, final Release release) {
		this(projectId, release, null);
	}

	@UiHandler("header")
	void onHeaderClicked(final ClickEvent event) {
		event.stopPropagation();

		if (!release.getId().isValid()) return;
		ClientServices.get().eventBus().fireEvent(new ReleaseSelectionEvent(release, projectId));
	}

	public void setSelected(final boolean b) {
		header.setStyleName(style.headerSelected(), b);
		root.setStyleName(style.rootSelected(), b);
	}

	@UiHandler("containerStateToggleButton")
	void onHeaderDoubleClicked(final ClickEvent event) {
		setContainerState(!containerState);
	}

	@Override
	public boolean update() {
		title.setText(release.getDescription());
		header.setStyleName(style.headerUnplanned(), !release.getId().isValid());

		final float accomplished = release.getAccomplishedEffortSum();
		final float total = release.getEffortSum();
		final float progressPercentage = accomplished == 0 ? 0 : accomplished / total;

		updateProgressBar(progressPercentage);

		updateChildReleases();

		return false;
	}

	private void updateProgressBar(final float progressPercentage) {
		final Style s = progressBar.getElement().getStyle();
		s.setRight((1.0 - progressPercentage) * 100, Unit.PCT);
		if (progressPercentage == 0.0) s.setMarginRight(16, Unit.PX);
		if (progressPercentage == 1.0) s.setMarginRight(-16, Unit.PX);
	}

	private void updateChildReleases() {
		final List<Release> children = release.getChildren();
		if (!children.isEmpty() && release.getEffortSum() > getEffortSum(children)) {
			final Release unplannedRelease = new Release(messages.unplanned(), UUID.INVALID_UUID);
			for (final Scope s : release.getScopeList())
				unplannedRelease.addScope(s.cloneWithoutRelationship());
			children.add(unplannedRelease);
		}
		childReleases.update(children);

		content.setVisible(!children.isEmpty() && containerState);
		containerStateToggleButton.setVisible(!children.isEmpty());
	}

	private float getEffortSum(final List<Release> children) {
		float sum = 0;
		for (final Release child : children) {
			sum += child.getEffortSum();
		}
		return sum;
	}

	private void createChildReleasesContainer() {
		childReleases = new ModelWidgetContainer<Release, ReleaseSummaryWidget>(new ModelWidgetFactory<Release, ReleaseSummaryWidget>() {
			@Override
			public ReleaseSummaryWidget createWidget(final Release modelBean) {
				return new ReleaseSummaryWidget(projectId, modelBean, ReleaseSummaryWidget.this);
			}
		}, new AnimatedContainer(new ReleaseEffortBasedHorizontalPanel(release)));
	}

	@Override
	public Release getModelObject() {
		return release;
	}

	public ReleaseSummaryWidget getChildWidgetFor(final Release modelBean) {
		return childReleases.getWidgetFor(modelBean);
	}

	public void adjustMinWidth(final float parentPercentage) {
		header.getElement().getStyle().setProperty("minWidth", MIN_WIDTH * (1 + parentPercentage), Unit.PX);
	}

	public void setHierarchicalContainerState(final boolean b) {
		setContainerState(b);
		if (parentReleaseWidget != null) parentReleaseWidget.setHierarchicalContainerState(b);
	}

	private void setContainerState(final boolean b) {
		if (!release.hasChildren()) return;

		content.setVisible(b);
		containerState = b;

		containerStateToggleButton.setStyleName("icon-caret-down", containerState);
		containerStateToggleButton.setStyleName("icon-caret-right", !containerState);
	}

}
