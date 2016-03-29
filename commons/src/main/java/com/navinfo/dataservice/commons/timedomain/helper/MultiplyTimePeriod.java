package com.navinfo.dataservice.commons.timedomain.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import com.navinfo.dataservice.commons.timedomain.TimeDecoder;

public class MultiplyTimePeriod {

	private String m_sGDFTimeOld = "";

	private String m_sGDFTimeNew = "";

	private List<TimePeriod> m_vTimeSpan = new ArrayList<TimePeriod>();
	private String m_vComputeChars = "";
	private String m_sWeekTime = "";

	public MultiplyTimePeriod(String sGDFTimeOld) {
		this.m_sGDFTimeOld = sGDFTimeOld;
	}

	public boolean parseGDFTime() {
		if (m_sGDFTimeOld.isEmpty()) {
			System.out
					.println("sGDFTime in MultiplyTimePeriod.cpp is empty, please check!!!");
			return false;
		}
	
		int nB = m_sGDFTimeOld.indexOf('[');
		int nE = m_sGDFTimeOld.lastIndexOf(']');
		if (nB != 0 || nE != (m_sGDFTimeOld.length() - 1)) {
			System.out
					.println("sGDFTime in MultiplyTimePeriod.cpp is error!!! please check the input carefully");
			return false;
		} else {
			int nBeg = m_sGDFTimeOld.indexOf('*');
			int nEnd = m_sGDFTimeOld.lastIndexOf('*');
	
			if (TimeDecoderUtils
					.validPairChar(
							TimeDecoderUtils.getSubStr(m_sGDFTimeOld, 0, nBeg),
							'[', ']')
					&& TimeDecoderUtils.validPairChar(TimeDecoderUtils.getSubStr(
							m_sGDFTimeOld, nEnd + 1, m_sGDFTimeOld.length()),
							'[', ']')) {
				m_sGDFTimeNew = m_sGDFTimeOld;
			} else {
				if (TimeDecoderUtils.isNeedDelete(m_sGDFTimeOld)) {
					m_sGDFTimeNew = m_sGDFTimeOld.substring(nB + 1, nE);
				} else {
					m_sGDFTimeNew = m_sGDFTimeOld;
				}
			}
		}
		if (splitTimeDomain() == false) {
			System.out
					.println("getTimeSpan in MultiplyTimePeriod.cpp return false, please check!!!");
			return false;
		}
		return true;
	}

	private boolean splitTimeDomain() {
		if (m_sGDFTimeNew.isEmpty())
			return false;

		int nBeg = 0;
		int nEnd = 0;
		String sLeft = "";
		for (int i = 0; i < m_sGDFTimeNew.length(); i++) {
			char cTimeChar = m_sGDFTimeNew.charAt(i);
			if (cTimeChar == '*') {
				nEnd = i;
				sLeft = TimeDecoderUtils.getSubStr(m_sGDFTimeNew, nBeg, nEnd);
				boolean flag1 = TimeDecoderUtils.validPairChar(sLeft, '[', ']');
				boolean flag2 = TimeDecoderUtils.validPairChar(sLeft, '{', '}');
				boolean flag3 = TimeDecoderUtils.validPairChar(sLeft, '(', ')');
				if (flag1 && flag2 && flag3) {
					TimePeriod TP = new TimePeriod(sLeft);
					TP.parseGDFTime();
					m_vTimeSpan.add(TP);
					m_vComputeChars += m_sGDFTimeNew.charAt(nEnd);
					nBeg = nEnd + 1;
				}
			}
		}
		TimePeriod TP = new TimePeriod(m_sGDFTimeNew.substring(nBeg));
		TP.parseGDFTime();
		m_vTimeSpan.add(TP);

		return true;
	}

