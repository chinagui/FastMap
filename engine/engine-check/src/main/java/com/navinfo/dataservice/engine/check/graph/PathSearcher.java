package com.navinfo.dataservice.engine.check.graph;

import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.check.graph.SearchFilter;

public class PathSearcher {
	private SearchFilter filter = null;
	private LinkedListRdLinkAndPid preLinks = new LinkedListRdLinkAndPid(); // 代表路径搜索过程中已经搜索了的link
	private HashSetRdLinkAndPid visitedLinks = new HashSetRdLinkAndPid();

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
	
	// 深度优先搜索---正向，由进入线向退出线方向搜索
	public void deepthFirstSearch(RdLink startLink, int nodePid,
			List<LinkedListRdLinkAndPid> chainList) throws Exception {
		if (startLink == null
				|| (filter != null && !filter.isValidLink(startLink)))				
			return;
		
		HashSetRdLinkAndPid outLinks = null;
		if (nodePid == 0) {
			outLinks = filter.getLinkOfExitLinks(startLink);
		} else {
			outLinks = filter.getLinkOfExitLinks(startLink, nodePid);
		}

		preLinks.add(startLink);
		visitedLinks.add(startLink); // 标记为已经访问
		if (filter != null)
			outLinks = filter.OutLinkFilter(preLinks, outLinks);

		boolean bStop = false;
		Iterator<RdLink> iter = null;
		if (outLinks != null) {
			iter = outLinks.iterator();
			while (iter.hasNext()) {
				RdLink outLinktmp=iter.next();
				if (visitedLinks.contains(outLinktmp.getPid())) // 防止循环递归
				{
					bStop = true;
					break;
				}
				if(!filter.isValidLink(outLinktmp))
					iter.remove();
			}
		}

		if (bStop || outLinks == null || outLinks.size() == 0) {
			LinkedListRdLinkAndPid chain = new LinkedListRdLinkAndPid();
			chain.addAll(preLinks);
			if (filter == null) {
				chainList.add(chain);
			} else if (filter.isValidChain(chain)) {
				chainList.add(chain);
			}
			
			preLinks.removeLast(); // 移除此列表的最后一个元素
			visitedLinks.remove(startLink);
			return;
		}

		iter = outLinks.iterator(); // 重置
		RdLink nextLink = null;
		while (iter.hasNext()) {
			nextLink = iter.next();
			if (filter != null && !filter.isValidPath(preLinks, nextLink)) {
				continue;
			}			
			if (nextLink.getsNodePid()==startLink.getsNodePid()
					|| nextLink.getsNodePid()==startLink.geteNodePid()) {
				deepthFirstSearch(nextLink, nextLink.geteNodePid(), chainList);
			} else {
				deepthFirstSearch(nextLink, nextLink.getsNodePid(), chainList);
			}
		}

		preLinks.removeLast(); // 移除此列表的最后一个元素
		visitedLinks.remove(startLink);
	}
}
