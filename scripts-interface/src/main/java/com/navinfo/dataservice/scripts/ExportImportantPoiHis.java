package com.navinfo.dataservice.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * @ClassName: ExportImportantPoiHis
 * @author: zhangpengpeng
 * @date: 2017年10月16日
 * @Desc: 重要poi更新履历导出
 */
public class ExportImportantPoiHis {

	protected static Logger log = LoggerRepos.getLogger(ExportImportantPoiHis.class);

	private static Map<Integer, String> outDoorUserMap = new HashMap<>();

	private static Map<Integer, String> allUserNameMap = new HashMap<>();

	private static Map<Integer, Connection> regionConnMap = new HashMap<Integer, Connection>();

	private static Map<String, String> fidNameMap = new HashMap<>();

	private static String fileName = "";

	private static String sheetName = "";

	private static String[] headers = { "库中ID", "外业原库重要POI名称", "变更人", "更新时间", "变更履历" };

	private static Map<Integer, String> getOutDoorUserMap() throws Exception {
		Connection conn = null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT USER_ID, USER_REAL_NAME");
		sb.append("  FROM (SELECT U.USER_ID, U.USER_REAL_NAME");
		sb.append("          FROM USER_GROUP T, GROUP_USER_MAPPING S, USER_INFO U");
		sb.append("         WHERE T.GROUP_TYPE = 0");
		sb.append("           AND T.GROUP_ID = S.GROUP_ID");
		sb.append("           AND S.USER_ID = U.USER_ID");
		sb.append("        UNION ALL");
		sb.append("        SELECT U.USER_ID, U.USER_REAL_NAME");
		sb.append("          FROM USER_GROUP T, USER_INFO U");
		sb.append("         WHERE T.GROUP_TYPE = 0");
		sb.append("           AND T.LEADER_ID = U.USER_ID)");
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			ResultSetHandler<Map<Integer, String>> rsHandler = new ResultSetHandler<Map<Integer, String>>() {

				@Override
				public Map<Integer, String> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					Map<Integer, String> data = new HashMap<>();
					while (rs.next()) {
						int userId = rs.getInt("USER_ID");
						String userName = rs.getString("USER_REAL_NAME");
						data.put(userId, userName);
					}
					return data;
				}
			};
			return run.query(conn, sb.toString(), rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	private static Map<Integer, String> getAllUserNameMap() throws Exception {
		Connection conn = null;
		String sql = "SELECT DISTINCT USER_ID, USER_REAL_NAME FROM USER_INFO";
		try {
			conn = DBConnector.getInstance().getManConnection();
			QueryRunner run = new QueryRunner();
			ResultSetHandler<Map<Integer, String>> rsHandler = new ResultSetHandler<Map<Integer, String>>() {

				@Override
				public Map<Integer, String> handle(ResultSet rs) throws SQLException {
					// TODO Auto-generated method stub
					Map<Integer, String> data = new HashMap<>();
					while (rs.next()) {
						int userId = rs.getInt("USER_ID");
						String userName = rs.getString("USER_REAL_NAME");
						data.put(userId, userName);
					}
					return data;
				}
			};
			return run.query(conn, sql, rsHandler);
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}

	@SuppressWarnings("resource")
	private static Map<String, String> getFidNameMap(File file) throws Exception {
		Workbook wb = null;
		Sheet sheet = null;
		Row row = null;
		try {
			fileName = file.getName();
			System.out.println("Excel file name: " + fileName);
			FileInputStream is = new FileInputStream(file);
			String ext = fileName.substring(fileName.lastIndexOf("."));
			if (".xls".equals(ext)) {
				wb = new HSSFWorkbook(is);
			} else if (".xlsx".equals(ext)) {
				wb = new XSSFWorkbook(is);
			} else {
				System.out.println("Error file type : " + fileName);
				throw new Exception("Error file type : " + fileName);
			}
			sheet = wb.getSheetAt(0);
			sheetName = sheet.getSheetName();
			System.out.println("Excel sheetName: " + sheetName);
			Map<String, String> map = new LinkedHashMap<>();
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				row = sheet.getRow(i);
				// --------------空行不解析----------------
				boolean flg = false;
				for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
					Cell cell = row.getCell(c);
					if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
						flg = true;
						break;
					}
				}
				if (!flg) {
					continue;
				}
				System.out.println("===== excel file row : " + i);
				Cell cell0 = row.getCell(0);
				String fid = "";
				int cellType = cell0.getCellType();
				if(cellType == Cell.CELL_TYPE_STRING){
					fid = cell0.getStringCellValue();
				}else{
					System.out.println("===== excel row:" + i + " type error, is not string");
					log.info("===== excel row:" + i + " type error, is not string");
					throw new Exception(" excel row:" + i + " type error, is not string");
				}
				Cell cell1 = row.getCell(1);
				cell1.setCellType(Cell.CELL_TYPE_STRING);
				String name = cell1.getStringCellValue();
				if (StringUtils.isEmpty(fid) || StringUtils.isEmpty(name)) {
					System.out.println("row: " + i + " is null,fid: " + fid + ",name:" + name);
					continue;
				}
				map.put(fid, name);
			}
			is.close();
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public static Map<Integer, Connection> queryAllRegionConn() throws Exception {
		Map<Integer, Connection> mapConn = new HashMap<Integer, Connection>();
		String sql = "select t.daily_db_id,region_id from region t order by t.daily_db_id";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Connection regionConn = DBConnector.getInstance().getConnectionById(rs.getInt("daily_db_id"));
				mapConn.put(rs.getInt("region_id"), regionConn);
				System.out.println("大区库region_id:" + rs.getInt("region_id") + "获取数据库连接成功");
			}
			return mapConn;

		} catch (Exception e) {
			for (Connection value : mapConn.values()) {
				DbUtils.rollbackAndCloseQuietly(value);
			}
			throw new SQLException("加载region失败：" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
		}
	}

