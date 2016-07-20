package com.navinfo.dataservice.engine.edit.bo.ad;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLinkMesh;
import com.vividsolutions.jts.geom.Point;

/**
 * @ClassName: BoAdLink
 * @author xiaoxiaowen4127
 * @date 2016年7月15日
 * @Description: BoAdLink.java
 */
public class AdLinkBo extends LinkBo{
	private AdLink adLink;
	private List<AdLinkMesh> meshes;
	private AdNodeBo sNode;
	private AdNodeBo eNode;
	
	public AdLinkBo(Connection conn, int linkPid, boolean isLock) {
		this.conn = conn;
		this.isLock = isLock;
		this.adLink = PoFactory.getInstance().getByPK(conn, AdLink.class, linkPid, isLock);
	}


	@Override
	public BreakResult breakoff(Point point) {
		return super.breakoff(point);
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
				this.meshes=PoFactory.getInstance().getByFK(conn, AdLinkMesh.class, "linkPid", this.adLink.getPid(), isLock);
			}
		}
		
		return this.meshes;
	}

	public void setMeshes(List<AdLinkMesh> meshes) {
		this.meshes = meshes;
	}

	public AdNodeBo getsNode() {
		if(null==sNode){
			sNode = new AdNodeBo(conn, this.adLink.getsNodePid(), isLock);
		}
		return sNode;
	}

	public void setsNode(AdNodeBo sNode) {
		this.sNode = sNode;
	}

	public AdNodeBo geteNode() {
		if(null==eNode){
			eNode = new AdNodeBo(conn, this.adLink.geteNodePid(), isLock);
		}
		return eNode;
	}

	public void seteNode(AdNodeBo eNode) {
		this.eNode = eNode;
	}

}