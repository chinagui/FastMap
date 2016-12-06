package com.navinfo.dataservice.bizcommons.glm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.JGeometryUtil;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;

/**
 * @ClassName: GlmGridCalculator
 * @author Xiao Xiaowen
 * @date 2016年4月13日 下午4:25:39
 * @Description: TODO
 */
public class GlmGridCalculator {

	protected Logger log = LoggerRepos.getLogger(this.getClass());
	private String tableName;

	public GlmGridCalculator(String gdbVersion, GlmGridCalculatorLock lock) {
		this.gdbVersion = gdbVersion;
	}

	private String gdbVersion;
	private Map<String, GlmGridRefInfo> glmGridRefInfoMap = null;// key:表名，value：表配置信息
	private QueryRunner run = new QueryRunner();

	private Map<String, GlmGridRefInfo> getGlmGridRefInfoMap() {
		if (glmGridRefInfoMap == null) {
			synchronized (this) {
				if (glmGridRefInfoMap == null) {
					glmGridRefInfoMap = loadGlmGridRefInfoMap();
				}
			}
		}
		return glmGridRefInfoMap;
	}

	private Map<String, GlmGridRefInfo> loadGlmGridRefInfoMap() {
		Connection conn = null;
		Map<String, GlmGridRefInfo> infoMap = null;
		try {
			conn = MultiDataSourceFactory.getInstance().getSysDataSource()
					.getConnection();
			QueryRunner runner = new QueryRunner();
			String sql = "SELECT TABLE_NAME,REF_COL_NAME,REF_INFO,SINGLE_MESH FROM GLM_GRID_MAP WHERE GDB_VERSION = ?";
			infoMap = runner.query(conn, sql,
					new ResultSetHandler<Map<String, GlmGridRefInfo>>() {

						@Override
						public Map<String, GlmGridRefInfo> handle(ResultSet rs)
								throws SQLException {
							Map<String, GlmGridRefInfo> map = new HashMap<String, GlmGridRefInfo>();
							while (rs.next()) {
								String tableName = rs.getString("TABLE_NAME");
								GlmGridRefInfo info = new GlmGridRefInfo(
										tableName);
								info.setGridRefCol(rs.getString("REF_COL_NAME"));
								info.setSingleMesh(rs.getInt("SINGLE_MESH") == 1 ? true
										: false);
								String refInfo = rs.getString("REF_INFO");
								refInfo = com.navinfo.dataservice.commons.util.StringUtils
										.removeBlankChar(refInfo);
								if (StringUtils.isNotEmpty(refInfo)) {
									String[] refArr = refInfo.split(",");
									List<String[]> refList = new ArrayList<String[]>();
									for (String ref : refArr) {
										String[] arr = ref.split(":");
										if (arr.length == 3) {
											refList.add(arr);
										} else {
											log.warn("GLM_GRID_MAP配置表中关于表"
													+ tableName
													+ "的参考配置信息格式不正确，系统已经忽略，请手工检查");
											break;
										}
									}
									if (refList.size() > 0) {
										info.setRefInfo(refList);
										map.put(tableName, info);
									} else {
										// ..忽略该表配置
									}
								} else {
									info.setRefInfo(null);
									map.put(tableName, info);
								}
							}
							return map;
						}

					}, gdbVersion);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
		return infoMap;
	}

	public GlmGridRefInfo getGlmGridRefInfo(String tableName) {
		return getGlmGridRefInfoMap().get(tableName);
	}

	public LogGeoInfo calc(String tableName, String pidColName, long pid,
			Connection dataConn) throws SQLException {
		log.debug("calc :" + tableName + "-" + pidColName + "-" + pid);
		String sql = assembleQueryGeoSql(tableName, pidColName, pid);
		LogGeoInfo geoInfo = run.query(dataConn, sql, new SingleRowGridHandler(
				tableName));
		return geoInfo;
	}

	/**
	 * 给定表的row_id查询所属grid
	 * 
	 * @param tableName
	 * @param rowIds
	 * @param dataConn
	 *            ：数据所在库的连接
	 * @param sameTableName
	 *            同一点或者同一线组成要素的tableName
	 * @return: key-value:key-rowId,value-grid号码字符串数组
	 * @throws Exception
	 */
	public LogGeoInfo calc(String tableName, String rowId, Connection dataConn,
			String... sameTableName) throws Exception {
		log.debug("calc :" + tableName + "-" + rowId);
		String sql = assembleQueryGeoSql(tableName, rowId);
		if (tableName.equals("RD_SAMENODE") && sameTableName.length > 0) {
			sql = sql.replaceAll("RD_NODE", sameTableName[0]);
		}
		if (tableName.equals("RD_SAMELINK") && sameTableName.length > 0) {
			sql = sql.replaceAll("RD_LINK", sameTableName[0]);
		}
		LogGeoInfo geoInfo = run.query(dataConn, sql, new SingleRowGridHandler(
				tableName));
		return geoInfo;
	}

	/**
	 * 给定表的row_id查询所属grid
	 * 
	 * @param tableName
	 * @param rowIds
	 * @param dataConn
	 *            ：数据所在库的连接
	 * @return:key-value:key-rowId,value-grid号码字符串数组
	 */
	public Map<String, String[]> calc(String tableName, Set<String> rowIds,
			Connection dataConn) {
		// ...
		return null;
	}

	/**
	 * 给定履历表的过滤条件，通过履历表查询grid号码 数据和履历在同一个库
	 * 
	 * @param tableName
	 * @param logOpTypes
	 *            :履历的OP_TP类型
	 * @param logConn
	 *            ：数据和履历所在库的连接
	 * @return key:log_detail.row_id,value:对应的记录所属的grid
	 */
	public Map<String, String[]> calc(String tableName, Integer[] logOpTypes,
			Connection logConn) throws SQLException {
		String sql = assembleQueryGeoSql(tableName, logOpTypes);
		Map<String, String[]> grids = run.query(logConn, sql,
				new MultiRowGridHandler(tableName));
		return grids;
	}

	/**
	 * 给定履历表的过滤条件，通过履历表查询grid号码 数据和履历不在同一个库，通过连接到履历查询
	 * 若remoteType=CROSS_USER，那么需要履历所在库和cross user库在同一台服务器上
	 * 若remoteType=DB_LINK,，那么前提是履历所在库已经存在指向参考的数据库的database link
	 * 
	 * @param tableName
	 * @param logOpTypes
	 *            ：履历的操作状态过滤条件
	 * @param logConn
	 *            :履历库所在库的连接
	 * @param remoteType
	 *            ：CROSS_USER/DB_LINK
	 * @param remoteParam
	 *            :cross user name/database link name
	 * @return
	 * @throws SQLException
	 */
	public Map<String, String[]> calc(String tableName, Integer[] logOpTypes,
			Connection logConn, String remoteType, String remoteParam)
			throws SQLException {
		String sql = null;
		if ("CROSS_USER".equals(remoteType)) {
			sql = assembleQueryGeoSqlByCrossUser(tableName, logOpTypes,
					remoteParam);
		} else if ("DB_LINK".equals(remoteType)) {
			sql = this.assembleQueryGeoSql_Dblink(tableName, logOpTypes,
					remoteParam);
		}
		Map<String, String[]> grids = run.query(logConn, sql,
				new MultiRowGridHandler(tableName));
		return grids;
	}

	/**
	 * 
	 * @param type
	 *            :rowid/log
	 * @return
	 * @throws Exception
	 */
	private String assembleQueryGeoSql(String tableName, String rowId)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		GlmGridRefInfo refInfo = getGlmGridRefInfoMap().get(tableName);
		try {
			sb.append(refInfo.getEditQuerySql());
			sb.append(" AND P.ROW_ID = HEXTORAW('");
			sb.append(rowId);
			sb.append("')");
		} catch (Exception e) {
			throw new Exception("表" + tableName + "未在glm_grid_map中配置映射关系");
		}

		return sb.toString();

	}

