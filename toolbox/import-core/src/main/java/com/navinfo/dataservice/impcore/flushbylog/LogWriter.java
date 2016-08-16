package com.navinfo.dataservice.impcore.flushbylog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.oracle.MyPoolGuardConnectionWrapper;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

/*
 * @author MaYunFei
 * 2016年6月16日
 * 描述：import-coreLogWriter.java
 */
public class LogWriter {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private static WKT wktUtil = new WKT();
	private Connection targetDbConn;
	/**
	 * @param conn 目标库的连接
	 * 默认情况下，出现履历执行的错误，不抛异常
	 */
	public LogWriter(Connection conn) {
		this.targetDbConn=conn;
	}

	public void write(EditLog editLog,ILogWriteListener listener){
		int op_tp = editLog.getOpType();
		if (op_tp == 1) {// 新增
			listener.preInsert();
			if (insertData(editLog) == 0) {
				listener.insertFail(editLog);
			}

		} else if (op_tp == 3) { // 修改

			listener.preUpdate();
			if (updateData(editLog) == 0) {
				listener.updateFailed(editLog);
			}

		} else if (op_tp == 2) { // 删除
			listener.preDelete();
			if (deleteData(editLog) == 0) {
				listener.deleteFailed(editLog);
			}
		}
	}
	static final Set includeFiledsSet=new HashSet();
	static{
		String[] includeFiledsArr = new String[]{"pid",
				"kind_code",
				"geometry",
				"x_guide",
				"y_guide",
				"link_pid",
				"side",
				"name_groupid",
				"road_flag",
				"pmesh_id",
				"admin_real",
				"importance",
				"chain",
				"airport_code",
				"access_flag",
				"open_24h",
				"mesh_id_5k",
				"mesh_id",
				"region_id",
				"post_code",
				"edit_flag",
				"dif_groupid",
				"reserved",
				"state",
				"field_state",
				"label",
				"type",
				"address_flag",
				"ex_priority",
				"edition_flag",
				"poi_memo",
				"old_blockcode",
				"old_name",
				"old_address",
				"old_kind",
				"poi_num",
				"log",
				"task_id",
				"data_version",
				"field_task_id",
				"verified_flag",
				"collect_time",
				"geo_adjust_flag",
				"full_attr_flag",
				"old_x_guide",
				"old_y_guide",
				"u_record",
				"u_fields",
				"u_date",
				"row_id",
				"level",
				"sports_venue",
				"indoor",
				"vip_flag"};
		includeFiledsSet.addAll(Arrays.asList(includeFiledsArr));
	}
	
