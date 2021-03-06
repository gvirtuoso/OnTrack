package br.com.oncast.ontrack.client.ui.places;

import br.com.oncast.ontrack.client.services.ClientServices;
import br.com.oncast.ontrack.client.services.authentication.AuthenticationService;
import br.com.oncast.ontrack.client.services.context.ContextProviderService;
import br.com.oncast.ontrack.client.services.serverPush.ServerPushClientService;
import br.com.oncast.ontrack.client.ui.places.loading.ContextLoadingActivity;
import br.com.oncast.ontrack.client.ui.places.loading.ServerPushLoadingActivity;
import br.com.oncast.ontrack.client.ui.places.loading.UserInformationLoadingActivity;
import br.com.oncast.ontrack.client.ui.places.planning.PlanningActivity;
import br.com.oncast.ontrack.client.ui.places.planning.PlanningPlace;
import br.com.oncast.ontrack.client.ui.places.projectSelection.ProjectSelectionActivity;
import br.com.oncast.ontrack.client.ui.places.projectSelection.ProjectSelectionPlace;
import br.com.oncast.ontrack.shared.model.uuid.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.googlecode.gwt.test.GwtModule;
import com.googlecode.gwt.test.GwtTest;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@GwtModule("br.com.oncast.ontrack.Application")
public class AppActivityMapperTest extends GwtTest {

	private static final UUID PROJECT_ID = new UUID();
	private AppActivityMapper appActivityMapper;
	private Boolean isContextAvailable;
	private Boolean isServerPushConnected = true;

	@Mock
	private ContextProviderService contextProvider;
	@Mock
	private ClientServices clientServiceProvider;
	@Mock
	private AuthenticationService authenticationService;
	@Mock
	private ServerPushClientService serverPushClientService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		when(clientServiceProvider.authentication()).thenReturn(authenticationService);
		when(clientServiceProvider.serverPush()).thenReturn(serverPushClientService);
		when(clientServiceProvider.contextProvider()).thenReturn(contextProvider);
		when(authenticationService.isUserAvailable()).thenReturn(true);
		when(contextProvider.isContextAvailable(PROJECT_ID)).thenAnswer(new Answer<Boolean>() {

			@Override
			public Boolean answer(final InvocationOnMock invocation) throws Throwable {
				return isContextAvailable;
			}
		});

		when(serverPushClientService.isConnected()).thenAnswer(new Answer<Boolean>() {

			@Override
			public Boolean answer(final InvocationOnMock arg0) throws Throwable {
				return isServerPushConnected;
			}
		});
		appActivityMapper = new AppActivityMapper(clientServiceProvider);
	}

	@Test
	public void whenContextProviderNotAvailableShouldCreateContextLoadingActivity() {
		isContextAvailable = false;

		assertTrue(appActivityMapper.getActivity(new PlanningPlace(PROJECT_ID)) instanceof ContextLoadingActivity);
	}

	@Test
	public void whenServerPushIsNotConnectedShouldCreateServerPushLoadingActivity() {
		isServerPushConnected = false;

		assertTrue(appActivityMapper.getActivity(new PlanningPlace(PROJECT_ID)) instanceof ServerPushLoadingActivity);
	}

	@Test
	public void whenContextProviderIsAvailableShouldCreatePlanningPlaceActivity() {
		isContextAvailable = true;

		assertTrue(appActivityMapper.getActivity(new PlanningPlace(PROJECT_ID)) instanceof PlanningActivity);
	}

	@Test
	public void whenContextProviderIsAvailableAndProjectIdIsZeroShouldCreateAProjectSelectionActivity() {
		isContextAvailable = true;

		assertTrue(appActivityMapper.getActivity(new PlanningPlace(UUID.INVALID_UUID)) instanceof ProjectSelectionActivity);
	}

	@Test
	public void whenContextProviderIsAvailableAndProjectSelectionPlaceRequestedShouldCreateAProjectSelectionActivity() {
		isContextAvailable = true;

		assertTrue(appActivityMapper.getActivity(new ProjectSelectionPlace()) instanceof ProjectSelectionActivity);
	}

	@Test
	public void activityShouldBeNullWhenPlaceNotInstanceOfPlanningPlaceNorContextLoadingPlaceAndContextAvailable() {
		isContextAvailable = true;
		assertNull(appActivityMapper.getActivity(null));
	}

	@Test
	public void contextAvaliabilityShouldBeCheckedWhenProjectDependentPlaceIsPassed() {
		isContextAvailable = true;

		final ProjectDependentPlace projectDependentPlace = mock(ProjectDependentPlace.class);
		final UUID projectId = new UUID();
		when(projectDependentPlace.getRequestedProjectId()).thenReturn(projectId);
		appActivityMapper.getActivity(projectDependentPlace);

		verify(contextProvider).isContextAvailable(Mockito.any(UUID.class));
	}

	@Test
	public void contextAvaliabilityShouldNotBeCheckedWhenPassedPlaceIsNotAProjectDependentPlace() {
		final ProjectSelectionPlace projectIndependentPlace = mock(ProjectSelectionPlace.class);

		appActivityMapper.getActivity(projectIndependentPlace);

		verify(contextProvider, times(0)).isContextAvailable(Mockito.any(UUID.class));
	}

	@Test
	public void contextShouldBeLoadedForProjectDependentPlaces() {
		isContextAvailable = false;

		final ProjectDependentPlace projectDependentPlace = mock(ProjectDependentPlace.class);
		when(projectDependentPlace.getRequestedProjectId()).thenReturn(new UUID());

		assertTrue(appActivityMapper.getActivity(projectDependentPlace) instanceof ContextLoadingActivity);
	}

	@Test
	public void userDataShouldBeLoadedIfNotPresentWhenAnotherPlaceIsRequested() {
		when(authenticationService.isUserAvailable()).thenReturn(false);
		assertTrue(appActivityMapper.getActivity(new ProjectSelectionPlace()) instanceof UserInformationLoadingActivity);
	}
}