	private static List<ImportantPoiHis> getRegionDataHis(Set<String> regionFid) throws Exception {
		Map<String, ImportantPoiHis> data = new HashMap<>();
		List<ImportantPoiHis> allData = new ArrayList<>();

		try {
			for (Entry<Integer, Connection> entry : regionConnMap.entrySet()) {

				System.out.println("search data from regionConn,regionId:" + entry.getKey());
				Connection conn = null;
				conn = entry.getValue();
				Clob clobFid = null;
				Clob clobUserId = null;
				String fidString;
				String userIdString;
				List<Clob> clobs = new ArrayList<>();
				if (regionFid.size() > 1000) {
					clobFid = ConnectionUtil.createClob(conn);
					clobFid.setString(1, StringUtils.join(regionFid, ","));
					fidString = "POI_NUM IN (select column_value from table(clob_to_table(?)))";
					clobs.add(clobFid);
				} else {
					fidString = "POI_NUM IN ('" + StringUtils.join(regionFid, "','") + "')";
				}
				if (outDoorUserMap.size() > 1000) {
					clobUserId = ConnectionUtil.createClob(conn);
					clobUserId.setString(1, StringUtils.join(outDoorUserMap.keySet(), ","));
					userIdString = "US_ID IN (select to_number(column_value) from table(clob_to_table(?)))";
					clobs.add(clobUserId);
				} else {
					userIdString = "US_ID IN (" + StringUtils.join(outDoorUserMap.keySet(), ",") + ")";
				}

				Map<String, ImportantPoiHis> tmpFidObjMap = getFidObjMap(conn, clobs, fidString, userIdString);
				Map<String, String> tmpFidHisMap = getFidHisMap(conn, fidString, clobFid);

				for (Entry<String, String> en : tmpFidHisMap.entrySet()) {
					String fid = en.getKey();
					String his = en.getValue();
					if (tmpFidObjMap.containsKey(fid)) {
						tmpFidObjMap.get(fid).setHis(his);
					} else {
						String poiName = fidNameMap.get(fid);
						ImportantPoiHis importantPoi = new ImportantPoiHis(fid, poiName, "", "", his);
						tmpFidObjMap.put(fid, importantPoi);
					}
				}
				
				data.putAll(tmpFidObjMap);
			}
			// data为查出有履历的数据,与excel差分,将没有履历的数据存进返回值allData
			Set<String> hasHisFid = data.keySet();
			// 差分出没有履历的fid
			regionFid.removeAll(hasHisFid);
			for(String fid: regionFid){
				String poiName = fidNameMap.get(fid);
				ImportantPoiHis e = new ImportantPoiHis(fid, poiName, "", "", "");
				allData.add(e);
			}
			
			allData.addAll(data.values());
			
			
		} catch (Exception e) {
			System.out.println("search data from regionConn error....");
			e.printStackTrace();
			for (Connection value : regionConnMap.values()) {
				DbUtils.rollbackAndCloseQuietly(value);
			}
			throw e;
		} finally {
			for (Connection value : regionConnMap.values()) {
				DbUtils.commitAndCloseQuietly(value);
			}
		}
		return allData;
	}

