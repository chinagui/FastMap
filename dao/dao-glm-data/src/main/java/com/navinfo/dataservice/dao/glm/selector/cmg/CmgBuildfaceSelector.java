
package com.navinfo.dataservice.dao.glm.selector.cmg;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildfaceTenant;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildfaceTopo;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.exception.DAOException;
import com.navinfo.navicommons.exception.ServiceException;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Title: CmgBuildfaceSelector
 * @Package: com.navinfo.dataservice.dao.glm.selector.cmg
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/4/11
 * @Version: V1.0
 */
public class CmgBuildfaceSelector extends AbstractSelector {

    /**
     * 日志记录类
     */
    private Logger logger = Logger.getLogger(CmgBuildfaceSelector.class);

    public CmgBuildfaceSelector(Connection conn) {
        super(CmgBuildface.class, conn);
    }

    /**
     * 根据线查找关联面以及子表信息
     * @param linkPid 线PID
     * @param isLock 是否加锁
     * @return 关联面信息， 无关联面时返回 EMPTY LIST
     * @throws Exception 查询关联面时出错
     */
    public List<CmgBuildface> listTheAssociatedFaceOfTheLink(int linkPid, boolean isLock) throws Exception {
        List<CmgBuildface> result = new ArrayList<>();

        String sql = "select t1.* from cmg_buildface t1, cmg_buildface_topo t2 where t1.face_pid = t2.face_pid and "
                + "t2.link_pid = :1 and t1.u_record <> 2 and t2.u_record <> 2";
        if (isLock) {
            sql += " for update nowait";
        }

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = getConn().prepareStatement(sql);
            pstmt.setInt(1, linkPid);
            resultSet = pstmt.executeQuery();
            generateCmgBuildface(result, resultSet);
        } catch (SQLException e) {
            logger.error("method listTheAssociatedFaceOfTheLink error. [ sql : " + sql + " ] ");
            throw new DAOException(e.getMessage());
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return result;
    }

    /**
     * 根据点查找关联面以及子表信息
     * @param nodepid CMG-NODE主键
     * @param isLock 是否加锁
     * @return 关联面信息， 无关联面时返回 EMPTY LIST
     * @throws Exception 查询关联面时出错
     */
    public List<CmgBuildface> listTheAssociatedFaceOfTheNode(int nodepid, boolean isLock) throws Exception {
        List<CmgBuildface> result = new ArrayList<>();

        String sql = "with tmp as (select distinct cmt.face_pid from cmg_buildnode cmn, cmg_buildlink cml, cmg_buildface_topo cmt where "
                + "cmn.node_pid = :1 and (cml.s_node_pid = cmn.node_pid or cml.e_node_pid = cmn.node_pid) and cml.link_pid = cmt.link_pid)"
                + " select t1.* from cmg_buildface t1, tmp t2 where t1.face_pid = t2.face_pid";
        if (isLock) {
            sql += " for update nowait";
        }

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = getConn().prepareStatement(sql);
            pstmt.setInt(1, nodepid);
            resultSet = pstmt.executeQuery();
            generateCmgBuildface(result, resultSet);
        } catch (SQLException e) {
            logger.error("method listTheAssociatedFaceOfTheNode error. [ sql : " + sql + " ] ");
            throw new DAOException(e.getMessage());
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return result;
    }
    
    /**
     * 根据根据建筑物pid查cmgface
     * @param buildingPid 建筑物PID
     * @param isLock 是否加锁
     * @return 关联面信息， 无关联面时返回 EMPTY LIST
     * @throws Exception 查询关联面时出错
     */
    public List<CmgBuildface> loadFaceByBuildingPid(int buildingPid, boolean isLock) throws Exception {
        List<CmgBuildface> result = new ArrayList<>();

        String sql = "SELECT * FROM CMG_BUILDFACE T WHERE T.BUILDING_PID = :1 AND T.U_RECORD <> 2";
        
        if (isLock) {
            sql += " for update nowait";
        }

        PreparedStatement pstmt = null;
        
        ResultSet resultSet = null;
        
        try {
            pstmt = getConn().prepareStatement(sql);
            
            pstmt.setInt(1, buildingPid);
            
            resultSet = pstmt.executeQuery();
            
            generateCmgBuildface(result, resultSet);
            
        } catch (Exception e) {
            logger.error("method loadFaceByBuildingPid error. [ sql : " + sql + " ] ");
            throw new DAOException(e.getMessage());
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return result;
    }

    /**
     * 根据结果组装CMG-FACE对象
     * @param result 结果集
     * @param resultSet 查询结果
     * @throws Exception 组装CMG-FACE出错
     */
    private void generateCmgBuildface(List<CmgBuildface> result, ResultSet resultSet) throws Exception {
        while (resultSet.next()) {
            CmgBuildface cmgBuildface = new CmgBuildface();
            ReflectionAttrUtils.executeResultSet(cmgBuildface, resultSet);

            List<IRow> topos = new AbstractSelector(CmgBuildfaceTopo.class, getConn()).loadRowsByParentId(cmgBuildface.pid(), false);
            cmgBuildface.setTopos(topos);
            List<IRow> tenants = new AbstractSelector(CmgBuildfaceTenant.class, getConn()).loadRowsByParentId(cmgBuildface.pid(), false);
            cmgBuildface.setTenants(tenants);

            result.add(cmgBuildface);
        }
    }

    /**
     * 计算与指定几何相交的市街图面
     * @param wkt 几何
     * @param isLock 是否加锁
     * @return 相交面
     */
    public List<CmgBuildface> listCmgBuildface(String wkt, List<Integer> excludes, boolean isLock) throws ServiceException {
        List<CmgBuildface> list = new ArrayList<>();
        String sql = "SELECT T.FACE_PID, T.GEOMETRY FROM CMG_BUILDFACE T WHERE SDO_WITHIN_DISTANCE(T.GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = "
                + "'TRUE' AND T.U_RECORD <> 2 AND T.FACE_PID NOT IN(:2)";
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = getConn().prepareStatement(sql);
            pstmt.setString(1, wkt);
            pstmt.setString(2, StringUtils.getInteStr(excludes));
            resultSet = pstmt.executeQuery();
            while (resultSet.next()) {
                CmgBuildface face = new CmgBuildface();
                ReflectionAttrUtils.executeResultSet(face, resultSet);
                list.add(face);
            }
        } catch (SQLException e){
            logger.error("计算与指定几何相交的市街图面数量出错", e);
            throw new DAOException(e.getMessage());
        } catch (Exception e) {
            logger.error("组转数据出错", e);
            throw new ServiceException(e.getMessage());
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
        return list;
    }
}