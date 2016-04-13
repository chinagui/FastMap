package com.navinfo.dataservice.commons.timedomain.helper;

public class TimePoint {

	private String m_sTimePoint="";
	private int m_nYear;
	private int m_nMonth;
	private int m_nWeek;
	private int m_nSeason;
	private int m_nDay;
	private int m_nHour;
	private int m_nMinute;

	public TimePoint(String sTimePoint) {
		this.m_sTimePoint = sTimePoint;
	}

	public boolean parseGDFTime() {
		String sTimePoint;
		if (m_sTimePoint.isEmpty())
			return false;
		if (m_sTimePoint.charAt(0) != '('
				|| m_sTimePoint.charAt(m_sTimePoint.length() - 1) != ')')
			return false;
		else
			sTimePoint = TimeDecoderUtils.getSubStr(m_sTimePoint, 1,
					m_sTimePoint.length() - 1);

		char sInputChar = 0;
		String sDigiter = "";
		for (int i = 0; i < sTimePoint.length(); i++) {
			if ((sTimePoint.charAt(i) >= 'a' && sTimePoint.charAt(i) <= 'z')
					|| (sTimePoint.charAt(i) >= 'A' && sTimePoint.charAt(i) <= 'Z')) {
				if (!sDigiter.isEmpty()) {
					switch (sInputChar) {
					case 'y':
						m_nYear = Integer.valueOf(sDigiter);
						break;
					case 'M':
						m_nMonth = Integer.valueOf(sDigiter);
						break;
					case 't':
						m_nWeek = Integer.valueOf(sDigiter);
						break;
					case 'd':
						m_nDay = Integer.valueOf(sDigiter);
						break;
					case 'h':
						m_nHour = Integer.valueOf(sDigiter);
						break;
					case 'm':
						m_nMinute = Integer.valueOf(sDigiter);
						break;
					case 'z':
						m_nSeason = Integer.valueOf(sDigiter);
						break;
					}
					sDigiter = "";
				}
				sInputChar = sTimePoint.charAt(i);
			}
			if (sTimePoint.charAt(i) >= '0' && sTimePoint.charAt(i) <= '9') {
				sDigiter += sTimePoint.charAt(i);
			}
		}
		if (!sDigiter.isEmpty()) {
			switch (sInputChar) {
			case 'y':
				m_nYear = Integer.valueOf(sDigiter);
				break;
			case 'M':
				m_nMonth = Integer.valueOf(sDigiter);
				break;
			case 't':
				m_nWeek = Integer.valueOf(sDigiter);
				break;
			case 'd':
				m_nDay = Integer.valueOf(sDigiter);
				break;
			case 'h':
				m_nHour = Integer.valueOf(sDigiter);
				break;
			case 'm':
				m_nMinute = Integer.valueOf(sDigiter);
				break;
			case 'z':
				m_nSeason = Integer.valueOf(sDigiter);
				break;
			}
		}
		return true;
	}

	public int getYear() {
		return m_nYear;
	}

	public int getMonth() {
		return m_nMonth;
	}

	public int getWeek() {
		return m_nWeek;
	}

	public int getSeason() {
		return m_nSeason;
	}

	public int getDay() {
		return m_nDay;
	}

	public int getHour() {
		return m_nHour;
	}

	public int getMinute() {
		return m_nMinute;
	}
	
}
