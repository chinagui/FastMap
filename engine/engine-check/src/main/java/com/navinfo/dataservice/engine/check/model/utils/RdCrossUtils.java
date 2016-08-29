package com.navinfo.dataservice.engine.check.model.utils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.check.graph.CrossChainFilter;
import com.navinfo.dataservice.engine.check.graph.LinkedListRdLinkAndPid;
import com.navinfo.dataservice.engine.check.graph.PathSearcher;

public class RdCrossUtils {

	public RdCrossUtils() {
		// TODO Auto-generated constructor stub
	}
	
	public static ArrayList<LinkedListRdLinkAndPid> getGoThroughChainByLinkNode(Connection conn,RdCross crossObj, RdLink link,int nodePid) throws Exception{
		List<RdLink> jointLinks=getJointLinks(conn,crossObj.getPid());
		ArrayList<LinkedListRdLinkAndPid> chainList=new ArrayList<LinkedListRdLinkAndPid>();
		PathSearcher search=new PathSearcher();
		CrossChainFilter crossFilter=new CrossChainFilter(conn,jointLinks);
		search.registerFilter(crossFilter);
		search.deepthFirstSearch(link, nodePid, chainList);	
		return chainList;
	}
	
	/**
	 * 返回路口的挂接link
	 * 
	 * @return List<RdLink>
	 * @throws Exception 
	 */
	public static List<RdLink> getJointLinks(Connection conn,int crossPid) throws Exception
	{
		String sql="SELECT L.*"
				+ "  FROM RD_LINK L, RD_CROSS_NODE N"
				+ " WHERE (N.NODE_PID = L.S_NODE_PID OR N.NODE_PID = L.E_NODE_PID)"
				+ "   AND N.PID = "+crossPid
				+ "   AND NOT EXISTS (SELECT 1"
				+ "          FROM RD_CROSS_LINK CL"
				+ "         WHERE CL.PID = "+crossPid
				+ "           AND CL.LINK_PID = L.LINK_PID)";
		RdLinkSelector selector=new RdLinkSelector(conn);
		return selector.loadBySql(sql, false);
	}
}
