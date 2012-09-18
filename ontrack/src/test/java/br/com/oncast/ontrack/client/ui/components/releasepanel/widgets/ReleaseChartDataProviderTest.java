package br.com.oncast.ontrack.client.ui.components.releasepanel.widgets;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionService;
import br.com.oncast.ontrack.client.ui.components.releasepanel.widgets.chart.ReleaseChartDataProvider;
import br.com.oncast.ontrack.shared.model.action.ReleaseDeclareEndDayAction;
import br.com.oncast.ontrack.shared.model.action.ReleaseDeclareEstimatedVelocityAction;
import br.com.oncast.ontrack.shared.model.action.ReleaseDeclareStartDayAction;
import br.com.oncast.ontrack.shared.model.effort.EffortInferenceEngine;
import br.com.oncast.ontrack.shared.model.progress.Progress.ProgressState;
import br.com.oncast.ontrack.shared.model.progress.ProgressInferenceEngine;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.release.ReleaseEstimator;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.model.user.User;
import br.com.oncast.ontrack.shared.model.uuid.UUID;
import br.com.oncast.ontrack.shared.utils.WorkingDay;
import br.com.oncast.ontrack.shared.utils.WorkingDayFactory;
import br.com.oncast.ontrack.utils.mocks.models.ScopeTestUtils;
import br.com.oncast.ontrack.utils.mocks.models.UserTestUtils;

import com.google.gwt.user.client.rpc.impl.ReflectionHelper;
import com.ibm.icu.util.Calendar;

public class ReleaseChartDataProviderTest {

	private ReleaseEstimator estimatorMock;
	private Release releaseMock;

	private WorkingDay estimatedEndDay;
	private WorkingDay estimatedStartDay;
	private Float releaseEffortSum;
	private ActionExecutionService actionExecutionServiceMock;
	private static List<Scope> releaseScopes;

	@Before
	public void setup() throws Exception {
		releaseMock = Mockito.mock(Release.class);
		releaseEffortSum = 10f;
		releaseScopes = new ArrayList<Scope>();
		setupReleaseMock();

		estimatorMock = Mockito.mock(ReleaseEstimator.class);
		estimatedStartDay = WorkingDayFactory.create();
		estimatedEndDay = WorkingDayFactory.create().add(5);
		setupEstimatorMock();

		actionExecutionServiceMock = Mockito.mock(ActionExecutionService.class);
	}

	public void verifyMocks() {
		Mockito.verify(estimatorMock, Mockito.atLeastOnce()).getEstimatedEndDayFor(releaseMock);
		Mockito.verify(estimatorMock, Mockito.atLeastOnce()).getEstimatedStartDayFor(releaseMock);
		Mockito.verify(releaseMock, Mockito.atLeastOnce()).getAllScopesIncludingDescendantReleases();
	}

	@Test
	public void releaseDaysShouldHaveOnlyTheEstimatedDayWhenEstimatedStartDayAndEstimatedEndDayAreEqual() throws Exception {
		estimatedEndDay = estimatedStartDay.copy();
		for (int i = 0; i < 10; i++) {
			estimatedStartDay.add(i);
			estimatedEndDay.add(i);
			assertEquals(estimatedStartDay, estimatedEndDay);
			assertReleaseDays(estimatedEndDay);
		}
		verifyMocks();
	}

	@Test
	public void releaseDaysShouldStartOnReleaseStartDay() throws Exception {
		assertEquals(estimatedStartDay.getDayAndMonthString(), getProvider().getReleaseDays().get(0).getDayAndMonthString());
		verifyMocks();
	}

	@Test
	public void releaseDaysShouldEndOnEstimatedEndDayWhenReleaseEndDayIsNull() throws Exception {
		final List<WorkingDay> releaseDays = getProvider().getReleaseDays();
		final int lastIndex = releaseDays.size() - 1;
		assertNull(releaseMock.getEndDay());
		assertEquals(estimatedEndDay, releaseDays.get(lastIndex));
		verifyMocks();
	}

	@Test
	public void releaseDaysShouldEndOnEstimatedEndDayWhenEstimatedEndDayIsAfterTheReleaseEndDay() throws Exception {
		Accomplish.effortPoints(1).on(estimatedEndDay.copy().add(-1));

		final List<WorkingDay> releaseDays = getProvider().getReleaseDays();
		final int lastIndex = releaseDays.size() - 1;

		assertTrue(estimatedEndDay.isAfter(releaseMock.getEndDay()));
		assertEquals(estimatedEndDay.getDayAndMonthString(), releaseDays.get(lastIndex).getDayAndMonthString());
		verifyMocks();
	}

