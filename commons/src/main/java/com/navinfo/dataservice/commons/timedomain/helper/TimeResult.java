package com.navinfo.dataservice.commons.timedomain.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

public class TimeResult {

	private String m_sWeekTime = "";
	private String m_sInputTimeSpan = "";
	private List<Map<String, Pair<Integer, Integer>>> m_vTimeSpan = new ArrayList<Map<String, Pair<Integer, Integer>>>();

	// start time && end time
	private int m_nStartYear, m_nEndYear;
	private int m_nStartMonth, m_nEndMonth;
	private int m_nStartDay, m_nEndDay;
	private int m_nStartHour, m_nEndHour;
	private int m_nStartMinute, m_nEndMinute;
	private int m_nStartWeek, m_nEndWeek;

	public void setWeekTime(String sIn) {
		m_sWeekTime = sIn;
	}

	public String getWeekTime() {
		return m_sWeekTime;
	}

	public void addWeekTime(String sIn) {
		if (m_sWeekTime.isEmpty()) {
			m_sWeekTime = sIn;
		} else {
			m_sWeekTime += "," + sIn;
		}
	}

	public void insertTimeSpan(Map<String, Pair<Integer, Integer>> mTimeSpan) {
		m_vTimeSpan.add(mTimeSpan);
	}

	public List<Map<String, Pair<Integer, Integer>>> getTimeSpan() {
		return m_vTimeSpan;
	}

	public String getInputTimeSpan() {
		return m_sInputTimeSpan;
	}

	public void setInputTimeSpan(String sInput) {
		m_sInputTimeSpan = sInput;
	}

	public String getTimeSpanResult() {
		String sOutResult = "";

		if (!m_vTimeSpan.isEmpty()) {
			for (int i = 0; i < m_vTimeSpan.size(); i++) {

				Set<Map.Entry<String, Pair<Integer, Integer>>> set = m_vTimeSpan
						.get(i).entrySet();

				Iterator<Map.Entry<String, Pair<Integer, Integer>>> iter = set
						.iterator();

				while (iter.hasNext()) {

					Map.Entry<String, Pair<Integer, Integer>> me = iter.next();

					String key = me.getKey();

					Pair<Integer, Integer> value = me.getValue();

					if (key == "year") {
						m_nStartYear = value.getKey();
						m_nEndYear = value.getValue();
					} else if (key == "month") {
						m_nStartMonth = value.getKey();
						m_nEndMonth = value.getValue();
					} else if (key == "day") {
						m_nStartDay = value.getKey();
						m_nEndDay = value.getValue();
					} else if (key == "week") {
						m_nStartWeek = value.getKey();
						m_nEndWeek = value.getValue();
					} else if (key == "hour") {
						m_nStartHour = value.getKey();
						m_nEndHour = value.getValue();
					} else if (key == "minute") {
						m_nStartMinute = value.getKey();
						m_nEndMinute = value.getValue();
					}
				}
				sOutResult += getCHITimeResult() + "; ";
			}
		} else {
			sOutResult += m_sWeekTime + "; ";
		}
		return sOutResult;
	}

