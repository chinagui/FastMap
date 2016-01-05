package com.navinfo.dataservice.commons.timedomain;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.timedomain.helper.MultiplyTimePeriod;
import com.navinfo.dataservice.commons.timedomain.helper.TimeDecoderUtils;
import com.navinfo.dataservice.commons.timedomain.helper.TimeResult;
import com.navinfo.dataservice.commons.timedomain.helper.vTimeResult;

public class TimeDecoder {

	private String m_sGDFTime = "";
	private List<MultiplyTimePeriod> m_vSubTimePeriod = new ArrayList<MultiplyTimePeriod>();
	private String m_vComputeChars = "";

	public TimeDecoder() {
	}

	public TimeDecoder(String sGDFTime) {
		this.m_sGDFTime = sGDFTime;
	}

	public boolean splitTimeDomain(String sGDFTime) {
		if (sGDFTime.isEmpty()) {
			System.out
					.println("sGDFTime in NavGdfTime.cpp is empty, please check!!!");
			return false;
		}

		String sLeft = "";

		int nBeg = 0;
		int nEnd = 0;

		for (int i = 0; i < sGDFTime.length(); i++) {

			char cTimeChar = sGDFTime.charAt(i);
			if (cTimeChar == '+' || cTimeChar == '-') {
				nEnd = i;
				sLeft = TimeDecoderUtils.getSubStr(sGDFTime, nBeg, nEnd);
				boolean flag1 = TimeDecoderUtils.validPairChar(sLeft, '[', ']');
				boolean flag2 = TimeDecoderUtils.validPairChar(sLeft, '{', '}');
				boolean flag3 = TimeDecoderUtils.validPairChar(sLeft, '(', ')');
				if (flag1 && flag2 && flag3) {
					MultiplyTimePeriod MTP = new MultiplyTimePeriod(sLeft);
					MTP.parseGDFTime();
					m_vSubTimePeriod.add(MTP);
					m_vComputeChars += sGDFTime.charAt(nEnd);
					nBeg = nEnd + 1;
				}

			}
		}
		MultiplyTimePeriod MTP = new MultiplyTimePeriod(
				sGDFTime.substring(nBeg));
		MTP.parseGDFTime();
		m_vSubTimePeriod.add(MTP);
		return true;
	}

	public boolean parseGDFTime() {
		if (m_sGDFTime.isEmpty())
			return false;

		String sGDFTime = m_sGDFTime;
		sGDFTime = sGDFTime.replace("\t", "");
		sGDFTime = sGDFTime.replace(" ", "");

		int sGDFTimeLength = TimeDecoderUtils.getPairCharPositon(sGDFTime, '[',
				']', 0, 1);
		if (sGDFTimeLength != sGDFTime.length() - 1) {
			System.out
					.println("input is error!!! please check the input correctly");
			return false;
		}

		int nBeg = sGDFTime.indexOf('[');
		int nEnd = sGDFTime.lastIndexOf(']');
		if (nBeg != 0 || nEnd != sGDFTime.length() - 1) {
			System.out
					.println("timespan is error!!! please check the input correctly");
			return false;
		} else {
			if (TimeDecoderUtils.isNeedDelete(sGDFTime))
				sGDFTime = sGDFTime.substring(nBeg + 1, nEnd);
		}

		if (splitTimeDomain(sGDFTime) == false) {
			System.out
					.println("splitTimeDomain in NavGdfTime.cpp return false, please check!!!");
			return false;
		}

		return true;
	}

	public List<TimeResult> computeTime() {
		List<TimeResult> noResult = new ArrayList<TimeResult>();
		List<TimeResult> Result = new ArrayList<TimeResult>();
		if (m_vSubTimePeriod.isEmpty()) {
			return noResult;
		}

		for (int i = 0; i < m_vSubTimePeriod.size(); i++) {
			TimeResult TR = new TimeResult();
			if (m_vSubTimePeriod.get(i).computeTime(TR) == false) {
				System.out.println("第" + i + "次计算错误 in NavGdfTime.cpp");
				System.out.println(getGDFTime());
				return noResult;
			}
			Result.add(TR);
		}
		return Result;
	}

	/**
	 * 解析时间域到中文
	 * @param sInputTime
	 * @return
	 */
	public String decode(String sInputTime) {

		m_sGDFTime = sInputTime;
		m_vSubTimePeriod = new ArrayList<MultiplyTimePeriod>();
		m_vComputeChars = "";
		parseGDFTime();
		vTimeResult Out = new vTimeResult();
		Out.setTimeResult(computeTime());
		return Out.getTimeResult();
	}

	public void setGDFTime(String sGDFTime) {
		m_sGDFTime = sGDFTime;
	}

	public String getGDFTime() {
		return m_sGDFTime;
	}

}
