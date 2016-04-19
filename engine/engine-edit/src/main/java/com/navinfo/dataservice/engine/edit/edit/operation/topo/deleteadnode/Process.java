package com.navinfo.dataservice.engine.edit.edit.operation.topo.deleteadnode;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.db.DBOraclePoolManager;
import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IProcess;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdFace;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdNode;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdFaceSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.cross.RdCrossSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.speedlimit.RdSpeedlimitSelector;
import com.navinfo.dataservice.dao.log.LogWriter;
import com.navinfo.dataservice.engine.edit.edit.operation.OperatorFactory;

/**
 * @author zhaokk
 * 行政区划点删除操作类
 */

public class Process implements IProcess {

	private Command command;

	private Result result;

	private Connection conn;

	private String postCheckMsg;
	protected Logger log = Logger.getLogger(this.getClass());
	public Process(ICommand command) throws Exception {
		this.command = (Command) command;

		this.result = new Result();

		this.conn = DBOraclePoolManager.getConnection(this.command
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

	public String preCheck() throws Exception {
		return null;

	}
	/*
	 * 加载行政区划点对应的行政区划线
	 */
	public void lockAdLink() throws Exception {

		AdLinkSelector selector = new AdLinkSelector(this.conn);
		List<AdLink> links = selector.loadByNodePid(command.getNodePid(), true);
		List<Integer> linkPids = new ArrayList<Integer>();
		for(AdLink link : links){
			linkPids.add(link.getPid());
		}
		command.setLinks(links);
		
		command.setLinkPids(linkPids);
	}
	/*
	 * 加载行政区划点对应的行政区点
	 */
	public void lockAdNode() throws Exception {

		AdNodeSelector selector = new AdNodeSelector(this.conn);

		AdNode node = (AdNode) selector.loadById(command.getNodePid(), true);

		command.setNode(node);

	}
	/*
	 * 加载行政区划点对应的行政区盲端节点
	 */
	public void lockEndAdNode() throws Exception {

		AdNodeSelector selector = new AdNodeSelector(this.conn);

		List<Integer> nodePids = new ArrayList<Integer>();
		
		nodePids.add(command.getNodePid());

		List<AdNode> nodes = new ArrayList<AdNode>();

		for (Integer linkPid: command.getLinkPids()) {

			List<AdNode> list = selector.loadEndAdNodeByLinkPid(linkPid,
					true);

			for (AdNode node : list) {
				int nodePid = node.getPid();
				
				if (nodePids.contains(nodePid)) {
					continue;
				}

				nodePids.add(node.getPid());

				nodes.add(node);
			}

		}

		command.setNodes(nodes);

		command.setNodePids(nodePids);
	}
	/*
	 * 加载行政区划点对应的行政区划线
	 */
		public void lockAdFace() throws Exception {

			AdFaceSelector selector = new AdFaceSelector(this.conn);

			List<AdFace> faces = new ArrayList<AdFace>();

			for (Integer linkPid: command.getLinkPids()) {

				List<AdFace> list = selector.loadAdFaceByLinkId(linkPid,
						true);

				for (AdFace face : list) {
					faces.add(face);
					
				}
			}
			command.setFaces(faces);
		}

	@Override
	public boolean prepareData() throws Exception {

		// 检查是否可以删除
		String msg = preCheck();

		if (null != msg) {
			throw new Exception(msg);
		}

		// 获取该adnode对象
		lockAdNode();

		if (command.getNode() == null) {

			throw new Exception("指定删除的RDNODE不存在！");
		}

		lockAdLink();

		lockEndAdNode();
		lockAdFace();
		return true;
	}

	@Override
	public String run() throws Exception {

		try {
				conn.setAutoCommit(false);
				String preCheckMsg = this.preCheck();
				if (preCheckMsg != null) {
					throw new Exception(preCheckMsg);
				}
				prepareData();
				//删除行政区划点有关行政区划点、线具体操作
				IOperation op = new OpTopo(command);
				op.run(result);
				//删除行政区划点有关行政区划面具体操作
				IOperation opAdFace = new OpRefAdFace(command);
				opAdFace.run(result);
				recordData();
				postCheck();
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
	public boolean recordData() throws Exception {

		OperatorFactory.recordData(conn, result);

		LogWriter lw = new LogWriter(conn);

		lw.recordLog(command, result);

		return true;

	}

	private void releaseResource(PreparedStatement pstmt, ResultSet resultSet) {
		try {
			resultSet.close();
		} catch (Exception e) {

		}

		try {
			pstmt.close();
		} catch (Exception e) {

		}
	}

	@Override
	public void postCheck() {

		// 对数据进行检查、检查结果存储在数据库，并存储在临时变量postCheckMsg中
	}

	@Override
	public String getPostCheck() throws Exception {

		return postCheckMsg;
	}

}