	private String getCHITimeResultOld() {
		String sStartTimeYearMonth = "", sEndTimeYearMonth = "";
		String sStartDay = "", sEndDay = "";
		String sStartWeek = "", sEndWeek = "";
		String sStartHour = "", sEndHour = "";

		if (m_nStartYear > 0) {
			sStartTimeYearMonth += m_nStartYear + "年";
		}
		if (m_nStartMonth > 0) {
			sStartTimeYearMonth += m_nStartMonth + "月";
		}
		if (m_nEndYear > 0) {
			sEndTimeYearMonth += m_nEndYear + "年";
		}
		if (m_nEndMonth > 0) {
			sEndTimeYearMonth += m_nEndMonth + "月";
		}

		// 计算日
		if (m_nStartDay > 0) {
			sStartDay += m_nStartDay;
		}
		if (m_nEndDay > 0) {
			sEndDay += m_nEndDay;
		}

		if (m_nStartWeek > 0) {
			if (m_nStartWeek >= 2 && m_nStartWeek <= 7)
				sStartWeek += "周" + (m_nStartWeek - 1);
			else if (m_nStartWeek == 1)
				sStartWeek += "周日";
		}
		if (m_nEndWeek > 0) {
			if (m_nEndWeek >= 2 && m_nEndWeek <= 7)
				sEndWeek += "周" + (m_nEndWeek - 1);
			else if (m_nEndWeek == 1)
				sEndWeek += "周日";
		}

		// 计算 时分秒
		if (m_nStartHour >= 0) {
			sStartHour += (m_nStartHour) + "时";
		}
		if (m_nStartMinute >= 0) {
			sStartHour += (m_nStartMinute) + "分";
		}
		if (m_nEndHour >= 0) {
			sEndHour += (m_nEndHour) + "时";
		}
		if (m_nEndMinute >= 0) {
			sEndHour += (m_nEndMinute) + "分";
		}

		String sOut = "";
		if (sStartDay.isEmpty() && sEndDay.isEmpty()) {
			if (sStartTimeYearMonth.isEmpty() && sEndTimeYearMonth.isEmpty()) {
				if (sStartWeek.isEmpty() && sEndWeek.isEmpty()) {
					sOut += m_sWeekTime + " " + sStartHour + "到" + sEndHour;
				} else {
					if (sStartWeek == sEndWeek) {
						sOut += sEndWeek + " " + sStartHour + "到" + sEndHour;
					} else
						sOut += sStartWeek + "到" + sEndWeek + " " + sStartHour
								+ "到" + sEndHour;
				}
			} else {
				if (sStartTimeYearMonth != sEndTimeYearMonth) {
					if (sStartWeek != sEndWeek) {
						sOut += sStartTimeYearMonth + "到" + sEndTimeYearMonth
								+ sStartWeek + "到" + sEndWeek + " "
								+ sStartHour + "到" + sEndHour;
					} else {
						sOut += sStartTimeYearMonth + "到" + sEndTimeYearMonth
								+ sStartWeek + " " + sStartHour + "到"
								+ sEndHour;
					}
				} else {
					if (sStartWeek != sEndWeek) {
						sOut += sEndTimeYearMonth + sStartWeek + "到" + sEndWeek
								+ " " + sStartHour + "到" + sEndHour;
					} else {
						sOut += sEndTimeYearMonth + sStartWeek + " "
								+ sStartHour + "到" + sEndHour;
					}
				}
			}
		} else {
			int nIdex = m_sInputTimeSpan.indexOf(sEndDay);
			if (nIdex != -1) {
				char cIn = m_sInputTimeSpan.charAt(nIdex + sEndDay.length());
				if (cIn == ')') {
					sOut += sStartTimeYearMonth + sStartDay + "日" + "到"
							+ sEndTimeYearMonth + sEndDay + "日" + ", "
							+ sStartHour + "到" + sEndHour;
				} else {
					sOut += sStartTimeYearMonth + sStartDay + "日" + sStartHour
							+ "到" + sEndTimeYearMonth + sEndDay + "日"
							+ sEndHour;
				}
			}
		}

		return sOut;
	}
	