	@Test
	public void releaseDaysShouldEndOnReleaseEndDayWhenTheReleaseEndDayIsAfterEstimatedEndDay() throws Exception {
		Accomplish.effortPoints(1).on(estimatedEndDay.copy().add(1));

		final List<WorkingDay> releaseDays = getProvider().getReleaseDays();
		final int lastIndex = releaseDays.size() - 1;

		final WorkingDay releaseEndDay = releaseMock.getEndDay();
		assertTrue(releaseEndDay.isAfter(estimatedEndDay));
		assertEquals(releaseEndDay.getDayAndMonthString(), releaseDays.get(lastIndex).getDayAndMonthString());
		verifyMocks();
	}

	@Test
	public void releaseDaysShouldContainAllDaysFromStartDayToEndDayInOrder() throws Exception {
		estimatedStartDay = WorkingDayFactory.create(2011, Calendar.JANUARY, 3);
		estimatedEndDay = WorkingDayFactory.create(2011, Calendar.JANUARY, 5);
		assertReleaseDays("03/01", "04/01", "05/01");
		verifyMocks();
	}

	@Test
	public void getEffortSumShouldReturnEffortSumOfTheRelease() throws Exception {
		for (int i = 0; i < 20; i++) {
			releaseEffortSum = (float) i;
			assertEquals(releaseEffortSum, getProvider().getEffortSum());
		}
	}

	@Test
	public void getEstimatedEndDayShouldReturnTheReleaseEstimatorsEstimatedEndDay() throws Exception {
		for (int i = 0; i < 20; i++) {
			estimatedEndDay.add(i);
			assertEquals(estimatedEndDay, getProvider().getEstimatedEndDay());
		}
		Mockito.verify(estimatorMock, Mockito.atLeast(20)).getEstimatedEndDayFor(releaseMock);
	}

	@Test
	public void accomplishedEffortByDateShouldHaveOnlyOneZeroWhenReleaseEffortSumIsZero() throws Exception {
		releaseEffortSum = 0f;
		assertAccomplishedEffortsByDate(0);
		estimatedStartDay = WorkingDayFactory.create(2011, Calendar.JANUARY, 3);
		setReleaseDuration(3);
		assertAccomplishedEffortsByDate(0);
		verifyMocks();
	}

	@Test
	public void shouldNotHaveAccomplishedEffortAfterToday() throws Exception {
		setReleaseDuration(20);
		Accomplish.effortPoints(5).today();
		Accomplish.effortPoints(13).on(WorkingDayFactory.create().add(5));

		assertAccomplishedEffortsByDate(5);
		verifyMocks();
	}

	@Test
	public void shouldNotReplicateAccomplishedEffortAfterReachingTheReleaseEffortSum() throws Exception {
		releaseEffortSum = 10f;
		final WorkingDay startDay = WorkingDayFactory.create().add(-10);
		estimatedStartDay = startDay.copy();
		setReleaseDuration(5);
		Accomplish.effortPoints(5).on(startDay);
		Accomplish.effortPoints(5).on(startDay.copy().add(2));

		assertAccomplishedEffortsByDate(5, 5, 10);
		verifyMocks();
	}

	@Test
	public void getEstimatedVelocityShouldReturnTheDeclaredOneWhenAlreadyDeclared() throws Exception {
		final Float declaredVelocity = 1.5f;
		Mockito.when(releaseMock.hasDeclaredEstimatedVelocity()).thenReturn(true);
		Mockito.when(releaseMock.getEstimatedVelocity()).thenReturn(declaredVelocity);

		assertEquals(declaredVelocity, getProvider().getEstimatedVelocity());

		Mockito.verify(releaseMock).getEstimatedVelocity();
	}

	@Test
	public void getEstimatedVelocityShouldReturnTheInferedOneWhenNotDeclared() throws Exception {
		final Float inferedVelocity = 5.6f;

		Mockito.when(releaseMock.hasDeclaredEstimatedVelocity()).thenReturn(false);
		Mockito.when(estimatorMock.getInferedEstimatedVelocityOnDay(Mockito.any(WorkingDay.class))).thenReturn(inferedVelocity);

		assertEquals(inferedVelocity, getProvider().getEstimatedVelocity());

		Mockito.verify(releaseMock, Mockito.never()).getEstimatedVelocity();
		Mockito.verify(estimatorMock).getInferedEstimatedVelocityOnDay(Mockito.any(WorkingDay.class));
	}

