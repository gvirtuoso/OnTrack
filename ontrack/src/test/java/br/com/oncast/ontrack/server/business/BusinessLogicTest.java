package br.com.oncast.ontrack.server.business;

import br.com.oncast.ontrack.server.business.actionPostProcessments.ActionPostProcessmentsInitializer;
import br.com.oncast.ontrack.server.model.project.ProjectSnapshot;
import br.com.oncast.ontrack.server.services.actionPostProcessing.ActionPostProcessingService;
import br.com.oncast.ontrack.server.services.actionPostProcessing.ActionPostProcessor;
import br.com.oncast.ontrack.server.services.authentication.AuthenticationManager;
import br.com.oncast.ontrack.server.services.authorization.AuthorizationManager;
import br.com.oncast.ontrack.server.services.email.MailService;
import br.com.oncast.ontrack.server.services.exportImport.xml.UserActionTestUtils;
import br.com.oncast.ontrack.server.services.integration.IntegrationService;
import br.com.oncast.ontrack.server.services.metrics.ServerAnalytics;
import br.com.oncast.ontrack.server.services.multicast.ClientManager;
import br.com.oncast.ontrack.server.services.multicast.MulticastService;
import br.com.oncast.ontrack.server.services.persistence.PersistenceService;
import br.com.oncast.ontrack.server.services.persistence.exceptions.NoResultFoundException;
import br.com.oncast.ontrack.server.services.persistence.exceptions.PersistenceException;
import br.com.oncast.ontrack.server.services.persistence.jpa.PersistenceServiceJpaImpl;
import br.com.oncast.ontrack.server.services.persistence.jpa.entity.ProjectAuthorization;
import br.com.oncast.ontrack.server.services.serverPush.CometClientConnection;
import br.com.oncast.ontrack.server.services.serverPush.ServerPushConnection;
import br.com.oncast.ontrack.server.services.session.Session;
import br.com.oncast.ontrack.server.services.session.SessionManager;
import br.com.oncast.ontrack.server.services.threadSync.SyncronizationService;
import br.com.oncast.ontrack.shared.exceptions.authentication.UserNotFoundException;
import br.com.oncast.ontrack.shared.exceptions.authorization.AuthorizationException;
import br.com.oncast.ontrack.shared.exceptions.authorization.PermissionDeniedException;
import br.com.oncast.ontrack.shared.exceptions.business.InvalidIncomingAction;
import br.com.oncast.ontrack.shared.exceptions.business.ProjectNotFoundException;
import br.com.oncast.ontrack.shared.exceptions.business.UnableToHandleActionException;
import br.com.oncast.ontrack.shared.exceptions.business.UnableToLoadProjectException;
import br.com.oncast.ontrack.shared.exceptions.business.UnableToRemoveProjectException;
import br.com.oncast.ontrack.shared.model.action.ActionContext;
import br.com.oncast.ontrack.shared.model.action.FileUploadAction;
import br.com.oncast.ontrack.shared.model.action.ModelAction;
import br.com.oncast.ontrack.shared.model.action.ScopeInsertChildAction;
import br.com.oncast.ontrack.shared.model.action.ScopeMoveLeftAction;
import br.com.oncast.ontrack.shared.model.action.ScopeMoveUpAction;
import br.com.oncast.ontrack.shared.model.action.ScopeUpdateAction;
import br.com.oncast.ontrack.shared.model.action.TeamInviteAction;
import br.com.oncast.ontrack.shared.model.action.TeamRevogueInvitationAction;
import br.com.oncast.ontrack.shared.model.action.UserAction;
import br.com.oncast.ontrack.shared.model.action.exceptions.UnableToCompleteActionException;
import br.com.oncast.ontrack.shared.model.file.FileRepresentation;
import br.com.oncast.ontrack.shared.model.project.Project;
import br.com.oncast.ontrack.shared.model.project.ProjectContext;
import br.com.oncast.ontrack.shared.model.project.ProjectRepresentation;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.user.Profile;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.shared.model.user.UserRepresentation;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.shared.services.actionExecution.ActionExecuter;
import br.com.oncast.ontrack.shared.services.actionSync.ModelActionSyncEvent;
import br.com.oncast.ontrack.shared.services.requestDispatch.ModelActionSyncRequest;
import br.com.oncast.ontrack.shared.services.requestDispatch.ProjectContextRequest;
import br.com.oncast.ontrack.utils.deepEquality.DeepEqualityTestUtils;
import br.com.oncast.ontrack.utils.mocks.actions.ActionTestUtils;
import br.com.oncast.ontrack.utils.mocks.models.UserRepresentationTestUtils;
import br.com.oncast.ontrack.utils.mocks.requests.ModelActionSyncTestUtils;
import br.com.oncast.ontrack.utils.model.FileRepresentationTestUtils;
import br.com.oncast.ontrack.utils.model.ProjectTestUtils;
import br.com.oncast.ontrack.utils.model.UserTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static br.com.oncast.ontrack.server.business.BusinessLogicTestFactory.businessLogic;

public class BusinessLogicTest {

	private static final UUID PROJECT_ID = new UUID();

	@Mock
	private PersistenceService persistence;

	@Mock
	private ClientManager clientManager;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private AuthorizationManager authorizationManager;

	@Mock
	private MulticastService multicast;

	@Mock
	private SessionManager sessionManager;

	@Mock
	private ActionContext actionContext;

