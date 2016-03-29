package com.navinfo.dataservice.commons.timedomain.helper;

import java.util.ArrayList;
import java.util.List;

public class vTimeResult {

	private List<TimeResult> m_vTimeResult= new ArrayList<TimeResult>();
	
	private List<String> m_vSOutResult= new ArrayList<String>();

	public void setTimeResult(List<TimeResult> TimeResult) {
		m_vTimeResult = TimeResult;
	}

	public List<TimeResult> getVTimeResult() {
		return m_vTimeResult;
	}

	public String getTimeResult() {
		for (int i = 0; i < m_vTimeResult.size(); i++) {
			String sOutResultTmp="";
			String sWeekTime = m_vTimeResult.get(i).getWeekTime();
			if (sOutResultTmp.indexOf(sWeekTime) == -1) {
				sOutResultTmp += m_vTimeResult.get(i).getTimeSpanResult();
			} else {
				StringBuffer f =new StringBuffer(m_vTimeResult.get(i).getTimeSpanResult());
				int n = m_vTimeResult.get(i).getTimeSpanResult().indexOf(sWeekTime);
				sOutResultTmp += f.replace(n, n+sWeekTime.length(), "");
			}
			
			m_vSOutResult.add(sOutResultTmp);
		}
		
		String sOut="";
		for(int i = 0; i < m_vSOutResult.size(); i++)
		{
			sOut += m_vSOutResult.get(i);
		}
		
		return sOut;
	}
}
