package com.navinfo.dataservice.dao.glm.selector.rd.slope;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlopeVia;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;

/***
 *
 * @author zhaokk
 *
 */
public class RdSlopeSelector extends AbstractSelector {

    private Connection conn;

    public RdSlopeSelector(Connection conn) {
        super(conn);
        this.conn = conn;
        this.setCls(RdSlope.class);
    }

    /***
     *
     * 通过退出线查找坡度信息
     *
     * @param linkPid
     * @param isLock
     * @return
     * @throws Exception
     */
    public List<RdSlope> loadByOutLink(int linkPid, boolean isLock) throws Exception {

        List<RdSlope> rows = new ArrayList<RdSlope>();

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            String sql = "SELECT pid, row_id FROM rd_slope WHERE link_pid =:1 and u_record !=2";

            if (isLock) {
                sql += " for update nowait";
            }

            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, linkPid);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                AbstractSelector abSelector = new AbstractSelector(RdSlope.class, conn);
                RdSlope slope = (RdSlope) abSelector.loadById(resultSet.getInt("pid"), false);
                rows.add(slope);
            }

            return rows;
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
    }

    /***
     *
     * 通过接续线查找坡度信息
     *
     * @param linkPid
     * @param isLock
     * @return
     * @throws Exception
     */
    public List<RdSlope> loadByViaLink(int linkPid, boolean isLock) throws Exception {

        List<RdSlope> rows = new ArrayList<RdSlope>();

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            String sql = "SELECT pid, row_id FROM RD_SLOPE WHERE U_RECORD != 2 AND PID IN (SELECT DISTINCT " + "" +
                    "(SLOPE_PID) FROM RD_SLOPE_VIA WHERE U_RECORD != 2 AND LINK_PID = :1)";

            if (isLock) {
                sql += " for update nowait";
            }

            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, linkPid);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                AbstractSelector abSelector = new AbstractSelector(RdSlope.class, conn);
                RdSlope slope = (RdSlope) abSelector.loadById(resultSet.getInt("pid"), false);
                rows.add(slope);
            }

            return rows;
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
    }

    /***
     *
     * 通过退出线查找坡度信息
     *
     * @param linkPid
     * @param isLock
     * @return
     * @throws Exception
     */
    public List<RdSlopeVia> loadBySeriesLink(int linkPid, boolean isLock) throws Exception {

        List<RdSlopeVia> rows = new ArrayList<RdSlopeVia>();

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            String sql = "SELECT link_pid,slope_pid,seq_num,row_id FROM rd_slope_via WHERE link_pid =:1 and u_record " +
                    "" + "!=2";

            if (isLock) {
                sql += " for update nowait";
            }

            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, linkPid);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {
                RdSlopeVia slopeVia = new RdSlopeVia();
                ReflectionAttrUtils.executeResultSet(slopeVia, resultSet);
                rows.add(slopeVia);
            }

            return rows;
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }

    }

    /***
     *
     * 通过接续link关联的接续link
     *
     * @param slopePid
     * @param isLock
     * @return
     * @throws Exception
     */
    public RdLink loadBySeriesRelationLink(int slopePid, int seqNum, boolean isLock) throws Exception {

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;
        RdLink link = null;
        try {
            String sql = "SELECT rs.link_pid,rl.s_node_pid,rl.e_node_pid FROM rd_slope_via rs ,rd_link rl WHERE rs" +
                    ".link_pid = rl.link_pid and  rs.slope_pid =:1 and rs.seq_num = :2 and rs.u_record !=2 and rl" +
                    ".u_record !=2";

            if (isLock) {
                sql += " for update nowait";
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, slopePid);
            pstmt.setInt(2, seqNum);
            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                link = new RdLink();
                ReflectionAttrUtils.executeResultSet(link, resultSet);
            }

            return link;
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }

    }


    /***
     *
     * 通过接续link关联的接续link
     *
     * @param slopePid
     * @param isLock
     * @return
     * @throws Exception
     */
    public RdLink loadByOutLinkBySlopePid(int slopePid, boolean isLock) throws Exception {

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;
        RdLink link = null;
        try {
            String sql = "SELECT rs.link_pid,rl.s_node_pid,rl.e_node_pid FROM rd_slope rs ,rd_link rl WHERE rs" + "" +
                    ".link_pid = rl.link_pid and  rs.pid =:1 and rs.u_record !=2 and rl.u_record !=2";

            if (isLock) {
                sql += " for update nowait";
            }

            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, slopePid);
            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {
                link = new RdLink();
                ReflectionAttrUtils.executeResultSet(link, resultSet);
            }

            return link;
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }

    }

    /***
     *
     * 通过进入点查找坡度信息
     *
     * @param nodePids
     * @param isLock
     * @return
     * @throws Exception
     */
    public List<RdSlope> loadByNodePids(Collection<Integer> nodePids, boolean isLock) throws Exception {
        List<RdSlope> rows = new ArrayList<RdSlope>();
        if (nodePids.isEmpty())
            return rows;

        StringBuffer sb = new StringBuffer();
        Iterator<Integer> it = nodePids.iterator();
        while (it.hasNext()) {
            sb.append(it.next()).append(",");
        }
        String inter = "''";
        if (sb.length() > 0)
            inter = sb.substring(0, sb.length() - 1);

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            String sql = "SELECT pid,row_id FROM rd_slope WHERE node_pid in (" + inter + ") and u_record !=2";
            if (isLock) {
                sql += " for update nowait";
            }
            pstmt = conn.prepareStatement(sql);
            resultSet = pstmt.executeQuery();
            AbstractSelector abSelector = new AbstractSelector(RdSlope.class, conn);
            while (resultSet.next()) {
                RdSlope slope = (RdSlope) abSelector.loadById(resultSet.getInt("pid"), false);
                rows.add(slope);
            }
            return rows;
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }
    }

    
    /***
    *
    * 通过道路点查找坡度信息
    *
    * @param linkPid
    * @param isLock
    * @return
    * @throws Exception
    */
   public List<RdSlope> loadByNode(int nodePid, boolean isLock)
           throws Exception {

       List<RdSlope> rows = new ArrayList<RdSlope>();

       PreparedStatement pstmt = null;

       ResultSet resultSet = null;

       try {
           String sql = "SELECT pid, row_id FROM rd_slope WHERE node_pid =:1 and u_record !=2";

           if (isLock) {
               sql += " for update nowait";
           }

           pstmt = conn.prepareStatement(sql);

           pstmt.setInt(1, nodePid);

           resultSet = pstmt.executeQuery();

           while (resultSet.next()) {
               AbstractSelector abSelector = new AbstractSelector(
                       RdSlope.class, conn);
               RdSlope slope = (RdSlope) abSelector.loadById(
                       resultSet.getInt("pid"), false);
               rows.add(slope);
           }

           return rows;
       } catch (Exception e) {
           throw e;
       } finally {
           DbUtils.closeQuietly(resultSet);
           DbUtils.closeQuietly(pstmt);
       }
   }

}