	@Test
	public void shouldRequestReleaseDeclareStartDayActionWhenDeclareStartDayWereCalled() throws Exception {
		final Date declaredDate = new Date();
		final UUID releaseId = new UUID();
		when(releaseMock.getId()).thenReturn(releaseId);

		getProvider().declareStartDate(declaredDate);

		final ArgumentCaptor<ReleaseDeclareStartDayAction> captor = ArgumentCaptor.forClass(ReleaseDeclareStartDayAction.class);
		verify(actionExecutionServiceMock).onUserActionExecutionRequest(captor.capture());
		verify(releaseMock).getId();

		final ReleaseDeclareStartDayAction action = captor.getValue();
		assertEquals(releaseId, action.getReferenceId());
		assertEquals(declaredDate, ReflectionHelper.getField(ReleaseDeclareStartDayAction.class, action, "date"));
	}

	@Test
	public void shouldRequestReleaseDeclareEndDayActionWhenDeclareEndDayWereCalled() throws Exception {
		final Date declaredDate = new Date();
		final UUID releaseId = new UUID();
		when(releaseMock.getId()).thenReturn(releaseId);

		getProvider().declareEndDate(declaredDate);

		final ArgumentCaptor<ReleaseDeclareEndDayAction> captor = ArgumentCaptor.forClass(ReleaseDeclareEndDayAction.class);
		verify(actionExecutionServiceMock).onUserActionExecutionRequest(captor.capture());
		verify(releaseMock).getId();

		final ReleaseDeclareEndDayAction action = captor.getValue();
		assertEquals(releaseId, action.getReferenceId());
		assertEquals(declaredDate, ReflectionHelper.getField(ReleaseDeclareEndDayAction.class, action, "endDay"));
	}

	@Test
	public void shouldRequestReleaseDeclareEstimatedVelocityActionWhenDeclareEstimatedVelocityWereCalled() throws Exception {
		final Float declaredVelocity = 12.5f;
		final UUID releaseId = new UUID();
		when(releaseMock.getId()).thenReturn(releaseId);

		getProvider().declareEstimatedVelocity(declaredVelocity);

		final ArgumentCaptor<ReleaseDeclareEstimatedVelocityAction> captor = ArgumentCaptor.forClass(ReleaseDeclareEstimatedVelocityAction.class);
		verify(actionExecutionServiceMock).onUserActionExecutionRequest(captor.capture());
		verify(releaseMock).getId();

		final ReleaseDeclareEstimatedVelocityAction action = captor.getValue();
		assertEquals(releaseId, action.getReferenceId());
		assertEquals(declaredVelocity, ReflectionHelper.getField(ReleaseDeclareEstimatedVelocityAction.class, action, "estimatedVelocity"));
	}

	@Test
	public void shouldConsiderDescendantScopesProgressOnAccomplishedEffortCalculation() throws Exception {
		final WorkingDay startDay1 = WorkingDayFactory.create(2012, 6, 2);
		final WorkingDay endDay1 = WorkingDayFactory.create(2012, 6, 5);
		final WorkingDay startDay2 = WorkingDayFactory.create(2012, 6, 2);
		final WorkingDay endDay2 = WorkingDayFactory.create(2012, 6, 6);
		final WorkingDay startDay3 = WorkingDayFactory.create(2012, 6, 3);
		final WorkingDay endDay3 = WorkingDayFactory.create(2012, 6, 3);

		final Scope parent = ScopeTestUtils.createScope();
		final Scope child1 = ScopeTestUtils.createScope("child1", ProgressState.DONE, 1, startDay1, endDay1);
		final Scope child3 = ScopeTestUtils.createScope("child2", ProgressState.DONE, 2, startDay2, endDay2);
		final Scope child2 = ScopeTestUtils.createScope("child3", ProgressState.DONE, 3, startDay3, endDay3);

		parent.add(child1);
		parent.add(child2);
		parent.add(child3);

		releaseScopes.add(parent);

		releaseEffortSum = 6F;
		estimatedStartDay = WorkingDayFactory.create(2012, 6, 2);
		estimatedEndDay = WorkingDayFactory.create(2012, 6, 7);

		assertAccomplishedEffortsByDate(0, 3, 3, 4, 6);
	}

