package com.navinfo.dataservice.engine.meta.rdname;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.dbutils.DbUtils;

import com.mysql.jdbc.StringUtils;
import com.navinfo.dataservice.bizcommons.service.PidUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: RdNameOperation.java
 * @author liya
 * @date 2016-6-24上午11:44:01
 * @Description: 元数据库RDName操作类
 */
public class RdNameOperation {
	
	private Connection conn;
	
	
	public RdNameOperation(Connection conn) {
		this.conn = conn;
	}

	/**
	 * @Description:新增一条道路名
	 * @param name
	 * @param langCode
	 * @param adminId
	 * @param srcResume
	 * @throws Exception
	 * @author: y
	 * @time:2016-6-28 下午5:03:18
	 */
	public RdName saveName(RdName rdName) throws Exception {

		String insertSql = "	  Insert Into RD_NAME( \n"
				+ "NAME_ID,									\n"
				+ "NAME_GROUPID,     \n"
				+ "LANG_CODE,        \n"
				+ "NAME,             \n"
				+ "TYPE,             \n"
				+ "BASE,             \n"
				+ "PREFIX,           \n"
				+ "INFIX,            \n"
				+ "SUFFIX,           \n"
				+ "NAME_PHONETIC,    \n"
				+ "TYPE_PHONETIC,    \n"
				+ "BASE_PHONETIC,    \n"
				+ "PREFIX_PHONETIC,  \n"
				+ "INFIX_PHONETIC,   \n"
				+ "SUFFIX_PHONETIC,  \n"
				+ "SRC_FLAG,         \n"
				+ "ROAD_TYPE,        \n"
				+ "ADMIN_ID,         \n"
				+ "CODE_TYPE,        \n"
				+ "VOICE_FILE,       \n"
				+ "SRC_RESUME,       \n"
				+ "PA_REGION_ID,     \n"
				+ "MEMO,             \n"
				+ "ROUTE_ID,         \n"
				+ "PROCESS_FLAG,     \n"
				+ "U_RECORD,         \n"
				+ "U_FIELDS,         \n"
				+ "SPLIT_FLAG        \n";
		// web端需要维护city字段 add by wangdongbin
		if (rdName.getLangCode().equals("CHI") && (StringUtils.isNullOrEmpty(rdName.getNamePhonetic()))) {
			insertSql +=  ",CITY		\n"
					+ ") VALUES \n"
					+ "	 (?, ?, ?, ?, ?, ?, ?, ?, ?, ( SELECT PY_UTILS_WORD.CONVERT_HZ_TONE(?, NULL, NULL) PHONETIC FROM DUAL), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?,?,?,?) \n";
		} else {
			insertSql += ",CITY		\n"
					+ ") VALUES \n"
					+ "	 (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?) \n";
		}

		PreparedStatement pstmt = null;
		try {
			System.out.println("rd_name: "+insertSql);
			pstmt = conn.prepareStatement(insertSql);

			Integer nameId = rdName.getNameId();
			Integer nameGroupid = rdName.getNameGroupid();
			
			if (rdName.getNameId() == null) {
				nameId = applyPid();
			}
			if (rdName.getNameGroupid() == null) {
				nameGroupid = applyPid();
			}

			pstmt.setLong(1, nameId);
			pstmt.setLong(2, nameGroupid);
			pstmt.setString(3, rdName.getLangCode());
			pstmt.setString(4, rdName.getName());
			pstmt.setString(5, rdName.getType());
			pstmt.setString(6, rdName.getBase());
			pstmt.setString(7, rdName.getPrefix());
			pstmt.setString(8, rdName.getInfix());
			pstmt.setString(9, rdName.getSuffix());
			// 名称发音，通过名称生成
			if (rdName.getLangCode().equals("CHI") && (StringUtils.isNullOrEmpty(rdName.getNamePhonetic()))) {
				pstmt.setString(10, rdName.getName());
			} else {
				pstmt.setString(10, rdName.getNamePhonetic());
			}
			
			pstmt.setString(11, rdName.getTypePhonetic());
			pstmt.setString(12, rdName.getBasePhonetic());
			pstmt.setString(13, rdName.getPrefixPhonetic());
			pstmt.setString(14, rdName.getInfixPhonetic());
			pstmt.setString(15, rdName.getSuffixPhonetic());
			pstmt.setInt(16, rdName.getSrcFlag());
			pstmt.setInt(17, rdName.getRoadType());
			pstmt.setInt(18, rdName.getAdminId());
			pstmt.setInt(19, rdName.getCodeType());
			pstmt.setString(20, rdName.getVoiceFile());
			pstmt.setString(21, rdName.getSrcResume());
			
			if(rdName.getPaRegionId()!=null){
				pstmt.setInt(22, rdName.getPaRegionId());	
			}else{
				pstmt.setNull(22, Types.INTEGER);
			}
			
			pstmt.setString(23, rdName.getMemo());
			
			if(rdName.getRouteId()!=null){
				pstmt.setInt(24, rdName.getRouteId());	
			}else{
				pstmt.setNull(24, Types.INTEGER);
			}
			pstmt.setInt(25, rdName.getProcessFlag());
			pstmt.setInt(26, rdName.getuRecord());
			pstmt.setString(27, rdName.getuFields());
			pstmt.setInt(28, rdName.getSplitFlag());
			pstmt.setString(29, rdName.getCity());
			System.out.println("rd_name: "+insertSql+" ("+nameId+","+nameGroupid+","+rdName.getLangCode()+","+rdName.getName()
			+","+rdName.getType()+","+rdName.getBase()+","+rdName.getPrefix()+","+rdName.getInfix()+","+rdName.getSuffix()
			+","+rdName.getName()+","+rdName.getTypePhonetic()+","+rdName.getBasePhonetic()+","+rdName.getPrefixPhonetic()+","
			+rdName.getInfixPhonetic()+","+rdName.getSuffixPhonetic()+","+rdName.getSrcFlag()+","+rdName.getRoadType()
			+","+rdName.getAdminId()+","+rdName.getCodeType()+","+rdName.getVoiceFile()+","+rdName.getSrcResume()+","
			+rdName.getPaRegionId()+","+rdName.getMemo()+","+rdName.getRouteId()+","+rdName.getProcessFlag()+","
			+rdName.getuRecord()+","+rdName.getuFields()+","+rdName.getSplitFlag()+","+rdName.getCity()+")");
			pstmt.execute();

			rdName.setNameId(nameId);
			rdName.setNameGroupid(nameGroupid);
			return rdName;
		} catch (SQLException e) {
			e.printStackTrace();
			if (conn != null) {
				DbUtils.rollback(conn);
			}
			throw new Exception("新增道路名出错：" + e.getMessage(), e);
		} finally {
			DbUtils.close(pstmt);
			//conn由调用者关闭
		}

	}

