package com.navinfo.dataservice.engine.statics;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.navinfo.dataservice.engine.statics.poiCollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.poiDaily.PoiDailyMain;

public class StatMain {
	// 定义统计库名称
	private static final String db_name = "fm_stat";
	// 定义当前统计时间
	private static final String stat_date = new SimpleDateFormat("yyyyMMdd").format(new Date());
	// 统计 collect
	private static final String flag_collect_poi = "cp";
	private static final String flag_collect_road = "cr";
	// 统计 daily 大区库
	private static final String flag_daily_poi = "dp";
	private static final String flag_daily_road = "dr";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// String flag = "dp";
		String flag = String.valueOf(args[0]);
		if (flag == null) {
			System.exit(0);
		} else {
			if (flag.equalsIgnoreCase(flag_daily_poi)) {
				new PoiDailyMain(db_name, stat_date).runStat();
			} else if (flag.equalsIgnoreCase(flag_collect_poi)) {
				new PoiCollectMain(db_name, stat_date).runStat();
			}
		}

	}
}
