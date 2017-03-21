package com.navinfo.dataservice.scripts;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.engine.statics.Import2Oracle;
import com.navinfo.dataservice.engine.statics.overview.OverviewBlockMain;
import com.navinfo.dataservice.engine.statics.overview.OverviewGroupMain;
import com.navinfo.dataservice.engine.statics.overview.OverviewMain;
import com.navinfo.dataservice.engine.statics.overview.OverviewProgramMain;
import com.navinfo.dataservice.engine.statics.overview.OverviewSubtaskMain;
import com.navinfo.dataservice.engine.statics.overview.OverviewTaskMain;
import com.navinfo.dataservice.engine.statics.poicollect.PoiCollectMain;
import com.navinfo.dataservice.engine.statics.poidaily.PoiDailyMain;
import com.navinfo.dataservice.engine.statics.poimonthly.PoiMonthlyMain;
import com.navinfo.dataservice.engine.statics.roadcollect.RoadCollectMain;
import com.navinfo.dataservice.engine.statics.roaddaily.RoadDailyMain;
import com.navinfo.dataservice.engine.statics.season.PoiSeasonMain;
import com.navinfo.dataservice.engine.statics.season.RoadSeasonMain;

/**
 * @ClassName: InitProjectScriptsInterface
 * @author Xiao Xiaowen
 * @date 2016-1-15 下午3:40:32
 * @Description: TODO
 */
public class StaticsInterface {

	// 定义统计库名称
	public static final String db_name = SystemConfigFactory.getSystemConfig()
			.getValue(PropConstant.fmStat);//"fm_stat";
	// 定义当前统计时间
	private static final String stat_time = new SimpleDateFormat("yyyyMMddkkmmss").format(new Date());

	// 统计 collect
	private static final String flag_collect_poi = "cp";
	private static final String flag_collect_road = "cr";
	// 统计 daily
	private static final String flag_daily_poi = "dp";
	private static final String flag_daily_road = "dr";
	//统计月编poi
	private static final String flag_monthly_poi = "mp";
	//统计子任务
	private static final String flag_subtask = "subtask";
	//统计blockman
	//private static final String flag_blockman = "blockman";
	//统计任务
	private static final String flag_task = "task";
	//统计项目
	private static final String flag_program = "program";
	//统计总概览
	private static final String flag_overview = "overview";
	//统计group概览
	private static final String flag_group_overview = "group";
	// 统计 month
//	private static final String flag_month_poi = "mp";
//	private static final String flag_month_road = "mr";
	// 统计 seasion
	private static final String flag_season_poi = "sp";
	private static final String flag_season_road = "sr";
	// 统计预期图
	//private static final String flag_expect_stat="es";
	//统计结果库
	//public static final String col_name_subtask = "fm_stat_overview_subtask";
	//public static final String col_name_blockman = "fm_stat_collect_overview_blockman";
	//public static final String col_name_task = "fm_stat_overview_task";
	//public static final String col_name_program = "fm_stat_overview_program";
	
	private static final String flag_imp_oracle="imp_oracle";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String flag = String.valueOf(args[0]);
		if (flag == null) {
			System.exit(0);
		} else {
			//初始化context
			JobScriptsInterface.initContext();
			if (flag.equalsIgnoreCase(flag_collect_poi)) {
				new PoiCollectMain(db_name, stat_time).runStat();
			} else if (flag.equalsIgnoreCase(flag_collect_road)) {
				new RoadCollectMain(db_name, stat_time).runStat();
			} else if (flag.equalsIgnoreCase(flag_daily_poi)) {
				new PoiDailyMain(db_name, stat_time).runStat();
			} else if (flag.equalsIgnoreCase(flag_daily_road)) {
				new RoadDailyMain(db_name, stat_time).runStat();
			} else if (flag.equalsIgnoreCase(flag_monthly_poi)) {
				new PoiMonthlyMain(db_name, stat_time).runStat();
			} else if (flag.equalsIgnoreCase(flag_subtask)) {
				new OverviewSubtaskMain(db_name, stat_time).runStat();
				Import2Oracle.impOracle(OverviewSubtaskMain.col_name_subtask);
			//}else if (flag.equalsIgnoreCase(flag_blockman)) {
			//	new OverviewBlockMain(db_name, stat_time).runStat();
			}else if (flag.equalsIgnoreCase(flag_task)) {
				new OverviewTaskMain(db_name, stat_time).runStat();
				Import2Oracle.impOracle(OverviewTaskMain.col_name_task);
			}else if (flag.equalsIgnoreCase(flag_program)) {
				new OverviewProgramMain(db_name, stat_time).runStat();
				Import2Oracle.impOracle(OverviewProgramMain.col_name_program);
			}else if (flag.equalsIgnoreCase(flag_overview)) {
				new OverviewMain(db_name, stat_time).runStat();
				Import2Oracle.impOracle(OverviewMain.col_name_overview_main);
			}else if (flag.equalsIgnoreCase(flag_group_overview)) {
				new OverviewGroupMain(db_name, stat_time).runStat();
				Import2Oracle.impOracle(OverviewGroupMain.col_name_group);
			}else if (flag.equalsIgnoreCase(flag_season_poi)) {
				new PoiSeasonMain(db_name, stat_time).runStat();
			} else if (flag.equalsIgnoreCase(flag_season_road)) {
				new RoadSeasonMain(db_name, stat_time).runStat();
			} 
//			else if (flag.equalsIgnoreCase(flag_expect_stat)) {
//				new PoiCollectExpectMain(db_name, stat_time).runStat();
//				new RoadCollectExpectMain(db_name, stat_time).runStat();
//				new PoiDailyExpectMain(db_name, stat_time).runStat();
//				new RoadDailyExpectMain(db_name, stat_time).runStat();
//				new ExpectStatusMain(db_name, stat_time).runStat();
//			}
		}
		System.exit(0);
	}
}
