package com.navinfo.dataservice.engine.edit.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class CalLinkOperateUtils {

    /**
     * 计算关系类型
     *
     * @param conn
     * @param nodePid    进入点
     * @param outLinkPid 退出线
     * @return
     * @throws Exception
     */
    public int getRelationShipType(Connection conn, int nodePid, int outLinkPid)
            throws Exception {
    	
        String sql = "with c1 as (select node_pid from rd_cross_node a where exists (select null from rd_cross_node b where a.pid=b.pid and b.node_pid=:1))  select count(1) count from rd_link c where c.link_pid=:2 and (c.s_node_pid=:3 or c.e_node_pid=:4 or exists(select null from c1 where c.s_node_pid=c1.node_pid or c.e_node_pid=c1.node_pid))";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, nodePid);

            pstmt.setInt(2, outLinkPid);

            pstmt.setInt(3, nodePid);

            pstmt.setInt(4, nodePid);

            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {

                int count = resultSet.getInt("count");

                if (count > 0) {
                    return 1;
                } else {
                    return 2;
                }
            }

        } catch (Exception e) {

            throw e;
        } finally {
            try {
                resultSet.close();
            } catch (Exception e) {

            }

            try {
                pstmt.close();
            } catch (Exception e) {

            }
        }

        return 1;
    }

    /**
     * 计算经过线
     *
     * @param conn
     * @param inLinkPid  进入线
     * @param nodePid    进入点
     * @param outLinkPid 退出线
     * @return
     * @throws Exception
     */
    public List<Integer> calViaLinks(Connection conn, int inLinkPid,
                                     int nodePid, int outLinkPid) throws Exception {

        String sql = "select * from table(package_utils.get_restrict_points(:1,:2,:3))";

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, inLinkPid);

            pstmt.setInt(2, nodePid);

            pstmt.setString(3, String.valueOf(outLinkPid));

            resultSet = pstmt.executeQuery();

            if (resultSet.next()) {

                String viaPath = resultSet.getString("via_path");

                List<Integer> viaLinks = new ArrayList<Integer>();

                if (viaPath != null) {

                    String[] splits = viaPath.split(",");

                    for (String s : splits) {
                        if (!s.equals("")) {

                            int viaPid = Integer.valueOf(s);

                            if (viaPid == inLinkPid || viaPid == outLinkPid) {
                                continue;
                            }

                            viaLinks.add(viaPid);
                        }
                    }

                }

                return viaLinks;
            }

        } catch (Exception e) {
            throw e;

        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (Exception e) {
            }

        }

        return null;
    }

    /**
     * 将一组link按顺序挂接
     *
     * @param rdlinks
     * @return
     */
    public List<RdLink> sortLink(List<RdLink> rdlinks) {

        List<RdLink> sortLinks = new ArrayList<RdLink>();

        if (rdlinks == null || rdlinks.size() == 0) {

            return sortLinks;
        }

        if (rdlinks.size() < 3) {

            return rdlinks;
        }

        List<RdLink> cacheLinks = new ArrayList<RdLink>();

        cacheLinks.addAll(rdlinks);

        int targetNodePid = cacheLinks.get(0).getsNodePid();

        getConnectLink(targetNodePid, cacheLinks, sortLinks, 1);

        getConnectLink(targetNodePid, cacheLinks, sortLinks, 0);

        return sortLinks;
    }

    /**
     * 获取挂接link
     *
     * @param targetNodePid 连接点
     * @param cacheLinks    link池
     * @param sortLinks     有序link
     * @param type          挂接类型 1：顺向、2：逆向
     */
    private void getConnectLink(int targetNodePid, List<RdLink> cacheLinks,
                                List<RdLink> sortLinks, int type) {

        RdLink connectLink = null;

        for (RdLink link : cacheLinks) {

            if (targetNodePid != link.getsNodePid()
                    && targetNodePid != link.geteNodePid()) {
                continue;
            }
            if (sortLinks.contains(link)) {
                continue;
            }

            targetNodePid = (targetNodePid == link.getsNodePid()) ? link
                    .geteNodePid() : link.getsNodePid();

            if (type == 1) {

                sortLinks.add(link);

            } else {

                sortLinks.add(0, link);
            }

            connectLink = link;

            break;
        }

        if (connectLink != null) {

            cacheLinks.remove(connectLink);

            getConnectLink(targetNodePid, cacheLinks, sortLinks, type);
        }
    }

    /**
     * 计算link的经过点
     *
     * @param links 目标link
     * @return 经过点Pid
     */
    public static List<Integer> calNodePids(List<RdLink> links) {
        List<Integer> nodePids = new ArrayList<>();
        if (null == links || links.isEmpty())
            return nodePids;
        Map<Integer, Integer> map = new HashMap<>();
        for (RdLink link : links) {
            Integer sNum = map.get(link.getsNodePid());
            Integer eNUm = map.get(link.geteNodePid());
            if (null == sNum) {
                map.put(link.getsNodePid(), 1);
            } else {
                map.put(link.getsNodePid(), sNum + 1);
            }
            if (null == eNUm) {
                map.put(link.geteNodePid(), 1);
            } else {
                map.put(link.geteNodePid(), eNUm + 1);
            }
        }
        for (Integer nodePid : map.keySet()) {
            Integer num = map.get(nodePid);
            if (num > 1) {
                nodePids.add(nodePid);
            }
        }
        return nodePids;
    }
}
