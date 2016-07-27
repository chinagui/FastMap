package com.navinfo.dataservice.engine.edit.bo;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.vividsolutions.jts.geom.Geometry;


public class BreakResult extends Result {

	protected LinkBo targetLinkBo;
	protected LinkBo newLeftLink;
	protected LinkBo newRightLink;
	protected Geometry newLeftGeometry;
	protected Geometry newRightGeometry;
	public LinkBo getTargetLinkBo() {
		return targetLinkBo;
	}
	public void setTargetLinkBo(LinkBo targetLinkBo) {
		this.targetLinkBo = targetLinkBo;
	}
	public LinkBo getNewLeftLink() {
		return newLeftLink;
	}
	public void setNewLeftLink(LinkBo newLeftLink) {
		this.newLeftLink = newLeftLink;
	}
	public LinkBo getNewRightLink() {
		return newRightLink;
	}
	public void setNewRightLink(LinkBo newRightLink) {
		this.newRightLink = newRightLink;
	}
	public Geometry getNewLeftGeometry() {
		return newLeftGeometry;
	}
	public void setNewLeftGeometry(Geometry newLeftGeometry) {
		this.newLeftGeometry = newLeftGeometry;
	}
	public Geometry getNewRightGeometry() {
		return newRightGeometry;
	}
	public void setNewRightGeometry(Geometry newRightGeometry) {
		this.newRightGeometry = newRightGeometry;
	}
	
}
