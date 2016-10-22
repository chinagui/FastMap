package com.navinfo.dataservice.engine.check.graph;

import java.sql.Connection;
import java.util.Iterator;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class HuanDaoSearchFilter extends SearchFilter {
	
	public HuanDaoSearchFilter(Connection conn) {
		super(conn);
		// TODO Auto-generated constructor stub
	}

	//根据elementSet这个返回符合条件的接续link集
	public HashSetRdLinkAndPid getAjointSet(HashSetRdLinkAndPid elementSet) throws Exception{
		HashSetRdLinkAndPid graySet=new HashSetRdLinkAndPid();
		Iterator<RdLink> elementIterator=elementSet.iterator();
		while(elementIterator.hasNext()){
			RdLink linkTmp=elementIterator.next();
			String nodePids=linkTmp.geteNodePid()+","+linkTmp.getsNodePid();
			String Sql="select * from rd_link r where (s_node_pid in ("+nodePids+") or e_node_pid in ("+nodePids+")) and u_record!=2"
					+ " and exists(select 1 from rd_link_form f where r.link_pid=f.link_pid and f.form_of_way=33 AND F.U_RECORD != 2)";
			//获取终点挂接link，且挂接link是环岛
			HashSetRdLinkAndPid ajointLinkSet=this.getRdLinksBySql(Sql);
			Iterator<RdLink> ajointLinkIterator=ajointLinkSet.iterator();
			while(ajointLinkIterator.hasNext()){
				RdLink alinkTmp=ajointLinkIterator.next();
				if(!graySet.contains(alinkTmp.getPid())){
					graySet.add(alinkTmp);
				}
			}		}
		return graySet;
	}
}
