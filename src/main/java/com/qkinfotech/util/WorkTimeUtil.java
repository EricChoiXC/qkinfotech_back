package com.qkinfotech.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkTimeUtil {

	public static class DayInfo {
		
		private int type;

		private int workdaysOfYear;

		private int worksecsOfYear;

		private List<WorkTimePeriod> periods;
		
		public DayInfo(int type, int workdaysOfYear, int worksecsOfYear, List<WorkTimePeriod> periods) {
			super();
			this.type = type;
			this.workdaysOfYear = workdaysOfYear;
			this.worksecsOfYear = worksecsOfYear;
			this.periods = periods;
		}

		public int getType() {
			return type;
		}

		public int getWorkdaysOfYear() {
			return workdaysOfYear;
		}

		public int getWorksecsOfYear() {
			return worksecsOfYear;
		}

		public List<WorkTimePeriod> getPeriods() {
			return periods;
		}
		
	}
	
	public static class Builder {
		
		public static class Rule {
		
			private LocalDate startDate;
		
			private LocalDate endDate;
		
			private LocalTime startTime;

			private LocalTime endTime;

			public Rule(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {
				this.startDate = startDate;
				this.endDate = endDate;
				this.startTime = startTime;
				this.endTime = endTime;
			}

			public LocalDate getStartDate() {
				return startDate;
			}

			public LocalDate getEndDate() {
				return endDate;
			}

			public LocalTime getStartTime() {
				return startTime;
			}

			public LocalTime getEndTime() {
				return endTime;
			}
			
		}
		
		private List<Rule> rules = new ArrayList<Rule>();
		
		private Set<LocalDate> holidays = new HashSet<LocalDate>();
		
		private Set<LocalDate> workdays = new HashSet<LocalDate>();
		
		private int secondsOfWorkPerDay = 8 * 60 * 60;
				
		public Builder addRule(LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime ) {
			rules.add(new Rule(startDate, endDate, startTime, endTime ));
			return this;
		}
		
		public Builder addHolidays(LocalDate... holidays) {
			this.holidays.addAll(List.of(holidays));
			return this;
		}

		public Builder addHolidays(Set<LocalDate> holidays) {
			this.holidays.addAll(holidays);
			return this;
		}

		public Builder setHolidays(Set<LocalDate> holidays) {
			this.holidays = holidays;
			return this;
		}

		public Builder addWorkdays(LocalDate... workdays) {
			this.workdays.addAll(List.of(workdays));
			return this;
		}

		public Builder addWorkdays(Set<LocalDate> workdays) {
			this.workdays.addAll(workdays);
			return this;
		}

		public Builder setWorkdays(Set<LocalDate> workdays) {
			this.workdays = workdays;
			return this;
		}
		
		public Builder secondsOfWorkPerDay(int secondsOfWorkPerDay) {
			this.secondsOfWorkPerDay = secondsOfWorkPerDay;
			return this;
		}
		
		private DayInfo[] newDayInfo(int year) {
			LocalDate sdate = LocalDate.of(year, 1, 1);
			LocalDate edate = LocalDate.of(year, 12, 31);
			long days = ChronoUnit.DAYS.between(sdate, edate) + 1;
			return new DayInfo[(int) days];
		}
		
		private boolean isWorkDay(LocalDate date) {
			if(workdays.contains(date)) {
				return true;
			}
			
			if(holidays.contains(date)) {
				return false;
			}
			
			DayOfWeek dow = date.getDayOfWeek();
			
			return !(dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY);
		}

		private int getSecondsOfDay(List<WorkTimePeriod> periods) {
			int secondsOfDay = 0;
			for(WorkTimePeriod period : periods) {
				secondsOfDay += period.getSecondOfWorkTime();
			}
			return secondsOfDay;
		}
		
		private List<WorkTimePeriod> getPeriods(LocalDate date) {
			List<WorkTimePeriod> periods = new ArrayList<WorkTimePeriod>(); 
			for(int i = rules.size() - 1; i>= 0; --i) {
				Rule rule = rules.get(i);
				LocalDate start = rule.getStartDate();
				LocalDate end = rule.getEndDate();
				if(start.isAfter(date) || end.isBefore(date)) {
					continue;
				}
				periods.add(new WorkTimePeriod(rule.getStartTime(), rule.getEndTime()));
			}
			return periods;
		}
		
		public WorkTimeUtil build(LocalDate start, LocalDate end) {
			Map<Integer, DayInfo[]> dayInfoOfYear = new HashMap<>();
			
			for(int year = start.getYear(); year <= end.getYear(); ++ year) {
				DayInfo[] dayInfos = newDayInfo(year);
				int workdaysOfYear = 0;
				int worksecsOfYear = 0;
				LocalDate sdate = LocalDate.of(year, 1, 1);
				for (int i = 0; i < dayInfos.length; ++i) {
					LocalDate date = sdate.plusDays(i);
					int type = isWorkDay(date)? 1 : 0;
					workdaysOfYear += type;
					List<WorkTimePeriod> periods = getPeriods(date);
					worksecsOfYear += type * getSecondsOfDay(periods);
					dayInfos[i] = new DayInfo(type, workdaysOfYear, worksecsOfYear, periods);
				}
				dayInfoOfYear.put(year, dayInfos);
			}
			
			return new WorkTimeUtil(dayInfoOfYear, secondsOfWorkPerDay);
		}

	}

	public static Builder builder() {
		return new Builder();
	}
	
	private Map<Integer, DayInfo[]> dayInfoOfYear = new HashMap<>();
	
	private int secondsOfWorkPerDay = 8 * 60 * 60;

	private WorkTimeUtil(Map<Integer, DayInfo[]> dayInfoOfYear, int secondsOfWorkPerDay) {
		this.dayInfoOfYear = dayInfoOfYear;
		this.secondsOfWorkPerDay = secondsOfWorkPerDay;
	}

	private List<WorkTimePeriod> getPeriods(LocalDate date) {
		int year = date.getYear();
		DayInfo[] di = dayInfoOfYear.get(year);
		int sIdx = date.getDayOfYear() - 1;
		
		return di[sIdx].getPeriods();
	}
	
	public boolean isWorkDay(LocalDate date) {
		int year = date.getYear();
		DayInfo[] di = dayInfoOfYear.get(year);
		int sIdx = date.getDayOfYear() - 1;
		return di[sIdx].getType() == 1;
	}
	
	public boolean isWorkTime(LocalDate date, LocalTime time) {
		int year = date.getYear();
		DayInfo[] di = dayInfoOfYear.get(year);
		int sIdx = date.getDayOfYear() - 1;
		if(di[sIdx].getType() == 1) {
			for(WorkTimePeriod period : di[sIdx].getPeriods()) {
				if(period.isWorkTime(time)) {
					return true;
				}
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	WorkTimeDiff getWorkTimeDiff(Date ds, Date de) {
		/* 这里使用过期方法，主要是考虑速度
		 * 使用 ds.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() 需要 100ms
		 * 使用过期方法运行时间小于 1ms
		 */
		LocalDate sd = LocalDate.of(ds.getYear()+1900, ds.getMonth() + 1, ds.getDate());
		LocalDate ed = LocalDate.of(de.getYear()+1900, de.getMonth() + 1, de.getDate());
		
		LocalTime st = LocalTime.of(ds.getHours(), ds.getMinutes(), ds.getSeconds());
		LocalTime et = LocalTime.of(de.getHours(), de.getMinutes(), de.getSeconds());
		
		
		if (sd.isAfter(ed)) {
			throw new IllegalArgumentException();
		}
		
		/*
		 * 计算逻辑：
		 * 计算间隔的工作日数
		 * 计算间隔的时间
		 * 开始时间如果是非工作日，则改变到最近的工作日的开始时间（日期，时间），向前
		 * 结束时间如果是非工作日，则改变到最近的工作日的结束时间（日期，时间），向后
		 * 如果调整后，开始时间大于结束时间，返回 0天 0秒
		 * 如果调整后，开始时间等于结束时间，计算当天的 秒数
		 */
		
		// 计算间隔的日数
		long days = 0;
		long totalSeconds = 0;
		LocalDate s = sd.plusDays(1);
		if(!sd.equals(ed)) {
			while (true) {
				int year = s.getYear();
				LocalDate e = LocalDate.of(year, 12, 31);
				if (e.isAfter(ed)) {
					e = ed;
					if(e.getYear() != year) {
						break;
					}
				}
				DayInfo[] di = dayInfoOfYear.get(year);
				int sDayIdx = s.getDayOfYear() - 1;
				int eDayIdx = e.getDayOfYear() - 1;
	
				days += di[eDayIdx].getWorkdaysOfYear() - di[sDayIdx].getWorkdaysOfYear();
				totalSeconds += di[eDayIdx].getWorksecsOfYear() - di[sDayIdx].getWorksecsOfYear();
				if (e.equals(ed)) {
					break;
				}
				s = LocalDate.of(year + 1, 1, 1);
			}
		}
		
		// 计算间隔时间
		int secondsOfDay = 0;
		List<WorkTimePeriod> speriods = getPeriods(sd);
		List<WorkTimePeriod> eperiods = getPeriods(ed);
		
		if(!isWorkDay(sd) && isWorkDay(ed)) {
			// 开始日非工作日。计算 结束日当天的时间
			for(WorkTimePeriod period : eperiods) {
				secondsOfDay += period.getSecondOfWorkTimeUntil(et);
			}
		} else if(isWorkDay(sd) && !isWorkDay(ed)) {
			// 结束日非工作日，计算 开始日当天的时间
			for(WorkTimePeriod period : speriods) {
				secondsOfDay += period.getSecondOfWorkTimeFrom(st);
			}
		
		} else if(isWorkDay(sd) && isWorkDay(ed)) {
			// 都是工作日
			if(sd.equals(ed)) {
				// 同一天
				for(WorkTimePeriod period : speriods) {
					secondsOfDay += period.getSecondOfWorkTimeUntil(et);
					secondsOfDay -= period.getSecondOfWorkTimeUntil(st);
				}
			} else {
				// 开始日当天时间 + 结束日当天时间
				for(WorkTimePeriod period : speriods) {
					secondsOfDay += period.getSecondOfWorkTimeFrom(st);
				}
				for(WorkTimePeriod period : eperiods) {
					secondsOfDay += period.getSecondOfWorkTimeUntil(et);
				}
			}
		} 
		totalSeconds += secondsOfDay;
		
		if(secondsOfDay > secondsOfWorkPerDay) {
			days ++;
			secondsOfDay -= secondsOfWorkPerDay;
		}
		return new WorkTimeDiff(days, secondsOfDay, totalSeconds);

	}

	public static void main(String[] args) throws ParseException {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Set<LocalDate> holidays = new HashSet<LocalDate>();
		holidays.add(LocalDate.of(2024,1,9));

		Set<LocalDate> workdays = new HashSet<LocalDate>();
		workdays.add(LocalDate.of(2024,1,6));

		Date sd = sdf.parse("2024-01-04 09:30:00");
		Date ed = sdf.parse("2024-01-08 09:40:01");
		
		long sm = System.currentTimeMillis();

		WorkTimeUtil wt = WorkTimeUtil.builder()
			.addHolidays(holidays)
			.addWorkdays(workdays)
			.addRule(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), LocalTime.of(9,0,0), LocalTime.of(12,0,0))
			.addRule(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), LocalTime.of(13,0,0), LocalTime.of(18,0,0))
			.build(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
		

		WorkTimeDiff diff = wt.getWorkTimeDiff(sd, ed);

		long em = System.currentTimeMillis();
		System.out.println(diff.toString());


		System.out.println(em - sm);

	}

}
