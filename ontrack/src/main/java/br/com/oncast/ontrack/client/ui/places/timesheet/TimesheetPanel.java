package br.com.oncast.ontrack.client.ui.places.timesheet;

import static br.com.oncast.ontrack.client.services.ClientServiceProvider.getCurrentUser;

import java.util.Date;
import java.util.List;
import java.util.Set;

import br.com.oncast.ontrack.client.services.ClientServiceProvider;
import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionListener;
import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionService;
import br.com.oncast.ontrack.client.ui.components.user.UserWidget;
import br.com.oncast.ontrack.client.ui.generalwidgets.EditableLabel;
import br.com.oncast.ontrack.client.ui.generalwidgets.EditableLabelEditionHandler;
import br.com.oncast.ontrack.client.ui.generalwidgets.ModelWidget;
import br.com.oncast.ontrack.client.ui.generalwidgets.PopupConfig.PopupAware;
import br.com.oncast.ontrack.client.ui.places.timesheet.widgets.ScopeTimeSpentWidget;
import br.com.oncast.ontrack.client.utils.number.ClientDecimalFormat;
import br.com.oncast.ontrack.client.utils.speedtracer.SpeedTracerConsole;
import br.com.oncast.ontrack.shared.model.action.ActionContext;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.ReleaseRenameAction;
import br.com.oncast.ontrack.shared.model.action.ScopeBindReleaseAction;
import br.com.oncast.ontrack.shared.model.action.ScopeDeclareTimeSpentAction;
import br.com.oncast.ontrack.shared.model.action.ScopeUpdateAction;
import br.com.oncast.ontrack.shared.model.action.TeamAction;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.release.Release.Condition;
import br.com.oncast.ontrack.shared.model.release.exceptions.ReleaseNotFoundException;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.HasCloseHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.RowFormatter;
import com.google.gwt.user.client.ui.Widget;

public class TimesheetPanel extends Composite implements ModelWidget<Release>, PopupAware, HasCloseHandlers<TimesheetPanel> {

	private static final ClientServiceProvider SERVICE_PROVIDER = ClientServiceProvider.getInstance();

	private static TimesheetPanelUiBinder uiBinder = GWT.create(TimesheetPanelUiBinder.class);

	interface TimesheetPanelStyle extends CssResource {

		String scopeDescriptionCell();

		String sumCell();

		String row();

		String currentUserColumn();

	}

	@UiField
	TimesheetPanelStyle style;

	@UiField(provided = true)
	EditableLabel releaseTitle;

	@UiField
	FlexTable timesheet;

	@UiField
	Button previousReleaseButton;

	@UiField
	Button nextReleaseButton;

	private Release release;

	private ActionExecutionListener actionExecutionListener;

	private Release previousRelease;

	private Release nextRelease;

	private Float[][] times;

	interface TimesheetPanelUiBinder extends UiBinder<Widget, TimesheetPanel> {}

