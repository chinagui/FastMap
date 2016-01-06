package com.navinfo.dataservice.FosEngine.edit.operation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class Helper {

	/**
	 * 计算退出线 如果node在路口上，计算挂接路口的退出线，否则计算挂接node的退出线
	 * 
	 * @param inLinkPid
	 * @param nodePid
	 * @param restricInfos
	 */
	public static List<Integer> calOutLinks(Connection conn, int inLinkPid,
			int nodePid) throws Exception {

		List<Integer> outLinkPids = new ArrayList<Integer>();

		String sql = "with c1 as  (select * from rd_cross_node where node_pid = :1), c2 as  (select *     from rd_link b    where exists (select null from c1 c where c.node_pid in (b.s_node_pid))      and link_pid not in          (select d.link_pid             from rd_cross_link d            where d.pid in (select pid from c1))   union all   select *     from rd_link b    where exists (select null from c1 c where c.node_pid in (b.e_node_pid))      and link_pid not in          (select d.link_pid             from rd_cross_link d            where d.pid in (select pid from c1))) select link_pid   from c2  where exists  (select null           from c1          where (c2.direct = 1)             or (c2.direct = 2 and c2.s_node_pid = c1.node_pid)             or (c2.direct = 3 and c2.e_node_pid = c1.node_pid))              union all select link_pid from rd_link a where ((s_node_pid=:2 and direct in (1,2)) or (e_node_pid=:3 and direct in (1,3) ))  and not exists (select null from rd_cross_node b where b.node_pid=:4);";
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, nodePid);

			pstmt.setInt(2, nodePid);

			pstmt.setInt(3, nodePid);

			pstmt.setInt(4, nodePid);

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				int outLinkPid = resultSet.getInt("link_pid");

				if (outLinkPid == inLinkPid) {
					continue;
				}

				outLinkPids.add(outLinkPid);
			}

			return outLinkPids;
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
	}

	/**
	 * 计算经过线
	 * 
	 * @param inLinkPid
	 * @param nodePid
	 * @param outLinkPids
	 * @throws Exception
	 */
	public static void calViaLinks(Connection conn, int inLinkPid, int nodePid,
			List<Integer> outLinkPids, LineSegment inLinkSegment,
			Map<Integer, LineSegment> outLinkSegmentMap,
			Map<Integer, List<Integer>> viaLinkPidMap,
			Map<Integer, Integer> relationTypeMap) throws Exception {
		
		outLinkSegmentMap = new HashMap<Integer, LineSegment>();
		
		viaLinkPidMap = new HashMap<Integer, List<Integer>>();
		
		relationTypeMap = new HashMap<Integer, Integer>();

		String sql = "select * from table(package_utils.get_restrict_points(:1,:2,:3))";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, inLinkPid);

			pstmt.setInt(2, nodePid);

			StringBuilder sb = new StringBuilder();

			for (int pid : outLinkPids) {

				sb.append(pid);

				sb.append(",");
			}

			sb.deleteCharAt(sb.length() - 1);

			pstmt.setString(3, sb.toString());

			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {

				if (inLinkSegment == null) {
					String inNode1 = resultSet.getString("in_node1");

					String inNode2 = resultSet.getString("in_node2");

					String[] splits = inNode1.split(",");

					Coordinate p1 = new Coordinate(Double.valueOf(splits[0]),
							Double.valueOf(splits[1]));

					splits = inNode2.split(",");

					Coordinate p2 = new Coordinate(Double.valueOf(splits[0]),
							Double.valueOf(splits[1]));

					inLinkSegment = new LineSegment(p1, p2);
				}

				int outLinkPid = resultSet.getInt("link_pid");

				int relationType = resultSet.getInt("relation_type");

				relationTypeMap.put(outLinkPid, relationType);

				String outNode1 = resultSet.getString("out_node1");

				String outNode2 = resultSet.getString("out_node2");

				String[] splits = outNode1.split(",");

				Coordinate p1 = new Coordinate(Double.valueOf(splits[0]),
						Double.valueOf(splits[1]));

				splits = outNode2.split(",");

				Coordinate p2 = new Coordinate(Double.valueOf(splits[0]),
						Double.valueOf(splits[1]));

				LineSegment line = new LineSegment(p1, p2);

				outLinkSegmentMap.put(outLinkPid, line);

				String viaPath = resultSet.getString("via_path");

				List<Integer> viaLinks = new ArrayList<Integer>();

				if (viaPath != null) {

					splits = viaPath.split(",");

					for (String s : splits) {
						if (!s.equals("")) {
							viaLinks.add(Integer.valueOf(s));
						}
					}

				}

				viaLinkPidMap.put(outLinkPid, viaLinks);
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
