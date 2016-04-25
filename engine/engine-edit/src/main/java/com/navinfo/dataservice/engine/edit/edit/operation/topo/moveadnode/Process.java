package com.navinfo.dataservice.engine.edit.edit.operation.topo.moveadnode;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.dao.pool.GlmDbPoolManager;
import com.navinfo.dataservice.engine.edit.edit.operation.OperatorFactory;

/**
 * @author zhaokk
 * 移动行政区划点具体执行类
 */
public class Process implements IProcess {
	
	private Command command;

	private Result result;

	private Connection conn;

	private String postCheckMsg;
	
	private AdNode updateNode;
	private List<AdFace> adFaces;
	public Process(ICommand command) throws Exception {
		
		this.command = (Command) command;

		this.result = new Result();

		this.conn = GlmDbPoolManager.getInstance().getConnection(this.command
				.getProjectId());
	}

	@Override
	public ICommand getCommand() {
		
		return command;
	}

	@Override
	public Result getResult() {
		
		return result;
	}

 /*
 * 移动行政区划点加载对应的行政区划线信息
 */
	public void lockAdLink() throws Exception {

		AdLinkSelector selector = new AdLinkSelector(this.conn);

		List<AdLink> links = selector.loadByNodePid(command.getNodePid(), true);
		command.setLinks(links);
	}
	
	 /*
	 * 移动行政区划点加载对应的行政区点线信息
	 */
	public void lockAdNode() throws Exception {

			AdNodeSelector nodeSelector = new AdNodeSelector(this.conn);
			
			this.updateNode = (AdNode) nodeSelector.loadById(command.getNodePid(), true);
		}
	
	 /*
		 * 移动行政区划点加载对应的行政区点面信息
		 */
    public void lockAdFace() throws Exception {

				AdFaceSelector faceSelector = new AdFaceSelector(this.conn);
				
				this.adFaces= faceSelector.loadAdFaceByNodeId(command.getNodePid(), true);
				command.setFaces(adFaces);
				
	}
		
	@Override
	public boolean prepareData() throws Exception {
		
		lockAdNode();
		lockAdLink();
		return false;
	}

	@Override
	public String preCheck() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String run() throws Exception {
		try {
			conn.setAutoCommit(false);

			this.prepareData();

			String preCheckMsg = this.preCheck();

			if (preCheckMsg != null) {
				throw new Exception(preCheckMsg);
			}

			IOperation operation = new Operation(command,updateNode);

			operation.run(result);

			this.recordData();

			this.postCheck();
			conn.commit();

		} catch (Exception e) {

			conn.rollback();

			throw e;
		} finally {
			try {
				conn.close();
			} catch (Exception e) {

			}
		}

		return null;
	}

	@Override
	public void postCheck() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPostCheck() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean recordData() throws Exception {
		
		LogWriter lw = new LogWriter(conn, this.command.getProjectId());
		
		lw.generateLog(command, result);
		
		OperatorFactory.recordData(conn, result);

		lw.recordLog(command, result);

		return true;
	}

}
