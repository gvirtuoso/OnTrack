package br.com.oncast.ontrack.client.ui.components.scopetree.interaction;

import static br.com.oncast.ontrack.client.utils.keyboard.BrowserKeyCodes.KEY_2;
import static br.com.oncast.ontrack.client.utils.keyboard.BrowserKeyCodes.KEY_3;
import static br.com.oncast.ontrack.client.utils.keyboard.BrowserKeyCodes.KEY_4;
import static br.com.oncast.ontrack.client.utils.keyboard.BrowserKeyCodes.KEY_5;
import static br.com.oncast.ontrack.client.utils.keyboard.BrowserKeyCodes.KEY_DELETE;
import static br.com.oncast.ontrack.client.utils.keyboard.BrowserKeyCodes.KEY_DOWN;
import static br.com.oncast.ontrack.client.utils.keyboard.BrowserKeyCodes.KEY_ENTER;
import static br.com.oncast.ontrack.client.utils.keyboard.BrowserKeyCodes.KEY_F2;
import static br.com.oncast.ontrack.client.utils.keyboard.BrowserKeyCodes.KEY_LEFT;
import static br.com.oncast.ontrack.client.utils.keyboard.BrowserKeyCodes.KEY_RIGHT;
import static br.com.oncast.ontrack.client.utils.keyboard.BrowserKeyCodes.KEY_UP;
import br.com.oncast.ontrack.client.services.ClientServiceProvider;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal.BindReleaseInternalAction;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal.DeclareEffortInternalAction;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal.DeclareProgressInternalAction;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal.DeclareValueInternalAction;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal.InsertChildInternalAction;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal.InsertFatherInternalAction;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal.InsertSiblingDownInternalAction;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal.InsertSiblingUpInternalAction;
import br.com.oncast.ontrack.client.ui.components.scopetree.actions.internal.NodeEditionInternalAction;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeTreeWidgetInteractionHandler;
import br.com.oncast.ontrack.client.ui.generalwidgets.PopupConfig.PopupCloseListener;
import br.com.oncast.ontrack.client.ui.keyeventhandler.EventProcessor;
import br.com.oncast.ontrack.client.ui.keyeventhandler.Shortcut;
import br.com.oncast.ontrack.client.ui.keyeventhandler.ShortcutMapping;
import br.com.oncast.ontrack.client.ui.keyeventhandler.modifier.AltModifier;
import br.com.oncast.ontrack.client.ui.keyeventhandler.modifier.ControlModifier;
import br.com.oncast.ontrack.client.ui.keyeventhandler.modifier.ShiftModifier;
import br.com.oncast.ontrack.client.ui.settings.ViewSettings.ScopeTreeColumn;
import br.com.oncast.ontrack.client.utils.keyboard.BrowserKeyCodes;
import br.com.oncast.ontrack.shared.model.action.ScopeMoveDownAction;
import br.com.oncast.ontrack.shared.model.action.ScopeMoveLeftAction;
import br.com.oncast.ontrack.shared.model.action.ScopeMoveRightAction;
import br.com.oncast.ontrack.shared.model.action.ScopeMoveUpAction;
import br.com.oncast.ontrack.shared.model.action.ScopeRemoveAction;
import br.com.oncast.ontrack.shared.model.scope.Scope;

