package com.navinfo.dataservice.dao.glm.selector.ad.geo;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
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

public class AdFaceSelector extends AbstractSelector {

    private static Logger logger = Logger.getLogger(AdFaceSelector.class);

    private Connection conn;

    public AdFaceSelector(Connection conn) {
        super(conn);
        this.conn = conn;
        this.setCls(AdFace.class);
    }

    public List<AdFace> loadAdFaceByLinkGeometry(String wkt, boolean isLock) throws Exception {
        List<AdFace> faces = new ArrayList<AdFace>();
        // TODO 临时方案不处理长度大于4000的几何图形，后期以存储过程代替
        if (wkt.length() > 4000)
            return faces;

        String sql = "select  a.*  from ad_face a where a.u_record != 2   and sdo_within_distance(a.geometry,  " +
                "sdo_geom.sdo_mbr(sdo_geometry(:1, 8307)), 'DISTANCE=0') = 'TRUE'";

        if (isLock) {
            sql += " for update nowait";
        }

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(sql);

            pstmt.setString(1, wkt);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                AdFace face = new AdFace();
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

    /**
     * 根据regionId查询ADFACE
     *
     * @param regionId
     * @param isLock
     * @return ADFACE集合
     * @throws Exception
     */
    public List<AdFace> loadAdFaceByRegionId(int regionId, boolean isLock) throws Exception {
        List<AdFace> faces = new ArrayList<AdFace>();

        StringBuilder bf = new StringBuilder();
        bf.append("select * from ad_face where region_id = :1");
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

                AdFace face = new AdFace();
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

    public List<AdFace> loadAdFaceByLinkId(int linkPid, boolean isLock) throws Exception {

        List<AdFace> faces = new ArrayList<AdFace>();

        StringBuilder bf = new StringBuilder();
        bf.append("select b.* from ad_face b where b.face_pid in (select a.face_pid ");
        bf.append("  FROM ad_face a, ad_face_topo t");
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

                AdFace face = new AdFace();
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

    public List<AdFace> loadAdFaceByNodeId(int nodePid, boolean isLock) throws Exception {

        List<AdFace> faces = new ArrayList<AdFace>();
        StringBuilder builder = new StringBuilder();
        builder.append(" SELECT b.* from ad_face b where b.face_pid in (select a.face_pid ");
        builder.append(" FROM ad_face a, ad_face_topo t, ad_link l ");
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

                AdFace face = new AdFace();
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

    private void setChildData(AdFace face, boolean isLock) throws Exception {
        // ad_face_topo
        List<IRow> adFaceTopo = new AbstractSelector(AdFaceTopo.class, conn).loadRowsByParentId(face.getPid(), isLock);

        for (IRow row : adFaceTopo) {
            row.setMesh(face.mesh());
        }

        face.setFaceTopos(adFaceTopo);

        for (IRow row : adFaceTopo) {
            AdFaceTopo obj = (AdFaceTopo) row;

            face.adFaceTopoMap.put(obj.rowId(), obj);
        }
    }

    /**
     * 根据传入几何参数查找与之相关联的ZoneFace面</br>
     * ADMIN_TYPE类型为:</br>
     * 国家地区级（0），省/直辖市/自治区（1），地级市/自治州/省直辖县（2）</br>
     * DUMMY地级市（2.5），地级市市区GCZone（3），地级市市区（未作区界 3.5）</br>
     * 区县/自治县（4），DUMMY区县（4.5），DUMMY区县（地级市下无区县 4.8）</br>
     * 区中心部（5），乡镇/街道（6）,飞地（7）
     *
     * @param geometry
     * @return
     */
    public List<AdFace> loadRelateFaceByGeometry(Geometry geometry) {
        List<AdFace> faces = new ArrayList<AdFace>();
        String sql = "select t1.geometry, t2.region_id from ad_face t1, ad_admin t2 where t1.u_record <> 2 and t2.u_record <> 2 and t1.region_id = t2.region_id and (t2.admin_type = 0 or t2.admin_type = 1 or t2.admin_type = 2 or t2.admin_type = 2.5 or t2.admin_type = 3 or t2.admin_type = 3.5 or t2.admin_type = 4 or t2.admin_type = 4.5 or t2.admin_type = 4.8 or t2.admin_type = 5 or t2.admin_type = 6 or t2.admin_type = 7) and sdo_relate(t1.geometry, sdo_geometry(?, 8307), 'mask=anyinteract') = 'TRUE' ";
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sql);

            Clob clob = ConnectionUtil.createClob(conn);
            clob.setString(1, GeoTranslator.jts2Wkt(geometry));
            pstmt.setClob(1, clob);
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                AdFace face = new AdFace();
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
     * 计算与指定几何相交的行政区划面
     * @param wkt 几何
     * @param isLock 是否加锁
     * @return 相交面
     */
    public List<AdFace> listAdface(String wkt, List<Integer> excludes, boolean isLock) throws ServiceException {
        List<AdFace> list = new ArrayList<>();
        String sql = "SELECT T.FACE_PID, T.GEOMETRY FROM AD_FACE T WHERE SDO_WITHIN_DISTANCE(T.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0'"
                + ") = 'TRUE' AND T.U_RECORD <> 2 AND T.FACE_PID NOT IN (:2)";
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = getConn().prepareStatement(sql);
            pstmt.setString(1, wkt);
            pstmt.setString(2, StringUtils.getInteStr(excludes));
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                AdFace face = new AdFace();
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