	public TimesheetPanel() {
		initializeReleaseTitle();
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void setRelease(final Release release) {
		this.release = release;
		times = new Float[release.getScopeList().size()][getUsers().size()];
		update();
		setupReleaseNavigationButtons();
	}

	private void setupReleaseNavigationButtons() {
		previousRelease = release.getLatestPastRelease(new Condition() {
			@Override
			public boolean eval(final Release release) {
				return !release.isRoot();
			}
		});
		previousReleaseButton.setEnabled(previousRelease != null);

		nextRelease = release.getFirstFutureRelease(new Condition() {
			@Override
			public boolean eval(final Release release) {
				return !release.isRoot();
			}
		});

		nextReleaseButton.setEnabled(nextRelease != null);
	}

	@Override
	public void show() {}

	@Override
	public void hide() {
		if (!this.isVisible()) return;
		CloseEvent.fire(this, this);
	}

	@Override
	public boolean update() {
		updateReleaseTitle();

		final List<UserRepresentation> users = getUsers();
		final List<Scope> scopes = release.getScopeList();

		mountTable(scopes, users);
		populateTable(scopes, users);

		return false;
	}

	private void updateSums(final UUID scopeId, final UUID userId) {
		final int row = getScopeRowIndex(scopeId);
		final int column = getUserColumnIndex(userId);
		if (row == -1 || column == -1) return;

		final Float previousValue = times[row - 1][column - 1];
		final float newValue = getTimeSpent(scopeId, userId);
		final float difference = newValue - previousValue;

		times[row - 1][column - 1] = newValue;

		final int lastColumn = timesheet.getCellCount(row) - 1;
		final int lastRow = timesheet.getRowCount() - 1;

		timesheet.setText(row, lastColumn, getRowSum(row) + "");
		timesheet.setText(lastRow, column, getColumnSum(column) + "");

		final Float previousTotal = Float.valueOf(timesheet.getText(lastRow, lastColumn));
		timesheet.setText(lastRow, lastColumn, previousTotal + difference + "");
	}

	private void populateTable(final List<Scope> scopes, final List<UserRepresentation> users) {
		final long time = new Date().getTime();

		SpeedTracerConsole.log("clear " + (new Date().getTime() - time));

		for (int i = 0; i < scopes.size(); i++) {
			final Scope scope = scopes.get(i);

			for (int j = 0; j < users.size(); j++) {
				final UserRepresentation user = users.get(j);

				final float value = getTimeSpent(scope.getId(), user.getId());
				times[i][j] = value;

				if (user.equals(getCurrentUser())) timesheet.setWidget(i + 1, j + 1, new ScopeTimeSpentWidget(scope, user));
				else timesheet.setText(i + 1, j + 1, round(value));
			}
		}

		SpeedTracerConsole.log("widget " + (new Date().getTime() - time));

		for (int i = 1; i < scopes.size() + 1; i++) {
			final Scope scope = scopes.get(i - 1);
			timesheet.setText(i, 0, scope.getDescription());

			timesheet.setText(i, users.size() + 1, round(getRowSum(i)));
		}

		SpeedTracerConsole.log("scopes " + (new Date().getTime() - time));

		float totalSum = 0;
		for (int j = 1; j < users.size() + 1; j++) {
			final UserRepresentation user = users.get(j - 1);
			timesheet.setWidget(0, j, new UserWidget(user));

			final float sum = getColumnSum(j);
			timesheet.setText(scopes.size() + 1, j, round(sum));
			totalSum += sum;
		}

		SpeedTracerConsole.log("users" + (new Date().getTime() - time));

		timesheet.setText(scopes.size() + 1, users.size() + 1, round(totalSum));
	}

	private void mountTable(final List<Scope> scopes, final List<UserRepresentation> users) {
		final int lastRow = scopes.size() + 1;
		final int lastColumn = users.size() + 1;

		timesheet.clear();

		timesheet.setText(0, 0, "");
		timesheet.setText(lastRow, lastColumn, "");

		final CellFormatter cellFormatter = timesheet.getCellFormatter();
		final RowFormatter rowFormatter = timesheet.getRowFormatter();

		for (int i = 1; i < lastRow; i++) {
			rowFormatter.addStyleName(i, style.row());
			cellFormatter.addStyleName(i, 0, style.scopeDescriptionCell());
			cellFormatter.addStyleName(i, lastColumn, style.sumCell());
		}

		for (int i = 1; i < lastColumn; i++) {
			cellFormatter.addStyleName(lastRow, i, style.sumCell());
		}

		timesheet.getColumnFormatter().addStyleName(getUserColumnIndex(getCurrentUser()), style.currentUserColumn());
		cellFormatter.addStyleName(lastRow, lastColumn, style.sumCell());
	}

	private void updateScopeDescriptions(final UUID scopeId) {
		final List<Scope> scopes = release.getScopeList();

		for (final Scope s : scopes) {
			if (s.getId().equals(scopeId)) {
				timesheet.setText(scopes.indexOf(s) + 1, 0, s.getDescription());
			}
		}
	}

	private void updateReleaseTitle() {
		releaseTitle.setValue(release.getDescription());
	}

	@UiHandler("previousReleaseButton")
	public void onPreviousReleaseClick(final ClickEvent event) {
		SERVICE_PROVIDER.getTimesheetService().showTimesheetFor(previousRelease.getId());
	}

	@UiHandler("nextReleaseButton")
	public void onNextReleaseClick(final ClickEvent event) {
		SERVICE_PROVIDER.getTimesheetService().showTimesheetFor(nextRelease.getId());
	}

	@Override
	public Release getModelObject() {
		return release;
	}

	public void unregisterActionExecutionListener() {
		ClientServiceProvider.getInstance().getActionExecutionService().removeActionExecutionListener(actionExecutionListener);
	}

	public void registerActionExecutionListener() {
		ClientServiceProvider.getInstance().getActionExecutionService().addActionExecutionListener(getActionExecutionListener());
	}

	private ActionExecutionListener getActionExecutionListener() {
		return actionExecutionListener == null ? actionExecutionListener = new ActionExecutionListener() {

			@Override
			public void onActionExecution(final ModelAction action, final ProjectContext context, final ActionContext actionContext,
					final Set<UUID> inferenceInfluencedScopeSet,
					final boolean isUserAction) {

				if (action instanceof ScopeDeclareTimeSpentAction) updateSums(action.getReferenceId(), actionContext.getUserId());

				else if (action instanceof ScopeBindReleaseAction) update();
				else if (action instanceof TeamAction) update();
				else if (action instanceof ReleaseRenameAction) updateReleaseTitle();
				else if (action instanceof ScopeUpdateAction) updateScopeDescriptions(action.getReferenceId());
			}

		} : actionExecutionListener;
	}

	@Override
	public HandlerRegistration addCloseHandler(final CloseHandler<TimesheetPanel> handler) {
		return this.addHandler(handler, CloseEvent.getType());
	}

	private void initializeReleaseTitle() {
		releaseTitle = new EditableLabel(new EditableLabelEditionHandler() {

			@Override
			public boolean onEditionRequest(final String text) {
				final ProjectContext projectContext = getContext();
				final ActionExecutionService actionExecutionService = SERVICE_PROVIDER.getActionExecutionService();

				try {
					projectContext.findRelease(release.getId());
					actionExecutionService.onUserActionExecutionRequest(new ReleaseRenameAction(release.getId(), text));
				}
				catch (final ReleaseNotFoundException e1) {
					throw new RuntimeException("Impossible to create an editable label for this annotation.");
				}
				return true;
			}
		});
	}

	private float getColumnSum(final int column) {
		Float result = 0F;
		for (final Float[] row : times)
			result += row[column - 1];

		return result;
	}

	private float getRowSum(final int row) {
		Float result = 0F;
		for (final Float value : times[row - 1])
			result += value;

		return result;
	}

	private int getUserColumnIndex(final UUID userId) {
		final List<UserRepresentation> users = getUsers();
		for (final UserRepresentation u : users) {
			if (u.getId().equals(userId)) return users.indexOf(u) + 1;
		}
		return -1;
	}

	private int getScopeRowIndex(final UUID scopeId) {
		final List<Scope> scopes = release.getScopeList();
		for (final Scope s : scopes) {
			if (s.getId().equals(scopeId)) return scopes.indexOf(s) + 1;
		}
		return -1;
	}

	private float getTimeSpent(final UUID scopeId, final UUID userId) {
		final Float declaredTimeSpent = getContext().getDeclaredTimeSpent(scopeId, userId);
		return declaredTimeSpent == null ? 0.0f : declaredTimeSpent;
	}

	private List<UserRepresentation> getUsers() {
		return getContext().getUsers();
	}

	private ProjectContext getContext() {
		return ClientServiceProvider.getCurrentProjectContext();
	}

	private String round(final float number) {
		return ClientDecimalFormat.roundFloat(number, 1);
	}

}
