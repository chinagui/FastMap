package com.navinfo.dataservice.engine.edit.edit.operation.topo.moveadnode;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;

/**
 * @author zhaokk
 * 移动行政区划点具体执行类
 */
public class Process extends AbstractProcess<Command> {
	
	
	public Process(AbstractCommand command) throws Exception {
		super(command);
		// TODO Auto-generated constructor stub
	}

	private AdNode updateNode;
	private List<AdFace> adFaces;

 /*
 * 移动行政区划点加载对应的行政区划线信息
 */
	public void lockAdLink() throws Exception {

		AdLinkSelector selector = new AdLinkSelector(this.getConn());

		List<AdLink> links = selector.loadByNodePid(this.getCommand().getNodePid(), true);
		this.getCommand().setLinks(links);
	}
	
	 /*
	 * 移动行政区划点加载对应的行政区点线信息
	 */
	public void lockAdNode() throws Exception {

		AdNodeSelector nodeSelector = new AdNodeSelector(this.getConn());
		
		this.updateNode = (AdNode) nodeSelector.loadById(this.getCommand().getNodePid(), true);
		}
	
	 /*
		 * 移动行政区划点加载对应的行政区点面信息
		 */
    public void lockAdFace() throws Exception {

		AdFaceSelector faceSelector = new AdFaceSelector(this.getConn());
		
		this.adFaces= faceSelector.loadAdFaceByNodeId(this.getCommand().getNodePid(), true);
		this.getCommand().setFaces(adFaces);
				
	}
		
	@Override
	public boolean prepareData() throws Exception {
		
		lockAdNode();
		lockAdLink();
		return false;
	}

	@Override
	public IOperation createOperation() {
		// TODO Auto-generated method stub
		return new Operation(this.getCommand(),updateNode,this.getConn());
	}

}