// TODO Refactor this class into a shortcut manager with better resposability division and better performance while mapping interactions.
public enum ScopeTreeShortcutMappings implements ShortcutMapping<ScopeTreeWidgetInteractionHandler> {
	UPDATE(new Shortcut(KEY_F2)) {
		@Override
		public void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			interactionHandler.onInternalAction(new NodeEditionInternalAction(scope));
		}
	},

	INSERT_SIBLING_SCOPE_DOWN(new Shortcut(KEY_ENTER)) {

		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			interactionHandler.onInternalAction(new InsertSiblingDownInternalAction(scope));
		}
	},

	INSERT_SIBLING_SCOPE_UP(new Shortcut(KEY_ENTER).with(ShiftModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			interactionHandler.onInternalAction(new InsertSiblingUpInternalAction(scope));
		}
	},

	INSERT_SCOPE_AS_CHILD(new Shortcut(KEY_ENTER).with(ControlModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			interactionHandler.onInternalAction(new InsertChildInternalAction(scope));
		}
	},

	INSERT_SCOPE_AS_PARENT(new Shortcut(KEY_ENTER).with(ControlModifier.PRESSED).with(ShiftModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			interactionHandler.onInternalAction(new InsertFatherInternalAction(scope));
		}
	},

	MOVE_SCOPE_UP(new Shortcut(KEY_UP).with(ControlModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			interactionHandler.onUserActionExecutionRequest(new ScopeMoveUpAction(scope.getId()));
		}
	},

	MOVE_SCOPE_DOWN(new Shortcut(KEY_DOWN).with(ControlModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			interactionHandler.onUserActionExecutionRequest(new ScopeMoveDownAction(scope.getId()));
		}

	},
	MOVE_SCOPE_RIGHT(new Shortcut(KEY_RIGHT).with(ControlModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			interactionHandler.onUserActionExecutionRequest(new ScopeMoveRightAction(scope.getId()));
		}
	},

	MOVE_SCOPE_LEFT(new Shortcut(KEY_LEFT).with(ControlModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			interactionHandler.onUserActionExecutionRequest(new ScopeMoveLeftAction(scope.getId()));
		}
	},

	DELETE_SCOPE(new Shortcut(KEY_DELETE)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			interactionHandler.onUserActionExecutionRequest(new ScopeRemoveAction(scope.getId()));
		}
	},

	BIND_RELEASE(new Shortcut(KEY_2).with(ShiftModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			interactionHandler.onInternalAction(new BindReleaseInternalAction(interactionHandler.getProjectContext(), scope));
		}
	},

	BIND_PROGRESS(new Shortcut(KEY_5).with(ShiftModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			interactionHandler.onInternalAction(new DeclareProgressInternalAction(interactionHandler.getProjectContext(), scope));
		}
	},

	BIND_EFFORT(new Shortcut(KEY_3).with(ShiftModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			interactionHandler.onInternalAction(new DeclareEffortInternalAction(scope, interactionHandler.getProjectContext()));
		}
	},

	BIND_VALUE(new Shortcut(KEY_4).with(ShiftModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			interactionHandler.onInternalAction(new DeclareValueInternalAction(scope, interactionHandler.getProjectContext()));
		}
	},

	OPEN_ANNOTATIONS(new Shortcut(BrowserKeyCodes.KEY_A).with(ShiftModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			ClientServiceProvider.getInstance().getAnnotationService().showAnnotationsFor(scope, new PopupCloseListener() {
				@Override
				public void onHasClosed() {
					interactionHandler.focusTree();
				}
			});
		}
	},

	TOGGLE_VALUE_COLUMN(new Shortcut(BrowserKeyCodes.KEY_4).with(AltModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			ScopeTreeColumn.VALUE.toggle();
		}
	},

	TOGGLE_EFFORT_COLUMN(new Shortcut(BrowserKeyCodes.KEY_3).with(AltModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			ScopeTreeColumn.EFFORT.toggle();
		}
	},

	TOGGLE_RELEASE_COLUMN(new Shortcut(BrowserKeyCodes.KEY_2).with(AltModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			ScopeTreeColumn.RELEASE.toggle();
		}
	},

	TOGGLE_PROGRESS_COLUMN(new Shortcut(BrowserKeyCodes.KEY_5).with(AltModifier.PRESSED)) {
		@Override
		protected void customExecution(final ScopeTreeWidgetInteractionHandler interactionHandler, final Scope scope) {
			ScopeTreeColumn.PROGRESS.toggle();
		}
	};

	private final Shortcut shortcut;

	ScopeTreeShortcutMappings(final Shortcut shortcut) {
		this.shortcut = shortcut;
	}

	@Override
	public void execute(final ScopeTreeWidgetInteractionHandler interactionHandler) {
		interactionHandler.assureConfigured();
		final Scope selectedScope = interactionHandler.getSelectedScope();
		if (selectedScope == null) return;
		customExecution(interactionHandler, selectedScope);
	}

	protected abstract void customExecution(ScopeTreeWidgetInteractionHandler interactionHandler, Scope scope);

	@Override
	public Shortcut getShortcut() {
		return this.shortcut;
	}

	@Override
	public EventProcessor getEventPostExecutionProcessor() {
		return EventProcessor.CONSUME;
	}

}
