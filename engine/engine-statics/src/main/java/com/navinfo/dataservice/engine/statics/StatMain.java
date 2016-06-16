package com.navinfo.dataservice.engine.statics;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.poidaily.PoiDailyMain;
import com.navinfo.dataservice.engine.statics.roadcollect.RoadCollectMain;

public class StatMain {
	// 定义统计库名称
	private static final String db_name = "fm_stat";
	// 定义当前统计时间
	private static final String stat_date = new SimpleDateFormat("yyyyMMdd").format(new Date());
	// 统计 collect
	private static final String flag_collect_poi = "cp";
	private static final String flag_collect_road = "cr";
	// 统计 daily
	private static final String flag_daily_poi = "dp";
	private static final String flag_daily_road = "dr";
	// 统计 daily
	private static final String flag_month_poi = "mp";
	private static final String flag_month_road = "mr";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// String flag = "cr";
		String flag = String.valueOf(args[0]);
		if (flag == null) {
			System.exit(0);
		} else {
			if (flag.equalsIgnoreCase(flag_collect_poi)) {
				new PoiCollectMain(db_name, stat_date).runStat();
			} else if (flag.equalsIgnoreCase(flag_collect_road)) {
				new RoadCollectMain(db_name, stat_date).runStat();
			} else if (flag.equalsIgnoreCase(flag_daily_poi)) {
				new PoiDailyMain(db_name, stat_date).runStat();
			}
		}

	}
}
