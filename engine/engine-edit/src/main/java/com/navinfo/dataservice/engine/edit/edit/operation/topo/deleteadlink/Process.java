package com.navinfo.dataservice.engine.edit.edit.operation.topo.deleteadlink;

import java.sql.Connection;
import java.util.ArrayList;
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
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;
import com.navinfo.dataservice.engine.edit.edit.operation.OperatorFactory;
import com.navinfo.dataservice.engine.edit.edit.operation.topo.deleteadlink.Command;

public class Process extends AbstractProcess<Command> {
//
//	private Command command;
//
//	private Result result;
//
//	private Connection conn;
//
//	private String postCheckMsg;

	public Process(AbstractCommand command) throws Exception {
		super(command);

	}


	public void lockAdLink() throws Exception {

		AdLinkSelector selector = new AdLinkSelector(this.getConn());

		AdLink link = (AdLink) selector.loadById(this.getCommand().getLinkPid(), true);

		this.getCommand().setLink(link);
	}

	// 锁定盲端节点
	public void lockAdNode() throws Exception {

		AdNodeSelector selector = new AdNodeSelector(this.getConn());

		List<AdNode> nodes = selector.loadEndAdNodeByLinkPid(this.getCommand().getLinkPid(),
				false);
		
		List<Integer> nodePids = new ArrayList<Integer>();
		
		for(AdNode node : nodes){
			nodePids.add(node.getPid());
		}
		this.getCommand().setNodes(nodes);
		
		this.getCommand().setNodePids(nodePids);
	}
	
	// 锁定盲端节点
			public void lockAdFace() throws Exception {
				AdFaceSelector selector = new AdFaceSelector(this.getConn());
				List<AdFace> faces = selector.loadAdFaceByLinkId(this.getCommand().getLinkPid(),true);
				this.getCommand().setFaces(faces);
			}

	@Override
	public boolean prepareData() throws Exception {

		// 检查link是否可以删除
		String msg = preCheck();

		if (null != msg) {
			throw new Exception(msg);
		}

		// 获取该link对象
		lockAdLink();

		if (this.getCommand().getLink() == null) {

			throw new Exception("指定删除的LINK不存在！");
		}

		lockAdNode();
		return true;
	}
	
	@Override
	public IOperation createOperation() {
		// TODO Auto-generated method stub
		return new OpRefAdFace(this.getCommand());
	}
//
//	@Override
//	public String run() throws Exception {
//
//		try {
//			if (!command.isCheckInfect()) {
//				conn.setAutoCommit(false);
//				String preCheckMsg = this.preCheck();
//				if (preCheckMsg != null) {
//					throw new Exception(preCheckMsg);
//				}
//				prepareData();
//				IOperation op = new OpTopo(command);
//				op.run(result);
//				IOperation opRefAdface = new OpRefAdFace(command);
//				opRefAdface.run(result);
//				recordData();
//				
//				postCheck();
//				
//				conn.commit();
//			}
//		} catch (Exception e) {
//			
//
//			conn.rollback();
//
//			throw e;
//		} finally {
//			try {
//				conn.close();
//			} catch (Exception e) {
//				
//			}
//		}
//
//		return null;
//	}
//
//	@Override
//	public boolean recordData() throws Exception {
//		
//		LogWriter lw = new LogWriter(conn, this.command.getProjectId());
//		
//		lw.generateLog(command, result);
//		
//		OperatorFactory.recordData(conn, result);
//
//		lw.recordLog(command, result);
//
//		return true;
//	}
//
//	@Override
//	public void postCheck() {
//
//		// 对数据进行检查、检查结果存储在数据库，并存储在临时变量postCheckMsg中
//	}
//
//	@Override
//	public String getPostCheck() throws Exception {
//
//		return postCheckMsg;
//	}

}