	private String getCHITimeResult() {
        String sStartTimeYearMonth = "", sEndTimeYearMonth = "";
        String sStartDay = "", sEndDay = "";
        String sStartWeek = "", sEndWeek = "";
        String sStartHour = "", sEndHour = "";

        if (m_nStartYear > 0) {
            sStartTimeYearMonth += m_nStartYear + "年";
        }
        if (m_nStartMonth > 0) {
            sStartTimeYearMonth += m_nStartMonth + "月";
        }
        if (m_nEndYear > 0) {
            sEndTimeYearMonth += m_nEndYear + "年";
        }
        if (m_nEndMonth > 0) {
            sEndTimeYearMonth += m_nEndMonth + "月";
        }

        // 计算日
        if (m_nStartDay > 0) {
            sStartDay += m_nStartDay;
        }
        if (m_nEndDay > 0) {
            sEndDay += m_nEndDay;
        }

        if (m_nStartWeek > 0) {
            if (m_nStartWeek >= 2 && m_nStartWeek <= 7)
                sStartWeek += "周" + (m_nStartWeek - 1);
            else if (m_nStartWeek == 1)
                sStartWeek += "周日";
        }
        if (m_nEndWeek > 0) {
            if (m_nEndWeek >= 2 && m_nEndWeek <= 7)
                sEndWeek += "周" + (m_nEndWeek - 1);
            else if (m_nEndWeek == 1)
                sEndWeek += "周日";
        }

        // 计算 时分秒
        if (m_nStartHour >= 0)
        {
            if(m_nStartHour < 10)
            {
                sStartHour += "0";
            }
            sStartHour += m_nStartHour + ":";
        }
        if (m_nStartMinute >= 0)
        {
            if(m_nStartMinute < 10)
            {
                sStartHour += "0";
            }
            sStartHour += m_nStartMinute + "";
        }
        if (m_nEndHour >= 0)
        {
            if(m_nEndHour < 10)
            {
                sEndHour += "0";
            }
            sEndHour += m_nEndHour + ":";
        }
        if (m_nEndMinute >= 0)
        {
            if(m_nEndMinute < 10)
            {
                sEndHour += "0";
            }
            sEndHour += m_nEndMinute + "";
        }

        String sOut = "";
        if (sStartDay.isEmpty() && sEndDay.isEmpty()) {
            if (sStartTimeYearMonth.isEmpty() && sEndTimeYearMonth.isEmpty()) {
                if (sStartWeek.isEmpty() && sEndWeek.isEmpty()) {
                    sOut += m_sWeekTime + " " + sStartHour + "-" + sEndHour;
                } else {
                    if (sStartWeek == sEndWeek) {
                        sOut += sEndWeek + " " + sStartHour + "-" + sEndHour;
                    } else
                        sOut += sStartWeek + "-" + sEndWeek + " " + sStartHour
                                + "-" + sEndHour;
                }
            } else {
                if (sStartTimeYearMonth != sEndTimeYearMonth) {
                    if (sStartWeek != sEndWeek) {
                        sOut += sStartTimeYearMonth + "-" + sEndTimeYearMonth
                                + sStartWeek + "-" + sEndWeek + " "
                                + sStartHour + "-" + sEndHour;
                    } else {
                        sOut += sStartTimeYearMonth + "-" + sEndTimeYearMonth
                                + sStartWeek + " " + sStartHour + "-"
                                + sEndHour;
                    }
                } else {
                    if (sStartWeek != sEndWeek) {
                        sOut += sEndTimeYearMonth + sStartWeek + "-" + sEndWeek
                                + " " + sStartHour + "-" + sEndHour;
                    } else {
                        sOut += sEndTimeYearMonth + sStartWeek + " "
                                + sStartHour + "-" + sEndHour;
                    }
                }
            }
        } else {
            int nIdex = m_sInputTimeSpan.indexOf(sEndDay);
            if (nIdex != -1) {
                char cIn = m_sInputTimeSpan.charAt(nIdex + sEndDay.length());
                if (cIn == ')') {
                    sOut += sStartTimeYearMonth + sStartDay + "日" + "-"
                            + sEndTimeYearMonth + sEndDay + "日" + ", "
                            + sStartHour + "-" + sEndHour;
                } else {
                    sOut += sStartTimeYearMonth + sStartDay + "日" + sStartHour
                            + "-" + sEndTimeYearMonth + sEndDay + "日"
                            + sEndHour;
                }
            }
        }

        return sOut;
    }
	
}
