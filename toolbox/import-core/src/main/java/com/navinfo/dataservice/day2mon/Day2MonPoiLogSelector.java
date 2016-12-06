package com.navinfo.dataservice.day2mon;

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.man.model.FmDay2MonSync;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;

public class Day2MonPoiLogSelector {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	public Day2MonPoiLogSelector(DbInfo dailyDbInfo, 
			List<Integer> gridsOfCity, 
			FmDay2MonSync lastSyncInfo,
			FmDay2MonSync curSyncInfo) {
		OracleSchema dailyDbSchema = new OracleSchema(
				DbConnectConfig.createConnectConfig(dailyDbInfo.getConnectParam()));
//		Connection conn = dailyDbSchema.getPoolDataSource().getConnection();
		String sql = "select * from dual";//TODO: 履历查询接口
	}
	/**
	 *  3.1）查询得到该城市最后一次成功同步时间戳day_mon_sync.sync_start_time
		3.2）获取当前时间戳curTimeStamp=sysdate
		3.3）insert into day_mon_sync 
		city_id为当前的city，sync_start_time=curTimeStamp，sync_end_time=null，sync_status=1 返回sid
		3.4）在日库上，获取在sync_time到curTimeStamp之间的、grid范围在city所含的grids的 poi履历,履历需要满足下面的条件
		     a)log_operation.op_dt>sync_time and log_operation.op_dt<=curTimeStamp and ob_nm=’IX_POI’ and com_sta=0（未日落月）；
			 b)poi_edit_status.status为已提交
		     c)排除掉父子关系不完整的数据的履历：如过当前的poi为提交状态，但是其子为未提交的；当前为子poi且是提交状态，但是其父是未提交状态的；
		3.5）扩履历
		返回履历临时表名称

	 * @return
	 */
	public String select() {
		String logTempTableName=null;//TODO
		//TODO:
		return logTempTableName;
	}
	
}