	@Test
	public void inferenceEngineShouldNotMessWithTheBurnUpDates() throws Exception {
		final WorkingDay parentCreation = WorkingDayFactory.create(2012, 6, 2);
		estimatedStartDay = WorkingDayFactory.create(2012, 6, 3);

		final WorkingDay childBAccomplishDay = WorkingDayFactory.create(2012, 6, 5);
		estimatedEndDay = WorkingDayFactory.create(2012, 6, 6);
		releaseEffortSum = 10F;

		final Scope parent = ScopeTestUtils.createScope(parentCreation);
		ScopeTestUtils.setDelcaredEffort(parent, releaseEffortSum);

		final Scope childB = ScopeTestUtils.createScope(parentCreation);
		parent.add(childB);
		processInference(parent, parentCreation);

		final Scope childA = ScopeTestUtils.createScope(estimatedStartDay);
		parent.add(childA);
		processInference(parent, estimatedStartDay);

		ScopeTestUtils.setProgress(childA, ProgressState.UNDER_WORK, estimatedStartDay);
		processInference(parent, estimatedStartDay);

		ScopeTestUtils.setProgress(childB, ProgressState.DONE, childBAccomplishDay);
		processInference(parent, childBAccomplishDay);

		ScopeTestUtils.setProgress(childA, ProgressState.DONE, estimatedEndDay);
		processInference(parent, estimatedEndDay);

		releaseScopes.add(parent);
		assertAccomplishedEffortsByDate(0, 0, 5, 10);
	}

	private void processInference(final Scope scope, final WorkingDay day) {
		final User admin = UserTestUtils.getAdmin();
		new ProgressInferenceEngine().process(scope, admin, day.getJavaDate());
		new EffortInferenceEngine().process(scope, admin, day.getJavaDate());
	}

	private void setReleaseDuration(final int nDays) {
		estimatedEndDay = estimatedStartDay.copy().add(nDays - 1);
	}

	private void assertAccomplishedEffortsByDate(final float... efforts) {
		final List<Float> list = new ArrayList<Float>(getProvider().getAccomplishedEffortPointsByDate().values());
		assertEquals(efforts.length, list.size());
		for (int i = 0; i < efforts.length; i++) {
			assertEquals(efforts[i], list.get(i));
		}
	}

	private void assertReleaseDays(final WorkingDay... days) {
		final List<WorkingDay> list = getProvider().getReleaseDays();
		assertEquals(days.length, list.size());
		for (int i = 0; i < days.length; i++) {
			assertEquals(days[i].getDayAndMonthString(), list.get(i).getDayAndMonthString());
		}

	}

	private void assertReleaseDays(final String... days) {
		final List<WorkingDay> list = getProvider().getReleaseDays();
		assertEquals(days.length, list.size());
		for (int i = 0; i < days.length; i++) {
			assertEquals(days[i], list.get(i).getDayAndMonthString());
		}
	}

	private void setupEstimatorMock() {
		Mockito.when(estimatorMock.getEstimatedStartDayFor(releaseMock)).thenAnswer(new Answer<WorkingDay>() {

			@Override
			public WorkingDay answer(final InvocationOnMock invocation) throws Throwable {
				return estimatedStartDay.copy();
			}
		});
		Mockito.when(estimatorMock.getEstimatedEndDayFor(releaseMock)).thenAnswer(new Answer<WorkingDay>() {

			@Override
			public WorkingDay answer(final InvocationOnMock invocation) throws Throwable {
				return estimatedEndDay.copy();
			}
		});
	}

	private void setupReleaseMock() {
		Mockito.when(releaseMock.getEffortSum()).thenAnswer(new Answer<Float>() {

			@Override
			public Float answer(final InvocationOnMock invocation) throws Throwable {
				return releaseEffortSum;
			}
		});
		Mockito.when(releaseMock.getAllScopesIncludingDescendantReleases()).thenAnswer(new Answer<List<Scope>>() {

			@Override
			public List<Scope> answer(final InvocationOnMock invocation) throws Throwable {
				return releaseScopes;
			}
		});
		Mockito.when(releaseMock.getInferedEndDay()).thenCallRealMethod();
		Mockito.when(releaseMock.getEndDay()).thenAnswer(new Answer<WorkingDay>() {
			@Override
			public WorkingDay answer(final InvocationOnMock invocation) throws Throwable {
				return releaseMock.getInferedEndDay();
			}
		});
	}

	private ReleaseChartDataProvider getProvider() {
		return new ReleaseChartDataProvider(releaseMock, estimatorMock, actionExecutionServiceMock);
	}

	private static class Accomplish {

		private final Scope scope;

		public Accomplish(final int effort) {
			scope = createScope(ProgressState.UNDER_WORK, effort);
			releaseScopes.add(scope);
		}

		public void today() {
			ScopeTestUtils.setProgress(scope, ProgressState.DONE);
		}

		public static Accomplish effortPoints(final int effort) {
			return new Accomplish(effort);
		}

		public void on(final WorkingDay workingDay) throws Exception {
			ScopeTestUtils.setEndDate(scope, workingDay);
		}

		private static Scope createScope(final ProgressState progress, final int effort) {
			final Scope scope = ScopeTestUtils.createScope("Scope " + effort);
			ScopeTestUtils.setProgress(scope, progress);
			ScopeTestUtils.setDelcaredEffort(scope, effort);
			return scope;
		}

	}
}