	public static Map<String, ImportantPoiHis> getFidObjMap(Connection conn, List<Clob> clobs, String fidString,
			String userIdString) throws Exception {
		Map<String, ImportantPoiHis> data = new HashMap<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT FID,PID,USERID,UPDATETIME");
		sb.append("  FROM (WITH T AS (SELECT P.PID, P.POI_NUM FID");
		sb.append("                     FROM IX_POI P");
		sb.append("                    WHERE ");
		sb.append("                      P." + fidString + " )");
		sb.append("         SELECT T.FID,T.PID,");
		sb.append("                LA.US_ID USERID,");
		sb.append("                TO_CHAR(LO.OP_DT, 'YYYY-MM-DD HH24:MI:SS') UPDATETIME,");
		sb.append("                ROW_NUMBER() OVER(PARTITION BY T.FID ORDER BY LO.OP_DT DESC) RN");
		sb.append("           FROM T, LOG_DETAIL LD, LOG_OPERATION LO, LOG_ACTION LA");
		sb.append("          WHERE T.PID = LD.OB_PID");
		sb.append("            AND LD.OP_ID = LO.OP_ID");
		sb.append("            AND LO.ACT_ID = LA.ACT_ID");
		sb.append("            AND LA." + userIdString + " )");
		sb.append("  WHERE RN = 1");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			if (clobs != null && !clobs.isEmpty()) {
				for (int i = 0; i < clobs.size(); i++) {
					pstmt.setClob(i + 1, clobs.get(i));
				}
			}
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String fid = rs.getString("FID");
				String poiName = fidNameMap.get(fid);
				Integer userId = rs.getInt("USERID");
				String userName = "";
				if (outDoorUserMap.containsKey(userId)) {
					userName = outDoorUserMap.get(userId) + userId.toString();
				} else {
					userName = userId.toString();
				}
				String updateTime = rs.getString("UPDATETIME");
				ImportantPoiHis imp = new ImportantPoiHis(fid, poiName, userName, updateTime, "");
				data.put(fid, imp);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
		return data;
	}

