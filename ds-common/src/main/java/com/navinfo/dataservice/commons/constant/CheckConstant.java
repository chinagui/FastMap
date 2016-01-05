package com.navinfo.dataservice.commons.constant;

public class CheckConstant {

	private static final String GLM08049 = "交限进入到退出线无通路";

	private static final String GLM08044_1 = "相同进入线，相同退出线，不同经过线的多组交限（排除卡车交限），交限号码不应相同";

	private static final String GLM08039 = "交限的限制类型不能为“未调查”";

	private static final String GLM08033 = "路口交限的进入线或退出线为交叉口link";

	public static String getCheckMessage(String ruleId) {
		switch (ruleId) {
		case "GLM08049":
			return GLM08049;
		case "GLM08044_1":
			return GLM08044_1;
		case "GLM08039":
			return GLM08039;
		case "GLM08033":
			return GLM08033;
		default:
			return null;
		}
	}
}
