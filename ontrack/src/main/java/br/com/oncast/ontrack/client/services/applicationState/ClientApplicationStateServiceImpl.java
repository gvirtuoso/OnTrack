package br.com.oncast.ontrack.client.services.applicationState;

import br.com.oncast.ontrack.client.i18n.ClientMessages;
import br.com.oncast.ontrack.client.services.alerting.ClientAlertingService;
import br.com.oncast.ontrack.client.services.context.ContextChangeListener;
import br.com.oncast.ontrack.client.services.context.ContextProviderService;
import br.com.oncast.ontrack.client.services.storage.ClientStorageService;
import br.com.oncast.ontrack.client.ui.components.releasepanel.events.ReleaseContainerStateChangeEvent;
import br.com.oncast.ontrack.client.ui.components.releasepanel.events.ReleaseContainerStateChangeEventHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ActivateTagFilterEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ActivateTagFilterEventHandler;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ClearTagFilterEvent;
import br.com.oncast.ontrack.client.ui.components.scopetree.events.ClearTagFilterEventHandler;
import br.com.oncast.ontrack.client.ui.events.ScopeSelectionEvent;
import br.com.oncast.ontrack.client.ui.events.ScopeSelectionEventHandler;
import br.com.oncast.ontrack.client.ui.settings.DefaultViewSettings;
import br.com.oncast.ontrack.client.ui.settings.ViewSettings.ScopeTreeColumn;
import br.com.oncast.ontrack.client.ui.settings.ViewSettings.ScopeTreeColumn.VisibilityChangeListener;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.release.exceptions.ReleaseNotFoundException;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.scope.exceptions.ScopeNotFoundException;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public class ClientApplicationStateServiceImpl implements ClientApplicationStateService {

	private final EventBus eventBus;
	private final ContextProviderService contextProviderService;
	private final ClientStorageService clientStorageService;
	private final ClientAlertingService alertingService;
	private final ClientMessages messages;

	private final Set<HandlerRegistration> handlerRegistrations;

	private final Stack<Scope> previousSelectedScopes;
	private final Stack<Scope> nextSelectedScopes;

	private UUID filterTagId;

	public ClientApplicationStateServiceImpl(final EventBus eventBus, final ContextProviderService contextProviderService, final ClientStorageService clientStorageService,
			final ClientAlertingService alertingService, final ClientMessages messages) {
		this.eventBus = eventBus;
		this.contextProviderService = contextProviderService;
		this.clientStorageService = clientStorageService;
		this.alertingService = alertingService;
		this.messages = messages;
		this.previousSelectedScopes = new Stack<Scope>();
		this.nextSelectedScopes = new Stack<Scope>();

		handlerRegistrations = new HashSet<HandlerRegistration>();
		contextProviderService.addContextLoadListener(new ContextChangeListener() {
			@Override
			public void onProjectChanged(final UUID projetId, final Long loadedProjectRevision) {
				previousSelectedScopes.clear();
				nextSelectedScopes.clear();
			}
		});
		registerFilterByTagEvent();
	}

	private void registerFilterByTagEvent() {
		handlerRegistrations.add(eventBus.addHandler(ActivateTagFilterEvent.getType(), new ActivateTagFilterEventHandler() {
			@Override
			public void onFilterByTagRequested(final UUID tagId) {
				filterTagId = tagId;
			}
		}));

		handlerRegistrations.add(eventBus.addHandler(ClearTagFilterEvent.getType(), new ClearTagFilterEventHandler() {
			@Override
			public void onClearTagFilterRequested() {
				filterTagId = null;
			}
		}));
	}

	@Override
	public void startRecording() {
		registerScopeSelectionEventListener();
		registerScopeTreeColumnVisibilityChangeListener();
		registerReleaseContainerStateChangeListener();
	}

	private void registerReleaseContainerStateChangeListener() {
		handlerRegistrations.add(eventBus.addHandler(ReleaseContainerStateChangeEvent.getType(), new ReleaseContainerStateChangeEventHandler() {
			@Override
			public void onReleaseContainerStateChange(final ReleaseContainerStateChangeEvent releaseContainerStateChangeEvent) {
				clientStorageService.storeReleaseContainerState(releaseContainerStateChangeEvent.getTargetRelease(), releaseContainerStateChangeEvent.getTargetContainerState());
			}
		}));
	}

	private void registerScopeTreeColumnVisibilityChangeListener() {
		for (final ScopeTreeColumn column : ScopeTreeColumn.values()) {
			handlerRegistrations.add(column.register(new VisibilityChangeListener() {
				@Override
				public void onVisiblityChange(final boolean isVisible) {
					clientStorageService.storeScopeTreeColumnVisibility(column, isVisible);
				}
			}));
		}
	}

	private void registerScopeSelectionEventListener() {
		handlerRegistrations.add(eventBus.addHandler(ScopeSelectionEvent.getType(), new ScopeSelectionEventHandler() {
			@Override
			public void onScopeSelectionRequest(final ScopeSelectionEvent event) {
				setSelectedScope(event);
			}
		}));
	}

	@Override
	public void stopRecording() {
		for (final HandlerRegistration handlerRegistration : handlerRegistrations) {
			handlerRegistration.removeHandler();
		}

		handlerRegistrations.clear();
	}

	@Override
	public UUID getFilterTagId() {
		return filterTagId;
	}

	@Override
	public void restore() {
		fireScopeSelectionEvent();
		restoreScopeTreeColumnVisibility();
		fireReleaseContainerStateChangeEvents();
	}

	@Override
	public void restore(final UUID scopeSelectedId) {
		try {
			if (scopeSelectedId != null) {
				previousSelectedScopes.add(contextProviderService.getCurrent().findScope(scopeSelectedId));
			}
			restore();
		} catch (final ScopeNotFoundException e) {
			alertingService.showError(messages.errorSelectingScope());
		}
	}

	private void fireReleaseContainerStateChangeEvents() {
		final ProjectContext context = contextProviderService.getCurrent();
		final List<UUID> savedReleaseContainerStates = clientStorageService.loadModifiedContainerStateReleases();
		for (final UUID releaseId : savedReleaseContainerStates) {
			try {
				final Release release = context.findRelease(releaseId);
				Scheduler.get().scheduleEntry(new ScheduledCommand() {
					@Override
					public void execute() {
						eventBus.fireEvent(new ReleaseContainerStateChangeEvent(release, !DefaultViewSettings.RELEASE_PANEL_CONTAINER_STATE));
					}
				});
			} catch (final ReleaseNotFoundException e) {}
		}
		if (savedReleaseContainerStates.isEmpty()) {
			for (final Release r : context.getProjectRelease().getAllReleasesInTemporalOrder()) {
				if (!r.isRoot() && r.isDone() == DefaultViewSettings.RELEASE_PANEL_CONTAINER_STATE) {
					eventBus.fireEvent(new ReleaseContainerStateChangeEvent(r, !DefaultViewSettings.RELEASE_PANEL_CONTAINER_STATE));
				}
			}
		}
	}

	private void restoreScopeTreeColumnVisibility() {
		for (final ScopeTreeColumn column : ScopeTreeColumn.values()) {
			column.setVisibility(clientStorageService.loadScopeTreeColumnVisibility(column));
		}
	}

	private void fireScopeSelectionEvent() {
		Scheduler.get().scheduleEntry(new ScheduledCommand() {
			@Override
			public void execute() {
				eventBus.fireEvent(new ScopeSelectionEvent(getSelectedScope(), true));
			}
		});
	}

	private void setSelectedScope(final ScopeSelectionEvent event) {
		if (getSelectedScope().equals(event.getTargetScope())) return;

		nextSelectedScopes.clear();

		previousSelectedScopes.add(event.getTargetScope());
		storeSelectedScope();
	}

	private Scope getSelectedScope() {
		return previousSelectedScopes.isEmpty() ? loadSelectedScope() : previousSelectedScopes.peek();
	}

	private void storeSelectedScope() {
		clientStorageService.storeSelectedScopeId(previousSelectedScopes.peek().getId());
	}

	private Scope loadSelectedScope() {
		final ProjectContext currentContext = contextProviderService.getCurrent();
		Scope selectedScope = null;
		try {
			selectedScope = currentContext.findScope(clientStorageService.loadSelectedScopeId(currentContext.getProjectScope().getId()));
		} catch (final ScopeNotFoundException e) {
			selectedScope = currentContext.getProjectScope();
		}
		previousSelectedScopes.add(selectedScope);
		return selectedScope;
	}

	@Override
	public void jumpToPreviousSelection() {
		if (previousSelectedScopes.isEmpty()) return;

		nextSelectedScopes.add(previousSelectedScopes.pop());
		fireScopeSelectionEvent();
		storeSelectedScope();
	}

	@Override
	public void jumpToNextSelection() {
		if (nextSelectedScopes.isEmpty()) return;

		previousSelectedScopes.add(nextSelectedScopes.pop());
		fireScopeSelectionEvent();
		storeSelectedScope();
	}
}
