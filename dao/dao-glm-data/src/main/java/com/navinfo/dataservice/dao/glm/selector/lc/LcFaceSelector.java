package com.navinfo.dataservice.dao.glm.selector.lc;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.lc.LcFace;
import com.navinfo.dataservice.dao.glm.model.lc.LcFaceName;
import com.navinfo.dataservice.dao.glm.model.lc.LcFaceTopo;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.exception.DAOException;
import com.navinfo.navicommons.exception.ServiceException;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangyt
 * @Title: LcFaceSelector.java
 * @Description: TODO
 * @date: 2016年7月27日 上午11:37:16
 * @version: v1.0
 */
public class LcFaceSelector extends AbstractSelector {

    private static Logger logger = Logger.getLogger(LcFaceSelector.class);

    private Connection conn;

    public LcFaceSelector(Connection conn) throws InstantiationException, IllegalAccessException {
        super(LcFace.class, conn);
        this.conn = conn;
    }

    public List<LcFace> loadLcFaceByLinkId(int linkPid, boolean isLock) throws Exception {
        List<LcFace> faces = new ArrayList<LcFace>();
        StringBuilder bf = new StringBuilder();
        bf.append("select b.* from lc_face b where b.face_pid in (select a.face_pid ");
        bf.append("  FROM lc_face a, lc_face_topo t");
        bf.append(" WHERE a.u_record != 2  AND T.U_RECORD != 2");
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
                LcFace face = new LcFace();
                ReflectionAttrUtils.executeResultSet(face, resultSet);
                faces.add(face);
                List<IRow> lcFaceTopo = new LcFaceTopoSelector(conn).loadRowsByParentId(face.getPid(), isLock);
                for (IRow row : lcFaceTopo) {
                    row.setMesh(face.mesh());
                }
                face.setTopos(lcFaceTopo);
                for (IRow row : lcFaceTopo) {
                    LcFaceTopo obj = (LcFaceTopo) row;
                    face.lcFaceTopoMap.put(obj.rowId(), obj);
                }
                List<IRow> lcFaceName = new LcFaceNameSelector(conn).loadRowsByParentId(face.getPid(), isLock);
                face.setNames(lcFaceName);
                for (IRow row : lcFaceName) {
                    LcFaceName obj = (LcFaceName) row;
                    face.lcFaceNameMap.put(obj.rowId(), obj);
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Exception e) {
            }
        }
        return faces;
    }

    public List<LcFace> loadLcFaceByNodeId(int nodePid, boolean isLock) throws Exception {
        List<LcFace> faces = new ArrayList<LcFace>();
        StringBuilder builder = new StringBuilder();
        builder.append(" SELECT b.* from lc_face b where b.face_pid in (select a.face_pid ");
        builder.append(" FROM lc_face a, lc_face_topo t, lc_link l ");
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
                LcFace face = new LcFace();
                ReflectionAttrUtils.executeResultSet(face, resultSet);
                faces.add(face);
                List<IRow> lcFaceTopo = new LcFaceTopoSelector(conn).loadRowsByParentId(face.getPid(), isLock);
                for (IRow row : lcFaceTopo) {
                    row.setMesh(face.mesh());
                }
                face.setTopos(lcFaceTopo);
                for (IRow row : lcFaceTopo) {
                    LcFaceTopo obj = (LcFaceTopo) row;
                    face.lcFaceTopoMap.put(obj.rowId(), obj);
                }
                List<IRow> lcFaceName = new LcFaceNameSelector(conn).loadRowsByParentId(face.getPid(), isLock);
                face.setNames(lcFaceName);
                for (IRow row : lcFaceName) {
                    LcFaceName obj = (LcFaceName) row;
                    face.lcFaceNameMap.put(obj.rowId(), obj);
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (Exception e) {
            }
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Exception e) {
            }
        }
        return faces;
    }

    /**
     * 计算与指定几何相交的土地覆盖面
     * @param wkt 几何
     * @param isLock 是否加锁
     * @return 相交面
     */
    public List<LcFace> listLcface(String wkt, List<Integer> excludes, boolean isLock) throws ServiceException {
        List<LcFace> list = new ArrayList<>();
        String sql = "SELECT T.FACE_PID, T.GEOMETRY, T.KIND FROM LC_FACE T WHERE SDO_WITHIN_DISTANCE(T.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0'"
                + ") = 'TRUE' AND T.U_RECORD <> 2 AND T.FACE_PID NOT IN(:2)";
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = getConn().prepareStatement(sql);
            pstmt.setString(1, wkt);
            pstmt.setString(2, StringUtils.getInteStr(excludes));
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                LcFace face = new LcFace();
                ReflectionAttrUtils.executeResultSet(face, resultSet);
                list.add(face);
            }
        } catch (SQLException e){
            logger.error("计算与指定几何相交的行政区划面数量出错", e);
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
