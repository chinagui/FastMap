package com.navinfo.dataservice.engine.check.graph;

import java.util.Iterator;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.graph.SearchFilter;

public class PathSearcher {
	private SearchFilter filter = null;

	public PathSearcher() {
	}

	public PathSearcher(SearchFilter filter) {
		this.filter = filter;
	}

	public void registerFilter(SearchFilter filter) {
		this.filter = filter;
	}

	public void unRegisterFilter() {
		this.filter = null;
	}
	
	/**
	 * 深度优先搜索
	 * @param graySet 灰色集合
	 * @param ResultSet 结果集合，即所有的黑色集合
	 * @throws Exception 
	 */
	public void breadthFirstSearch(HashSetRdLinkAndPid graySet,HashSetRdLinkAndPid resultSet) throws Exception{
		if (graySet==null||graySet.size()==0|| filter == null) return;
		resultSet.addAll(graySet);
		HashSetRdLinkAndPid graySetTmp=filter.getAjointSet(graySet);
		if (graySetTmp==null) return;
		HashSetRdLinkAndPid grayTmp1=new HashSetRdLinkAndPid();
		Iterator<RdLink> grayTmpIter=graySetTmp.iterator();
		while(grayTmpIter.hasNext()){
			RdLink grayTmp=grayTmpIter.next();
			if(!resultSet.contains(grayTmp.getPid()))
			{
				grayTmp1.add(grayTmp);
			}
		}
		if (grayTmp1==null||grayTmp1.size()==0) return;
		HashSetRdLinkAndPid grayTmp2=new HashSetRdLinkAndPid();
		if(filter!=null)
			grayTmp2=filter.outSetFilter(resultSet,grayTmp1);
		if (grayTmp2==null||grayTmp2.size()==0) return;
		breadthFirstSearch(grayTmp2,resultSet);
	}
}