	/**
	 * @Description:申请pid
	 * @return PID
	 * @author: y
	 * @throws Exception
	 * @time:2016-6-28 下午4:39:47
	 */
	private int applyPid() throws Exception {
		return PidUtil.getInstance().applyRdNamePid();
	}
	
	/**
	 * web插入或更新rdname
	 * @author wangdongbin
	 * @param rdName
	 * @return
	 * @throws Exception
	 */
	public RdName saveOrUpdate(RdName rdName) throws Exception {
		try {
			// 中文名
			if (rdName.getNameId() == null) {
				// 新增
				// 判断是新增中文名还是英文/葡文名
				if (rdName.getLangCode().equals("CHI") || rdName.getLangCode().equals("CHT")) {
					// 中文名
//					rdName.setCity(true);
					rdName = saveName(rdName);
				} else {
					// 英文/葡文名
//					rdName.setCity(true);
					rdName = saveName(rdName);
				}
			} else {
				// 修改
				rdName = updateName(rdName);
			}
			
			return rdName;
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 查询同组下，是否有英文/葡文名
	 * @author wangdongbin
	 * @param rdName
	 * @return
	 * @throws Exception
	 */
	public boolean checkEngName(int nameGroupid) throws Exception {
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		String sql = "SELECT name_id FROM rd_name WHERE name_groupid=:1 AND lang_code in ('ENG','POR')";
		
		try {
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setLong(1, nameGroupid);

			resultSet = pstmt.executeQuery();
			
			if (resultSet.next()) {
				return true;
			}
			
			return false;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		
	}
	
	/**
	 * 更新rdName
	 * @author wangdongbin
	 * @param rdName
	 * @return
	 * @throws Exception
	 */
	public RdName updateName(RdName rdName) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		sb.append("UPDATE rd_name SET ");
		sb.append("LANG_CODE = ?,");
		sb.append("NAME = ?,");
		sb.append("TYPE = ?,");
		sb.append("BASE = ?,");
		sb.append("PREFIX = ?,");
		sb.append("INFIX = ?,");
		sb.append("SUFFIX = ?,");
		if (rdName.getLangCode().equals("CHI") && (StringUtils.isNullOrEmpty(rdName.getNamePhonetic()))) {
			sb.append("NAME_PHONETIC = ( SELECT PY_UTILS_WORD.CONVERT_HZ_TONE(?, NULL, NULL) PHONETIC FROM DUAL),");
		}else{
			sb.append("NAME_PHONETIC = ?,");
		}
		sb.append("TYPE_PHONETIC = ?,");
		sb.append("BASE_PHONETIC = ?,");
		sb.append("PREFIX_PHONETIC = ?,");
		sb.append("INFIX_PHONETIC = ?,");
		sb.append("SUFFIX_PHONETIC = ?,");
		sb.append("SRC_FLAG = ?,");
		sb.append("ROAD_TYPE = ?,");
		sb.append("ADMIN_ID = ?,");
		sb.append("CODE_TYPE = ?,");
		sb.append("VOICE_FILE = ?,");
		sb.append("SRC_RESUME = ?,");
		sb.append("PA_REGION_ID = ?,");
		sb.append("MEMO = ?,");
		sb.append("ROUTE_ID = ?,");
		sb.append("PROCESS_FLAG = ?,");
		sb.append("U_RECORD = ?,");
		sb.append("U_FIELDS = ?,");
		sb.append("SPLIT_FLAG = ?,");
		sb.append(" CITY = ? ");
		sb.append(" WHERE NAME_ID = ?");
		
		PreparedStatement pstmt = null;
		PreparedStatement subPstms = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.setString(1, rdName.getLangCode());
			pstmt.setString(2, rdName.getName());
			pstmt.setString(3, rdName.getType());
			pstmt.setString(4, rdName.getBase());
			pstmt.setString(5, rdName.getPrefix());
			pstmt.setString(6, rdName.getInfix());
			pstmt.setString(7, rdName.getSuffix());
			if (rdName.getLangCode().equals("CHI") && (StringUtils.isNullOrEmpty(rdName.getNamePhonetic()))) {
				pstmt.setString(8, rdName.getName());
			} else {
				pstmt.setString(8, rdName.getNamePhonetic());
			}
			pstmt.setString(9, rdName.getTypePhonetic());
			pstmt.setString(10, rdName.getBasePhonetic());
			pstmt.setString(11, rdName.getPrefixPhonetic());
			pstmt.setString(12, rdName.getInfixPhonetic());
			pstmt.setString(13, rdName.getSuffixPhonetic());
			pstmt.setInt(14, rdName.getSrcFlag());
			pstmt.setInt(15, rdName.getRoadType());
			pstmt.setInt(16, rdName.getAdminId());
			pstmt.setInt(17, rdName.getCodeType());
			pstmt.setString(18, rdName.getVoiceFile());
			pstmt.setString(19, rdName.getSrcResume());
			
			if(rdName.getPaRegionId()!=null){
				pstmt.setInt(20, rdName.getPaRegionId());	
			}else{
				pstmt.setNull(20, Types.INTEGER);
			}
			
			pstmt.setString(21, rdName.getMemo());
			
			if(rdName.getRouteId()!=null){
				pstmt.setInt(22, rdName.getRouteId());	
			}else{
				pstmt.setNull(22, Types.INTEGER);
			}
			pstmt.setInt(23, rdName.getProcessFlag());
			pstmt.setInt(24, 3);
			pstmt.setString(25, rdName.getuFields());
			pstmt.setInt(26, rdName.getSplitFlag());
			pstmt.setString(27, rdName.getCity());
			pstmt.setLong(28, rdName.getNameId());

			pstmt.execute();
			
			// 查询是否存在英文/葡文名
			if (rdName.getLangCode().equals("CHI") && checkEngName(rdName.getNameGroupid())) {
				// 存在，则更新“道路类型（ROAD_TYPE）”、“国家编号(CODE_TYPE)”、“行政区划(ADMIN_ID)”
				String sql = "UPDATE rd_name SET road_type=?,code_type=?,admin_id=? WHERE name_groupid=? and lang_code in ('ENG','POR')";
				subPstms = conn.prepareStatement(sql);
				
				subPstms.setInt(1, rdName.getRoadType());
				subPstms.setInt(2, rdName.getCodeType());
				subPstms.setInt(3, rdName.getAdminId());
				subPstms.setLong(4, rdName.getNameGroupid());
				
				subPstms.execute();
			}
			return rdName;
		} catch (SQLException e) {
			e.printStackTrace();
			DbUtils.rollback(conn);
			throw new Exception("道路名出错：" + e.getMessage(), e);
		} finally {
			DbUtils.close(pstmt);
			DbUtils.close(subPstms);
		}
	}
	
	/**
	 * web端拆分接口
	 * @author wangdongbin
	 * @param dataList
	 * @throws Exception
	 */
	public void teilenRdName(JSONArray dataList) throws Exception {
		RdNameTeilen teilen = new RdNameTeilen(conn);
		try {
			for (int i=0;i<dataList.size();i++) {
				JSONObject data = dataList.getJSONObject(i);
				teilen.teilenName(data.getInt("nameId"), data.getInt("nameGroupid"), data.getString("langCode"), data.getInt("roadType"));
			}
		} catch (Exception e) {
			throw e;
		}
		
	}
	
	/**
	 * 整任务拆分
	 * @param tips
	 * @throws Exception
	 */
	public void teilenRdNameByTask(JSONArray tips) throws Exception {
		RdNameTeilen teilen = new RdNameTeilen(conn);
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			StringBuilder sql = new StringBuilder();
			String ids = "";
			String tmep = "";
			Clob pidClod = null;
			if (tips.size()>0) {
				sql.append("SELECT a.name_id,a.name_groupid,a.lang_code,a.road_type");
				sql.append(" FROM rd_name a where a.split_flag!=1");
				for (int i=0;i<tips.size();i++) {
					JSONObject tipsObj = tips.getJSONObject(i);
					ids += tmep;
					tmep = ",";
					ids += "'" + tipsObj.getString("id") + "'";
				}
				if (tips.size()>1000) {
					pidClod = conn.createClob();
					pidClod.setString(1, ids);
					sql.append(" and a.SRC_RESUME in (select to_number(pid) from table(clob_to_table(:1)))");
				} else {
					sql.append(" and a.SRC_RESUME in (");
					sql.append(ids);
					sql.append(")");
				}
			}
			
			if (sql.length()>0) {
				pstmt = conn.prepareStatement(sql.toString());
				if(tips.size() > 1000)
				{
					pstmt.setClob(1, pidClod);
				}
				resultSet = pstmt.executeQuery();
				while (resultSet.next()) {
					teilen.teilenName(resultSet.getInt("name_id"), resultSet.getInt("name_groupid"), resultSet.getString("lang_code"), resultSet.getInt("road_type"));
				}
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
}
