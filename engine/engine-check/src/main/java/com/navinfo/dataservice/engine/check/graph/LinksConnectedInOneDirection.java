package com.navinfo.dataservice.engine.check.graph;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

/** 
 * @ClassName: LinksConnectedInOneDirection
 * @author songdongyan
 * @date 2016年8月22日
 * @Description: link是噢否是可通行方向上的接续link
 */
public class LinksConnectedInOneDirection {

	/**
	 * 
	 */
	public LinksConnectedInOneDirection() {
		// TODO Auto-generated constructor stub
	}
	
	public LinksConnectedInOneDirection(int sNode,RdLink outLink,List<IRow> viaLinks) {
		// TODO Auto-generated constructor stub
		this.rdLinkList = viaLinks;
		this.sLink = outLink;
		this.sNode = sNode;
	}
	
	private int sNode;
	private RdLink sLink;
	private List<IRow> rdLinkList;
	
	public int getsNode(){
		return sNode;
	}
	public void setsNode(int sNode){
		this.sNode = sNode;
	}
	public RdLink getsLink(){
		return sLink;
	}
	public void setsLink(RdLink sLink){
		this.sLink = sLink;
	}
	public List<IRow> getRdLinkList(){
		return rdLinkList;
	}
	public void setRdLinkList(List<IRow> rdLinkList){
		this.rdLinkList = rdLinkList;
	}
	
	public boolean isConnected(){
		int startNode = sNode;
		List<Integer> nodeList = new ArrayList<Integer>();
		nodeList.add(sNode);
		if(sLink.geteNodePid() == startNode){
			startNode = sLink.getsNodePid();
		}else{
			startNode = sLink.geteNodePid();
		}
		
		while(rdLinkList.size() > 0){
			int i = 0;
			while(i<rdLinkList.size()){
				RdLink rdLink = (RdLink)rdLinkList.get(i);
				if((rdLink.getsNodePid() == startNode)&&(rdLink.getDirect()!=3)&&(!nodeList.contains(rdLink.getsNodePid()))){
					nodeList.add(startNode);
					startNode = rdLink.geteNodePid();
					rdLinkList.remove(i);
					break;
				}else if((rdLink.geteNodePid() == startNode)&&(rdLink.getDirect()!=2)&&(!nodeList.contains(rdLink.getsNodePid()))){
					nodeList.add(startNode);
					startNode = rdLink.getsNodePid();
					rdLinkList.remove(i);
					break;
				}
				i += 1;
				if(i == rdLinkList.size()){
					return false;
				}
			}
			
		}

		return true;
	}
	
	
	public static void main(String[] args) throws Exception{
		
		List<IRow> rdLinks = new ArrayList<IRow>();
		RdLink rdLink = new RdLink();
		rdLink.setPid(1);
		rdLink.setsNodePid(3);
		rdLink.seteNodePid(4);
		rdLink.setDirect(1);
		rdLinks.add(rdLink);
		
		RdLink rdLink1 = new RdLink();
		rdLink1.setPid(2);
		rdLink1.setsNodePid(2);
		rdLink1.seteNodePid(3);
		rdLink1.setDirect(1);
		rdLinks.add(rdLink1);
		
		RdLink startLink = new RdLink();
		startLink.setPid(3);
		startLink.setsNodePid(1);
		startLink.seteNodePid(2);
		startLink.setDirect(1);
		
		int startNode = 1;
		LinksConnectedInOneDirection linksConnectedInOneDirection = new LinksConnectedInOneDirection(startNode,startLink,rdLinks);
		if(!linksConnectedInOneDirection.isConnected()){
			System.out.println("there");
		}

	}
	
	
}
