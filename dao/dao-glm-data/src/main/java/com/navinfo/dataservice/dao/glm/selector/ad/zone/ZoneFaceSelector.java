package com.navinfo.dataservice.dao.glm.selector.ad.zone;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFace;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneFaceTopo;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.exception.DAOException;
import com.navinfo.navicommons.exception.ServiceException;
import com.vividsolutions.jts.geom.Geometry;
import oracle.jdbc.OracleTypes;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ZONE:Face查询接口
 * 
 * @author zhaokk
 * 
 */
public class ZoneFaceSelector extends AbstractSelector {

    private static Logger logger = Logger.getLogger(ZoneFaceSelector.class);

	private Connection conn;

	public ZoneFaceSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(ZoneFace.class);
	}

	/**
	 * 获取此ZONELINK上行政取区划面拓扑关系
	 * 
	 * @param linkPid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public List<ZoneFace> loadZoneFaceByLinkId(int linkPid, boolean isLock) throws Exception {
		List<ZoneFace> faces = new ArrayList<ZoneFace>();
		StringBuilder bf = new StringBuilder();
		bf.append("select b.* from zone_face b where b.face_pid in (select a.face_pid ");
		bf.append("  FROM zone_face a, zone_face_topo t");
		bf.append(" WHERE     a.u_record != 2  AND T.u_record != 2");
		bf.append("  AND a.face_pid = t.face_pid");
		bf.append(" AND t.link_pid = :1 group by a.face_pid)");
		if (isLock) {
			bf.append(" for update nowait");
		}
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(bf.toString());

			pstmt.setInt(1, linkPid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				ZoneFace face = new ZoneFace();
				ReflectionAttrUtils.executeResultSet(face, resultSet);
				this.setChildData(face, isLock);
				faces.add(face);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return faces;
	}

	public List<ZoneFace> loadZoneFaceByNodeId(int nodePid, boolean isLock) throws Exception {

		List<ZoneFace> faces = new ArrayList<ZoneFace>();

		StringBuilder builder = new StringBuilder();
		builder.append(" SELECT b.* from zone_face b where b.face_pid in (select a.face_pid ");
		builder.append(" FROM zone_face a, zone_face_topo t, zone_link l ");
		builder.append(" WHERE a.u_record != 2 and t.u_record != 2 and l.u_record != 2 ");
		builder.append(" AND a.face_pid = t.face_pid");
		builder.append(" AND t.link_pid = l.link_pid");
		builder.append(" AND (l.s_node_pid = :1 OR l.e_node_pid = :2) group by a.face_pid)");

		if (isLock) {
			builder.append(" for update nowait");
		}
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(builder.toString());

			pstmt.setInt(1, nodePid);

			pstmt.setInt(2, nodePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				ZoneFace face = new ZoneFace();
				ReflectionAttrUtils.executeResultSet(face, resultSet);
				this.setChildData(face, isLock);
				faces.add(face);

			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return faces;
	}

	private void setChildData(ZoneFace face, boolean isLock) throws Exception {
		List<IRow> adFaceTopo = new ZoneFaceTopoSelector(conn).loadRowsByParentId(face.getPid(), isLock);

		for (IRow row : adFaceTopo) {
			row.setMesh(face.mesh());
		}

		face.setFaceTopos(adFaceTopo);

		for (IRow row : adFaceTopo) {
			ZoneFaceTopo obj = (ZoneFaceTopo) row;

			face.zoneFaceTopoMap.put(obj.rowId(), obj);
		}
	}

	/**
	 * 根据传入几何参数查找与之相关联的ZoneFace面</br>
	 * ADMIN_TYPE类型为:</br>
	 * KDZone（8）或AOI（9）
	 * 
	 * @param geometry
	 * @return
	 */
	public List<ZoneFace> loadRelateFaceByGeometry(Geometry geometry) {
		List<ZoneFace> faces = new ArrayList<ZoneFace>();
		String sql = "select t1.geometry, t2.region_id from zone_face t1, ad_admin t2 where t1.u_record <> 2 and t2.u_record <> 2 and t1.region_id = t2.region_id and (t2.admin_type = 8 or t2.admin_type = 9) and sdo_relate(t1.geometry, sdo_geometry(?, 8307), 'mask=anyinteract') = 'TRUE' ";
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);

            Clob clob = ConnectionUtil.createClob(conn);
            clob.setString(1, GeoTranslator.jts2Wkt(geometry));

			pstmt.setClob(1, clob);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				ZoneFace face = new ZoneFace();
				ReflectionAttrUtils.executeResultSet(face, resultSet);
				faces.add(face);
			}
		} catch (Exception e) {
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return faces;
	}

	/**
	 * 根据regionId查询ZONEFACE
	 * @param regionId 
	 * @param isLock
	 * @return ZoneFace集合
	 * @throws Exception
	 */
	public List<ZoneFace> loadZoneFaceByRegionId(int regionId, boolean isLock) throws Exception {
		List<ZoneFace> faces = new ArrayList<ZoneFace>();

		StringBuilder bf = new StringBuilder();
		bf.append("select * from zone_face where region_id = :1");
		if (isLock) {
			bf.append(" for update nowait");
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = this.conn.prepareStatement(bf.toString());

			pstmt.setInt(1, regionId);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				ZoneFace face = new ZoneFace();
				ReflectionAttrUtils.executeResultSet(face, resultSet);
				faces.add(face);
			}
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}

		return faces;
	}

    /**
     * 计算与指定几何相交的Zone面数量
     * @param wkt 几何
     * @param isLock 是否加锁
     * @return 相交面数量
     */
    public List<ZoneFace> listZoneface(String wkt, List<Integer> excludes, boolean isLock) throws ServiceException {
        List<ZoneFace> list = new ArrayList<>();
        String sql = "SELECT T.FACE_PID, T.REGION_ID, T.GEOMETRY FROM ZONE_FACE T WHERE SDO_WITHIN_DISTANCE(T.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0'"
                + ") = 'TRUE' AND T.U_RECORD <> 2 AND T.FACE_PID NOT IN (:2)";
        if (isLock) {
            sql += " for update nowait";
        }
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = getConn().prepareStatement(sql);
            pstmt.setString(1, wkt);
            pstmt.setString(2, StringUtils.getInteStr(excludes));
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                ZoneFace face = new ZoneFace();
                ReflectionAttrUtils.executeResultSet(face, resultSet);
                list.add(face);
            }
        } catch (SQLException e){
            logger.error("计算与指定几何相交的Zone面数量出错", e);
            throw new DAOException(e.getMessage());
        } catch (Exception e) {
            logger.error("组装数据失败", e);
            throw new ServiceException(e.getMessage());
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
        return list;
    }
}
