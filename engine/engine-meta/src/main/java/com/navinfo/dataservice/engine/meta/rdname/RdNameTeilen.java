package com.navinfo.dataservice.engine.meta.rdname;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: RdNameTeilen.java
 * @author y
 * @date 2016-6-28下午7:38:13
 * @Description: 道路名拆分
 *  
 */
public class RdNameTeilen {
	
	private Connection conn;
	
	private static final String  SIMPLE_CHINESE="CHI";
	
	public RdNameTeilen() {
		
	}
	
	public RdNameTeilen(Connection conn) {
		this.conn = conn;
	}

	/**
	 * 道路名拆分
	 * @param pageNum
	 * @param rdName
	 * @return
	 * @throws Exception
	 * @throws SQLException
	 */
	public synchronized  void teilenName(Integer nameId,Integer nameGroupId,String langCode,Integer roadType) throws Exception,
			SQLException {

		CallableStatement cstmt = null;
		PreparedStatement pst=null;
		Connection subconn = null;
		try {
			if (conn == null) {
				subconn = DBConnector.getInstance().getMetaConnection();
				String spName = "{call NAVI_RD_NAME_SPLITE.RD_NAME_SPLIT_UPDATE(?)}";
				cstmt = subconn.prepareCall(spName);
				cstmt.setString(1, String.valueOf(nameId));
				cstmt.executeUpdate();
				
				// 拆分时处理英文名称：拆分完简体中文后，根据组号，来拆分英文名,大陆的拆分英文，港澳的不拆分英文
				if (SIMPLE_CHINESE.equals(langCode)) {
					teilenEngName(subconn,String.valueOf(nameGroupId), String.valueOf(nameId),roadType);
				}
				String updateSql = "update rd_name set U_FIELDS = to_char(sysdate,'YYYY-MM-DD HH24:MI:SS'),split_flag=2 where NAME_GROUPID = " +  nameGroupId;
				pst=subconn.prepareStatement(updateSql);
				pst.execute(updateSql);
			} else {
				conn = DBConnector.getInstance().getMetaConnection();
				String spName = "{call NAVI_RD_NAME_SPLITE.RD_NAME_SPLIT_UPDATE(?)}";
				cstmt = conn.prepareCall(spName);
				cstmt.setString(1, String.valueOf(nameId));
				cstmt.executeUpdate();
				
				// 拆分时处理英文名称：拆分完简体中文后，根据组号，来拆分英文名,大陆的拆分英文，港澳的不拆分英文
				if (SIMPLE_CHINESE.equals(langCode)) {
					teilenEngName(conn,String.valueOf(nameGroupId), String.valueOf(nameId),roadType);
				}
				String updateSql = "update rd_name set U_FIELDS = to_char(sysdate,'YYYY-MM-DD HH24:MI:SS'),split_flag=2 where NAME_GROUPID = " +  nameGroupId;
				pst=conn.prepareStatement(updateSql);
				pst.execute(updateSql);
			}
			
			
		}catch (Exception e){
			throw new Exception("调用拆分存储过程出错," + e.getMessage(), e);
		}finally {
			DbUtils.closeQuietly(pst);
			DbUtils.closeQuietly(cstmt);
			DbUtils.commitAndCloseQuietly(subconn);
		}
	}
	
	
	/**
	 * 根据名称组号，及中文名称的ID来
	 * @param conn 
	 * @param nameGruopId
	 */
	private Map<String,Object> teilenEngName(Connection conn, String nameGruopId, String nameId,Integer roadType) throws Exception{
		Map<String,Object> returnMap = null;  
		// 根据组号，取英文名的名称ID
		String sqlForEng = "SELECT n.*,'' ADMIN_NAME, '' MESSAGE, substr(n.U_FIELDS,0,instr(n.U_FIELDS,',')-1) userName,substr(n.U_FIELDS,instr(n.U_FIELDS,',') + 1) time FROM RD_NAME N WHERE N.LANG_CODE = 'ENG' AND N.NAME_GROUPID = ?";
		
		QueryRunner runner=new QueryRunner();
		
		List<RdName> listEngName=runner.query(conn, sqlForEng, new ResultSetHandler<List<RdName>>(){
				@Override
				public List<RdName> handle(ResultSet rs) throws SQLException{
					 List<RdName> rdNameList=new ArrayList<RdName>();
					while(rs.next()){
						rdNameList.add(new RdName().mapRow(rs));
					}
					return rdNameList;
				}
			} ,nameGruopId);
		
		// 根据名称ID，取中文名
		String sqlForChi = "SELECT n.*,'' ADMIN_NAME, '' MESSAGE, substr(n.U_FIELDS,0,instr(n.U_FIELDS,',')-1) userName,substr(n.U_FIELDS,instr(n.U_FIELDS,',') + 1) time FROM RD_NAME N WHERE N.LANG_CODE = 'CHI' AND N.NAME_ID = ?";
		List<RdName> listName=runner.query(conn, sqlForChi, new ResultSetHandler<List<RdName>>(){
			@Override
			public List<RdName> handle(ResultSet rs) throws SQLException{
				 List<RdName> rdNameList=new ArrayList<RdName>();
				while(rs.next()){
					rdNameList.add(new RdName().mapRow(rs));
				}
				return rdNameList;
			}
		} ,nameId);

		RdName engRdName = new RdName();
		RdName chiRdName = new RdName();
		chiRdName = listName.get(0);

		// 如果有一个，则判断此英文名是否可以修改（英文名来源字段为数字“1”：按规则翻译），则根据最新的拆分结果来更新此英文道路名
		if (listEngName.size() > 1) {
			throw new Exception("一个组下有2个英文名，请验证，组号为：" + nameGruopId);
		}

		// 如果有一个，则是更新
		if (listEngName.size() == 1) {
			engRdName = listEngName.get(0);
		}
		String engPrefix = "";
		String engSuffix = "";
		String engInfix = "";
		String engType = "";
		// 拆分时，取配置表的时候如果报错，给出道路名的ID
		
		try {
				//前后缀
			String sqlPreSuf = "SELECT P.ENGLISHNAME VALUE FROM SC_ROADNAME_SUFFIX P WHERE P.LANG_CODE='CHI' AND P.NAME=?";
			// 英文中缀
			String  sqlIndex = "SELECT P.ENGLISHNAME VALUE FROM SC_ROADNAME_INFIX P WHERE P.LANG_CODE='CHI' AND P.NAME=?";
			// 英文类型名
			String  sqlType = "SELECT P.ENGLISHNAME VALUE FROM SC_ROADNAME_TYPENAME P WHERE P.LANG_CODE='CHI' AND P.NAME=?";
			
			// 英文前缀：
			engPrefix = getEngValue(conn,chiRdName.getPrefix(), sqlPreSuf);
			engRdName.setPrefix(engPrefix);
			engRdName.setPrefixPhonetic("");
			// 英文后缀
			engSuffix = getEngValue(conn,chiRdName.getSuffix(), sqlPreSuf);
			engRdName.setSuffix(engSuffix);
			engRdName.setSuffixPhonetic("");
			// 英文中缀
			engInfix = getEngValue(conn,chiRdName.getInfix(), sqlIndex);
			engRdName.setInfix(engInfix);
			engRdName.setInfixPhonetic("");
			// 英文类型名
			engType = getEngValue(conn,chiRdName.getType(), sqlType);
			if (StringUtils.isEmpty(engType)) {
				engType = "*";
			}

			engRdName.setType(engType);
			engRdName.setTypePhonetic("");

		} catch (Exception e) {
			//log.error("出错的道路名ID=" + chiRdName.getNameId());
			throw new Exception("出错的道路名ID=" + chiRdName.getNameId(), e);
		}
		String engBaseName = "";
		if (StringUtils.isNotEmpty(chiRdName.getBasePhonetic())) {
			engBaseName = getBaseEng(conn,chiRdName.getNameId(),chiRdName.getBase(),chiRdName.getBasePhonetic(),chiRdName.getType(),chiRdName.getLangCode());
		}

		// 如果英文名的类型是*，则不应该放入到名称中
		String engName = engPrefix + " " + engBaseName + " " + engInfix + " "
				+ (engType == "*" ? "" : engType) + " " + engSuffix;

		// 处理掉前后的空格，把多个空格连在一起的替换成单个空格,注意：这里不会为空了，不用判断为空的情况
		engName = engName.trim();
		engName = engName.replace("   ", " ");
		engName = engName.replace("  ", " ");

		engRdName.setName(engName);
		engRdName.setNamePhonetic("");
		engRdName.setBase(engBaseName);
		engRdName.setBasePhonetic("");
		// 英文名
		engRdName.setLangCode("ENG");
		engRdName.setNameGroupid(chiRdName.getNameGroupid());

		// 赋值行政区划，道路名类型，国家编号（英文跟中文保持一致）
		engRdName.setAdminId(chiRdName.getAdminId());
		engRdName.setAdminName(chiRdName.getAdminName());

		engRdName.setCodeType(chiRdName.getCodeType());
		engRdName.setRoadType(chiRdName.getRoadType());

		// ROUTE_ID赋值为默认值
		/*if (engRdName.getRouteId() == null)
			engRdName.setRouteId(RdName.DEFAULT_NUM_VALUE);*/

		// 判断如果以前有英文名，则只有英文名来源字段为数字1时才进行更新，否则不改
		//5)	在对中文记录进行拆分时，根据道路名组对英文进行同步拆分。注意：对英文中名称来源字段为“未定义”、“按规则翻译”的数据进行维护，其他类型不维护。
		if (engRdName.getNameId() != null && engRdName.getNameId() != 0
				&& (engRdName.getSrcFlag() == 1||engRdName.getSrcFlag() == 0)) {
			new RdNameOperation().saveName(engRdName);
		} else if ((engRdName.getNameId() == null || engRdName.getNameId() == 0)) {
			engRdName.setSrcFlag(1);
			 new RdNameOperation().saveName(engRdName);
		}	
		return returnMap;
	}
	
	

