package br.com.oncast.ontrack.client.ui.components.releasepanel;

import java.util.Set;

import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionListener;
import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionRequestHandler;
import br.com.oncast.ontrack.client.ui.components.Component;
import br.com.oncast.ontrack.client.ui.components.releasepanel.interaction.ReleasePanelInteractionHandler;
import br.com.oncast.ontrack.client.ui.components.releasepanel.widgets.ReleasePanelWidget;
import br.com.oncast.ontrack.shared.model.actions.ModelAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeInsertAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeInsertParentRollbackAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeRemoveAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeRemoveRollbackAction;
import br.com.oncast.ontrack.shared.model.actions.ScopeUpdateAction;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.utils.deepEquality.IgnoredByDeepEquality;

import com.google.gwt.user.client.ui.Widget;

public class ReleasePanel implements Component {

	private final ReleasePanelWidget releasePanelWidget;

	@IgnoredByDeepEquality
	private final ActionExecutionListener actionExecutionListener;

	// TODO Review if this should be tested by deepEquality
	@IgnoredByDeepEquality
	private Release rootRelease;

	@IgnoredByDeepEquality
	private ReleasePanelInteractionHandler releasePanelInteractionHandler;

	public ReleasePanel() {
		releasePanelWidget = new ReleasePanelWidget(releasePanelInteractionHandler = new ReleasePanelInteractionHandler());

		actionExecutionListener = new ActionExecutionListener() {
			@Override
			public void onActionExecution(final ModelAction action, final ProjectContext context, final Set<UUID> inferenceInfluencedScopeSet,
					final boolean isUserAction) {
				// FIXME Add ReleaseRemoveAction to the update verification.
				if (action instanceof ScopeUpdateAction || action instanceof ScopeRemoveAction || action instanceof ScopeInsertAction
						|| action instanceof ScopeInsertParentRollbackAction || action instanceof ScopeRemoveRollbackAction) update();
			}
		};
	}

	protected void update() {
		releasePanelWidget.update();
	}

	public void setRelease(final Release release) {
		this.rootRelease = release;
		releasePanelWidget.setRelease(rootRelease);
	}

	@Override
	public ActionExecutionListener getActionExecutionListener() {
		return actionExecutionListener;
	}

	@Override
	public void setActionExecutionRequestHandler(final ActionExecutionRequestHandler actionHandler) {
		releasePanelInteractionHandler.configure(actionHandler);
	}

	@Override
	public Widget asWidget() {
		return releasePanelWidget;
	}

	@Override
	public int hashCode() {
		return rootRelease.hashCode();
	}
}