	private int insertData(EditLog editLog) {

		StringBuilder sb = new StringBuilder("insert into ");

		PreparedStatement pstmt = null;

		try {
			String newValue = editLog.getNewValue();

			JSONObject json = JSONObject.fromObject(newValue);
			
			String tableName = editLog.getTableName().toLowerCase();

			sb.append(tableName);
			sb.append(" (");

			Iterator<String> it = json.keys();

			int keySize = json.keySet().size();

			int tmpPos = 0;
			//"access_flag":0,"address_flag":0,"admin_real":0,"airport_code":"","chain":"","changed_fields":{},"collect_time":"20160729165352","data_version":"250+","dif_groupid":"","edit_flag":1,"edition_flag":"","ex_priority":"","field_state":"改种别代码","field_task_id":0,"full_attr_flag":9,"geo_adjust_flag":9,"geometry":"POINT (116.47389 40.01167)","importance":0,"indoor":0,"kind_code":"110101","label":"","\"LEVEL\"":"B2","link_pid":574126,"log":"改名称|改分类|改POI_LEVEL|改RELATION","mesh_id":605603,"mesh_id_5k":"","name_groupid":0,"old_address":"","old_blockcode":"","old_kind":"110101","old_name":"测试餐饮企业集团","old_x_guide":0,"old_y_guide":0,"open_24h":0,"pid":1152117135,"pmesh_id":0,"poi_memo":"","poi_num":"00166420160729165352","post_code":"","region_id":0,"reserved":"","road_flag":0,"row_id":"00B0AD28E8A74984AC6C94C749BC653E","side":0,"sports_venue":"","state":0,"status":0,"task_id":0,"type":0,"u_date":"","u_record":1,"verified_flag":9,"vip_flag":"","x_guide":116.47389,"y_guide":40.01162}
			
			
			while (it.hasNext()) {
				String filed = it.next();
				if("IX_POI".equalsIgnoreCase(tableName)&& !includeFiledsSet.contains(filed) ){
					continue;
				}
				if (isExcludeField(filed,tableName)){
					continue;
				}
				filed = unescapeField(filed);
				boolean isOracleKey = isOracleKey(filed);
				if (0==tmpPos){
					if (isOracleKey){
						sb.append("\""+filed.toUpperCase()+"\"");
					}else{
						sb.append(filed);
					}
					
				}else{
					if (isOracleKey){
						sb.append(",\""+filed.toUpperCase()+"\"");
					}else{
						sb.append(","+filed);
					}
					
				}
				tmpPos++;
				
			}
			boolean hasUrecord = false;
			if(sb.indexOf(",u_record")!=-1){
				hasUrecord=true;
			}
			if (hasUrecord){
				sb.append(") ");
			}else{
				sb.append(",u_record) ");
			}

			sb.append("values(");
			this.log.debug("json"+json);
			it = json.keys();

			tmpPos = 0;
			this.log.debug(json.keys());
			int i =0;
			for (Object key : json.keySet()){
				if("IX_POI".equalsIgnoreCase(tableName)&& !includeFiledsSet.contains(key.toString()) ){
					continue;
				}
				if (isExcludeField(key.toString(),tableName)){
					continue;
				}
				if (i==0){
					sb.append("?");
				}else{
					sb.append(",?");
				}
				i++;
			}
			if (hasUrecord){
				sb.append(") ");
			}else{
				sb.append(",1)");
			}
			

			it = json.keys();

			tmpPos = 0;
			this.log.debug(sb);
			pstmt = this.targetDbConn.prepareStatement(sb.toString());

			while (it.hasNext()) {
				String keyName = it.next();
				if("IX_POI".equalsIgnoreCase(tableName)&& !includeFiledsSet.contains(keyName) ){
					continue;
				}
				if (isExcludeField(keyName.toString(),tableName)){
					continue;
				}
				tmpPos++;

				Object valObj = json.get(keyName);

				if (!"geometry".equalsIgnoreCase(keyName)) {
					
					if(tableName.equals("ck_exception")){
						
						if("create_date".equalsIgnoreCase(keyName) || "update_date".equalsIgnoreCase(keyName))
						{
							Timestamp ts = new Timestamp( DateUtils.stringToLong(valObj.toString(), "yyyy-MM-dd HH:mm:ss"));
									
							pstmt.setTimestamp(tmpPos, ts);
						}
						else{
							pstmt.setObject(tmpPos, valObj);
						}
					}
					else{
						pstmt.setObject(tmpPos, valObj);
					}
				} else {
					
					if(tableName.equalsIgnoreCase("ck_exception")){
						pstmt.setObject(tmpPos, valObj);
					}
					else{
						JGeometry jg = wktUtil.toJGeometry(valObj.toString()
								.getBytes());
	
						jg.setSRID(8307);
						if (this.targetDbConn instanceof MyPoolGuardConnectionWrapper){
							STRUCT s = JGeometry.store(jg, ((MyPoolGuardConnectionWrapper)this.targetDbConn).getInnermostDelegate());
							pstmt.setObject(tmpPos, s);
						}else{
							STRUCT s = JGeometry.store(jg, this.targetDbConn);
							pstmt.setObject(tmpPos, s);
						}
						
	
						
					}
				}

			}

			int result = pstmt.executeUpdate();
			return result;
		} catch (Exception e) {
			log.info(sb.toString());
			log.error(e.getMessage(),e);

			return 0;
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}
	}

