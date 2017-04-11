package com.navinfo.dataservice.dao.glm.selector.cmg;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildfaceTenant;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildfaceTopo;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    public List<CmgBuildface> findTheAssociatedFaceOfTheLine(int linkPid, boolean isLock) throws Exception {
        List<CmgBuildface> result = new ArrayList<>();

        String sql = "select t1.face_pid, t2.link_pid from lc_face t1, lc_face_topo t2 where t1.face_pid = t2.face_pid and "
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
            while (resultSet.next()) {
                CmgBuildface cmgBuildface = new CmgBuildface();
                ReflectionAttrUtils.executeResultSet(cmgBuildface, resultSet);

                List<IRow> topos = new AbstractSelector(CmgBuildfaceTopo.class, getConn()).loadRowsByParentId(cmgBuildface.pid(), false);
                cmgBuildface.setTopos(topos);
                List<IRow> tenants = new AbstractSelector(CmgBuildfaceTenant.class, getConn()).loadRowsByParentId(cmgBuildface.pid(), false);
                cmgBuildface.setTenants(tenants);
            }
        } catch (Exception e) {
            logger.error("method findTheAssociatedFaceOfTheLine error. [ sql : " + sql + " ] ");
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
        return result;
    }
}
