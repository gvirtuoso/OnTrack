package br.com.oncast.ontrack.shared.utils;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.datepicker.client.CalendarUtil;

@SuppressWarnings("deprecation")
public class WorkingDay implements Comparable<WorkingDay>, Serializable {

	private static final long serialVersionUID = 6260976764571783140L;

	private static final int INITIAL_YEAR = 1900;

	private Date javaDate;

	// IMPORTANT A package-visible default constructor is necessary for serialization. Do not remove this and do not set any field inside it.
	WorkingDay() {}

	WorkingDay(final int year, final int month, final int day) {
		final Date date = new Date();
		date.setYear(year - INITIAL_YEAR);
		date.setMonth(month);
		date.setDate(day);
		javaDate = CalendarUtil.copyDate(date);
	}

	WorkingDay(final Date date) {
		javaDate = CalendarUtil.copyDate(date);
	}

	public WorkingDay add(final int nWorkingDays) {
		final boolean forward = nWorkingDays >= 0;
		final int abs = Math.abs(nWorkingDays);
		for (int i = 0; i < abs; i++) {
			addOneWorkingDay(forward);
		}
		return this;
	}

	public boolean isBefore(final WorkingDay day) {
		if (day == null) return true;

		return getDaysBetween(day) > 0;
	}

	public boolean isBeforeOrSameDayOf(final WorkingDay day) {
		return getDaysBetween(day) >= 0;
	}

	public boolean isAfter(final WorkingDay day) {
		if (day == null) return true;

		return getDaysBetween(day) < 0;
	}

	/**
	 * Counts the number of working days until the given day If the given day is same day it will return 1 It's safe to give previous days, it will return the negative number of days representing x
	 * days ago
	 * 
	 * @param day
	 * @return the number of days until the given day (the given day is included)
	 */
	public int countTo(final WorkingDay day) {
		if (day == null) return 0;
		if (isAfter(day)) return -day.countTo(this);

		int nWorkingDays = 1; // Today included
		final WorkingDay copy = copy();
		while (copy.isBefore(day)) {
			copy.add(1);
			nWorkingDays++;
		}
		return nWorkingDays;
	}

	public Date getJavaDate() {
		return CalendarUtil.copyDate(javaDate);
	}

	public WorkingDay copy() {
		return new WorkingDay(javaDate);
	}

	private boolean isWeekend(final Date date) {
		return false;
	}

	private void addOneWorkingDay(final boolean forward) {
		CalendarUtil.addDaysToDate(javaDate, forward ? 1 : -1);
	}

	private int getDaysBetween(final WorkingDay day) {
		return CalendarUtil.getDaysBetween(javaDate, day.javaDate);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + javaDate.getYear();
		result = prime * result + javaDate.getMonth();
		result = prime * result + javaDate.getDate();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof WorkingDay)) return false;
		final WorkingDay other = (WorkingDay) obj;

		return javaDate.getYear() == other.javaDate.getYear() && javaDate.getMonth() == other.javaDate.getMonth() && javaDate.getDate() == other.javaDate.getDate();
	}

	@Override
	public int compareTo(final WorkingDay day) {
		if (equals(day)) return 0;
		return isBefore(day) ? -1 : 1;
	}

	@Override
	public String toString() {
		return getDayAndMonthString() + "/" + getYear();
	}

	public String getDayAndMonthString() {
		return fillWithZeroIfDataHasJustOneDigit(javaDate.getDate()) + "/" + fillWithZeroIfDataHasJustOneDigit(javaDate.getMonth() + 1);
	}

	public String getDayMonthShortYearString() {
		return getDayAndMonthString() + "/" + fillWithZeroIfDataHasJustOneDigit(javaDate.getYear() % 100);
	}

	public int getYear() {
		return javaDate.getYear() + INITIAL_YEAR;
	}

	private String fillWithZeroIfDataHasJustOneDigit(final int date) {
		String dateString = String.valueOf(date);
		if (dateString.length() == 1) dateString = "0" + dateString;

		return dateString;
	}

	public static WorkingDay getLatest(final WorkingDay... workingDays) {
		WorkingDay latest = null;
		for (final WorkingDay workingDay : workingDays) {
			latest = latest == null ? workingDay : latest.isAfter(workingDay) ? latest : workingDay;
		}
		return latest;
	}

	public static WorkingDay getEarliest(final WorkingDay... workingDays) {
		WorkingDay earliest = null;
		for (final WorkingDay day : workingDays) {
			earliest = earliest == null ? day : earliest.isBefore(day) ? earliest : day;
		}
		return earliest;
	}
}