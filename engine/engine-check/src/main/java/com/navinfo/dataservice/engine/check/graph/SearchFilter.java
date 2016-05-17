package com.navinfo.dataservice.engine.check.graph;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;

public class SearchFilter {
	private SearchFilter nextFilter=null;
	private Connection conn;

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
		List<RdLink> links=rdLinkSelector.loadByNodePid(nodePid, false);
		HashSetRdLinkAndPid resultSet=new HashSetRdLinkAndPid();
		for(int i=0;i<links.size();i++){
			resultSet.add(links.get(i));
		}
		return resultSet;		
	}

}