	public static Map<String, String> getFidHisMap(Connection conn, String fidString, Clob clobFid) throws Exception {
		Map<String, String> data = new HashMap<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		StringBuilder sb = new StringBuilder();
		sb.append(" WITH T AS (SELECT P.PID,P.POI_NUM FID");
		sb.append("                     FROM IX_POI P");
		sb.append("                    WHERE  ");
		sb.append("                      P." + fidString + " )");
		sb.append("         SELECT T.FID,");
		sb.append("                LA.US_ID USERID,LD.OP_TP,");
		sb.append("                TO_CHAR(LO.OP_DT, 'YYYY-MM-DD HH24:MI:SS') UPDATETIME");
		sb.append("           FROM T, LOG_DETAIL LD, LOG_OPERATION LO, LOG_ACTION LA");
		sb.append("          WHERE T.PID = LD.OB_PID");
		sb.append("            AND LD.OP_ID = LO.OP_ID");
		sb.append("            AND LO.ACT_ID = LA.ACT_ID");
		sb.append("            ORDER BY LO.OP_DT");
		try {
			pstmt = conn.prepareStatement(sb.toString());
			if (clobFid != null) {
				pstmt.setClob(1, clobFid);
			}

			rs = pstmt.executeQuery();
			while (rs.next()) {
				String fid = rs.getString("FID");
				Integer userId = rs.getInt("USERID");
				Integer lifeCycle = rs.getInt("OP_TP");
				String updateTime = rs.getString("UPDATETIME");
				String userName = "";
				if (allUserNameMap.containsKey(userId)) {
					userName = allUserNameMap.get(userId) + userId.toString();
				} else {
					userName = userId.toString();
				}
				String newHis = userName + "," + updateTime + "," + lifeCycle.toString() + ";";
				if (data.containsKey(fid)) {
					String His = data.get(fid);
					data.put(fid, His + newHis);
				} else {
					data.put(fid, newHis);
				}
			}

		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
		return data;
	}

	public static class ImportantPoiHis {
		private String fid;
		private String poiName;
		private String userName;
		private String updateTime;
		private String his;

		public String getFid() {
			return fid;
		}

		public void setFid(String fid) {
			this.fid = fid;
		}

		public String getPoiName() {
			return poiName;
		}

		public void setPoiName(String poiName) {
			this.poiName = poiName;
		}

		public String getUserName() {
			return userName;
		}

		public void setUserName(String userName) {
			this.userName = userName;
		}

		public String getUpdateTime() {
			return updateTime;
		}

		public void setUpdateTime(String updateTime) {
			this.updateTime = updateTime;
		}

		public String getHis() {
			return his;
		}

		public void setHis(String his) {
			this.his = his;
		}

		public ImportantPoiHis(String fid, String poiName, String userName, String updateTime, String his) {
			this.fid = fid;
			this.poiName = poiName;
			this.userName = userName;
			this.updateTime = updateTime;
			this.his = his;
		}

	}

	public static void excute(String filePath) {
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				System.out.println("File not exists : " + filePath);
				return;
			}
			outDoorUserMap = getOutDoorUserMap();
			allUserNameMap = getAllUserNameMap();
			regionConnMap = queryAllRegionConn();
			fidNameMap = getFidNameMap(file);

			Set<String> allFid = fidNameMap.keySet();

			List<ImportantPoiHis> regionData = getRegionDataHis(allFid);
//			String txtPath = "./excel.csv";
//			PrintWriter pw = new PrintWriter(txtPath);
			for (ImportantPoiHis his : regionData) {
				log.info("====================Data : " + his.getFid() + "\t" + his.getPoiName() + "\t"
						+ his.getUserName() + "\t" + his.getUpdateTime() + "\t" + his.getHis().length());
//				pw.println(his.getFid() + "," + his.getPoiName() + ","
//						+ his.getUserName() + "," + his.getUpdateTime() + "," + his.getHis());
			}
//			if(pw!=null){
//				pw.close();
//			}

			
			String newFilePath = "";
			if (filePath.endsWith(".xlsx")) {
				newFilePath = filePath.replace(".xlsx", "_NEW.xls");
			}
			if (filePath.endsWith(".xls")) {
				newFilePath = filePath.replace(".xls", "_NEW.xls");
			}
			System.out.println("Export file name: " + newFilePath);

			File newFile = new File(newFilePath);
			if (!newFile.getParentFile().isDirectory()) {
				newFile.getParentFile().mkdirs();
			}
			if (!newFile.exists()) {
				newFile.createNewFile();
			}
			ExportExcel<ImportantPoiHis> ex = new ExportExcel<ImportantPoiHis>();
			OutputStream out = new FileOutputStream(newFile);
			ex.exportExcel(sheetName, headers, regionData, out, null);
			out.close();
			
			System.out.println("export excel success...");

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("export excel error...");
			log.error(e.getMessage());
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("args.length:" + args.length);
		if (args == null || args.length != 1) {
			System.out.println("ERROR:args error");
			return;
		}
		System.out.println("Start...");
		initContext();
		excute(args[0]);
		System.out.println("End...");
	}

	public static void initContext() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-app-scripts.xml", "dubbo-scripts.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
}