	/**
	 * 
	 * @param type
	 *            :rowid/log
	 * @return
	 */
	private String assembleQueryGeoSql(String tableName, String pidColName,
			long pid) {
		StringBuilder sb = new StringBuilder();
		GlmGridRefInfo refInfo = getGlmGridRefInfoMap().get(tableName);
		sb.append(refInfo.getEditQuerySql());
		sb.append(" AND P.");
		sb.append(pidColName);
		sb.append(" = " + pid);
		return sb.toString();

	}

	private String assembleQueryGeoSql(String tableName, Integer[] logOpTypes) {
		StringBuilder sb = new StringBuilder();
		GlmGridRefInfo refInfo = getGlmGridRefInfoMap().get(tableName);
		sb.append(refInfo.getDiffQuerySql());
		sb.append(" AND L.OP_TP IN (" + StringUtils.join(logOpTypes, ",") + ")");
		return sb.toString();
	}

	private String assembleQueryGeoSql_Dblink(String tableName,
			Integer[] logOpTypes, String dbLinkName) {
		StringBuilder sb = new StringBuilder();
		GlmGridRefInfo refInfo = getGlmGridRefInfoMap().get(tableName);
		sb.append(refInfo.replaceDiffSqlByDbLink(dbLinkName));
		sb.append(" AND L.OP_TP IN (" + StringUtils.join(logOpTypes, ",") + ")");
		return sb.toString();
	}

	private String assembleQueryGeoSqlByCrossUser(String tableName,
			Integer[] logOpTypes, String crossUserName) {
		StringBuilder sb = new StringBuilder();
		GlmGridRefInfo refInfo = getGlmGridRefInfoMap().get(tableName);
		sb.append(refInfo.replaceDiffSqlByCrossUser(crossUserName));
		sb.append(" AND L.OP_TP IN (" + StringUtils.join(logOpTypes, ",") + ")");
		return sb.toString();
	}

	/**
	 * 一次查询的一条记录的grid号
	 * 
	 * @ClassName: SingleRowGridHandler
	 * @author xiaoxiaowen4127
	 * @date 2016年7月27日
	 * @Description: GlmGridCalculator.java
	 */
	class SingleRowGridHandler implements ResultSetHandler<LogGeoInfo> {

