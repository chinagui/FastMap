package com.navinfo.dataservice.engine.editplus.operation;

import com.navinfo.dataservice.engine.editplus.bo.AbstractLinkBo;
import com.navinfo.dataservice.engine.editplus.bo.AbstractNodeBo;


public class LinkBreakResult extends EditorOperationResult {

	protected AbstractLinkBo targetLinkBo;
	protected AbstractLinkBo newLeftLink;
	protected AbstractLinkBo newRightLink;
	protected AbstractNodeBo newNode;
	public AbstractLinkBo getTargetLinkBo() {
		return targetLinkBo;
	}
	public void setTargetLinkBo(AbstractLinkBo targetLinkBo) {
		this.targetLinkBo = targetLinkBo;
	}
	public AbstractLinkBo getNewLeftLink() {
		return newLeftLink;
	}
	public void setNewLeftLink(AbstractLinkBo newLeftLink) {
		this.newLeftLink = newLeftLink;
	}
	public AbstractLinkBo getNewRightLink() {
		return newRightLink;
	}
	public void setNewRightLink(AbstractLinkBo newRightLink) {
		this.newRightLink = newRightLink;
	}
	public AbstractNodeBo getNewNode() {
		return newNode;
	}
	public void setNewNode(AbstractNodeBo newNode) {
		this.newNode = newNode;
	}
}
