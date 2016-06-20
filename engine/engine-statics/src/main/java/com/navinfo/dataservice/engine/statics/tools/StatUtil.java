package com.navinfo.dataservice.engine.statics.tools;

import java.math.BigDecimal;

public class StatUtil {

	public static double formatDouble(double d) {

		BigDecimal b = new BigDecimal(d);

		return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static void main(String[] args) {
		System.out.println(formatDouble(123456789.12345));

	}
}