	/**
	 * 根据名称，转英文名
	 * 
	 * @param value
	 * @return
	 */
	private String getBaseEng(Connection conn,int nameId, String base,String basePhonetic,String type,String langCode) throws Exception{
		String engName = "";
		try {
			String sql = "";
			sql = "SELECT PY_UTILS_WORD.CONVERT_BASE_ENG('"
					+ base + "','" + basePhonetic + "','"+type+"','"+langCode+"') ENGNAME FROM DUAL";
			
			QueryRunner runner=new QueryRunner();
			engName =runner.queryForString(conn, sql, null);

		} catch (SQLException e) {
			//log.error("调用知识库的转英文名称过程出错,参数为:" + base + ";" + e.getMessage(), e);
			throw new SQLException("调用知识库的转英文名称过程出错,nameId为:" + nameId + ";" + e.getMessage(), e);
		}
		return engName;
	}
	
	private String getEngValue(Connection conn, String value, String sql) throws SQLException {
		String returnValue = "";
		QueryRunner runner=new QueryRunner();
		try {
			if (StringUtils.isEmpty(value) || "*".equals(value))
				returnValue = "";
			else {
				returnValue=runner.queryForString(conn, sql, value);
			}
		} catch (SQLException e) {
			throw new SQLException("根据中文拆分英文时，从配置表中取数据出错，参数为：" + value
						+ ";" + e.getMessage(), e);
		}
		return returnValue;
	}

}
