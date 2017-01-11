package com.navinfo.dataservice.engine.edit.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.geom.AngleCalculator;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.vividsolutions.jts.geom.LineSegment;

public class CalLinkOperateUtils {
	private Connection conn;
	
	public CalLinkOperateUtils()
	{
	}
	
	public CalLinkOperateUtils(Connection conn)
	{
		this.conn = conn;
	}
	
    /**
     * 计算关系类型
     *
     * @param conn
     * @param nodePid    进入点
     * @param outLinkPid 退出线
     * @return
     * @throws Exception
     */
    public int getRelationShipType(int nodePid, int outLinkPid)
            throws Exception {
    	
        String sql = "with c1 as (select node_pid from rd_cross_node a where exists (select null from rd_cross_node b where a.pid=b.pid and b.node_pid=:1 and B.U_RECORD !=2))  select count(1) count from rd_link c where c.link_pid=:2 and (c.s_node_pid=:3 or c.e_node_pid=:4 or exists(select null from c1 where c.s_node_pid=c1.node_pid or c.e_node_pid=c1.node_pid))and C.U_RECORD !=2";

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
    
    /**
     * 计算进入点联通的线(排除进入线和已经选择该link作为退出线的)
     * @param conn
     * @param nodePid 进入点
     * @param inLinkPid 进入线
     * @param linkPidList 需要排除在外的退出线pid
     * @return
     */
	public List<Integer> getInNodeLinkPids(int nodePid,int inLinkPid,List<Integer> linkPidList) {
		RdLinkSelector selector = new RdLinkSelector(conn);

		List<Integer> linkPids = new ArrayList<>();

		try {
			linkPids = selector.loadLinkPidByNodePid(nodePid, true);

			if (CollectionUtils.isNotEmpty(linkPids)) {
				// 剔除进入线，防止进入线和退出线是一条线
				if (linkPids.contains(inLinkPid)) {
					linkPids.remove(linkPids.indexOf(inLinkPid));
				}

				// 删除已经作为指定方向的退出线
				linkPids.removeAll(linkPidList);
			}
		} catch (Exception e) {
		}
		return linkPids;
	}
	
	/**
	 * 计算箭头的限制信息
	 * 
	 * @param arrow
	 * @param infoMap
	 */
	public static int calIntInfo(String arrow) {
		if (arrow.contains("[")) {
			// 理论值带[]
			return Integer.parseInt(arrow.substring(1, 2));
		} else {
			// 实际值不带
			return Integer.parseInt(arrow);
		}
	}
	
	/**
	 * 获取最小夹角的退出线
	 *
	 * @param outLinkPids
	 *            退出线
	 * @param infoList
	 *            交限信息
	 */
	public static int getMinAngleOutLinkPidOnArrowDir(List<Integer> outLinkPids, int arrow,Map<Integer, LineSegment> outLinkSegmentMap,LineSegment inLinkSegment) {
		// 最小夹角对应的退出线
		int minAngleOutLinkPid = 0;

		// 最小夹角
		double temAngle = 361;

		List<Integer> resultOutLinkPids = new ArrayList<>();

		resultOutLinkPids.addAll(outLinkPids);

		for (Integer outPid : resultOutLinkPids) {
			LineSegment outLinkSegment = outLinkSegmentMap.get(outPid);

			if (outLinkSegment != null) {
				// 获取线的夹角
				double angle = AngleCalculator.getAngle(inLinkSegment, outLinkSegment);
				// 计算交限信息
				int restricInfo = calRestricInfo(angle);

				if (arrow == restricInfo) {
					// link计算的夹角比上个link的夹角小的替换最小夹角和对应的linkPid
					if (angle < temAngle) {

						temAngle = angle;

						minAngleOutLinkPid = outPid;
					}
				}
			}

		}

		return minAngleOutLinkPid;
	}
	
	/**
	 * 计算限制信息
	 *
	 * @param angle
	 * @return
	 */
	public static int calRestricInfo(double angle) {
		if (angle > 45 && angle <= 135) {
			return 3;
		} else if (angle > 135 && angle <= 225) {
			return 4;
		} else if (angle > 225 && angle <= 315) {
			return 2;
		} else {
			return 1;
		}

	}
}
