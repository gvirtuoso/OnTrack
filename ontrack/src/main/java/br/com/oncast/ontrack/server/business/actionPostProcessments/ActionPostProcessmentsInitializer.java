package br.com.oncast.ontrack.server.business.actionPostProcessments;

import br.com.oncast.ontrack.server.services.actionPostProcessing.ActionPostProcessingService;
import br.com.oncast.ontrack.server.services.actionPostProcessing.ActionPostProcessor;
import br.com.oncast.ontrack.server.services.integration.IntegrationService;
import br.com.oncast.ontrack.server.services.multicast.MulticastService;
import br.com.oncast.ontrack.server.services.notification.NotificationServerService;
import br.com.oncast.ontrack.server.services.persistence.PersistenceService;
import br.com.oncast.ontrack.shared.model.action.AnnotationCreateAction;
import br.com.oncast.ontrack.shared.model.action.AnnotationDeprecateAction;
import br.com.oncast.ontrack.shared.model.action.FileUploadAction;
import br.com.oncast.ontrack.shared.model.action.ImpedimentCreateAction;
import br.com.oncast.ontrack.shared.model.action.ImpedimentSolveAction;
import br.com.oncast.ontrack.shared.model.action.ScopeAddAssociatedUserAction;
import br.com.oncast.ontrack.shared.model.action.ScopeBindReleaseAction;
import br.com.oncast.ontrack.shared.model.action.ScopeDeclareProgressAction;
import br.com.oncast.ontrack.shared.model.action.ScopeUpdateAction;
import br.com.oncast.ontrack.shared.model.action.TeamAction;
import br.com.oncast.ontrack.shared.model.action.TeamDeclareCanInviteAction;
import br.com.oncast.ontrack.shared.model.action.TeamDeclareReadOnlyAction;
import br.com.oncast.ontrack.shared.model.action.TeamInviteAction;
import br.com.oncast.ontrack.shared.model.action.TeamRevogueInvitationAction;

public class ActionPostProcessmentsInitializer {

	private boolean initialized;
	private final ActionPostProcessingService postProcessingService;
	private final PersistenceService persistenceService;
	private final MulticastService multicastService;
	private final NotificationServerService notificationServerService;
	private NotificationCreationPostProcessor notificationCreationPostProcessor;
	private TeamActionPostProcessor sendActionToCurrentClientPostProcessor;
	private FileUploadPostProcessor fileUploadPostProcessor;
	private ScopeBindHumanIdPostProcessor scopeBindIdPostProcessor;
	private ScopeUpdatePostProcessor scopeUpdatePostProcessor;
	private IntegrationServiceProfileUpdateNotifierPostProcessor integrationServiceProfileUpdateNotifierPostProcessor;
	private final IntegrationService integrationService;
	private IntegrationServiceTeamInviteRevoguedNotifierPostProcessor integrationServiceTeamInviteRevoguedNotifierPostProcessor;

	public ActionPostProcessmentsInitializer(final ActionPostProcessingService actionPostProcessingService, final PersistenceService persistenceService, final MulticastService multicastService,
			final NotificationServerService notificationServerService, final IntegrationService integrationService) {
		this.postProcessingService = actionPostProcessingService;
		this.persistenceService = persistenceService;
		this.multicastService = multicastService;
		this.notificationServerService = notificationServerService;
		this.integrationService = integrationService;
	}

	@SuppressWarnings("unchecked")
	public synchronized void initialize() {
		if (initialized) return;
		postProcessingService.registerPostProcessor(getFileUploadPostProcessor(), FileUploadAction.class);
		postProcessingService.registerPostProcessor(getIntegrationServiceProfileUpdateNotifierPostProcessor(), TeamDeclareCanInviteAction.class, TeamDeclareReadOnlyAction.class);
		postProcessingService.registerPostProcessor(getIntegrationServiceTeamInviteRevoguedNotifierPostProcessor(), TeamRevogueInvitationAction.class);
		postProcessingService.registerPostProcessor(getTeamActionPostProcessor(), TeamInviteAction.class, TeamRevogueInvitationAction.class);
		postProcessingService.registerPostProcessor(getNotificationCreationPostProcessor(), ImpedimentCreateAction.class, ImpedimentSolveAction.class, ScopeDeclareProgressAction.class,
				AnnotationCreateAction.class, AnnotationDeprecateAction.class, TeamInviteAction.class, TeamRevogueInvitationAction.class, ScopeAddAssociatedUserAction.class);
		postProcessingService.registerPostProcessor(getScopeBindIdPostProcessor(), ScopeBindReleaseAction.class);
		postProcessingService.registerPostProcessor(getScopeUpdatePostProcessor(), ScopeUpdateAction.class);
		initialized = true;
	}

	private synchronized ActionPostProcessor<ScopeUpdateAction> getScopeUpdatePostProcessor() {
		if (scopeUpdatePostProcessor == null) {
			scopeUpdatePostProcessor = new ScopeUpdatePostProcessor(persistenceService, multicastService);
		}
		return scopeUpdatePostProcessor;
	}

	public synchronized ScopeBindHumanIdPostProcessor getScopeBindIdPostProcessor() {
		if (scopeBindIdPostProcessor == null) {
			scopeBindIdPostProcessor = new ScopeBindHumanIdPostProcessor(persistenceService);
		}
		return scopeBindIdPostProcessor;
	}

	public synchronized NotificationCreationPostProcessor getNotificationCreationPostProcessor() {
		if (notificationCreationPostProcessor == null) {
			notificationCreationPostProcessor = new NotificationCreationPostProcessor(notificationServerService, persistenceService);
		}
		return notificationCreationPostProcessor;
	}

	private synchronized ActionPostProcessor<TeamAction> getIntegrationServiceProfileUpdateNotifierPostProcessor() {
		if (integrationServiceProfileUpdateNotifierPostProcessor == null) {
			integrationServiceProfileUpdateNotifierPostProcessor = new IntegrationServiceProfileUpdateNotifierPostProcessor(integrationService, persistenceService);
		}
		return integrationServiceProfileUpdateNotifierPostProcessor;
	}

	private synchronized ActionPostProcessor<TeamRevogueInvitationAction> getIntegrationServiceTeamInviteRevoguedNotifierPostProcessor() {
		if (integrationServiceTeamInviteRevoguedNotifierPostProcessor == null) {
			integrationServiceTeamInviteRevoguedNotifierPostProcessor = new IntegrationServiceTeamInviteRevoguedNotifierPostProcessor(integrationService, persistenceService);
		}
		return integrationServiceTeamInviteRevoguedNotifierPostProcessor;
	}

	public synchronized ActionPostProcessor<TeamAction> getTeamActionPostProcessor() {
		if (sendActionToCurrentClientPostProcessor == null) {
			sendActionToCurrentClientPostProcessor = new TeamActionPostProcessor(multicastService, persistenceService);
		}
		return sendActionToCurrentClientPostProcessor;
	}

	public synchronized ActionPostProcessor<FileUploadAction> getFileUploadPostProcessor() {
		if (fileUploadPostProcessor == null) {
			fileUploadPostProcessor = new FileUploadPostProcessor(persistenceService);
		}
		return fileUploadPostProcessor;
	}

}
