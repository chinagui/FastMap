package com.navinfo.dataservice.commons.timedomain.helper;

import java.util.ArrayList;
import java.util.List;

public class vTimeResult {

	private List<TimeResult> m_vTimeResult= new ArrayList<TimeResult>();
	private String sOutResult="";

	public void setTimeResult(List<TimeResult> TimeResult) {
		m_vTimeResult = TimeResult;
	}

	public List<TimeResult> getVTimeResult() {
		return m_vTimeResult;
	}

	public String getTimeResult() {
		String sWeekTime="";
		for (int i = 0; i < m_vTimeResult.size(); i++) {
			sWeekTime = m_vTimeResult.get(i).getWeekTime();
			if (sOutResult.indexOf(sWeekTime) == -1) {
				sOutResult += m_vTimeResult.get(i).getTimeSpanResult();
			} else {
				sOutResult += m_vTimeResult.get(i).getTimeSpanResult()
						.replaceFirst(sWeekTime, "");
			}
		}
		return sOutResult;
	}
}
