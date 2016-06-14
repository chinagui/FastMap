package com.navinfo.dataservice.engine.check.graph;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class ChainLoader {
	HashSetRdLinkAndPid SpecTrafficChain=new HashSetRdLinkAndPid();
	HashSetRdLinkAndPid huandaoChain=new HashSetRdLinkAndPid();

	public ChainLoader() {
		// TODO Auto-generated constructor stub
	}
	
	public HashSetRdLinkAndPid loadSpecTrafficChain(Connection conn,RdLink linkObj) throws Exception{
		synchronized(this){
			if(SpecTrafficChain.size()==0 || !SpecTrafficChain.contains(linkObj.getPid())){
				HashSetRdLinkAndPid graySet=new HashSetRdLinkAndPid();
				HashSetRdLinkAndPid resultSet=new HashSetRdLinkAndPid();
				graySet.add(linkObj);
				SpecTrafficSearchFilter specObj=new SpecTrafficSearchFilter(conn);
				PathSearcher pathSearcherObj=new PathSearcher();
				pathSearcherObj.registerFilter(specObj);
				pathSearcherObj.breadthFirstSearch(graySet, resultSet);
				SpecTrafficChain=resultSet;}
			return SpecTrafficChain;
		}
	}
	
	public HashSetRdLinkAndPid loadHandaoChain(Connection conn,RdLink linkObj) throws Exception{
		synchronized(this){
			if(huandaoChain.size()==0 || !huandaoChain.contains(linkObj.getPid())){
				HashSetRdLinkAndPid graySet=new HashSetRdLinkAndPid();
				HashSetRdLinkAndPid resultSet=new HashSetRdLinkAndPid();
				graySet.add(linkObj);
				HuanDaoSearchFilter huandaoObj=new HuanDaoSearchFilter(conn);
				PathSearcher pathSearcherObj=new PathSearcher();
				pathSearcherObj.registerFilter(huandaoObj);
				pathSearcherObj.breadthFirstSearch(graySet, resultSet);
				huandaoChain=resultSet;}
			return huandaoChain;
		}
	}
}
