package com.navinfo.dataservice.engine.meta.rdname;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.pidservice.PidService;

/**
 * @ClassName: RdNameOperation.java
 * @author liya
 * @date 2016-6-24上午11:44:01
 * @Description: 元数据库RDName操作类
 */
public class RdNameOperation {

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
				+ "SPLIT_FLAG        \n"
				+ ") VALUES \n"
				+ "	 (?, ?, ?, ?, ?, ?, ?, ?, ?, ( SELECT PY_UTILS_WORD.CONVERT_HZ_TONE(?, NULL, NULL) PHONETIC FROM DUAL), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?,?,?) \n";

		PreparedStatement pstmt = null;
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(insertSql);

			Integer nameId = rdName.getNameId();
			Integer nameGroupId = rdName.getNameGroupId();
			
			if (rdName.getNameId() == null) {
				nameId = applyPid();
			}
			if (rdName.getNameGroupId() == null) {
				nameGroupId = applyPid();
			}

			pstmt.setLong(1, nameId);
			pstmt.setLong(2, nameGroupId);
			pstmt.setString(3, rdName.getLangCode());
			pstmt.setString(4, rdName.getName());
			pstmt.setString(5, rdName.getType());
			pstmt.setString(6, rdName.getBase());
			pstmt.setString(7, rdName.getPrefix());
			pstmt.setString(8, rdName.getInfix());
			pstmt.setString(9, rdName.getSuffix());
			// 名称发音，通过名称生成
			pstmt.setString(10, rdName.getName());
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

			pstmt.execute();

			rdName.setNameId(nameId);
			rdName.setNameGroupId(nameGroupId);
			return rdName;
		} catch (SQLException e) {
			e.printStackTrace();
			DbUtils.rollback(conn);
			throw new Exception("新增道路名出错：" + e.getMessage(), e);
		} finally {
			DbUtils.close(pstmt);
			DbUtils.commitAndCloseQuietly(conn);
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
		PidService pidSercice = new PidService();
		return pidSercice.applyRdNamePid();
	}

}
