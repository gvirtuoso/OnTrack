package br.com.oncast.ontrack.client.services.applicationState;

import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeSelectionEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ScopeSelectionEventHandler;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.scope.Scope;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class ClientApplicationStateServiceImpl implements ClientApplicationStateService {

	private Scope selectedScope;
	private final Scope defaultSelectionScope;
	private final EventBus eventBus;
	private HandlerRegistration handlerRegistration;

	public ClientApplicationStateServiceImpl(final EventBus eventBus, final ProjectContext projectContext) {
		this.eventBus = eventBus;
		this.defaultSelectionScope = projectContext.getProjectScope();
	}

	@Override
	public void startRecording() {
		registerScopeSelectionEventListener();
	}

	private void registerScopeSelectionEventListener() {
		handlerRegistration = eventBus.addHandler(ScopeSelectionEvent.getType(), new ScopeSelectionEventHandler() {
			@Override
			public void onScopeSelectionRequest(final ScopeSelectionEvent event) {
				selectedScope = event.getTargetScope();
			}
		});
	}

	@Override
	public void stopRecording() {
		handlerRegistration.removeHandler();
	}

	@Override
	public void restore() {
		fireScopeSelectionEvent();
	}

	private void fireScopeSelectionEvent() {
		Scheduler.get().scheduleEntry(new ScheduledCommand() {
			@Override
			public void execute() {
				eventBus.fireEvent(new ScopeSelectionEvent(getSelectedScope()));
			}
		});
	}

	private Scope getSelectedScope() {
		return selectedScope == null ? defaultSelectionScope : selectedScope;
	}
}