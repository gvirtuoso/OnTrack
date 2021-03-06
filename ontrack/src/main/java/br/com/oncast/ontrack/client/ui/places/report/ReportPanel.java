package br.com.oncast.ontrack.client.ui.places.report;

import br.com.oncast.ontrack.client.ui.components.annotations.widgets.ReleaseDetailWidget;
import br.com.oncast.ontrack.client.ui.components.releasepanel.widgets.chart.ReleaseChart;
import br.com.oncast.ontrack.client.ui.components.report.ImpedimentReportTable;
import br.com.oncast.ontrack.client.ui.components.report.ReportMessages;
import br.com.oncast.ontrack.client.ui.components.report.ScopeReportTable;
import br.com.oncast.ontrack.client.ui.places.timesheet.widgets.TimesheetWidget;
import br.com.oncast.ontrack.shared.model.description.exceptions.DescriptionNotFoundException;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.scope.Scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import static br.com.oncast.ontrack.client.services.ClientServices.getCurrentProjectContext;

public class ReportPanel extends Composite {

	private static final DateTimeFormat FORMATTER = DateTimeFormat.getFormat("dd/MM/yyyy - HH:mm");

	private static ReportPanelUiBinder uiBinder = GWT.create(ReportPanelUiBinder.class);

	private static ReportMessages messages = GWT.create(ReportMessages.class);

	interface ReportPanelUiBinder extends UiBinder<Widget, ReportPanel> {}

	@UiField
	HTMLPanel rootPanel;

	@UiField
	Label releaseTitle;

	@UiField
	Panel burnUpPanel;

	@UiField(provided = true)
	ReleaseDetailWidget details;

	@UiField(provided = true)
	ScopeReportTable epicsTable;

	@UiField(provided = true)
	ScopeReportTable scopeTable;

	@UiField(provided = true)
	ImpedimentReportTable impedimentTable;

	@UiField(provided = true)
	TimesheetWidget timesheet;

	@UiField
	InlineHTML description;

	@UiField
	DivElement descriptionContainer;

	@UiField
	DivElement impedimentsContainer;

	@UiField
	DivElement timesheetContainer;

	@UiField
	InlineHTML ancestors;

	@UiField
	Label timestamp;

	private final ReleaseChart chart;

	public ReportPanel(final ProjectContext projectContext, final Release release) {
		chart = new ReleaseChart(release, true);
		details = ReleaseDetailWidget.forReport(release);
		scopeTable = new ScopeReportTable(release.getAllScopesIncludingDescendantReleases(), projectContext, messages);
		epicsTable = new ScopeReportTable(getEpics(release.getAllScopesIncludingDescendantReleases()), projectContext, messages);
		epicsTable.showOnlyEpicColumns();
		impedimentTable = new ImpedimentReportTable(release, projectContext, messages);
		timesheet = new TimesheetWidget(release, true);

		initWidget(uiBinder.createAndBindUi(this));

		releaseTitle.setText(release.getDescription());
		ancestors.setHTML(getAncestorsBreadcrumb(release));
		timestamp.setText(FORMATTER.format(new Date()));

		try {
			description.setHTML(getCurrentProjectContext().findDescriptionFor(release.getId()).getDescription());
		} catch (final DescriptionNotFoundException e) {
			descriptionContainer.removeFromParent();
		}

		if (timesheet.isEmpty()) timesheetContainer.removeFromParent();
		if (impedimentTable.isEmpty()) impedimentsContainer.removeFromParent();

		burnUpPanel.add(chart);
		chart.updateData();
	}

	@UiHandler("showTimingColunsCheck")
	void onShowTimingColumnsCheckValueChange(final ValueChangeEvent<Boolean> e) {
		impedimentTable.setTimingColumnsVisible(e.getValue());
	}

	private List<Scope> getEpics(final List<Scope> scopes) {
		final ArrayList<Scope> epics = new ArrayList<Scope>();
		for (final Scope scope : scopes) {
			final Scope parent = scope.getParent();
			if (!epics.contains(parent)) epics.add(parent);
		}
		return epics;
	}

	private String getAncestorsBreadcrumb(final Release release) {
		final String projectName = "<span style=\"color: #2D4171; font-size: 150%;\">" + release.getRootRelease().getDescription() + "</span>";

		final List<Release> ancestors = release.getAncestors();
		if (ancestors.isEmpty()) return projectName;

		final List<String> ancestorsSimpleDescriptions = new ArrayList<String>();
		for (final Release r : ancestors) {
			ancestorsSimpleDescriptions.add(r.getDescription());
		}

		Collections.reverse(ancestorsSimpleDescriptions);
		return projectName + " &gt; " + Joiner.on(" &gt; ").join(ancestorsSimpleDescriptions);
	}

	public HTMLPanel getAlertingContainer() {
		return rootPanel;
	}

	public ScopeReportTable getScopeReportTable() {
		return scopeTable;
	}
}
