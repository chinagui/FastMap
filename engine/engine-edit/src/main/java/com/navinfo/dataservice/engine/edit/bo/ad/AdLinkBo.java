package com.navinfo.dataservice.engine.edit.bo.ad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.vividsolutions.jts.geom.Point;

/** 
 * @ClassName: BoAdLink
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: BoAdLink.java
 */
public class AdLinkBo {
	private AdLink adLink;
	private List<AdLinkMesh> meshes;
	private AdNodeBo sNode;
	private AdNodeBo eNode;
	private Connection conn;
	
	public AdLinkBo(Connection conn, int linkPid){
		this.conn=conn;
		
		AdLinkSelector selector = new AdLinkSelector(conn);
		
		this.adLink = selector.loadById(linkPid, isLock);
	}
	
	public void breakoff(Result result, Point point){
	}
	public void breakoff(Result result, AdNodeBo node){
	}

	public AdLink getAdLink() {
		return adLink;
	}

	public void setAdLink(AdLink adLink) {
		this.adLink = adLink;
	}

	public List<AdLinkMesh> getMeshes() {
		if(null==this.meshes){
			if(this.adLink.getPid()==0){
				this.meshes=new ArrayList<AdLinkMesh>();
			}
			else{
				
			}
		}
		
		return this.meshes
	}

	public void setMeshes(List<AdLinkMesh> meshes) {
		this.meshes = meshes;
	}

	public AdNodeBo getsNode() {
		return sNode;
	}

	public void setsNode(AdNodeBo sNode) {
		this.sNode = sNode;
	}

	public AdNodeBo geteNode() {
		return eNode;
	}

	public void seteNode(AdNodeBo eNode) {
		this.eNode = eNode;
	}
	
}