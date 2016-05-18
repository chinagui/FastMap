package com.navinfo.dataservice.engine.check.graph;

import java.sql.Connection;
import java.util.Iterator;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class SpecTrafficSearchFilter extends SearchFilter {
	
	public SpecTrafficSearchFilter(Connection conn) {
		super(conn);
		// TODO Auto-generated constructor stub
	}

	//根据elementSet这个返回符合条件的接续link集
	public HashSetRdLinkAndPid getAjointSet(HashSetRdLinkAndPid elementSet) throws Exception{
		HashSetRdLinkAndPid graySet=new HashSetRdLinkAndPid();
		Iterator<RdLink> elementIterator=elementSet.iterator();
		while(elementIterator.hasNext()){
			RdLink linkTmp=elementIterator.next();
			//获取终点挂接link，且挂接link是特殊交通类型
			HashSetRdLinkAndPid ajointELinkSet=this.getRdLinksByNodePid(linkTmp.geteNodePid());
			Iterator<RdLink> ajointELinkIterator=ajointELinkSet.iterator();
			while(ajointELinkIterator.hasNext()){
				RdLink aElinkTmp=ajointELinkIterator.next();
				if(aElinkTmp.getSpecialTraffic()==1 && !graySet.contains(aElinkTmp.getPid())){
					graySet.add(aElinkTmp);
				}
			}
			//获取起点挂接link，且挂接link是特殊交通类型
			HashSetRdLinkAndPid ajointSLinkSet=this.getRdLinksByNodePid(linkTmp.getsNodePid());
			Iterator<RdLink> ajointSLinkIterator=ajointSLinkSet.iterator();
			while(ajointSLinkIterator.hasNext()){
				RdLink aSlinkTmp=ajointSLinkIterator.next();
				if(aSlinkTmp.getSpecialTraffic()==1 && !graySet.contains(aSlinkTmp.getPid())){
					graySet.add(aSlinkTmp);
				}
			}
		}
		return graySet;
	}
}
