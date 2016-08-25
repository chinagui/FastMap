package com.navinfo.dataservice.engine.check.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.dao.check.CheckCommand;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.engine.check.CheckEngine;

/** 
 * @ClassName: IsConnected
 * @author songdongyan
 * @date 2016年8月19日
 * @Description: IsConnected.java
 */
public class TwoNodeConnected {

	/**
	 * 
	 */
	
	private int startNodePid;
	private int endNodePid;
	private List<RdLink> viaRdLinks;
	
	
	public TwoNodeConnected() {
		// TODO Auto-generated constructor stub
	}
	
	public TwoNodeConnected(int startNodePid,int endNodePid,List<RdLink> viaRdLinks) {
		// TODO Auto-generated constructor stub
		this.startNodePid = startNodePid;
		this.endNodePid = endNodePid;
		this.viaRdLinks = viaRdLinks;
	}
	
	public int getStartNodePid(){
		return startNodePid;
	}
	public int getEndNodePid(){
		return endNodePid;
	}
	public List<RdLink> getViaRdLink(){
		return viaRdLinks;
	}
	
	public void setStartNodePid(int startNodePid){
		this.startNodePid = startNodePid;
	}
	public void setEndNodePid(int endNodePid){
		this.endNodePid = endNodePid;
	}
	public void setViaRdLink(List<RdLink> viaRdLinks){
		this.viaRdLinks = viaRdLinks;
	}
	public boolean isConnected(){
		List<Integer> resultSet = new ArrayList<Integer>();
		if(endNodePid==startNodePid){
			return true;
		}
		resultSet.add(startNodePid);
		int i = 0;
		while(i<resultSet.size()){
			Iterator<RdLink> viaRdLinksIter = viaRdLinks.iterator();
			while(viaRdLinksIter.hasNext()){
				RdLink viaRdLink = (RdLink) viaRdLinksIter.next();
				if((viaRdLink.getsNodePid() == resultSet.get(i)) && (viaRdLink.getDirect()!=3)){
					if(viaRdLink.geteNodePid() == endNodePid){
						return true;
					}
					resultSet.add(viaRdLink.geteNodePid());
					viaRdLinksIter.remove();
					break;
				}else if((viaRdLink.geteNodePid() == resultSet.get(i))&&(viaRdLink.getDirect()!=2)){
					if(viaRdLink.getsNodePid() == endNodePid){
						return true;
					}
					resultSet.add(viaRdLink.getsNodePid());
					viaRdLinksIter.remove();
					break;
				}
			}
			resultSet.remove(i);
		}		
		return false;
		
	}
	
	public static void main(String[] args) throws Exception{
		
		List<RdLink> viaRdLinks = new ArrayList<RdLink>();
		RdLink rdLink = new RdLink();
		rdLink.setPid(277564);
		rdLink.setsNodePid(280385);
		rdLink.seteNodePid(260513);
		rdLink.setDirect(1);
		viaRdLinks.add(rdLink);
		
		RdLink rdLink1 = new RdLink();
		rdLink1.setPid(284053);
		rdLink1.setsNodePid(260440);
		rdLink1.seteNodePid(260513);
		rdLink1.setDirect(1);
		viaRdLinks.add(rdLink1);
		
		int startNode = 280385;
		int endNode = 280385;
		TwoNodeConnected twoNodeConnected = new TwoNodeConnected(startNode,endNode,viaRdLinks);
		if(!twoNodeConnected.isConnected()){
			System.out.println("there");
		}
		
		startNode = 260440;
		endNode = 280385;
		TwoNodeConnected twoNodeConnected_1 = new TwoNodeConnected(startNode,endNode,viaRdLinks);
		if(!twoNodeConnected_1.isConnected()){
			System.out.println("there");
		}
	}

}
