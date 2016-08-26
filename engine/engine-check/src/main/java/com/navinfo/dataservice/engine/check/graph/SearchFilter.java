package com.navinfo.dataservice.engine.check.graph;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;

public class SearchFilter {
	private SearchFilter nextFilter=null;
	private Connection conn;
	
	public boolean isValidLink(RdLink link) {
		if (nextFilter != null)
			return nextFilter.isValidLink(link);
		return true;
	}

	public boolean isValidPath(LinkedListRdLinkAndPid preLinks,
			RdLink nextLink) {
		if (nextFilter != null)
			return nextFilter.isValidPath(preLinks, nextLink);
		return true;
	}

	public HashSetRdLinkAndPid OutLinkFilter(
			LinkedListRdLinkAndPid preLinks,
			HashSetRdLinkAndPid outLinks) {
		if (nextFilter != null)
			return nextFilter.OutLinkFilter(preLinks, outLinks);
		return outLinks;
	}

	public boolean isValidChain(LinkedListRdLinkAndPid roadChain) {
		if(roadChain==null ||roadChain.size()<=1) return false;
		if (nextFilter != null)
			return nextFilter.isValidChain(roadChain);
		return true;
	}

	public SearchFilter(Connection conn) {
		this.conn=conn;
	}

	public SearchFilter getNextFilter() {
		return nextFilter;
	}


	public void setNextFilter(SearchFilter nextFilter) {
		this.nextFilter = nextFilter;
	}
	
	//根据elementSet这个返回符合条件的接续link集
	public HashSetRdLinkAndPid getAjointSet(HashSetRdLinkAndPid elementSet) throws Exception{
		return null;
	}
	
	//根据graySet，获取其中符合条件的结果集添加到resultSet中
	public HashSetRdLinkAndPid outSetFilter(HashSetRdLinkAndPid resultSet,HashSetRdLinkAndPid graySet){
		if (nextFilter != null)
			return nextFilter.outSetFilter(resultSet, graySet);
		return graySet;
	}
	
	public HashSetRdLinkAndPid getRdLinksByNodePid(int nodePid) throws Exception{
		RdLinkSelector rdLinkSelector=new RdLinkSelector(this.conn);
		List<RdLink> links=rdLinkSelector.loadByNodePidOnlyRdLink(nodePid, false);
		HashSetRdLinkAndPid resultSet=new HashSetRdLinkAndPid();
		for(int i=0;i<links.size();i++){
			resultSet.add(links.get(i));
		}
		return resultSet;		
	}
	
	public HashSetRdLinkAndPid getRdLinksBySql(String sql) throws Exception{
		RdLinkSelector rdLinkSelector=new RdLinkSelector(this.conn);
		List<RdLink> links=rdLinkSelector.loadBySql(sql, false);
		HashSetRdLinkAndPid resultSet=new HashSetRdLinkAndPid();
		for(int i=0;i<links.size();i++){
			resultSet.add(links.get(i));
		}
		return resultSet;		
	}

	/**
	 * 获取link的退出link
	 * 
	 * @param 道路link对象
	 * @return link的所有退出link,如果传入参数为空返回null
	 * @throws Exception 
	 */
	public HashSetRdLinkAndPid getLinkOfExitLinks(RdLink link) throws Exception {
		if (link == null)
			return null;
		
		//获取link退出方向上的link集合
		int sNodePid=link.getsNodePid();
		int eNodePid=link.geteNodePid();
		
		int direct = link.getDirect();
		if (direct == 0 || direct == 1) {
			HashSetRdLinkAndPid outLinks = null;
			HashSetRdLinkAndPid sNodeExitLinks = this.getNodeOfExitLinks(sNodePid);
			HashSetRdLinkAndPid eNodeExitLinks = this.getNodeOfExitLinks(eNodePid);
			if (sNodeExitLinks != null) {
				outLinks = sNodeExitLinks;
				if (eNodeExitLinks != null) {
					outLinks.addAll(eNodeExitLinks);
				}
			} else if (eNodeExitLinks != null) {
				outLinks = eNodeExitLinks;
			}
			if (outLinks != null) {
				outLinks.remove(link);
			}
			return outLinks;
		} else if (direct == 2) {
			return getNodeOfExitLinks(eNodePid);
		} else if (direct == 3) {
			return getNodeOfExitLinks(sNodePid);
		} else {
			return null;
		}
	}
	
	/**
	 * 获取“进入线--点--退出线”结构中的退出link
	 * 
	 * @param 道路link对象,道路node点对象,
	 * @return 所有退出link,如果传入参数为空返回null
	 * @throws Exception 
	 */
	public HashSetRdLinkAndPid getLinkOfExitLinks(RdLink inLink, int nodePid) throws Exception {
		if (inLink == null || nodePid == 0)
			return null;
		
		HashSetRdLinkAndPid outlinks = this.getNodeOfExitLinks(nodePid);
		int direct = inLink.getDirect();
		if (direct == 1 || direct == 0) {
			outlinks.remove(inLink);
		}
		
		return outlinks;
	}
	
	/**
	 * 获取node点的退出link
	 * 
	 * @param node点对象
	 * @return 退出该node点的所有link对象,如果传入node点为空则返回null
	 * @throws Exception 
	 */
	public HashSetRdLinkAndPid getNodeOfExitLinks(int nodePid) throws Exception {
		if (nodePid == 0)
			return null;
		
		String sql="SELECT *"
				+ "  FROM RD_LINK L"
				+ " WHERE L.DIRECT IN (0, 1)"
				+ "   AND (L.S_NODE_PID="+nodePid+" OR"
				+ "       L.E_NODE_PID ="+nodePid+")"
				+ " UNION ALL"
				+ " SELECT *"
				+ "  FROM RD_LINK L"
				+ " WHERE L.DIRECT = 3"
				+ "   AND L.E_NODE_PID ="+nodePid
				+ " UNION ALL"
				+ " SELECT *"
				+ "  FROM RD_LINK L"
				+ " WHERE L.DIRECT = 2"
				+ "   AND L.S_NODE_PID ="+nodePid;
		
		RdLinkSelector rdLinkSelector=new RdLinkSelector(conn);
		List<RdLink> list=rdLinkSelector.loadBySql(sql, false);
		HashSetRdLinkAndPid hashSetObj=new HashSetRdLinkAndPid();
		hashSetObj.addAll(list);
		return hashSetObj;
	}

}
