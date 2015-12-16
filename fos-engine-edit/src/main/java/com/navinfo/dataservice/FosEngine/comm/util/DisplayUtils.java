package com.navinfo.dataservice.FosEngine.comm.util;

public class DisplayUtils {

	public static int kind2Color(int kind) {
		if (kind == 13) {
			return 13;
		} else if (kind == 15) {
			return 14;
		} else {
			return kind + 1;
		}
	}
}
