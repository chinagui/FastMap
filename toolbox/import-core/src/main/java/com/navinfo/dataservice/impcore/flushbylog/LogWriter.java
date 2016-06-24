package com.navinfo.dataservice.impcore.flushbylog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Iterator;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.DateUtils;

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

			while (it.hasNext()) {
				if (++tmpPos < keySize) {
					sb.append(it.next());

					sb.append(",");
				} else {
					sb.append(it.next());
				}
			}

			sb.append(",u_record) ");

			sb.append("values(");

			it = json.keys();

			tmpPos = 0;

			while (it.hasNext()) {
				sb.append(":");

				sb.append(++tmpPos);

				if (tmpPos < keySize) {

					sb.append(",");
				}
			}

			sb.append(",1)");

			it = json.keys();

			tmpPos = 0;
			this.log.debug(sb);
			pstmt = this.targetDbConn.prepareStatement(sb.toString());

			while (it.hasNext()) {
				tmpPos++;

				String keyName = it.next();

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
	
						STRUCT s = JGeometry.store(jg, this.targetDbConn);
	
						pstmt.setObject(tmpPos, s);
					}
				}

			}

			int result = pstmt.executeUpdate();
			return result;
		} catch (Exception e) {
			System.out.println(sb.toString());
			e.printStackTrace();

			return 0;
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}
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

				Object valObj = json.get(keyName);

				sb.append(keyName);

				sb.append("=:");

				sb.append(++tmpPos);

				if (tmpPos < keySize) {

					sb.append(",");
				}
			}

			sb.append(",u_record=3 where row_id = hextoraw('");

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
	
						STRUCT s = JGeometry.store(jg, this.targetDbConn);
	
						pstmt.setObject(tmpPos, s);
					}
				}

			}
			int result = pstmt.executeUpdate();
			return result;

		} catch (Exception e) {
			System.out.println(sb.toString());
			e.printStackTrace();

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
			System.out.println(sb.toString());
			e.printStackTrace();

			return 0;
		} finally {
			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}

	}
}

