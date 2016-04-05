package com.navinfo.dataservice.commons.timedomain.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;

public class TimePeriod {

	private String m_sTimeSpan = "";

	private List<TimePoint> m_vTP = new ArrayList<TimePoint>();
	private Map<String, Pair<Integer, Integer>> m_mSPair = new HashMap<String, Pair<Integer, Integer>>();
	private String m_sWeekTime = "";

	public TimePeriod(String sTimeSpan) {
		this.m_sTimeSpan = sTimeSpan;
	}

	public boolean parseGDFTime()
	{
		if(m_sTimeSpan.isEmpty())
			return false;

		if(m_sTimeSpan.length() <= 2)
			return false;

		if(m_sTimeSpan.startsWith("[") && m_sTimeSpan.charAt(1) == '(' && m_sTimeSpan.endsWith("]") && m_sTimeSpan.charAt(m_sTimeSpan.length()-2) == ')'){
			int nBeg = 0;
			int nEnd = 0;
			String sTimePoint;
			for(int i = 1; i < m_sTimeSpan.length()-1; i++){
				if(m_sTimeSpan.charAt(i) == '(')
					nBeg = i;
				if(m_sTimeSpan.charAt(i) == ')'){
					nEnd = i;
					sTimePoint = TimeDecoderUtils.getSubStr(m_sTimeSpan, nBeg, nEnd + 1);
					if(!sTimePoint.isEmpty()){
						TimePoint TP = new TimePoint(sTimePoint);
						TP.parseGDFTime();
						m_vTP.add(TP);
					}
				}
			}
		}else if(m_sTimeSpan.charAt(0) == '[' && m_sTimeSpan.charAt(1) == '(' && m_sTimeSpan.endsWith("]") && m_sTimeSpan.charAt(m_sTimeSpan.length()-2) == '}'){
			int nWeekBegin = getWeekTimePoint(m_sTimeSpan,'(', ')');
			int nWeekEnd = getWeekTimePoint(m_sTimeSpan,'{', '}');

			if(nWeekBegin == -1 || nWeekEnd == -1)
			{
				return false;
			}

			if( (nWeekBegin < 1 || nWeekBegin > 7) || (nWeekEnd < 1 || nWeekEnd > 7))
			{
				return false;
			}

			if(nWeekBegin == 1)
			{
				m_sWeekTime += "周日到";

				if(nWeekEnd == 1)
				{
					m_sWeekTime = "周日";
				}else{
					m_sWeekTime += "周" + (nWeekEnd-1);
				}

			}else{
				m_sWeekTime += "周" + (nWeekBegin-1) + "到";

				if(nWeekEnd == 1)
				{
					m_sWeekTime = "周" + (nWeekBegin - 1);
				}else{
					nWeekEnd = (nWeekBegin - 1 + nWeekEnd - 1) % 7;
					if(nWeekEnd == 0)
					{
						m_sWeekTime  += "周日";
					}else{
						m_sWeekTime += "周" + (nWeekEnd);
					}
				}
			}
		}
		
		else if(m_sTimeSpan.charAt(0) == '(' && m_sTimeSpan.charAt(m_sTimeSpan.length() - 1) == ')'){
			if(computeWeekTime(m_sTimeSpan) == false)
				return false;
		}else {
			return false;
		}
		return true;
	}

	public Map<String, Pair<Integer, Integer>> getTimeSpan() {
		return m_mSPair;
	}

	public String getStrTimeSpan() {
		return m_sTimeSpan;
	}

	public String getWeekTime() {
		return m_sWeekTime;
	}

	public boolean computeTimeSpan() {
		if (m_vTP.size() != 2) {
			return false;
		}
		m_mSPair.put("year", new Pair<Integer, Integer>(m_vTP.get(0).getYear(),
				m_vTP.get(1).getYear()));
		m_mSPair.put("month", new Pair<Integer, Integer>(m_vTP.get(0)
				.getMonth(), m_vTP.get(1).getMonth()));
		m_mSPair.put("week", new Pair<Integer, Integer>(m_vTP.get(0).getWeek(),
				m_vTP.get(1).getWeek()));
		m_mSPair.put("season", new Pair<Integer, Integer>(m_vTP.get(0)
				.getSeason(), m_vTP.get(1).getSeason()));
		m_mSPair.put("day", new Pair<Integer, Integer>(m_vTP.get(0).getDay(),
				m_vTP.get(1).getDay()));
		m_mSPair.put("hour", new Pair<Integer, Integer>(m_vTP.get(0).getHour(),
				m_vTP.get(1).getHour()));
		m_mSPair.put("minute", new Pair<Integer, Integer>(m_vTP.get(0)
				.getMinute(), m_vTP.get(1).getMinute()));

		return true;
	}

	public boolean computeTime() {
		if (m_vTP.isEmpty()) {
			return false;
		}

		for (int i = 0; i < m_vTP.size(); i++) {
			if (computeTimeSpan() == false) {
				return false;
			}
		}

		return true;
	}

	private boolean computeWeekTime(String sIn) {
		if (sIn.isEmpty())
			return false;
		String sDigiter = "";
		for (int i = 0; i < sIn.length(); i++) {
			if (sIn.charAt(i) == 't' || sIn.charAt(i) == 'T') {
				if (!sDigiter.isEmpty()) {
					int nDig = Integer.valueOf(sDigiter);
					if (nDig >= 2 && nDig <= 7) {
						m_sWeekTime += "周" + (nDig - 1) + " ";
					} else if (nDig == 1) {
						m_sWeekTime += "周日";
					} else if (nDig == 8) {
						m_sWeekTime += "节假日";
					}
					sDigiter = "";
				}
			}
			if (sIn.charAt(i) >= '0' && sIn.charAt(i) <= '9') {
				sDigiter += sIn.charAt(i);
			}
		}
		if (!sDigiter.isEmpty()) {
			int nDig = Integer.valueOf(sDigiter);
			if (nDig >= 2 && nDig <= 7) {
				m_sWeekTime += "周" + (nDig - 1) + " ";
			} else if (nDig == 1) {
				m_sWeekTime += "周日";
			} else if (nDig == 8) {
				m_sWeekTime += "节假日";
			}
			sDigiter = "";
		}

		return true;
	}

	private int getWeekTimePoint(String sIn, char lc, char rc)
	{
		int nBeg = 0;
		int nEnd = 0;

		nBeg = m_sTimeSpan.indexOf(lc);
		nEnd = m_sTimeSpan.indexOf(rc);
		if(nBeg == -1 || nEnd == -1)
		{
			return -1;
		}

		String sTmp = TimeDecoderUtils.getSubStr(m_sTimeSpan, nBeg, nEnd + 1);
		if(sTmp.isEmpty() || sTmp.length() != 4)
		{
			return -1;
		}

		if(sTmp.charAt(2) >= '0' && sTmp.charAt(2) <= '9')
		{
			return sTmp.charAt(2) - '0';
		}
		
		return -1;
	}
}
