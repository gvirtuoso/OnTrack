package br.com.oncast.ontrack.client.ui.components.releasepanel.widgets.chart;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import br.com.oncast.ontrack.client.services.actionExecution.ActionExecutionService;
import br.com.oncast.ontrack.shared.model.action.ReleaseDeclareEndDayAction;
import br.com.oncast.ontrack.shared.model.action.ReleaseDeclareEstimatedVelocityAction;
import br.com.oncast.ontrack.shared.model.action.ReleaseDeclareStartDayAction;
import br.com.oncast.ontrack.shared.model.release.Release;
import br.com.oncast.ontrack.shared.model.release.ReleaseEstimator;
import br.com.oncast.ontrack.shared.model.scope.Scope;
import br.com.oncast.ontrack.shared.utils.WorkingDay;
import br.com.oncast.ontrack.shared.utils.WorkingDayFactory;

public class ReleaseChartDataProvider {

	private final Release release;
	private final ReleaseEstimator releaseEstimator;
	private HashMap<WorkingDay, Float> accomplishedEffortByDate;
	private HashMap<WorkingDay, Float> accomplishedValueByDate;
	private List<WorkingDay> releaseDays;
	private final ActionExecutionService actionService;

	public ReleaseChartDataProvider(final Release release, final ReleaseEstimator estimator, final ActionExecutionService actionService) {
		this.release = release;
		this.releaseEstimator = estimator;
		this.actionService = actionService;
	}

	public float getEffortSum() {
		return release.getEffortSum();
	}

	public WorkingDay getInferedEstimatedEndDay() {
		return releaseEstimator.getEstimatedEndDayUsingInferedEstimatedVelocity(release);
	}

	public float getEstimatedVelocity() {
		return release.hasDeclaredEstimatedVelocity() ? release.getEstimatedVelocity() : releaseEstimator
				.getInferedEstimatedVelocityOnDay(getEstimatedStartDay());
	}

	public WorkingDay getEstimatedStartDay() {
		return releaseEstimator.getEstimatedStartDayFor(release);
	}

	public void declareStartDate(final Date date) {
		actionService.onUserActionExecutionRequest(new ReleaseDeclareStartDayAction(release.getId(), date));
	}

	public void declareEndDate(final Date date) {
		actionService.onUserActionExecutionRequest(new ReleaseDeclareEndDayAction(release.getId(), date));
	}

	public WorkingDay getEstimatedEndDay() {
		return releaseEstimator.getEstimatedEndDayFor(release);
	}

	public boolean hasDeclaredStartDay() {
		return release.hasDeclaredStartDay();
	}

	public boolean hasDeclaredEndDay() {
		return release.hasDeclaredEndDay();
	}

	public void declareEstimatedVelocity(final Float velocity) {
		actionService.onUserActionExecutionRequest(new ReleaseDeclareEstimatedVelocityAction(release.getId(), velocity));
	}

	public List<WorkingDay> getReleaseDays() {
		if (releaseDays == null) evaluateData();
		return new ArrayList<WorkingDay>(releaseDays);
	}

	public Map<WorkingDay, Float> getAccomplishedValuePointsByDate() {
		if (accomplishedValueByDate == null) evaluateData();
		return accomplishedValueByDate;
	}

	public Map<WorkingDay, Float> getAccomplishedEffortPointsByDate() {
		if (accomplishedEffortByDate == null) evaluateData();
		return accomplishedEffortByDate;
	}

	public void evaluateData() {
		accomplishedValueByDate = new LinkedHashMap<WorkingDay, Float>();
		accomplishedEffortByDate = new LinkedHashMap<WorkingDay, Float>();

		final float effortSum = getEffortSum();
		releaseDays = calculateReleaseDays();
		for (final WorkingDay releaseDay : releaseDays) {
			final Float accomplishedValue = getAccomplishedValueFor(releaseDay);
			final Float accomplishedEffort = getAccomplishedEffortFor(releaseDay);

			if (accomplishedValue != null) accomplishedValueByDate.put(releaseDay, accomplishedValue);
			if (accomplishedEffort != null) {
				accomplishedEffortByDate.put(releaseDay, accomplishedEffort);
				if (accomplishedEffort >= effortSum) break;
			}

		}
	}

	private List<WorkingDay> calculateReleaseDays() {
		final WorkingDay startDay = releaseEstimator.getEstimatedStartDayFor(release);
		final WorkingDay inferedEstimatedEndDay = releaseEstimator.getEstimatedEndDayUsingInferedEstimatedVelocity(release);
		final WorkingDay estimatedEndDay = releaseEstimator.getEstimatedEndDayFor(release);

		final WorkingDay lastReleaseDay = WorkingDay.getLatest(inferedEstimatedEndDay, estimatedEndDay, release.getInferedEndDay());

		final List<WorkingDay> releaseDays = new ArrayList<WorkingDay>();

		final int daysCount = startDay.countTo(lastReleaseDay);
		for (int i = 0; i < daysCount; i++) {
			releaseDays.add(startDay.copy().add(i));
		}

		return releaseDays;
	}

	private Float getAccomplishedValueFor(final WorkingDay day) {
		if (day.isAfter(WorkingDayFactory.create())) return null;

		float accomplishedValueSum = 0;
		for (final Scope scope : release.getAllScopesIncludingDescendantReleases()) {
			if (scope.getProgress().isDone() && scope.getProgress().getEndDay().isBeforeOrSameDayOf(day)) {
				accomplishedValueSum += scope.getValue().getInfered();
			}
		}
		return accomplishedValueSum;
	}

	private Float getAccomplishedEffortFor(final WorkingDay day) {
		if (day.isAfter(WorkingDayFactory.create())) return null;

		float accomplishedEffortSum = 0;
		for (final Scope scope : release.getAllScopesIncludingDescendantReleases()) {
			if (scope.getProgress().isDone() && scope.getProgress().getEndDay().isBeforeOrSameDayOf(day)) {
				accomplishedEffortSum += scope.getEffort().getInfered();
			}
		}
		return accomplishedEffortSum;
	}

	public boolean hasDeclaredEstimatedVelocity() {
		return release.hasDeclaredEstimatedVelocity();
	}

	public Float getCurrentVelocity() {
		final WorkingDay currentDay = WorkingDay.getEarliest(release.getEndDay(), WorkingDayFactory.create());
		final WorkingDay startDay = release.getStartDay();

		if (currentDay.isBefore(startDay)) return 0f;

		return getAccomplishedEffortFor(currentDay) / startDay.countTo(currentDay);
	}

	public float getValueSum() {
		return release.getValueSum();
	}
}