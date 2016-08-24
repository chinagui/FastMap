package com.navinfo.dataservice.engine.check.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/** 
 * @ClassName: LinksContainClosedLoop
 * @author songdongyan
 * @date 2016年8月24日
 * @Description: RdLink中是否包含闭环
 */
public class LinksContainClosedLoop {

	/**
	 * 
	 */
	private List<IRow> rdLinks;
	
	public void setRdLinks(List<IRow> rdLinks){
		this.rdLinks = rdLinks;
	}
	public List<IRow> getRdLinks(){
		return rdLinks;
	}
	public LinksContainClosedLoop() {
		// TODO Auto-generated constructor stub
	}
	
	public LinksContainClosedLoop(List<IRow> viaLinks) {
		// TODO Auto-generated constructor stub
		this.rdLinks = viaLinks;
	}
	
	public boolean containClosedLoop(){
		List<Integer> nodeSet = new ArrayList<Integer>();
		if(rdLinks.isEmpty()){
			return false;
		}

		nodeSet.add(((RdLink) rdLinks.get(0)).getsNodePid());

		for(int i = 0;i<nodeSet.size();i++){
			int node = nodeSet.get(i);
			Iterator<IRow> rdLinksIterator = rdLinks.iterator();
			while(rdLinksIterator.hasNext()){
				RdLink rdLink = (RdLink) rdLinksIterator.next();
				if(rdLink.geteNodePid() == node){
					if(nodeSet.contains(rdLink.getsNodePid())){
						return true;
					}else{
						nodeSet.add(rdLink.getsNodePid());
						rdLinksIterator.remove();
						continue;
					}
				}
				else if(rdLink.getsNodePid() == node){	
					if(nodeSet.contains(rdLink.geteNodePid())){
						return true;
					}else{
						nodeSet.add(rdLink.geteNodePid());
						rdLinksIterator.remove();
						continue;
					}
				}
			}
		}
		return false;
		
	}
	
	public static void main(String[] args) throws Exception{
		List<IRow> rdLinks = new ArrayList<IRow>();
		RdLink rdLink1 = new RdLink();
		rdLink1.setPid(1);
		rdLink1.setsNodePid(1);
		rdLink1.seteNodePid(2);
		rdLinks.add(rdLink1);
		
		RdLink rdLink2 = new RdLink();
		rdLink2.setPid(2);
		rdLink2.setsNodePid(2);
		rdLink2.seteNodePid(3);
		rdLinks.add(rdLink2);
		
		
		RdLink rdLink3 = new RdLink();
		rdLink3.setPid(3);
		rdLink3.setsNodePid(4);
		rdLink3.seteNodePid(2);
		rdLinks.add(rdLink3);
		
		RdLink rdLink4 = new RdLink();
		rdLink4.setPid(4);
		rdLink4.setsNodePid(4);
		rdLink4.seteNodePid(5);
		rdLinks.add(rdLink4);
		
		RdLink rdLink5 = new RdLink();
		rdLink5.setPid(5);
		rdLink5.setsNodePid(5);
		rdLink5.seteNodePid(1);
		rdLinks.add(rdLink5);

		LinksContainClosedLoop linksContainClosedLoop = new LinksContainClosedLoop(rdLinks);
		if(!linksContainClosedLoop.containClosedLoop()){
			System.out.println("there");
		}
	}

}