	@Mock
	private ActionPostProcessmentsInitializer postProcessmentsInitializer;

	@Mock
	private IntegrationService integration;

	@Mock
	private ServerAnalytics serverAnalytics;

	private EntityManager entityManager;
	private ProjectRepresentation projectRepresentation;
	private BusinessLogic business;
	private User authenticatedUser;
	private User admin;
	private UserRepresentation adminRepresentation;
	private Project project;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);

		admin = UserTestUtils.getAdmin();
		adminRepresentation = UserRepresentationTestUtils.getAdmin();

		entityManager = Persistence.createEntityManagerFactory("ontrackPU").createEntityManager();
		projectRepresentation = assureProjectRepresentationExistance(PROJECT_ID);
		project = ProjectTestUtils.createProject();

		configureMockDefaultBehavior();

		when(actionContext.getUserId()).thenReturn(admin.getId());
		when(actionContext.getTimestamp()).thenReturn(new Date(0));
	}

	private void configureMockDefaultBehavior() throws Exception {
		authenticatedUser = UserTestUtils.createUser();
		configureToRetrieveAdmin();
		authenticateAndAuthorizeUser(authenticatedUser, PROJECT_ID);
		configureToRetrieveSnapshot(PROJECT_ID);

		when(authorizationManager.authorize(any(UUID.class), anyString(), any(Profile.class), anyBoolean())).thenReturn(new UUID());
	}

	private void configureToRetrieveAdmin() throws NoResultFoundException, PersistenceException, UserNotFoundException {
		when(persistence.retrieveUserById(admin.getId())).thenReturn(admin);
		when(authenticationManager.findUserByEmail(admin.getEmail())).thenReturn(admin);
	}

	private void authenticateAndAuthorizeUser(final User user, final UUID projectId) throws PersistenceException, NoResultFoundException {
		when(authenticationManager.getAuthenticatedUser()).thenReturn(user);
		final ProjectAuthorization authorization = mock(ProjectAuthorization.class);
		when(persistence.retrieveUserByEmail(user.getEmail())).thenReturn(user);
		when(persistence.retrieveUserById(user.getId())).thenReturn(user);
		when(persistence.retrieveProjectAuthorization(user.getId(), projectId)).thenReturn(authorization);

		project.addUser(UserRepresentationTestUtils.createUser(user));
		when(actionContext.getUserId()).thenReturn(user.getId());
	}

	private void configureToRetrieveSnapshot(final UUID projectId) throws Exception {
		final ProjectSnapshot snapshot = mock(ProjectSnapshot.class);
		when(persistence.retrieveProjectSnapshot(projectId)).thenReturn(snapshot);
		when(snapshot.getProject()).thenReturn(project);
	}

	@After
	public void tearDown() {
		entityManager.clear();
		entityManager.close();
	}

	@Test(expected = InvalidIncomingAction.class)
	public void invalidActionThrowsException() throws Exception {
		business = BusinessLogicTestFactory.createDefault();

		final ArrayList<ModelAction> actionList = new ArrayList<ModelAction>();
		actionList.add(new ScopeUpdateAction(new UUID("id"), "bllla"));

		business.handleIncomingActionSyncRequest(createModelActionSyncRequest(actionList));
	}

	@Test
	public void shouldSetReceiptTimestampToHandledActions() throws Exception {
		business = BusinessLogicTestFactory.businessLogic().with(persistence).with(multicast).create();
		final ModelAction action = new TeamInviteAction(new UUID(), Profile.CONTRIBUTOR);

		business.handleIncomingActionSyncRequest(createModelActionSyncRequest(Arrays.asList(action)));
		final UserAction persistedAction = capturePersistedActions().get(0);
		final Date persistedReceiptTimestamp = persistedAction.getReceiptTimestamp();
		assertNotNull(persistedReceiptTimestamp);
		final ModelActionSyncEvent syncEvent = captureMulticastedModelActionSyncEvent(projectRepresentation.getId());
		final Date multicastedReceiptTimestamp = syncEvent.getActionList().get(0).getReceiptTimestamp();
		assertNotNull(multicastedReceiptTimestamp);
		assertEquals(persistedReceiptTimestamp, multicastedReceiptTimestamp);
	}

	@Test
	public void shouldDoNothingWhenHandlingAlreadyHandledActions() throws Exception {
		business = BusinessLogicTestFactory.create(persistence);
		final UserAction action = UserActionTestUtils.create(new ScopeUpdateAction(new UUID("id"), "bllla"));
		final ArrayList<UserAction> actionList = new ArrayList<UserAction>();
		actionList.add(action);

		when(persistence.retrieveAction(PROJECT_ID, action.getId())).thenReturn(action);
		final long syncId = business.handleIncomingActionSyncRequest(createModelActionSyncRequestFromUserActions(actionList));
		verify(persistence, never()).persistActions(anyListOf(UserAction.class));
		assertEquals(action.getSequencialId(), syncId);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void shouldKeepNonHandledActionsWhenHandledAndNotHandledActionsAreSentInTheSameBatchForHandling() throws Exception {
		business = BusinessLogicTestFactory.create(persistence);
		final UserAction action = UserActionTestUtils.create(new TeamInviteAction(new UUID(), Profile.GUEST), admin.getId());
		final UserAction action2 = UserActionTestUtils.create(new TeamInviteAction(new UUID(), Profile.GUEST), admin.getId());
		final UserAction action3 = UserActionTestUtils.create(new TeamInviteAction(new UUID(), Profile.GUEST), admin.getId());
		final UserAction action4 = UserActionTestUtils.create(new TeamInviteAction(new UUID(), Profile.GUEST), admin.getId());
		final ArrayList<UserAction> actionList = new ArrayList<UserAction>();
		actionList.add(action);
		actionList.add(action2);
		actionList.add(action3);
		actionList.add(action4);

		when(persistence.retrieveAction(PROJECT_ID, action.getId())).thenReturn(action);
		when(persistence.retrieveAction(PROJECT_ID, action2.getId())).thenReturn(action2);
		final long lastPersistedSyncId = 123;
		when(persistence.persistActions(anyList())).thenReturn(lastPersistedSyncId);
		final long syncId = business.handleIncomingActionSyncRequest(createModelActionSyncRequestFromUserActions(actionList));
		final List<UserAction> persistedActions = capturePersistedActions();
		assertEquals(2, persistedActions.size());
		assertEquals(action3, persistedActions.get(0));
		assertEquals(action4, persistedActions.get(1));

		assertEquals(lastPersistedSyncId, syncId);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = InvalidIncomingAction.class)
	public void invalidActionIsNotPersisted() throws Exception {
		business = new BusinessLogicImpl(persistence, multicast, clientManager, authenticationManager, authorizationManager, sessionManager, mock(MailService.class), new SyncronizationService(),
				postProcessmentsInitializer, integration, serverAnalytics);

		final ArrayList<ModelAction> actionList = new ArrayList<ModelAction>();
		actionList.add(new ScopeMoveUpAction(UUID.INVALID_UUID));

		business.handleIncomingActionSyncRequest(createModelActionSyncRequest(actionList));

		verify(persistence, times(0)).persistActions(anyList());
	}

	@Test
	public void shouldConstructAScopeHierarchyFromActions() throws Exception {
		business = BusinessLogicTestFactory.createWithJpaPersistence();
		final Project project = ProjectTestUtils.createProject();
		final ProjectContext context = createContext(project);

		final List<UserAction> someActions = userAction(createSomeActionsWithRequiredUsers());
		for (final UserAction action : someActions) {
			ActionExecuter.executeAction(context, action);
		}

		business.handleIncomingActionSyncRequest(createModelActionSyncRequestFromUserActions(someActions));
		final Scope projectScope = loadProject().getProjectScope();

		DeepEqualityTestUtils.assertObjectEquality(project.getProjectScope(), projectScope);
	}

	private ProjectContext createContext(final Project project) {
		final ProjectContext context = new ProjectContext(project);
		return context;
	}

	/**
	 * The purpose of this test is to execute actions in both client and server sides. This test does not assert anything, but it is useful for checking actions being executed one after another and
	 * the conversion of actions into entities and vice-versa.
	 */
	@Test
	public void shouldPersistActionsAndTheirRollbacks() throws Exception {
		business = BusinessLogicTestFactory.createWithJpaPersistence();
		final Project project = ProjectTestUtils.createProject();
		final ProjectContext context = createContext(project);

		final List<UserAction> rollbackActions = new ArrayList<UserAction>();
		final List<UserAction> actions = userAction(createSomeActionsWithRequiredUsers());
		for (final UserAction action : actions) {
			final UserAction reverseAction = ActionExecuter.executeAction(context, action).getReverseUserAction();
			if (reverseAction != null) rollbackActions.add(reverseAction);
		}

		business.handleIncomingActionSyncRequest(createModelActionSyncRequestFromUserActions(actions));

		Collections.reverse(rollbackActions);
		business.handleIncomingActionSyncRequest(createModelActionSyncRequestFromUserActions(rollbackActions));
	}

	@Test
	public void loadProjectShouldGetEarliestSnapshotAndExecuteOnePendentAction() throws Exception {
		business = BusinessLogicTestFactory.createWithJpaPersistence();
		final Project project1 = loadProject();

		final List<UserAction> actionList = new ArrayList<UserAction>();
		actionList.add(userAction(new TeamInviteAction(admin.getId(), Profile.PROJECT_MANAGER)));
		final UserAction action = userAction(new ScopeInsertChildAction(project1.getProjectScope().getId(), "big son"));
		actionList.add(action);
		final ProjectContext context = new ProjectContext(project1);
		context.addUser(adminRepresentation);
		action.execute(context);

		business.handleIncomingActionSyncRequest(createModelActionSyncRequestFromUserActions(actionList));

		final Project project2 = loadProject();

		DeepEqualityTestUtils.assertObjectEquality(project1, project2);
	}

	@Test
	public void loadProjectShouldGetEarliestSnapshotAndExecuteTwoPendentActions() throws Exception {
		business = BusinessLogicTestFactory.createWithJpaPersistence();
		final Project project1 = loadProject();

		final ModelAction action1 = new ScopeInsertChildAction(project1.getProjectScope().getId(), "big son");
		final ProjectContext context = new ProjectContext(project1);
		context.addUser(adminRepresentation);
		action1.execute(context, actionContext);

		final List<ModelAction> actionList = new ArrayList<ModelAction>();
		actionList.add(new TeamInviteAction(admin.getId(), Profile.PROJECT_MANAGER));
		actionList.add(action1);

		final ModelAction action2 = new ScopeInsertChildAction(project1.getProjectScope().getId(), "small sister");
		action2.execute(context, actionContext);
		actionList.add(action2);

		business.handleIncomingActionSyncRequest(createModelActionSyncRequest(actionList));

		final Project project2 = loadProject();

		DeepEqualityTestUtils.assertObjectEquality(project1, project2);
	}

	@Test
	public void loadProjectShouldGetEarliestSnapshotAndExecuteManyPendentActions() throws Exception {
		business = BusinessLogicTestFactory.createWithJpaPersistence();
		final Project project1 = loadProject();

		final ProjectContext context = createContext(project1);
		final List<ModelAction> actionList = new ArrayList<ModelAction>();
		actionList.addAll(createSomeActionsWithRequiredUsers());

		for (final ModelAction action : actionList)
			action.execute(context, actionContext);

		business.handleIncomingActionSyncRequest(createModelActionSyncRequest(actionList));

		final Project project2 = loadProject();

		DeepEqualityTestUtils.assertObjectEquality(project1, project2);
	}

	@Test
	public void loadProjectShouldGetEarliestSnapshotAndExecuteManyPendentActions2() throws Exception {
		business = BusinessLogicTestFactory.createWithJpaPersistence();
		final Project project1 = loadProject();

		final List<ModelAction> actionList = executeActionsToProject(project1, createSomeActionsWithRequiredUsers());
		business.handleIncomingActionSyncRequest(createModelActionSyncRequest(actionList));

		final Project project2 = loadProject();

		DeepEqualityTestUtils.assertObjectEquality(project1, project2);

		final List<ModelAction> actionList2 = executeActionsToProject(project1, ActionTestUtils.getActions2());
		business.handleIncomingActionSyncRequest(createModelActionSyncRequest(actionList2));

		final Project project3 = loadProject();

		DeepEqualityTestUtils.assertObjectEquality(project1, project3);
	}

	@Test
	public void actionShouldNotUpdateOthersThanRelatedProject() throws Exception {
		final UUID OTHER_PROJECT_ID = new UUID();
		assureProjectRepresentationExistance(OTHER_PROJECT_ID);

		business = BusinessLogicTestFactory.createWithJpaPersistence();
		final Project project1 = loadProject();

		final ScopeInsertChildAction action = new ScopeInsertChildAction(project1.getProjectScope().getId(), "big son");
		final ProjectContext context = new ProjectContext(project1);
		context.addUser(adminRepresentation);
		action.execute(context, actionContext);

		final List<ModelAction> actionList = new ArrayList<ModelAction>();
		actionList.add(new TeamInviteAction(admin.getId(), Profile.PROJECT_MANAGER));
		actionList.add(action);
		business.handleIncomingActionSyncRequest(createModelActionSyncRequest(actionList));

		final Project updatedProject = loadProject(PROJECT_ID);
		final Project notUpdatedProject = loadProject(OTHER_PROJECT_ID);

		assertEquals(updatedProject.getProjectScope().getChild(0).getId(), action.getNewScopeId());
		assertTrue(notUpdatedProject.getProjectScope().getChildren().isEmpty());
	}

	@Test
	public void actionShouldOnlyUpdateRelatedProject() throws Exception {
		business = BusinessLogicTestFactory.createWithJpaPersistence();

		final Project project1 = loadProject();
		final List<ModelAction> actionList = executeActionsToProject(project1, createSomeActionsWithRequiredUsers());
		business.handleIncomingActionSyncRequest(createModelActionSyncRequest(actionList));

		final UUID OTHER_PROJECT_ID = new UUID();
		final ProjectRepresentation projectRepresentation2 = assureProjectRepresentationExistance(OTHER_PROJECT_ID);

		final Project project2 = loadProject(OTHER_PROJECT_ID);
		final List<ModelAction> actions2 = ActionTestUtils.getActions2();
		actions2.add(0, new TeamInviteAction(admin.getId(), Profile.PROJECT_MANAGER));
		final List<ModelAction> actionList2 = executeActionsToProject(project2, actions2);
		business.handleIncomingActionSyncRequest(createModelActionSyncRequest(actionList2, projectRepresentation2));

		final Project loadedProject1 = loadProject();
		final Project loadedProject2 = loadProject(OTHER_PROJECT_ID);
		DeepEqualityTestUtils.assertObjectEquality(project1, loadedProject1);
		DeepEqualityTestUtils.assertObjectEquality(project2, loadedProject2);
	}

	@Test
	public void shouldLoadProjectWithGivenId() throws Exception {
		final UUID projectId = new UUID();
		assureProjectRepresentationExistance(projectId);
		assureProjectRepresentationExistance(projectId);
		assureProjectRepresentationExistance(projectId);

		business = BusinessLogicTestFactory.createWithJpaPersistence();
		final Project loadedProject = loadProject(projectId);

		assertEquals(projectId, loadedProject.getProjectRepresentation().getId());
		assertEquals(ProjectTestUtils.DEFAULT_PROJECT_NAME, loadedProject.getProjectRepresentation().getName());
	}

	@Test(expected = ProjectNotFoundException.class)
	public void shouldNotLoadProjectIfInexistentIdIsGiven() throws Exception {
		business = BusinessLogicTestFactory.createWithJpaPersistence();
		final UUID inexistentProjectId = new UUID();
		loadProject(inexistentProjectId);
	}

	@Test
	public void shouldCreateANewProjectRepresentation() throws Exception {
		business = Mockito.spy(BusinessLogicTestFactory.create(persistence, authenticationManager));
		Mockito.doReturn(0L).when(business).handleIncomingActionSyncRequest(Mockito.any(ModelActionSyncRequest.class));

		final UUID projectId = new UUID();
		setupMocksToCreateProjectWithId(projectId);
		business.createProject("Name");

		final ArgumentCaptor<ProjectRepresentation> captor = ArgumentCaptor.forClass(ProjectRepresentation.class);
		verify(persistence).persistOrUpdateProjectRepresentation(captor.capture());

		final ProjectRepresentation createdProject = captor.getValue();
		assertEquals("Name", createdProject.getName());
	}

	@Test
	public void handleIncomingActionsShouldPostProcessActions() throws Exception {
		final ActionPostProcessingService postProcessingService = new ActionPostProcessingService();
		@SuppressWarnings("unchecked")
		final ActionPostProcessor<ScopeMoveLeftAction> postProcessor = mock(ActionPostProcessor.class);
		postProcessingService.registerPostProcessor(postProcessor, ScopeMoveLeftAction.class);

		business = BusinessLogicTestFactory.create(BusinessLogicTestFactory.businessLogic().with(persistence).with(authenticationManager).with(authorizationManager).with(postProcessingService));

		final List<ModelAction> actions = createSomeActionsWithRequiredUsers();
		final ModelActionSyncRequest actionSyncRequest = createModelActionSyncRequest(actions);

		business.handleIncomingActionSyncRequest(actionSyncRequest);

		verify(postProcessor, times(1)).process(any(ScopeMoveLeftAction.class), any(ActionContext.class), any(ProjectContext.class));
	}

	@Test
	public void createProjectShouldFailIfUsersProjectCreationQuotaValidationFails() throws Exception {
		doThrow(new PermissionDeniedException("")).when(authorizationManager).validateSuperUser(authenticatedUser.getId());
		try {
			BusinessLogicTestFactory.create(persistence, authenticationManager, authorizationManager).createProject("");
			Assert.fail("An authorization exception should have been thrown.");
		} catch (final Exception e) {} finally {
			verify(persistence, times(0)).persistOrUpdateProjectRepresentation(Mockito.any(ProjectRepresentation.class));
		}
	}

	@Test
	public void removeProjectShouldSetProjectRepresentationsRemovedAttributeToTrue() throws Exception {
		business = BusinessLogicTestFactory.create(persistence, authenticationManager, authorizationManager);
		final ProjectRepresentation project = ProjectTestUtils.createRepresentation();
		when(persistence.retrieveProjectRepresentation(project.getId())).thenReturn(project);

		business.removeProject(project.getId());

		verify(persistence).persistOrUpdateProjectRepresentation(project);
		assertEquals(true, project.removed());
	}

	@Test(expected = UnableToRemoveProjectException.class)
	public void shouldNotBeAbleToRemoveTheProjectWhenTheAuthenticatedUserDoesNotHaveAuthorizationForTheProject() throws Exception {
		business = BusinessLogicTestFactory.create(persistence, authenticationManager, authorizationManager);
		final UUID projectId = new UUID();
		doThrow(new AuthorizationException()).when(authorizationManager).assureActiveProjectAccessAuthorization(projectId);

		business.removeProject(projectId);
	}

	@Test(expected = UnableToRemoveProjectException.class)
	public void shouldNotBeAbleToRemoveTheProjectWhenTheAuthenticatedUserIsntSuperUser() throws Exception {
		business = BusinessLogicTestFactory.create(persistence, authenticationManager, authorizationManager);
		doThrow(new PermissionDeniedException()).when(authorizationManager).validateSuperUser(authenticatedUser.getId());

		business.removeProject(new UUID());
	}

	@Test
	public void bindClientToProjectAfterLoad() throws Exception {
		final ServerPushConnection clientId = new CometClientConnection("1", "lesession");

		projectRepresentation = ProjectTestUtils.createRepresentation(PROJECT_ID);
		when(persistence.persistOrUpdateProjectRepresentation(projectRepresentation)).thenReturn(projectRepresentation);

		final Session sessionMock = Mockito.mock(Session.class);
		when(sessionManager.getCurrentSession()).thenReturn(sessionMock);
		when(sessionMock.getThreadLocalClientId()).thenReturn(clientId);

		business = BusinessLogicTestFactory.create(persistence, multicast, clientManager, authenticationManager, sessionManager);

		final ProjectContextRequest request = new ProjectContextRequest(projectRepresentation.getId());
		business.loadProjectForClient(request).getProject();

		verify(clientManager, times(1)).bindClientToProject(clientId, request.getRequestedProjectId());
	}

	@Test
	public void shouldAuthorizeCurrentUserAfterProjectCreation() throws Exception {
		business = Mockito.spy(BusinessLogicTestFactory.create(persistence, authenticationManager, authorizationManager));
		Mockito.doReturn(0L).when(business).handleIncomingActionSyncRequest(Mockito.any(ModelActionSyncRequest.class));

		final UUID projectId = new UUID();
		setupMocksToCreateProjectWithId(projectId);
		when(authorizationManager.authorize(projectId, authenticatedUser.getEmail(), authenticatedUser.getGlobalProfile(), false)).thenReturn(authenticatedUser.getId());
		business.createProject("new Project");
		verify(authorizationManager).authorize(projectId, authenticatedUser.getEmail(), authenticatedUser.getGlobalProfile(), false);
		final ArgumentCaptor<ModelActionSyncRequest> captor = ArgumentCaptor.forClass(ModelActionSyncRequest.class);
		verify(business).handleIncomingActionSyncRequest(captor.capture());
		final ModelActionSyncRequest request = captor.getValue();
		assertTrue(request.getActionList().get(0).getModelAction() instanceof TeamInviteAction);
		assertEquals(projectId, request.getActionList().get(0).getProjectId());
		assertEquals(authenticatedUser.getId(), request.getActionList().get(0).getModelAction().getReferenceId());
	}

	@Test
	public void shouldAuthorizeAdminUserAfterProjectCreation() throws Exception {
		business = Mockito.spy(BusinessLogicTestFactory.create(persistence, authenticationManager, authorizationManager));
		Mockito.doReturn(0L).when(business).handleIncomingActionSyncRequest(Mockito.any(ModelActionSyncRequest.class));

		final ProjectRepresentation createdProject = setupMocksToCreateProjectWithId(new UUID());

		business.createProject("new Project");
		verify(authorizationManager).authorizeAdmin(createdProject);
		verify(authorizationManager).authorize(createdProject.getId(), authenticatedUser.getEmail(), authenticatedUser.getGlobalProfile(), false);
	}

	@Test(expected = AuthorizationException.class)
	public void notAuthorizedUserCannotExecuteActions() throws Exception {
		business = BusinessLogicTestFactory.create(persistence, authorizationManager);
		final ModelActionSyncRequest request = ModelActionSyncTestUtils.createModelActionSyncRequest();
		doThrow(new AuthorizationException()).when(authorizationManager).assureActiveProjectAccessAuthorization(request.getProjectId());

		business.handleIncomingActionSyncRequest(request);
	}

	@Test
	public void onlyAuthorizedUserCanExecuteActions() throws Exception {
		business = BusinessLogicTestFactory.create(persistence, authenticationManager, authorizationManager);
		final ModelActionSyncRequest request = ModelActionSyncTestUtils.createModelActionSyncRequestWithOneAction(PROJECT_ID);

		business.handleIncomingActionSyncRequest(request);

		verify(authorizationManager, atLeastOnce()).assureActiveProjectAccessAuthorization(request.getProjectId());
		verify(persistence).persistActions(eq(request.getActionList()));
	}

	@Test
	public void onlyAuthorizedProjectsAreReturnedToUser() throws Exception {
		business = BusinessLogicTestFactory.create(persistence, authenticationManager, authorizationManager);
		business.retrieveCurrentUserProjectList();
		verify(authorizationManager).listAuthorizedProjects(authenticatedUser.getId());
	}

	@Test
	public void onlyNotRemovedAuthorizedProjectsAreReturnedToUser() throws Exception {
		business = BusinessLogicTestFactory.create(persistence, authenticationManager, authorizationManager);
		final List<ProjectRepresentation> projectsList = new ArrayList<ProjectRepresentation>();
		final ProjectRepresentation project1 = ProjectTestUtils.createRepresentation();
		final ProjectRepresentation project2 = ProjectTestUtils.createRepresentation();
		projectsList.add(project1);
		projectsList.add(project2);
		projectsList.add(ProjectTestUtils.createRepresentation(true));
		when(authorizationManager.listAuthorizedProjects(authenticatedUser.getId())).thenReturn(projectsList);
		final List<ProjectRepresentation> retrievedList = business.retrieveCurrentUserProjectList();
		assertEquals(2, retrievedList.size());
		assertTrue(retrievedList.contains(project1));
		assertTrue(retrievedList.contains(project2));
	}

	@Test
	public void onlyAuthorizedUserCanLoadProjectForClient() throws Exception {
		business = BusinessLogicTestFactory.create(persistence, authenticationManager, authorizationManager, sessionManager);
		final ProjectContextRequest request = ModelActionSyncTestUtils.createProjectContextRequest(PROJECT_ID);

		final Session sessionMock = Mockito.mock(Session.class);
		when(sessionManager.getCurrentSession()).thenReturn(sessionMock);
		when(sessionMock.getThreadLocalClientId()).thenReturn(new CometClientConnection("", ""));

		business.loadProjectForClient(request).getProject();

		verify(authorizationManager, atLeastOnce()).assureActiveProjectAccessAuthorization(request.getRequestedProjectId());
	}

	@Test(expected = UnableToLoadProjectException.class)
	public void notAuthorizedUserCannotLoadProjectForClient() throws Exception {
		business = BusinessLogicTestFactory.create(persistence, authenticationManager, authorizationManager);

		final ProjectContextRequest request = ModelActionSyncTestUtils.createProjectContextRequest();
		doThrow(new AuthorizationException()).when(authorizationManager).assureActiveProjectAccessAuthorization(request.getRequestedProjectId());

		business.loadProjectForClient(request);
	}

	@Test
	public void actionsShouldBeBoundToAuthenticatedUser() throws Exception {
		business = BusinessLogicTestFactory.create(persistence, authenticationManager, authorizationManager);

		final List<ModelAction> actions = createSomeActionsWithRequiredUsers();
		final ModelActionSyncRequest actionSyncRequest = createModelActionSyncRequest(actions);
		business.handleIncomingActionSyncRequest(actionSyncRequest);

		verify(persistence).persistActions(eq(actionSyncRequest.getActionList()));
	}

	@Test
	public void actionsShouldBeBoundToAuthenticatedUser2() throws Exception {
		business = BusinessLogicTestFactory.create(persistence, authenticationManager, authorizationManager);

		Mockito.reset(authenticationManager);

		final UUID userId = new UUID();
		final User createdUser = UserTestUtils.createUser(userId);
		final List<UserAction> actions = UserActionTestUtils.create(ActionTestUtils.createSomeActions(admin, createdUser), userId, PROJECT_ID);
		final ModelActionSyncRequest actionSyncRequest = createModelActionSyncRequestFromUserActions(actions);

		authenticateAndAuthorizeUser(createdUser, PROJECT_ID);

		business.handleIncomingActionSyncRequest(actionSyncRequest);

		verify(persistence).persistActions(eq(actions));
	}

	@Test
	public void shouldCreateFileUploadActionWhenOnUploadCompletedWereCalled() throws Exception {
		final FileRepresentation fileRepresentation = FileRepresentationTestUtils.create();
		business = spy(BusinessLogicTestFactory.createDefault());

		business.onFileUploadCompleted(fileRepresentation);

		final ArgumentCaptor<ModelActionSyncRequest> captor = ArgumentCaptor.forClass(ModelActionSyncRequest.class);
		verify(business).handleIncomingActionSyncRequest(captor.capture());

		final ModelActionSyncRequest request = captor.getValue();
		assertEquals(fileRepresentation.getProjectId(), request.getProjectId());
		final ModelAction action = request.getActionList().get(0).getModelAction();
		assertTrue(action instanceof FileUploadAction);
		assertEquals(fileRepresentation.getId(), action.getReferenceId());
	}

	@Test(expected = AuthorizationException.class)
	public void unauthorizedUserCanNotRemoveOtherUsersAuthorizations() throws Exception {
		business = BusinessLogicTestFactory.create(businessLogic().with(authenticationManager).with(authorizationManager));
		final UUID projectId = new UUID();
		Mockito.doThrow(new AuthorizationException()).when(authorizationManager).assureActiveProjectAccessAuthorization(projectId);
		business.removeAuthorization(new UUID(), projectId);
		verify(authorizationManager).assureActiveProjectAccessAuthorization(projectId);
	}

	@Test
	public void authorizedSuperUsersCanRemoveOtherUsersAuthorizations() throws Exception {
		business = Mockito.spy(BusinessLogicTestFactory.create(businessLogic().with(authenticationManager).with(authorizationManager)));
		Mockito.doReturn(0L).when(business).handleIncomingActionSyncRequest(Mockito.any(ModelActionSyncRequest.class));
		final UUID userId = new UUID();
		final UUID projectId = userId;
		business.removeAuthorization(userId, projectId);
		verify(authorizationManager).removeAuthorization(projectId, userId);
		final ArgumentCaptor<ModelActionSyncRequest> captor = ArgumentCaptor.forClass(ModelActionSyncRequest.class);
		verify(business).handleIncomingActionSyncRequest(captor.capture());
		final ModelActionSyncRequest actionSyncRequest = captor.getValue();
		assertEquals(projectId, actionSyncRequest.getProjectId());
		assertEquals(1, actionSyncRequest.getActionList().size());
		final ModelAction action = actionSyncRequest.getActionList().get(0).getModelAction();
		assertTrue(action instanceof TeamRevogueInvitationAction);
		assertEquals(userId, action.getReferenceId());
	}

	@Test(expected = PermissionDeniedException.class)
	public void authorizedNonSuperUsersCanNotRemoveOtherUsersAuthorizations() throws Exception {
		business = BusinessLogicTestFactory.create(businessLogic().with(authenticationManager).with(authorizationManager));
		final UUID userId = new UUID();
		final UUID projectId = new UUID();
		Mockito.doThrow(new PermissionDeniedException()).when(authorizationManager).validateSuperUser(authenticatedUser.getId());
		business.removeAuthorization(userId, projectId);
	}

	@Test
	public void authorizedNonSuperUserCanRemoveItselfsAuthorization() throws Exception {
		business = Mockito.spy(BusinessLogicTestFactory.create(businessLogic().with(authenticationManager).with(authorizationManager)));
		Mockito.doReturn(0L).when(business).handleIncomingActionSyncRequest(Mockito.any(ModelActionSyncRequest.class));
		final UUID userId = authenticatedUser.getId();
		final UUID projectId = new UUID();
		business.removeAuthorization(userId, projectId);
		verify(authorizationManager).removeAuthorization(projectId, userId);
		verify(business).handleIncomingActionSyncRequest(Mockito.any(ModelActionSyncRequest.class));
	}

	@Test
	public void shouldRollbackGrantedAuthorizationWhenTeamInviteActionFails() throws Exception {
		final User user = UserTestUtils.createUser();
		final String userEmail = user.getEmail();
		final UUID projectId = new UUID();

		business = Mockito.spy(BusinessLogicTestFactory.create(businessLogic().with(authorizationManager)));

		when(authorizationManager.authorize(projectId, userEmail, Profile.PROJECT_MANAGER, false)).thenReturn(user.getId());
		Mockito.doThrow(new UnableToHandleActionException()).when(business).handleIncomingActionSyncRequest(Mockito.any(ModelActionSyncRequest.class));
		try {
			business.authorize(userEmail, projectId, Profile.PROJECT_MANAGER, false);
			fail("should not authorize user");
		} catch (final UnableToHandleActionException e) {
			verify(authorizationManager).removeAuthorization(projectId, user.getId());
		}
	}

	@Test
	public void shouldCreateUserWhenNoUserFoundOnCreateTrial() throws NoResultFoundException, PersistenceException {
		when(persistence.retrieveUserByEmail(Mockito.anyString())).thenReturn(null);

		business = Mockito.spy(BusinessLogicTestFactory.create(businessLogic().with(authorizationManager).with(authenticationManager).with(persistence)));
		final String userEmail = "example@example.com";
		business.createTrialUser(userEmail, Profile.ACCOUNT_MANAGER);
		verify(authenticationManager, Mockito.only()).createNewUser(userEmail, null, Profile.ACCOUNT_MANAGER);
	}

	@Test
	public void shouldNotCreateAnotherUserWhenFindOneOnCreateTrial() throws NoResultFoundException, PersistenceException {
		final User user = UserTestUtils.createUser();
		when(persistence.retrieveUserByEmail(Mockito.anyString())).thenReturn(user);

		business = Mockito.spy(BusinessLogicTestFactory.create(businessLogic().with(authorizationManager).with(authenticationManager).with(persistence)));
		final String userEmail = "example@example.com";
		business.createTrialUser(userEmail, Profile.ACCOUNT_MANAGER);
		verify(authenticationManager, Mockito.never()).createNewUser(userEmail, null, Profile.ACCOUNT_MANAGER);
	}

	private ModelActionSyncEvent captureMulticastedModelActionSyncEvent(final UUID projectId) {
		final ArgumentCaptor<ModelActionSyncEvent> eventCaptor = ArgumentCaptor.forClass(ModelActionSyncEvent.class);
		verify(multicast).multicastToAllUsersButCurrentUserClientInSpecificProject(eventCaptor.capture(), eq(projectId));
		return eventCaptor.getValue();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<UserAction> capturePersistedActions() throws PersistenceException {
		final ArgumentCaptor<List> persistenceCaptor = ArgumentCaptor.forClass(List.class);
		verify(persistence).persistActions(persistenceCaptor.capture());
		return persistenceCaptor.getValue();
	}

	private ProjectRepresentation setupMocksToCreateProjectWithId(final UUID projectId) throws PersistenceException, NoResultFoundException {
		final ProjectRepresentation projectRepresentation = ProjectTestUtils.createRepresentation(projectId);
		authenticateAndAuthorizeUser(authenticatedUser, projectId);

		when(persistence.retrieveProjectSnapshot(projectId)).thenThrow(new NoResultFoundException(null, null));
		when(persistence.persistOrUpdateProjectRepresentation(Mockito.any(ProjectRepresentation.class))).thenReturn(projectRepresentation);
		when(persistence.retrieveProjectRepresentation(projectId)).thenReturn(projectRepresentation);

		return projectRepresentation;
	}

	private List<ModelAction> executeActionsToProject(final Project project, final List<ModelAction> actions) throws UnableToCompleteActionException {
		final ProjectContext context = createContext(project);
		final List<ModelAction> actionList = new ArrayList<ModelAction>();

		for (final ModelAction action : actions) {
			actionList.add(action);
			action.execute(context, actionContext);
		}
		return actionList;
	}

	private ModelActionSyncRequest createModelActionSyncRequest(final List<ModelAction> actionList) {
		return createModelActionSyncRequest(actionList, projectRepresentation);
	}

	private ModelActionSyncRequest createModelActionSyncRequest(final List<ModelAction> actionList, final ProjectRepresentation project) {
		return new ModelActionSyncRequest(project.getId(), userAction(actionList, project, admin));
	}

	private List<UserAction> userAction(final List<ModelAction> modelActions, final ProjectRepresentation project, final User user) {
		final List<UserAction> list = new ArrayList<UserAction>();
		for (final ModelAction action : modelActions) {
			list.add(userAction(action, project, user));
		}
		return list;
	}

	private List<UserAction> userAction(final List<ModelAction> modelActions) {
		return userAction(modelActions, projectRepresentation, admin);
	}

	private List<ModelAction> createSomeActionsWithRequiredUsers() {
		final List<ModelAction> actions = ActionTestUtils.createSomeActions(admin, authenticatedUser);
		return actions;
	}

	private UserAction userAction(final ModelAction modelAction, final ProjectRepresentation project, final User user) {
		return UserActionTestUtils.create(modelAction, user.getId(), project.getId());
	}

	private UserAction userAction(final ModelAction modelAction) {
		return userAction(modelAction, projectRepresentation, admin);
	}

	private ModelActionSyncRequest createModelActionSyncRequestFromUserActions(final List<UserAction> actionList) {
		return new ModelActionSyncRequest(projectRepresentation.getId(), actionList);
	}

	private Project loadProject() throws UnableToLoadProjectException, ProjectNotFoundException {
		return loadProject(PROJECT_ID);
	}

	private Project loadProject(final UUID projectId) throws UnableToLoadProjectException, ProjectNotFoundException {
		return business.loadProject(projectId).getProject();
	}

	private ProjectRepresentation assureProjectRepresentationExistance(final UUID projectId) throws Exception {
		final ProjectRepresentation newProjectRepresentation = ProjectTestUtils.createRepresentation(projectId);
		new PersistenceServiceJpaImpl().persistOrUpdateProjectRepresentation(newProjectRepresentation);
		return newProjectRepresentation;
	}
}