	private boolean isExcludeField(String filed,String tableName) {
		return "changed_fields".equalsIgnoreCase(filed)
				||"status".equalsIgnoreCase(filed)
				||"rd_gate_condition_map".equalsIgnoreCase(filed)
				||("ix_poi_restaurant".endsWith(tableName)&&"mesh".equalsIgnoreCase(filed))
				||("ad_face_topo".endsWith(tableName)&&"mesh".equalsIgnoreCase(filed))
				||("ad_face_topo".endsWith(tableName)&&"mesh".equalsIgnoreCase(filed))
				||("ad_link".endsWith(tableName)&&"mesh".equalsIgnoreCase(filed))
				;
	}
	//特殊字段进行转换
	private String unescapeField(String filed) {
		if ("name_group_id".equalsIgnoreCase(filed)){
			filed="name_groupid";
		}
		if ("open_2_4h".equalsIgnoreCase(filed)){
			filed="open_24h";
		}
		if ("p_mesh_id".equalsIgnoreCase(filed)){
			filed="pmesh_id";
		}
		return filed;
	}

	private boolean isOracleKey(String filed) {
		return "level".equalsIgnoreCase(filed)
				||"log".equalsIgnoreCase(filed)
				||"label".equalsIgnoreCase(filed)
				||"tag".equalsIgnoreCase(filed)
				||"type".equalsIgnoreCase(filed);
	}

	private int updateData(EditLog editLog) {

		PreparedStatement pstmt = null;

		StringBuilder sb = new StringBuilder("update ");

		try {
			String newValue = editLog.getNewValue();

			JSONObject json = JSONObject.fromObject(newValue);
			
			String tableName = editLog.getTableName().toLowerCase();

			sb.append(tableName);

			sb.append(" set ");

			Iterator<String> it = json.keys();

			int keySize = json.keySet().size();

			int tmpPos = 0;

			while (it.hasNext()) {
				String keyName = it.next();
				keyName = unescapeField(keyName);
				if (isOracleKey(keyName)){
					sb.append("\""+keyName.toUpperCase()+"\"");
				}else {
					sb.append(keyName);
				}

				sb.append("=:");

				sb.append(++tmpPos);

				if (tmpPos < keySize) {

					sb.append(",");
				}
			}
			if (keySize==0||StringUtils.endsWith(sb.toString(), ",")){ 
				sb.append("u_record=3 where row_id = hextoraw('");
			}else{
				sb.append(",u_record=3 where row_id = hextoraw('");
			}
			

			sb.append(editLog.getTableRowId());

			sb.append("')");

			it = json.keys();

			tmpPos = 0;
			this.log.debug(sb);
			pstmt = this.targetDbConn.prepareStatement(sb.toString());

			while (it.hasNext()) {
				tmpPos++;

				String keyName = it.next();

				Object valObj = json.get(keyName);

				if (!"geometry".equalsIgnoreCase(keyName)) {

					pstmt.setObject(tmpPos, valObj);
				} else {
					
					if(tableName.equalsIgnoreCase("ck_exception")){
						pstmt.setObject(tmpPos, valObj);
					}
					else{
						JGeometry jg = wktUtil.toJGeometry(valObj.toString()
								.getBytes());
	
						jg.setSRID(8307);
	
						if (this.targetDbConn instanceof MyPoolGuardConnectionWrapper){
							STRUCT s = JGeometry.store(jg, ((MyPoolGuardConnectionWrapper)this.targetDbConn).getInnermostDelegate());
							pstmt.setObject(tmpPos, s);
						}else{
							STRUCT s = JGeometry.store(jg, this.targetDbConn);
							pstmt.setObject(tmpPos, s);
						}
					}
				}

			}
			int result = pstmt.executeUpdate();
			return result;

		} catch (Exception e) {
			log.info(sb.toString());
			log.error(e.getMessage(),e);
			return 0;
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}
	}

	private int deleteData(EditLog editLog) {

		PreparedStatement pstmt = null;

		StringBuilder sb = new StringBuilder("update ");
		try {
			String sql = "update " + editLog.getTableName()
					+ " set u_record = 2 where row_id =hextoraw('"
					+ editLog.getTableRowId() + "')";
			this.log.debug(sql);
			pstmt = this.targetDbConn.prepareStatement(sql);
			int result = pstmt.executeUpdate();
			return result;

		} catch (Exception e) {
			log.info(sb.toString());
			log.error(e.getMessage(),e);

			return 0;
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}

	}
}