		private String tableName;

		public SingleRowGridHandler(String tableName) {
			this.tableName = tableName;
		}

		@Override
		public LogGeoInfo handle(ResultSet rs) throws SQLException {
			String rowId = null;
			LogGeoInfo geoInfo = new LogGeoInfo();
			try {
				Set<String> rowGrids = new HashSet<String>();
				if ("CK_EXCEPTION".equals(tableName)) {
					WKT wkt = new WKT();
					while (rs.next()) {
						rowId = rs.getString("ROW_ID");
						geoInfo.setGeoName(rs.getString("GEO_NM"));
						geoInfo.setGeoPid(rs.getInt("GEO_PID"));
						JGeometry geo = wkt
								.toJGeometry(rs.getBytes("GEOMETRY"));
						int meshId = rs.getInt("MESH_ID");
						if (meshId > 0) {
							rowGrids.addAll(JGeometryUtil
									.intersectGeometryGrid(geo,
											String.valueOf(meshId)));
						} else {
							String[] meshes = JGeometryUtil.geo2MeshIds(geo);
							rowGrids.addAll(JGeometryUtil
									.intersectGeometryGrid(geo, meshes));
						}
					}
				} else {
					while (rs.next()) {
						rowId = rs.getString("ROW_ID");
						geoInfo.setGeoName(rs.getString("GEO_NM"));
						geoInfo.setGeoPid(rs.getInt("GEO_PID"));
						JGeometry geo = null;
						geo = JGeometry.load(rs.getBytes("GEOMETRY"));
						int meshId = rs.getInt("MESH_ID");
						if (meshId > 0) {
							rowGrids.addAll(JGeometryUtil
									.intersectGeometryGrid(geo,
											String.valueOf(meshId)));
						} else {
							String[] meshes = JGeometryUtil.geo2MeshIds(geo);
							rowGrids.addAll(JGeometryUtil
									.intersectGeometryGrid(geo, meshes));
						}
					}
				}
				geoInfo.setGrids(rowGrids.toArray(new String[0]));
				return geoInfo;
			} catch (Exception e) {
				throw new SQLException("查询的geometry可能格式错误，无法转换为object。row_id:"
						+ rowId, e);
			}
		}
	}

	/**
	 * 一次查询多条记录的grid号
	 * 
	 * @ClassName: MultiRowGridHandler
	 * @author xiaoxiaowen4127
	 * @date 2016年7月27日
	 * @Description: GlmGridCalculator.java
	 */
	class MultiRowGridHandler implements
			ResultSetHandler<Map<String, String[]>> {
		private String tableName;

		public MultiRowGridHandler(String tableName) {
			this.tableName = tableName;
		}

		@Override
		public Map<String, String[]> handle(ResultSet rs) throws SQLException {
			String rowId = null;
			try {
				Map<String, String[]> results = new HashMap<String, String[]>();
				Map<String, Set<String>> gs = new HashMap<String, Set<String>>();
				if ("CK_EXCEPTION".equals(tableName)) {
					WKT wkt = new WKT();
					while (rs.next()) {
						rowId = rs.getString("ROW_ID");
						Set<String> rowGrids = gs.get(rowId);
						if (rowGrids == null) {
							rowGrids = new HashSet<String>();
							gs.put(rowId, rowGrids);
						}
						JGeometry geo = wkt
								.toJGeometry(rs.getBytes("GEOMETRY"));
						int meshId = rs.getInt("MESH_ID");
						if (meshId > 0) {
							rowGrids.addAll(JGeometryUtil
									.intersectGeometryGrid(geo,
											String.valueOf(meshId)));
						} else {
							String[] meshes = JGeometryUtil.geo2MeshIds(geo);
							rowGrids.addAll(JGeometryUtil
									.intersectGeometryGrid(geo, meshes));
						}
					}
				} else {
					while (rs.next()) {
						rowId = rs.getString("ROW_ID");
						Set<String> rowGrids = gs.get(rowId);
						if (rowGrids == null) {
							rowGrids = new HashSet<String>();
							gs.put(rowId, rowGrids);
						}
						JGeometry geo = null;
						geo = JGeometry.load(rs.getBytes("GEOMETRY"));
						int meshId = rs.getInt("MESH_ID");
						if (meshId > 0) {
							rowGrids.addAll(JGeometryUtil
									.intersectGeometryGrid(geo,
											String.valueOf(meshId)));
						} else {
							String[] meshes = JGeometryUtil.geo2MeshIds(geo);
							rowGrids.addAll(JGeometryUtil
									.intersectGeometryGrid(geo, meshes));
						}
					}
				}
				// convert to array
				for (Map.Entry<String, Set<String>> entry : gs.entrySet()) {
					results.put(entry.getKey(),
							entry.getValue().toArray(new String[0]));
				}
				return results;
			} catch (Exception e) {
				log.info(e.getMessage(),e);
				throw new SQLException("查询的geometry可能格式错误，无法转换为object。row_id:"
						+ rowId, e);
			}
		}

	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
}
