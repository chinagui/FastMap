package com.navinfo.dataservice.engine.edit.bo;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.vividsolutions.jts.geom.Geometry;


public class LinkBreakResult extends Result {

	protected LinkBo targetLinkBo;
	protected LinkBo newLeftLink;
	protected LinkBo newRightLink;
	protected NodeBo newNode;
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
	public NodeBo getNewNode() {
		return newNode;
	}
	public void setNewNode(NodeBo newNode) {
		this.newNode = newNode;
	}
}