	public boolean computeTimeSpan(TimeResult TR, List<List<TimeResult>> vvTmp) {
		Map<String, Pair<Integer, Integer>> mSPair = new HashMap<String, Pair<Integer, Integer>>();
		int flag = 1000000;

		for (int i = 0; i < m_vTimeSpan.size(); i++) {
			if (!m_vTimeSpan.get(i).getTimeSpan().isEmpty()) {
				Map<String, Pair<Integer, Integer>> ms = m_vTimeSpan.get(i)
						.getTimeSpan();
				mSPair = ms;
				flag = i;
				break;
			}
		}
		
		for (int i = flag + 1; i < m_vTimeSpan.size(); i++) {
			Map<String, Pair<Integer, Integer>> mTmp = m_vTimeSpan.get(i)
					.getTimeSpan();
			if (mTmp.isEmpty())
				continue;

			Set<Map.Entry<String, Pair<Integer, Integer>>> set2 = mSPair
					.entrySet();
			for (Iterator<Map.Entry<String, Pair<Integer, Integer>>> iter = set2
					.iterator(); iter.hasNext();) {
				Map.Entry<String, Pair<Integer, Integer>> entry = (Map.Entry<String, Pair<Integer, Integer>>) iter
						.next();
				String key1 = entry.getKey();
				Pair<Integer, Integer> value1 = entry.getValue();

				Set<Map.Entry<String, Pair<Integer, Integer>>> set3 = mTmp
						.entrySet();
				for (Iterator<Map.Entry<String, Pair<Integer, Integer>>> it = set3
						.iterator(); it.hasNext();) {

					Map.Entry<String, Pair<Integer, Integer>> entry2 = (Map.Entry<String, Pair<Integer, Integer>>) it
							.next();
					String key2 = entry2.getKey();
					Pair<Integer, Integer> value2 = entry2.getValue();

					if (key1.equals(key2)) {
						Pair<Integer, Integer> value = TimeDecoderUtils
								.getIntersection(value1, value2);

						mSPair.put(key1, value);
					}
				}
			}
		}

		if (!mSPair.isEmpty()) {
			if (!vvTmp.isEmpty()) {
				boolean bflag = false;
				for (int i = 0; i < vvTmp.size(); i++) {
					for (int j = 0; j < vvTmp.get(i).size(); j++) {
						List<Map<String, Pair<Integer, Integer>>> vTmp = vvTmp
								.get(i).get(j).getTimeSpan();// TimeResult
						for (int k = 0; k < vTmp.size(); k++) {
							bflag=true;
									
							Set<Map.Entry<String, Pair<Integer, Integer>>> set2 = mSPair
									.entrySet();
							for (Iterator<Map.Entry<String, Pair<Integer, Integer>>> iter = set2
									.iterator(); iter.hasNext();) {
								Map.Entry<String, Pair<Integer, Integer>> entry = (Map.Entry<String, Pair<Integer, Integer>>) iter
										.next();
								String key1 = entry.getKey();
								Pair<Integer, Integer> value1 = entry
										.getValue();

								Set<Map.Entry<String, Pair<Integer, Integer>>> set3 = vTmp
										.get(i).entrySet();
								for (Iterator<Map.Entry<String, Pair<Integer, Integer>>> it = set3
										.iterator(); it.hasNext();) {

									Map.Entry<String, Pair<Integer, Integer>> entry2 = (Map.Entry<String, Pair<Integer, Integer>>) it
											.next();
									String key2 = entry2.getKey();
									Pair<Integer, Integer> value2 = entry2
											.getValue();

									if (key1.equals(key2)) {
										Pair<Integer, Integer> value = TimeDecoderUtils
												.getIntersection(value2,value1);

										vTmp.get(i).put(key2, value);
									}
								}
							}

							TR.insertTimeSpan(vTmp.get(i));
						}
						
						if(!vvTmp.get(i).get(j).getWeekTime().isEmpty()){
							TR.addWeekTime(vvTmp.get(i).get(j).getWeekTime());
						}
					}
				}
				
				if(!bflag){
					TR.insertTimeSpan(mSPair);
				}
			} else {
				TR.insertTimeSpan(mSPair);
			}
		} else {
			if(TR.getWeekTime().isEmpty())
			{
				return false;
			}
		}

		return true;
	}

	public boolean computeTime(TimeResult trResult) {
		if (m_vTimeSpan.isEmpty()) {
			return false;
		}
		List<List<TimeResult>> vvTmp = new ArrayList<List<TimeResult>>();
		for (int i = 0; i < m_vTimeSpan.size(); i++) {
			List<TimeResult> vTmp = new ArrayList<TimeResult>();
			if (m_vTimeSpan.get(i).computeTime() == false){
				if (m_vTimeSpan.get(i).getStrTimeSpan().isEmpty()) {
					System.out.println("第" + i
							+ "次计算错误 in MultiplyTimePeriod.cpp");
					return false;
				} 
				else if (m_vTimeSpan.get(i).getStrTimeSpan().indexOf('+') != -1) {
					String sTmp = m_vTimeSpan.get(i).getStrTimeSpan();
					TimeDecoder NGT = new TimeDecoder(sTmp);
					NGT.parseGDFTime();
					vTmp = NGT.computeTime();
				} else {
					trResult.addWeekTime(m_vTimeSpan.get(i).getWeekTime());
				}
				
				if (!vTmp.isEmpty()) {
					vvTmp.add(vTmp);
//					vTmp.clear();
				}
			}
			
		}
		trResult.setInputTimeSpan(m_sGDFTimeOld);
		if (computeTimeSpan(trResult, vvTmp) == false) {
			System.out.println("coputeTime is error in MultiplyTimePeriod");
			System.out.println(getGDFTime());
			return false;
		}

		return true;
	}

	public String getGDFTime() {
		return m_sGDFTimeOld;
	}

}
