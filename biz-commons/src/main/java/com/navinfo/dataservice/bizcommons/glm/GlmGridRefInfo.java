package com.navinfo.dataservice.bizcommons.glm;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @ClassName: GlmGridRefInfo
 * @author Xiao Xiaowen
 * @date 2016年4月13日 下午4:28:47
 * @Description: TODO
 */
public class GlmGridRefInfo {
	private String tableName;
	private String gridRefCol;
	private List<String[]> refInfo;// {"第一层参考表","关联的参考表的字段，一般为主键","参考表本身参考其他表的参考字段,如果没有，则为空字符串"}
	private boolean singleMesh;// 在glm模型上，是否属于唯一一个图幅，标识是参考的主表中有mesh_id字段
	private String editQuerySql;// 给编辑时查询数据记录所属grid使用的sql，row_id是数据记录的row_id
	private String diffQuerySql;// 给履历记录查询所属grid使用的sql，row_id是履历表的row_id
	public static Logger logger = Logger.getLogger(GlmGridRefInfo.class);

	public GlmGridRefInfo(String tableName) {
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getGridRefCol() {
		return gridRefCol;
	}

	public void setGridRefCol(String gridRefCol) {
		this.gridRefCol = gridRefCol;
	}

	public List<String[]> getRefInfo() {
		return refInfo;
	}

	public void setRefInfo(List<String[]> refInfo) {
		this.refInfo = refInfo;
		generateSql();
	}

	public boolean isSingleMesh() {
		return singleMesh;
	}

	public void setSingleMesh(boolean singleMesh) {
		this.singleMesh = singleMesh;
	}

	public String getEditQuerySql() {
		return editQuerySql;
	}

	public String getDiffQuerySql() {
		return diffQuerySql;
	}

	/**
	 * 
	 * @param name
	 *            :cross user name
	 * @return
	 */
	public String replaceDiffSqlByCrossUser(String crossUserName) {
		String s = null;
		if (StringUtils.isNotEmpty(diffQuerySql)) {
			s = diffQuerySql.replaceAll(tableName + " P", crossUserName + "."
					+ tableName + " P");
			if (refInfo != null) {
				for (String[] arr : refInfo) {
					s = s.replaceAll(arr[0] + " R", crossUserName + "."
							+ arr[0] + " R");
				}
			}
		}
		return s;
	}

	public String replaceDiffSqlByDbLink(String dbLinkName) {
		String s = null;
		if (StringUtils.isNotEmpty(diffQuerySql)) {
			s = diffQuerySql
					.replaceAll(tableName, tableName + "@" + dbLinkName);
			if (refInfo != null) {
				for (String[] arr : refInfo) {
					s = s.replaceAll(arr[0], arr[0] + "@" + dbLinkName);
				}
			}
		}
		return s;
	}

	private void generateSqlForSpecial() {
		editQuerySql = "SELECT P.ROW_ID,R1.GEOMETRY,0 MESH_ID,R1.NODE_PID GEO_PID,'RD_NODE' GEO_NM  FROM "
				+ tableName
				+ " P,RD_CROSS_NODE R2,RD_NODE R1"
				+ " WHERE P.PID=R2.PID AND R2.NODE_PID=R1.NODE_PID AND R2.IS_MAIN=1";
		diffQuerySql = "SELECT L.ROW_ID,R1.GEOMETRY,0 MESH_ID, R1.NODE_PID GEO_PID,'RD_NODE' GEO_NM  FROM "
				+ tableName
				+ " P,RD_CROSS_NODE R2,RD_NODE R1,LOG_DETAIL L"
				+ " WHERE P.PID=R2.PID AND R2.NODE_PID=R1.NODE_PID AND R2.IS_MAIN=1 AND P.ROW_ID=L.TB_ROW_ID AND L.TB_NM = '"
				+ tableName + "' ";
	}

	private void generateSqlForTrafficSignal() {
		editQuerySql = "SELECT P.ROW_ID,R1.GEOMETRY,0 MESH_ID ,R1.NODE_PID GEO_PID,'RD_NODE' GEO_NM  FROM "
				+ tableName
				+ " P,RD_CROSS_NODE R2,RD_NODE R1"
				+ " WHERE P.NODE_PID=R2.NODE_PID AND R2.NODE_PID=R1.NODE_PID";
		diffQuerySql = "SELECT L.ROW_ID,R1.GEOMETRY,0 MESH_ID ,R1.NODE_PID GEO_PID,'RD_NODE' GEO_NM FROM "
				+ tableName
				+ " P,RD_CROSS_NODE R2,RD_NODE R1,LOG_DETAIL L"
				+ " WHERE P.NODE_PID=R2.NODE_PID AND R2.NODE_PID=R1.NODE_PID AND P.ROW_ID=L.TB_ROW_ID AND L.TB_NM = '"
				+ tableName + "' ";
	}

	private void generateSql() {
		if (tableName.equals("RD_CROSS") || tableName.equals("RD_CROSS_NAME")) {
			generateSqlForSpecial();
			return;
		}
		if (tableName.equals("RD_TRAFFICSIGNAL")) {
			generateSqlForTrafficSignal();
			return;
		}
		StringBuilder sb4E = new StringBuilder();// edit查询grid使用sql
		StringBuilder sb4D = new StringBuilder();// diff查询grid使用sql

		// 先判断是否本身为主表，主表的refInfo为空
		if (refInfo != null && refInfo.size() > 0) {
			int size = refInfo.size();
			// ...
			sb4E.append("SELECT P.ROW_ID,R1.GEOMETRY");
			sb4D.append("SELECT L.ROW_ID,R1.GEOMETRY");

			// ref table part
			StringBuilder sb4S = new StringBuilder();
			StringBuilder sb4C = new StringBuilder();
			sb4C.append(" WHERE P." + gridRefCol);
			String geoTableName = "";
			String geoPidName = "";
			for (int i = 0; i < size; i++) {
				String[] s = refInfo.get(i);

				if ((size - i) == 1) {
					geoTableName = s[0];
					geoPidName = s[1];
				}
				// 查询关联表信息
				sb4S.append("," + s[0] + " R" + (size - i));
				// 拼接查询字段
				sb4C.append("=R" + (size - i) + "." + s[1]);
				if (i < (size - 1) && (!("NULL".equals(s[2])))) {
					sb4C.append(" AND R" + (size - i) + "." + s[2]);
				}
			}

			// 添加所属几何对应表 和对应pid

			sb4E.append(",'" + geoTableName + "' as GEO_NM ");
			sb4D.append(",'" + geoTableName + "' as GEO_NM ");

			sb4E.append(",R1." + geoPidName + " as GEO_PID ");

			sb4D.append(",R1." + geoPidName + " as GEO_PID ");
			// 添加查询图幅字段
			String meshSql = null;
			if (singleMesh) {
				meshSql = ",R1.MESH_ID FROM ";
			} else {
				meshSql = ",0 MESH_ID FROM ";
			}
			sb4E.append(meshSql);
			sb4D.append(meshSql);
			// 添加查询表名称
			sb4E.append(tableName + " P");
			sb4D.append(tableName + " P");

			// 添加关联表信息
			sb4E.append(sb4S.toString());
			sb4D.append(sb4S.toString());
			sb4D.append(",LOG_DETAIL L");
			// 添加查询条件
			sb4E.append(sb4C.toString());
			sb4D.append(sb4C.toString());
			sb4D.append(" AND P.ROW_ID=L.TB_ROW_ID AND L.TB_NM = '" + tableName
					+ "' ");
		} else {
			// ...
			sb4E.append("SELECT P.ROW_ID,P.GEOMETRY");
			sb4D.append("SELECT L.ROW_ID,P.GEOMETRY");

			// 添加所属几何对应表 和对应pid
			sb4E.append(",'" + tableName + "' as GEO_NM ");
			sb4D.append(",'" + tableName + "' as GEO_NM ");
			sb4E.append(",p." + getObjPidName(tableName) + " as GEO_PID ");
			sb4D.append(",p." + getObjPidName(tableName) + " as GEO_PID ");
			String meshSql = null;
			if (singleMesh) {
				meshSql = ",P.MESH_ID FROM ";
			} else {
				meshSql = ",0 MESH_ID FROM ";
			}
			sb4E.append(meshSql);
			sb4D.append(meshSql);
			//
			sb4E.append(tableName + " P");
			sb4D.append(tableName + " P,LOG_DETAIL L");
			// WHERE
			sb4E.append(" WHERE 1=1 ");
			sb4D.append(" WHERE P.ROW_ID=L.TB_ROW_ID AND L.TB_NM = '"
					+ tableName + "' ");
		}

		editQuerySql = sb4E.toString();
		diffQuerySql = sb4D.toString();
		logger.info("editQuerySql  :  " + editQuerySql.toString());
		logger.info("diffQuerySql  :   " + diffQuerySql.toString());

	}

	/**
	 * 获取有几何对应的pid名称
	 * 
	 * @param row
	 * @return
	 */
	public String getObjPidName(String tableName) {
		String defaultPidName = "";
		if (tableName.contains("LINK")) {
			defaultPidName = "LINK_PID";
		}
		if (tableName.contains("NODE")) {
			defaultPidName = "NODE_PID";
		}
		if (tableName.contains("FACE")) {
			defaultPidName = "FACE_PID";
		}
		switch (tableName) {
		// 点限速 立交 CRF对象 POI
		case "RD_SPEEDLIMIT":
		case "RD_GSC":
		case "IX_POI":
		case "RD_OBJECT":
		case "IX_ANNOTATION":
		case "RD_ELECTRONICEYE":
			return "PID";
			// 行政区划代表点
		case "AD_ADMIN":
			return "REGION_ID";
			// 语音引导

		default:
			return defaultPidName;
		}
	}

	public static void main(String[] args) {
		String info = "RD_NODE:NODE_PID:NULL";
		String blankInfo = com.navinfo.dataservice.commons.util.StringUtils
				.removeBlankChar(info);
		String[] refArr = blankInfo.split(",");
		List<String[]> refInfo = new ArrayList<String[]>();
		for (String ref : refArr) {
			String[] arr = ref.split(":");
			if (arr.length == 3) {
				refInfo.add(arr);
			}
		}

		String tableName = "RD_SLOPE";
		StringBuilder sb4E = new StringBuilder();// edit查询grid使用sql
		StringBuilder sb4D = new StringBuilder();// diff查询grid使用sql
		int size = refInfo.size();
		// ...
		sb4E.append("SELECT P.ROW_ID,R1.GEOMETRY");
		sb4D.append("SELECT L.ROW_ID,R1.GEOMETRY");

		// ref table part
		StringBuilder sb4S = new StringBuilder();
		StringBuilder sb4C = new StringBuilder();
		sb4C.append(" WHERE P." + "NODE_PID");
		String geoTableName = "";
		String geoPidName = "";
		for (int i = 0; i < size; i++) {
			String[] s = refInfo.get(i);

			if ((size - i) == 1) {
				geoTableName = s[0];
				geoPidName = s[1];
			}
			// 查询关联表信息
			sb4S.append("," + s[0] + " R" + (size - i));
			// 拼接查询字段
			sb4C.append("=R" + (size - i) + "." + s[1]);
			if (i < (size - 1) && (!("NULL".equals(s[2])))) {
				sb4C.append(" AND R" + (size - i) + "." + s[2]);
			}
		}
		// 添加所属几何对应表 和对应pid
		sb4E.append(",'" + geoTableName + "' as GEO_NM ");
		sb4D.append(",'" + geoTableName + "' as GEO_NM ");

		sb4E.append(",R1." + geoPidName + " as GEO_PID ");

		sb4D.append(",R1." + geoPidName + " as GEO_PID ");
		// 添加查询图幅字段
		String meshSql = null;
		if (true) {
			meshSql = ",R1.MESH_ID FROM ";
		}
		sb4E.append(meshSql);
		sb4D.append(meshSql);
		// 添加查询表名称
		sb4E.append(tableName + " P");
		sb4D.append(tableName + " P");

		// 添加关联表信息
		sb4E.append(sb4S.toString());
		sb4D.append(sb4S.toString());
		sb4D.append(",LOG_DETAIL L");
		// 添加查询条件
		sb4E.append(sb4C.toString());
		sb4D.append(sb4C.toString());
		sb4D.append(" AND P.ROW_ID=L.TB_ROW_ID AND L.TB_NM = '" + tableName
				+ "' ");
		System.out.println(sb4E.toString());
		System.out.println(sb4D.toString());

	}
}
