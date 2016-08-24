package com.navinfo.dataservice.engine.check.graph;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class CrossChainFilter extends SearchFilter
{
	private List<Integer> jointLinkPids=new ArrayList<Integer>();
	
	public CrossChainFilter(Connection conn,List<RdLink> jointLinks) {
		super(conn);
		for(RdLink tmp:jointLinks){
			jointLinkPids.add(tmp.getPid());
		}
	}
	
	public HashSetRdLinkAndPid OutLinkFilter(
			LinkedListRdLinkAndPid preLinks,
			HashSetRdLinkAndPid outLinks) {
		 if(preLinks==null  || preLinks.size()<=1) return outLinks;   //此处如果size=1表示开始搜索
	     //如果最后一条link恰好是挂接link则搜索停止
		 if(jointLinkPids !=null)
		 {
			 if(jointLinkPids.contains(preLinks.getLast().getPid()))
			 {
				 return null;   //搜索停止
			 }
		 }

		 if(this.getNextFilter() !=null) return this.getNextFilter().OutLinkFilter(preLinks, outLinks);
	     return outLinks;
	 }
}
