package com.navinfo.dataservice.dao.check;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import com.navinfo.dataservice.bizcommons.glm.Glm;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculator;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculatorFactory;
import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.dao.pool.OracleAddress;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;

public class NiValExceptionOperator {

	private Connection conn;
	private String gdbVersion;
	private Map<String, Geometry> geometryMap=new HashMap<String, Geometry>();
	private Map<String, Integer> meshMap=new HashMap<String, Integer>();
	private CkObjectNodeLoader objectNodeLoader=CkObjectNodeLoader.getInstance();
	private static Logger logg = Logger.getLogger(NiValExceptionOperator.class);
	public NiValExceptionOperator() {

	}

	public NiValExceptionOperator(Connection conn)
			throws Exception {
		this.conn = conn;
		this.gdbVersion=SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);

		//ProjectSelector selector = new ProjectSelector();

		//this.gdbVersion = selector.getGdbVersion(projectId);
	}

	public void insertCheckLogGrid(String md5, String loc) throws Exception {

		Glm glm = GlmCache.getInstance().getGlm(gdbVersion);

		GlmGridCalculator calculator = GlmGridCalculatorFactory.getInstance()
				.create(gdbVersion);

		String insertSql = "INSERT INTO NI_VAL_EXCEPTION_GRID (md5_code,GRID_ID) VALUES (?,?)";

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(insertSql);
			JGeometry geo = GeoTranslator.wkt2JGrometry(loc);
			String[] grids = CompGridUtil.point2Grids(geo.getPoint()[0],geo.getPoint()[1]);
			if(grids!=null){
				for (String grid : grids) {
					stmt.setString(1, md5);
					stmt.setLong(2, Long.valueOf(grid));
					stmt.addBatch();
				}
			}
			stmt.executeBatch();
			stmt.clearBatch();

		} catch (Exception e) {
			throw e;
		} finally {
			try {
				stmt.close();
			} catch (Exception e) {

			}
		}
	}
	
	private List<Object> calGeometryAndMesh(String tableName,String pid) throws Exception{
		List<Object> list=new ArrayList<Object>();
		String key=tableName+":"+pid;
		if(geometryMap.containsKey(key)){
			list.add(geometryMap.get(key));
			list.add(meshMap.get(key));
		}
		if(list.size()==0){
			CkObjectNode objectNode=objectNodeLoader.getObjectNode(tableName);
			if(objectNode.getMeshTable().equals("RD_LINK")){
				RdLinkSelector linkSelector=new RdLinkSelector(conn);
				String sql=objectNode.getMeshSql();
				sql=sql.replace("!OBJECT_PID!", pid);
				RdLink link=linkSelector.loadBySql(sql, false).get(0);
				geometryMap.put(key, GeometryUtils.getPointFromGeo(GeoTranslator.transform(link.getGeometry(), 0.00001,5)));
				meshMap.put(key, link.mesh());
			}
			if(objectNode.getMeshTable().equals("RD_NODE")){
				RdNodeSelector nodeSelector=new RdNodeSelector(conn);
				String sql=objectNode.getMeshSql();
				sql=sql.replace("!OBJECT_PID!", pid);
				RdNode node=nodeSelector.loadBySql(sql, false).get(0);
				Geometry geo=node.getGeometry();
				geometryMap.put(key, node.getGeometry());
				meshMap.put(key, Integer.valueOf(MeshUtils.point2Meshes(geo.getCoordinate().x*0.00001, geo.getCoordinate().y*0.00001)[0]));
			}
			list.add(geometryMap.get(key));
			list.add(meshMap.get(key));
		}
		return list;
	}
	
	private List calGeoAndMeshWithTarget(String targets) throws Exception{
		List<Object> list=null;
		String value=StringUtils.removeBlankChar(targets);
		if (value != null && value.length() > 2) {
			String subValue = value.substring(1, value.length() - 1);
			for (String table : subValue.split("\\];\\[")) {
				String[] arr = table.split(",");
				list=calGeometryAndMesh(arr[0],arr[1]);
				if(list!=null && list.size()!=0){return list;}
			}
		}
		return list;
	}

	public void insertCheckLog(String ruleId, String loc, String targets,
			int meshId, String worker) throws Exception {
		logg.debug("start insert ni_val:1");
		if(loc==null||loc.isEmpty()){
			List<Object> list=calGeoAndMeshWithTarget(targets);
			loc = GeoTranslator.jts2Wkt((Geometry) list.get(0), 0.00001, 5);
			meshId = (int) list.get(1);
		}
		logg.debug("start insert ni_val:2");
		String sql = "merge into ni_val_exception a using ( select * from ( select :1 as MD5_CODE from dual) where MD5_CODE not in ( select MD5_CODE          from ni_val_exception          where MD5_CODE is not null        union all        select RESERVED as MDS_CODE          from ck_exception          where RESERVED is not null          )) b on (a.MD5_CODE = b.MD5_CODE) when not matched then   insert     (MD5_CODE, ruleid, information, location, targets, mesh_id, worker, \"LEVEL\", created, updated )   values     (:2, :3, :4, sdo_geometry(:5, 8307), :6, :7, :8, :9, sysdate, sysdate)";
		PreparedStatement pstmt = conn.prepareStatement(sql);

		try {
			String md5 = this.generateMd5(ruleId,
					CheckItems.getInforByRuleId(ruleId), targets, null);

			pstmt.setString(1, md5);

			pstmt.setString(2, md5);

			pstmt.setString(3, ruleId);

			pstmt.setString(4, CheckItems.getInforByRuleId(ruleId));

			pstmt.setString(5, loc);

			pstmt.setString(6, targets);

			pstmt.setInt(7, meshId);

			pstmt.setString(8, worker);

			pstmt.setInt(9, 1);

			int res = pstmt.executeUpdate();

			logg.debug("start insert ni_val:3");

			if (res > 0) {

				CkResultObjectOperator op = new CkResultObjectOperator(conn);

				op.insertCkResultObject(md5, targets);
				logg.debug("start insert ni_val:3-1");

				this.insertCheckLogGrid(md5, loc);
				logg.debug("start insert ni_val:3-2");
			}

		} catch (Exception e) {
			throw e;
		} finally {

			try {
				pstmt.close();
			} catch (Exception e) {
			}
		}
	}

	public void insertCheckLog(String ruleId, String loc, String targets,
			int meshId, String log, String worker) throws Exception {
		logg.debug("start insert ni_val:1");
		if(loc==null||loc.isEmpty()){
			List<Object> list=calGeoAndMeshWithTarget(targets);
			loc = GeoTranslator.jts2Wkt((Geometry) list.get(0), 0.00001, 5);
			meshId = (int) list.get(1);
		}
		logg.debug("start insert ni_val:2");
		String md5Sql = "SELECT LOWER(UTL_RAW.CAST_TO_RAW(DBMS_OBFUSCATION_TOOLKIT.MD5(INPUT_STRING =>?||?||?||?))) "
				+ "AS MD5_CODE FROM DUAL "
				+ "minus "
		+"(SELECT N.MD5_CODE FROM NI_VAL_EXCEPTION N "
		+ "UNION SELECT C.Md5_Code FROM CK_EXCEPTION C)";
//		String md5 = this.generateMd5(ruleId, log, targets, null);
//		String sql = "merge into ni_val_exception a using (select :1 as MD5_CODE from dual) b on (a.MD5_CODE = b.MD5_CODE) when not matched then   insert (MD5_CODE, ruleid, information, location, targets, mesh_id, worker, \"LEVEL\", created, updated )   values     (:2, :3, :4, sdo_geometry(:5, 8307), :6, :7, :8, :9, sysdate, sysdate)";
		logg.debug("start insert ni_val:2-1");
//		String cSql = "SELECT 1 FROM NI_VAL_EXCEPTION WHERE MD5_CODE=? UNION SELECT 1 FROM CK_EXCEPTION WHERE MD5_CODE=?";
		String md5 = new QueryRunner().queryForString(conn, md5Sql, ruleId,log,targets,"null");
		
		if(StringUtils.isEmpty(md5))return;
		logg.debug("start insert ni_val:2-2");
		String sql = "insert into ni_val_exception(MD5_CODE, ruleid, information, location, targets, mesh_id, worker, \"LEVEL\", created, updated )   values     (:2, :3, :4, sdo_geometry(:5, 8307), :6, :7, :8, :9, sysdate, sysdate)";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		logg.debug("start insert ni_val:2-3");
		try {
			logg.debug("start insert ni_val:2-4");
			pstmt.setString(1, md5);
			logg.debug("start insert ni_val:2-5");
			pstmt.setString(2, ruleId);
			logg.debug("start insert ni_val:2-6");
			pstmt.setString(3, log);
			logg.debug("start insert ni_val:2-7");

			pstmt.setString(4, loc);
			logg.debug("start insert ni_val:2-8");
			pstmt.setString(5, targets);
			logg.debug("start insert ni_val:2-9");
			pstmt.setInt(6, meshId);
			logg.debug("start insert ni_val:2-10");
			pstmt.setString(7, worker);
			logg.debug("start insert ni_val:2-11");
			pstmt.setInt(8, 1);
			logg.debug("start insert ni_val:2-12");
			int res = pstmt.executeUpdate();
			logg.debug("start insert ni_val:3");

			if (res > 0) {

				CkResultObjectOperator op = new CkResultObjectOperator(conn);

				op.insertCkResultObject(md5, targets);
				logg.debug("start insert ni_val:3-1");
				
				this.insertCheckLogGrid(md5, loc);
				logg.debug("start insert ni_val:3-2");
			}

		} catch (Exception e) {
			throw e;
		} finally {

			try {
				pstmt.close();
			} catch (Exception e) {

			}

		}

	}

	public void deleteNiValException(String tableName, int pid)
			throws Exception {

		String sql = "delete from ni_val_exception a where exists (select null from ck_result_object b where a.MD5_CODE=b.md5_code and b.table_name=? and b.pid=?)";

		PreparedStatement pstmt = null;

		try {

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, tableName);

			pstmt.setInt(2, pid);

			pstmt.executeUpdate();

			pstmt.close();
			
			sql = "delete from NI_VAL_EXCEPTION_GRID a where exists (select null from ck_result_object b where a.md5_code=b.md5_code and b.table_name=? and b.pid=?)";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, tableName);

			pstmt.setInt(2, pid);

			pstmt.executeUpdate();
			
			pstmt.close();

			sql = "delete from ck_result_object a where a.md5_code in (select b.md5_code from ck_result_object b where b.table_name=? and b.pid=?)";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, tableName);

			pstmt.setInt(2, pid);

			pstmt.executeUpdate();

		} catch (Exception e) {
			throw e;
		} finally {

			try {
				pstmt.close();
			} catch (Exception e) {

			}

		}

	}

	/**
	 * 修改检查结果状态为例外、确认已修改、确认不修改 确认已修改进ni_val_exception_history表
	 * 例外、确认不修改就ck_exception表
	 * 
	 * @param md5
	 * @param projectId
	 * @param type
	 *            1例外，2确认不修改，3确认已修改
	 * @throws Exception
	 */
	public void updateCheckLogStatus(String md5, int type)
			throws Exception {

		conn.setAutoCommit(false);

		PreparedStatement pstmt = null;

		try {

			Result result = null;

			String sql = "";

			if (type == 3) {
				sql = "delete from ni_val_exception a where a.MD5_CODE=:1";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, md5);
				pstmt.executeUpdate();
				pstmt.close();
				
				sql = "delete from ni_val_exception_grid a where a.MD5_CODE=:1";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, md5);
				pstmt.executeUpdate();
				pstmt.close();
				
				sql = "delete from ck_result_object a where a.MD5_CODE=:1";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, md5);
				pstmt.executeUpdate();
				pstmt.close();				
			} else {

				NiValExceptionSelector selector = new NiValExceptionSelector(
						conn);

				NiValException exception = selector.loadById(md5, false);

				CkException ckexception = new CkException();

				ckexception.copy(exception);

				int pid = PidUtil.getInstance().applyCkExceptionId();

				ckexception.setExceptionId(pid);

				ckexception.setStatus(type);

				ckexception.setRowId(UuidUtils.genUuid());

				sql = "insert into ck_exception(exception_id, rule_id, task_name, status, group_id, rank, situation, information, suggestion, geometry, targets, addition_info, memo, create_date, update_date, mesh_id, scope_flag, province_name, map_scale, MD5_CODE, extended, task_id, qa_task_id, qa_status, worker, qa_worker, row_id, u_record) select :1,ruleid, task_name,:2,groupid, \"LEVEL\" level_, situation, information, suggestion,sdo_util.to_wktgeometry(location), targets, addition_info, '',created, updated, mesh_id, scope_flag, province_name, map_scale, MD5_CODE, extended, task_id, qa_task_id, qa_status, worker, qa_worker,:3,1 from ni_val_exception a where a.MD5_CODE=:4";

				pstmt = conn.prepareStatement(sql);
				
				pstmt.setInt(1, pid);
				
				pstmt.setInt(2, type);
				
				pstmt.setString(3, ckexception.rowId());

				pstmt.setString(4, md5);

				pstmt.executeUpdate();

				pstmt.close();
				
				sql = "insert into ck_exception_grid select :1,grid_id from NI_VAL_EXCEPTION_GRID where md5_code=:2";

				pstmt = conn.prepareStatement(sql);
				
				pstmt.setString(1, ckexception.rowId());

				pstmt.setString(2, md5);

				pstmt.executeUpdate();
				
				pstmt.close();
				
				result = new Result();

				result.insertObject(ckexception, ObjStatus.INSERT,
						ckexception.getExceptionId());

			}

			sql = "delete from ni_val_exception where MD5_CODE=:1";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, md5);

			pstmt.executeUpdate();

			pstmt.close();

			sql = "delete from ck_result_object where md5_code=:1";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, md5);

			pstmt.executeUpdate();
			
			pstmt.close();

			sql = "delete from NI_VAL_EXCEPTION_GRID where md5_code=:1";

			pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, md5);

			pstmt.executeUpdate();

			if (result != null) {
				LogWriter writer = new LogWriter(conn);

				Command command = new Command();

				writer.generateLog(command, result);

				writer.recordLog(command, result);
			}

			conn.commit();

		} catch (Exception e) {
			throw e;
		} finally {

			try {
				pstmt.close();
			} catch (Exception e) {

			}
		}
	}

	private String generateMd5(String ruleId, String infor, String targets,
			String addInfo) {

		StringBuilder sb = new StringBuilder(ruleId);

		sb.append(infor);

		sb.append(targets);

		if (addInfo != null) {
			sb.append(addInfo);
		}

		return getMd5(sb.toString());

	}

	private static MessageDigest md5 = null;
	static {
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * 用于获取一个String的md5值
	 * 
	 * @param string
	 * @return
	 */
	private static String getMd5(String str) {
		byte[] bs = md5.digest(str.getBytes());
		StringBuilder sb = new StringBuilder(40);
		for (byte x : bs) {
			if ((x & 0xff) >> 4 == 0) {
				sb.append("0").append(Integer.toHexString(x & 0xff));
			} else {
				sb.append(Integer.toHexString(x & 0xff));
			}
		}
		return sb.toString();
	}

	public static void main(String[] args) throws Exception {

		String username1 = "fm_prjgdb_bj02";

		String password1 = "fm_prjgdb_bj02";

		int port1 = 1521;

		String ip1 = "192.168.4.61";

		String serviceName1 = "orcl";

		OracleAddress oa1 = new OracleAddress(username1, password1, port1, ip1,
				serviceName1);

		NiValExceptionOperator op = new NiValExceptionOperator(oa1.getConn());

		// op.insertCheckLog("3213131", "POINT(116.1313 37.131)",
		// "[RD_LINK,32131]", 13, "13");

		op.updateCheckLogStatus("5490db11c96209409ce126ac3058c292", 3);

		// op.deleteNiValException("RD_LINK", 32131);

		System.out.println("done");

	}
}
