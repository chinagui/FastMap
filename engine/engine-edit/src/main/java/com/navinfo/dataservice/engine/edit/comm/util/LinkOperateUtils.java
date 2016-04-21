package com.navinfo.dataservice.engine.edit.comm.util;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNodeMesh;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeForm;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
/**
 * @author zhaokk
 * LINK 公共方法
 */
public class LinkOperateUtils {
	
	 /*
	  * 添加link获取下一条连接的link
	  */
	 public  static boolean getNextLink(List<AdLink> links,int currNodePid,AdLink currLink) throws Exception{
	     int nextNodePid = 0;
		 if(currNodePid == currLink.getStartNodePid()){
			 nextNodePid = currLink.getStartNodePid();
		 }else{
			 nextNodePid = currLink.getEndNodePid();
		 }
		 for(AdLink link :links){
			 if(link.getPid() == currLink.getPid()){
				 continue;
			 }
			 if(link.getStartNodePid() == nextNodePid || link.getEndNodePid() == nextNodePid){
				 currNodePid = nextNodePid;
				 currLink = link;
				 return true;
			 }
		 }
		 return false ;
	 }
	
}
