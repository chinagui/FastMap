package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakadpoint;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import oracle.net.aso.g;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.analysis.function.Add;
import org.apache.hadoop.hdfs.protocol.proto.XAttrProtos.ListXAttrsResponseProtoOrBuilder;
import org.springframework.web.jsf.FacesContextUtils;

import com.ctc.wstx.dtd.SeqContentSpec;
import com.mongodb.client.model.geojson.GeoJsonObjectType;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.GeometryUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFaceTopo;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.sun.research.ws.wadl.Link;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author zhaokk
 * 创建行政区划点有关行政区划面具体操作类
 *
 */
public class OpRefAdFace implements IOperation {

	private Command command;
	
	private Result result;
    
	private  Connection conn;
	
	public OpRefAdFace(Command command,Connection conn) {
		this.command = command;
		this.conn  = conn;

	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;
        if (command.getAdFaceTopos() != null &&command.getAdFaceTopos().size() > 0){
        	this.handleAdFaceTopo();
        }
		return null;
	}

	/*
	 *  @param List
	 *  修改AdFace 和AdLink topo 关系
	 *  
	 */
	private void handleAdFaceTopo()
			throws Exception {
		List<AdLink> links;
		//1.获取打断点涉及的面信息
		//2.删除打断线对应面的topo关系
		//3.重新获取组成面的link关系，重新计算面的形状
		for (AdFace face : command.getFaces()) {
			links = new ArrayList<AdLink>();
			for (IRow iRow :face.getFaceTopos()){
				AdFaceTopo obj = (AdFaceTopo) iRow;
				if(obj.getLinkPid()!=command.getLinkPid()){
					links.add((AdLink)new AdLinkSelector(conn).loadById(obj.getLinkPid(), true));
				}
				result.insertObject(obj,ObjStatus.DELETE,face.getPid());
			}
			 links.add(command.geteAdLink());
			 links.add(command.getsAdLink());
			 this.reCaleFaceGeometry(links,face);
		}
		
	}
	/*
	 *  @param List 
	 *  按照ADFACE的形状重新维护ADFACE
	 *  
	 */
	 private  void reCaleFaceGeometry(List<AdLink> links,AdFace face) throws Exception  { 
		  if (links == null  && links.size() < 1){
			  throw new Exception("重新维护面的形状:发现面没有组成link");
		  }
		  AdLink currLink = null;
		  for(AdLink adLink : links){
			  currLink = adLink;
			  break;
		  }
		  if (currLink == null){
			 return;
		  }
		  int startNodePid = currLink.getStartNodePid();
		  int currNodePid = startNodePid;
		  this.addLink(face, currLink,1);
		  int index = 1;
		  List<Geometry> list = new ArrayList<Geometry>();
		  list.add(currLink.getGeometry());
		  while(getNextLink(links,currNodePid,currLink)){
			  if(currNodePid == startNodePid){
				  break;
			  }
			  index++;
			  this.addLink(face, currLink, index);
			  list.add(currLink.getGeometry());
		  }
		  Geometry g = GeoTranslator.getCalLineToPython(list);
		  Coordinate [] c1 =  new Coordinate[g.getCoordinates().length];
		    if(!GeometryUtils.IsCCW(g.getCoordinates())){
	        	for(int i = g.getCoordinates().length -1;i >= 0 ;i-- ){
	        		c1[c1.length-i-1] = c1[0];	
	        	}
	        	this.reverseFaceTopo();
	        		
	        }
		 this.updateGeometry(GeoTranslator.getPolygonToPoints(c1), face) ;
		  
	 }
	 
	 private void  updateGeometry(Geometry g , AdFace face){
		 face.setGeometry(g);
	 }
	 private void  reverseFaceTopo(){
		 int newIndex = 0;
		 for(int i = result.getAddObjects().size() -1; i >= 0 ;i--){
			 if (result.getAddObjects().get(i) instanceof AdFaceTopo){
				  newIndex++;
				 ((AdFaceTopo)result.getAddObjects().get(i)).setSeqNum(newIndex);
				
			 }
		 }
	 }
	 private boolean getNextLink(List<AdLink> links,int currNodePid,AdLink currLink) throws Exception{
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
    public  void addLink(AdFace face,AdLink link,int seqNum){
		  AdFaceTopo faceTopo  = new AdFaceTopo();
		  faceTopo.setLinkPid(link.getPid());
		  faceTopo.setFacePid(face.getPid());
		  faceTopo.setSeqNum(seqNum);
		  result.getAddObjects().add(faceTopo);
      }
	
}