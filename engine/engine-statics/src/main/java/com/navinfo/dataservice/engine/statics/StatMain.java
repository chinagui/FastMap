package com.navinfo.dataservice.engine.statics;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.navinfo.dataservice.engine.statics.expect.ExpectStatusMain;
import com.navinfo.dataservice.engine.statics.expect.PoiCollectExpectMain;
import com.navinfo.dataservice.engine.statics.expect.PoiDailyExpectMain;
import com.navinfo.dataservice.engine.statics.expect.RoadCollectExpectMain;
import com.navinfo.dataservice.engine.statics.expect.RoadDailyExpectMain;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.poidaily.PoiDailyMain;
import com.navinfo.dataservice.engine.statics.roadcollect.RoadCollectMain;
import com.navinfo.dataservice.engine.statics.roaddaily.RoadDailyMain;
import com.navinfo.dataservice.engine.statics.season.PoiSeasonMain;
import com.navinfo.dataservice.engine.statics.season.RoadSeasonMain;

public class StatMain {
	// 定义统计库名称
	public static final String db_name = "fm_stat";
	// 定义当前统计时间
	private static final String stat_time = new SimpleDateFormat("yyyyMMddkkmmss").format(new Date());

	// 统计 collect
	private static final String flag_collect_poi = "cp";
	private static final String flag_collect_road = "cr";
	// 统计 daily
	private static final String flag_daily_poi = "dp";
	private static final String flag_daily_road = "dr";
	// 统计 month
	private static final String flag_month_poi = "mp";
	private static final String flag_month_road = "mr";
	// 统计 seasion
	private static final String flag_season_poi = "sp";
	private static final String flag_season_road = "sr";
	// 统计预期图
	private static final String flag_expect_stat="es";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String flag = String.valueOf(args[0]);
		if (flag == null) {
			System.exit(0);
		} else {
			if (flag.equalsIgnoreCase(flag_collect_poi)) {
				new PoiCollectMain(db_name, stat_time).runStat();
			} else if (flag.equalsIgnoreCase(flag_collect_road)) {
				new RoadCollectMain(db_name, stat_time).runStat();
			} else if (flag.equalsIgnoreCase(flag_daily_poi)) {
				new PoiDailyMain(db_name, stat_time).runStat();
			} else if (flag.equalsIgnoreCase(flag_daily_road)) {
				new RoadDailyMain(db_name, stat_time).runStat();
			} else if (flag.equalsIgnoreCase(flag_season_poi)) {
				new PoiSeasonMain(db_name, stat_time).runStat();
			} else if (flag.equalsIgnoreCase(flag_season_road)) {
				new RoadSeasonMain(db_name, stat_time).runStat();
			} else if (flag.equalsIgnoreCase(flag_expect_stat)) {
				new PoiCollectExpectMain(db_name, stat_time).runStat();
				new RoadCollectExpectMain(db_name, stat_time).runStat();
				new PoiDailyExpectMain(db_name, stat_time).runStat();
				new RoadDailyExpectMain(db_name, stat_time).runStat();
				new ExpectStatusMain(db_name, stat_time).runStat();
			}
		}
		System.exit(0);
	}
}
