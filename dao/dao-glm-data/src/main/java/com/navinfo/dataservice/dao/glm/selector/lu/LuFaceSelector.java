package com.navinfo.dataservice.dao.glm.selector.lu;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.lu.LuFace;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceName;
import com.navinfo.dataservice.dao.glm.model.lu.LuFaceTopo;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.exception.DAOException;
import com.navinfo.navicommons.exception.ServiceException;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LuFaceSelector extends AbstractSelector {

    private static Logger logger = Logger.getLogger(LuFaceSelector.class);

	private Connection conn;

	public LuFaceSelector(Connection conn) throws Exception {
		super(LuFace.class, conn);
		this.conn = conn;
	}

	private void setChildren(boolean isLock, LuFace face, ResultSet resultSet) throws SQLException, Exception {
		List<IRow> luFaceTopo = new LuFaceTopoSelector(conn).loadRowsByParentId(face.getPid(), isLock);
		for (IRow row : luFaceTopo) {
			row.setMesh(face.mesh());
		}
		face.setFaceTopos(luFaceTopo);
		for (IRow row : luFaceTopo) {
			LuFaceTopo obj = (LuFaceTopo) row;
			face.luFaceTopoMap.put(obj.rowId(), obj);
		}

		List<IRow> luFaceNames = new LuFaceNameSelector(conn).loadRowsByParentId(face.getPid(), isLock);
		for (IRow row : luFaceNames) {
			LuFaceName obj = (LuFaceName) row;
			face.luFaceNameMap.put(obj.rowId(), obj);
		}
		face.setFaceNames(luFaceNames);

	}

	public List<LuFace> loadLuFaceByLinkId(int linkPid, boolean isLock) throws Exception {
		List<LuFace> faces = new ArrayList<LuFace>();
		StringBuilder bf = new StringBuilder();
		bf.append("select b.* from lu_face b where b.face_pid in (select a.face_pid ");
		bf.append("  FROM lu_face a, lu_face_topo t");
		bf.append(" WHERE     a.u_record != 2  AND T.U_RECORD != 2");
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
				LuFace face = new LuFace();
				ReflectionAttrUtils.executeResultSet(face, resultSet);
				this.setChildren(isLock, face, resultSet);
				faces.add(face);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return faces;
	}

	public List<LuFace> loadLuFaceByNodeId(int nodePid, boolean isLock) throws Exception {
		List<LuFace> faces = new ArrayList<LuFace>();
		StringBuilder builder = new StringBuilder();
		builder.append(" SELECT b.* from lu_face b where b.face_pid in (select a.face_pid ");
		builder.append(" FROM lu_face a, lu_face_topo t, lu_link l ");
		builder.append(" WHERE a.u_record != 2 and t.u_record != 2 and l.u_record != 2");
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
				LuFace face = new LuFace();
				ReflectionAttrUtils.executeResultSet(face, resultSet);
				this.setChildren(isLock, face, resultSet);
				faces.add(face);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return faces;
	}

	/**
	 * 根据传入几何参数查找与之相关联的BUA面
	 * 
	 * @param geometry
	 * @return
	 */
	public List<LuFace> loadRelateFaceByGeometry(Geometry geometry) {
		List<LuFace> faces = new ArrayList<>();
		String sql = "SELECT *" + 
                "  FROM lu_face t" + 
                " WHERE t.u_record <> 2" + 
                "   AND t.kind = 21" + 
                "   AND sdo_relate(geometry, sdo_geometry(?, 8307), 'mask=anyinteract') =" +
                "       'TRUE'";
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);

            Clob clob = ConnectionUtil.createClob(conn);
            clob.setString(1, GeoTranslator.jts2Wkt(geometry));

            pstmt.setClob(1, clob);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				LuFace face = new LuFace();
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
     * 计算与指定几何相交的土地利用面
     * @param wkt 几何
     * @param isLock 是否加锁
     * @return 相交面
     */
    public List<LuFace> listLufaceRefWkt(String wkt, List<Integer> excludes, boolean isLock) throws ServiceException {
        List<LuFace> list = new ArrayList<>();
        String sql = "SELECT T.FACE_PID, T.GEOMETRY, T.KIND FROM LU_FACE T WHERE SDO_WITHIN_DISTANCE(T.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0'"
                + ") = 'TRUE' AND T.U_RECORD <> 2 AND T.FACE_PID NOT IN(:2)";
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = getConn().prepareStatement(sql);
            pstmt.setString(1, wkt);
            pstmt.setString(2, StringUtils.getInteStr(excludes));
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                LuFace face = new LuFace();
                ReflectionAttrUtils.executeResultSet(face, resultSet);
                list.add(face);
            }
        } catch (SQLException e){
            logger.error("计算与指定几何相交的行政区划面数量出错", e);
            throw new DAOException(e.getMessage());
        } catch (Exception e) {
            logger.error("封装LuFace数据出错", e);
            throw new ServiceException(e.getMessage());
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
        return list;
    }

}
