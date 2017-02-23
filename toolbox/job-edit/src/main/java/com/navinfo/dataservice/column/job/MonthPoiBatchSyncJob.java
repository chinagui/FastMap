package com.navinfo.dataservice.column.job;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

import com.navinfo.dataservice.dao.log.LogReader;

import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;

import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;

import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * @ClassName: MonthPoiBatchSyncJob
 * @author zhaokk
 * @date 2017年2月21日
 * @Description: MonthPoiBatchSyncJob.java
 */
public class MonthPoiBatchSyncJob extends AbstractJob {

	public MonthPoiBatchSyncJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		log.info("start MonthPoiBatchSyncJob");
		MonthPoiBatchSyncJobRequest myRequest = (MonthPoiBatchSyncJobRequest) request;
		ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
		int taskId = myRequest.getTaskId();
		Connection conn = null;
		try {
			log.info("获取任务对应的参数");
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			log.info("params:taskId=" + taskId);
			int dbId = subtask.getDbId();
			log.info("dbId = " + dbId);
			conn = DBConnector.getInstance().getConnectionById(dbId);
			LogReader logReader = new LogReader(conn);
			Collection<String> grids = new HashSet<String>();
			for (int grid : subtask.getGridIds()) {
				grids.add(String.valueOf(grid));
			}
			log.info("grids=" + grids.toString());
			log.info("获取任务范围内新增修改的poi信息");
			Map<Integer, Collection<Long>> map = logReader.getUpdatedObj(
					"IX_POI", "IX_POI", grids, null);
			Collection<Long> pids = new ArrayList<Long>();
			Collection<Long> addPids = map.get(1);
			Collection<Long> updatePids = map.get(3);
			pids.addAll(addPids);
			pids.addAll(updatePids);
			log.info("新增 poi 信息=" + addPids.toString());
			log.info("修改poi 信息=" + updatePids.toString());

			log.info("批管理字段几何标识 外业标识 外业任务编号 数据采集版本");
			this.updateBatchPoi(addPids, this.getBatchPoiCommonSql(), conn);
			log.info("批管理字段state 状态");
			// 新增
			this.updateBatchPoi(addPids, this.getStateParaSql(3), conn);
			// 修改
			this.updateBatchPoi(updatePids, this.getStateParaSql(2), conn);
			log.info("获取poi对应的log信息");
			Map<Long, List<LogDetail>> logs = PoiLogDetailStat
					.loadByRowEditStatus(conn, pids);
			log.info("加载poi信息以及对应子表信息");
			Set<String> tabNames = new HashSet<String>();
			tabNames.add("IX_POI_NAME");
			tabNames.add("IX_POI_ADDRESS");
			// 获取poi对象信息
			Map<Long, BasicObj> objs = ObjBatchSelector.selectByPids(conn,
					ObjectName.IX_POI, tabNames, false, pids, false, false);

			// 将poi对象与履历合并起来
			ObjHisLogParser.parse(objs, logs);
			// 改old_name
			Collection<Long> namePids = new ArrayList<Long>();
			// 改old_address
			Collection<Long> addressPids = new ArrayList<Long>();
			// 改old_kind
			Collection<Long> kindCodePids = new ArrayList<Long>();
			// 外业log
			// 改名称
			Collection<Long> logNamePids = new ArrayList<Long>();
			// 改地址
			Collection<Long> logAddressPids = new ArrayList<Long>();
			// 该分类
			Collection<Long> logKindCodePids = new ArrayList<Long>();
			// 改POI_LEVEL
			Collection<Long> logLevelPids = new ArrayList<Long>();
			// 改内部标识
			Collection<Long> logIndoorPids = new ArrayList<Long>();
			// 改运动场馆
			Collection<Long> logSportPids = new ArrayList<Long>();
			// 改RELATION
			Collection<Long> logLocationPids = new ArrayList<Long>();
			log.info("加载外业log引起变化的pid信息");
			for (long pid : objs.keySet()) {
				BasicObj obj = objs.get(pid);
				IxPoiObj poiObj = (IxPoiObj) obj;
				// 获取POI的主表信息
				IxPoi poi = (IxPoi) poiObj.getMainrow();
				if (poi.hisOldValueContains("kindCode")) {
					if (poi.getHisOpType() == OperationType.UPDATE) {
						logKindCodePids.add(pid);
					}
					kindCodePids.add(pid);
				}
				if (poi.getHisOpType() == OperationType.UPDATE) {
					if (poi.hisOldValueContains("level")) {
						logLevelPids.add(pid);
					}
					if (poi.hisOldValueContains("indoor")) {
						logIndoorPids.add(pid);
					}
					if (poi.hisOldValueContains("sportsVenue")) {
						logSportPids.add(pid);
					}
					if (poi.hisOldValueContains("geometry")) {
						logLocationPids.add(pid);
					}
				}
				// 作业季新增修改中文地址
				if (poiObj.getCHIAddress() != null) {
					IxPoiAddress address = poiObj.getCHIAddress();
					if (address.getHisOpType() == OperationType.UPDATE) {
						logAddressPids.add(pid);
					}
					addressPids.add(pid);
				}
				// 作业季新增修改中文原始
				if (poiObj.getOfficeOriginCHName() != null) {
					IxPoiName poiName = poiObj.getOfficeOriginCHName();
					if (poiName.getHisOpType() == OperationType.UPDATE) {
						logNamePids.add(pid);
					}
					namePids.add(pid);
				}

			}
			// 修改old_name
			this.updateBatchPoi(namePids, this.getUpdatePoiOldNameSql(), conn);
			// 修改old_address
			this.updateBatchPoi(addressPids, this.getUpdatePoiOldAddressSql(),
					conn);
			// 修改old_kind
			this.updateBatchPoi(kindCodePids,
					this.getUpdatePoiOldKindCodeSql(), conn);
			// 赋值外业log
			this.updateBatchPoi(logNamePids, this.getUpadeLogForSql("改名称"),
					conn);
			this.updateBatchPoi(logNamePids, this.getUpadeLogForSql("改地址"),
					conn);
			this.updateBatchPoi(logNamePids, this.getUpadeLogForSql("改分类"),
					conn);
			this.updateBatchPoi(logNamePids,
					this.getUpadeLogForSql("改POI_LEVEL"), conn);
			this.updateBatchPoi(logNamePids, this.getUpadeLogForSql("改运动场馆"),
					conn);
			this.updateBatchPoi(logNamePids, this.getUpadeLogForSql("改内部标识"),
					conn);
			this.updateBatchPoi(logNamePids,
					this.getUpadeLogForSql("改RELATION"), conn);
			// 处理验证标识
			Collection<Long> metaPids = this.getMetaPidsForPoi(conn);
			metaPids.retainAll(pids);
			pids.removeAll(metaPids);
			this.updateBatchPoi(metaPids, this.getVerifiedParaSql(3), conn);
			this.updateBatchPoi(pids, this.getVerifiedParaSql(9), conn);
			log.info("关闭任务");
			apiService.close(taskId);
		} catch (Exception e) {
			log.error("MonthPoiBatchSyncJob错误", e);
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);

		}

	}

	private String getVerifiedParaSql(int verifiedFlag) {
		return "update ix_poi p set verified_flag = "
				+ verifiedFlag
				+ "  where  pid in (select to_number(column_value) from table(clob_to_table(?)))";
	}

	private String getStateParaSql(int state) {
		return "update ix_poi p set state = "
				+ state
				+ "  where  pid in (select to_number(column_value) from table(clob_to_table(?)))";
	}

	private String getUpdatePoiOldNameSql() {
		return "   UPDATE ix_poi p								                           \n"
				+ "    SET p.old_name =                                                    \n"
				+ "          (SELECT n.name                                               \n"
				+ "                FROM ix_poi_name n                                      \n"
				+ "               WHERE n.name_class = 1                                    \n"
				+ "                     AND n.lang_code IN ('CHI', 'CHT')                   \n"
				+ "                     AND n.name_type = 2                                 \n"
				+ "                     AND n.poi_pid = p.pid                               \n"
				+ "                     AND NVL (p.old_name, -1) <> NVL (n.name, -1))      \n"
				+ "     WHERE p.pid = (select to_number(column_value) from table(clob_to_table(?)))  \n";
	}

	private String getUpdatePoiOldAddressSql() {
		return "   UPDATE ix_poi p								                                       \n"
				+ "    SET p.old_address =                                                             \n"
				+ "          (SELECT n.fullname                                                        \n"
				+ "                FROM ix_poi_address n                                               \n"
				+ "               WHERE n.lang_code IN ('CHI', 'CHT')                                  \n"
				+ "                     AND n.poi_pid = p.pid                                          \n"
				+ "                     AND NVL (p.old_address, -1) <> NVL (n.fullname, -1)),          \n"
				+ "     WHERE p.pid = (select to_number(column_value) from table(clob_to_table(?)))    \n";
	}

	private String getUpdatePoiOldKindCodeSql() {
		return "   UPDATE ix_poi p								                                       \n"
				+ "    SET p.old_kind = p.kind_code                                                    \n"
				+ "     WHERE p.pid = (select to_number(column_value) from table(clob_to_table(?)))    \n"
				+ "           AND NVL (p.old_kind, -1) <> NVL (n.kind_code, -1)                        \n";
	}

	private String getUpadeLogForSql(String logName) {

		return "   UPDATE ix_poi p								               \n"
				+ "    SET    p.log =  DECODE (INSTR (p.LOG, "
				+ logName
				+ "),                         \n"
				+ "                          NULL, ' "
				+ logName
				+ "|',                                \n"
				+ "                           0, p.LOG || ' "
				+ logName
				+ "|',                         \n"
				+ "                           p.LOG)                                        \n"
				+ "     WHERE p.pid (select to_number(column_value) from table(clob_to_table(?))) \n";
	}

	/**
	 * 批管理字段几何标识 外业标识 外业任务编号 数据采集版本
	 * 
	 * @return
	 */
	private String getBatchPoiCommonSql() {
		String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.gdbVersion);
		return " UPDATE ix_poi p    \n"
				+ "   SET p.geo_adjust_flag = 1 ,\n"
				+ "       p.full_attr_flag = 1 ,  \n"
				+ "       p.data_version = "
				+ gdbVersion
				+ " ,  \n"
				+ "       p.field_task_id = "
				+ jobInfo.getTaskId()
				+ "  \n"
				+ "     WHERE p.pid (select to_number(column_value) from table(clob_to_table(?))) \n";
	}

	public void updateBatchPoi(Collection<Long> pidList, String sql,
			Connection conn) throws Exception {

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			Clob pidsClob = ConnectionUtil.createClob(conn);
			pidsClob.setString(1, StringUtils.join(pidList, ","));
			pstmt = conn.prepareStatement(sql);
			pstmt.setClob(1, pidsClob);
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}

	/**
	 * 获取所有的代理店的poi的pid列表 【判断POI是否为代理店POI的方式】：
	 * 根据配置表SC_POINT_SPEC_KINDCODE_NEW与POI的种别，CHAIN等信息匹配，确定是否是代理店：：
	 * 1）配置表中TYPE=7且Category=1的记录的POI_KIND与POI的kindcode匹配，则说明是代理店；
	 * 2）配置表中TYPE=7且Category
	 * =3的记录的POI_KIND、CHAIN与POI的kindcode、brands.code匹配，则说明是代理店；
	 * 3）配置表中TYPE=7且Category
	 * =7的记录的POI_KIND、CHAIN、HM_FLAG与POI的kindcode、brands.code
	 * 、“大陆数据还是港澳数据”匹配，则说明是代理店； 以上均不满足，则说明是非代理店。 补充说明：poi是港澳的 则匹配HM或DHM；poi是大陆的
	 * 则匹配D或DHM
	 * 
	 * @throws Exception
	 */
	private Collection<Long> getMetaPidsForPoi(Connection conn)
			throws Exception {

		// 得到所有“表内代理店分类的POI”的PiD
		// 区分大陆港澳
		String hkFlagStr = "'D','DHM'";

		String tmpMetaTableCreateSql = "select * from(                             \n"
				+ "select p.pid                                                             \n"
				+ "  from sc_point_spec_kindCode_new@DBLINK_RMS M, ix_poi p     \n"
				+ " where m.type = 7                                                        \n"
				+ "   and m.category = 1                                                    \n"
				+ "   and p.kind_code = m.poi_kind                                          \n"
				+ "union                                                                    \n"
				+ "select p.pid                                                             \n"
				+ "  from sc_point_spec_kindCode_new@DBLINK_RMS M, ix_poi p     \n"
				+ " where m.type = 7                                                        \n"
				+ "   and m.category = 3                                                    \n"
				+ "   and p.kind_code = m.poi_kind                                          \n"
				+ "   and p.chain = m.chain                                                 \n"
				+ "union                                                                    \n"
				+ "select p.pid                                                             \n"
				+ "  from sc_point_spec_kindCode_new@DBLINK_RMS M, ix_poi p     \n"
				+ " where m.type = 7                                                        \n"
				+ "   and m.category = 7                                                    \n"
				+ "   and p.kind_code = m.poi_kind                                          \n"
				+ "   and p.chain = m.chain                                                 \n"
				+ "   and m.hm_flag in ("
				+ hkFlagStr
				+ "))                                                                        \n";
		return new QueryRunner().query(conn, tmpMetaTableCreateSql,
				new PidHandler());

	}

	class PidHandler implements ResultSetHandler<Collection<Long>> {
		@Override
		public Collection<Long> handle(ResultSet rs) throws SQLException {

			Collection<Long> resultPids = new ArrayList<Long>();
			while (rs.next()) {
				resultPids.add(rs.getLong("pid"));
			}
			return resultPids;
		}

	}

	public static void main(String[] args) {
		List<String> str1 = new ArrayList<String>();
		str1.add("s1");
		List<String> str2 = new ArrayList<String>();
		str2.add("s2");
		List<String> str3 = new ArrayList<String>();
		str3.addAll(str1);
		str3.addAll(str2);
		System.out.println(str1);
		System.out.println(str2);
		System.out.println(str3);
	}

}
