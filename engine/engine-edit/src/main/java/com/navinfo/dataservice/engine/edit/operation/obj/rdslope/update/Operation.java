package com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update;

import java.sql.Connection;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.IVia;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlopeVia;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.slope.RdSlopeSelector;

/**
 * @author zhaokk 修改坡度信息
 */
public class Operation implements IOperation {

	private Command command;
	private Connection conn;

	public Operation(Command command) {
		this.command = command;

	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		this.handRdSlopeVia(result);
		this.updateRdSlope(result);
		return null;
	}

	/***
	 * 修改坡度表信息
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void updateRdSlope(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {
			boolean isChanged = this.command.getSlope().fillChangeFields(
					content);

			if (isChanged) {
				result.insertObject(this.command.getSlope(), ObjStatus.UPDATE,
						this.command.getSlope().getPid());
			}

		}
	}

	/***
	 * 维护坡度接续link信息 1.如果只是修改坡度信息字段 持续link 参数就不需要传值 只需要维护主表坡度的信息 2.如果
	 * 修改退出线或者修改坡度接续link a. 如果退出线修改且和以前值不一致 原有的坡度的接续link需要删除掉 同时新增新的接续link b.
	 * 如果退出线没有修改: 1）如果传入的接续link比原有坡度信息接续link少 则删除坡度这部分少的接续link。
	 * 2）如果传入的接续link比原有坡度信息接续link多 则新增坡度这部分多的接续link。
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void handRdSlopeVia(Result result) throws Exception {

		if (this.command.getOutLinkPid() != 0
				&& this.command.getOutLinkPid() != this.command.getSlope()
						.getLinkPid()) {
			for (IRow row : this.command.getSlope().getSlopeVias()) {
				result.insertObject(row, ObjStatus.DELETE,
						this.command.getPid());
			}
			// 调用特殊打断
			this.breakRelation(result);
			for (int i = 0; i < this.command.getSeriesLinkPids().size(); i++) {
				this.addRdSlope(result, i);
			}

		} else {
			if (this.command.getJson().containsKey("linkPids")) {
				int sourceSize = this.command.getSlope().getSlopeVias().size();
				int currentSize = this.command.getSeriesLinkPids().size();

				if (sourceSize > currentSize) {
					for (int i = currentSize; i < sourceSize; i++) {
						result.insertObject(this.command.getSlope()
								.getSlopeVias().get(i), ObjStatus.DELETE,
								this.command.getPid());

					}
				}
				if (sourceSize < currentSize) {
					for (int i = sourceSize; i < currentSize; i++) {
						this.addRdSlope(result, i);
					}
				}

			}
		}
	}

	/***
	 * 调用创建130米特殊打断功能
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void breakRelation(Result result) throws Exception {
		JSONObject json = new JSONObject();
		JSONObject breakJson = new JSONObject();
		breakJson.put("dbId", this.command.getDbId());
		json.put("linkPid", this.command.getOutLinkPid());
		json.put("nodePid", this.command.getSlope().getNodePid());
		json.put("length", this.command.getLength());
		json.put("linkPids", this.command.getSeriesLinkPids());
		breakJson.put("data", json);
		com.navinfo.dataservice.engine.edit.operation.obj.rdslope.create.Command command = new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.create.Command(
				breakJson, this.command.getRequester());
		com.navinfo.dataservice.engine.edit.operation.obj.rdslope.create.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdslope.create.Operation(

		command, conn);
		RdLink link = operation.breakRelation(result);
		if (link != null) {
			if (this.command.getSeriesLinkPids().size() > 0) {
				this.command.getSeriesLinkPids().set(
						this.command.getSeriesLinkPids().size() - 1,
						link.getPid());
			} else {
				this.command.getContent().put("linkPid", link.getPid());
			}
		}

	}

	/***
	 * 新增坡度接续link信息
	 * 
	 * @param result
	 * @param seqNum
	 */
	private void addRdSlope(Result result, int seqNum) {
		RdSlopeVia rdSlopeVia = new RdSlopeVia();
		rdSlopeVia.setSlopePid(this.command.getPid());
		rdSlopeVia.setLinkPid(this.command.getSeriesLinkPids().get(seqNum));
		rdSlopeVia.setSeqNum(seqNum + 1);
		result.insertObject(rdSlopeVia, ObjStatus.INSERT, this.command.getPid());
	}

	/**
	 * 打断link维护坡度
	 * 
	 * @param nodeLinkRelation
	 *            平滑修行分离点挂接线信息
	 * @param newLinks
	 *            新生成的link组
	 * @param result
	 * @param linkPid
	 * @throws Exception
	 */
	public void breakRdLink(Map<RdNode, List<RdLink>> nodeLinkRelation,
			int linkPid, List<RdLink> newLinks, Result result) throws Exception {
		if (conn == null) {
			return;
		}
		RdSlopeSelector selector = new RdSlopeSelector(conn);
		// 如果打断的是退出线link
		List<RdSlope> slopes = selector.loadByOutLink(linkPid, true);
		// 如果删除的是接续 LINK需要维护 坡度接续 LINK 表
		List<RdSlopeVia> rdSlopeVias = selector.loadBySeriesLink(linkPid, true);

		if (null != nodeLinkRelation && !nodeLinkRelation.isEmpty()) {
			List<Integer> catchIds = new ArrayList<>();
			for (Map.Entry<RdNode, List<RdLink>> entry : nodeLinkRelation
					.entrySet()) {
				if (entry.getValue().size() > 1) {
					catchIds.add(entry.getKey().pid());
				}
			}
			List<RdSlope> nodeSlopes = selector.loadByNodePids(catchIds, true);
			catchIds = new ArrayList<>();
			for (RdSlope s : nodeSlopes) {
				catchIds.add(s.pid());
				result.insertObject(s, ObjStatus.DELETE, s.pid());
			}
			if (!catchIds.isEmpty()) {
				Iterator<RdSlope> iterator = slopes.iterator();
				while (iterator.hasNext()) {
					RdSlope slope = iterator.next();
					if (catchIds.contains(slope.pid())) {
						iterator.remove();
					}
				}
				Iterator<RdSlopeVia> viaIterator = rdSlopeVias.iterator();
				while (viaIterator.hasNext()) {
					RdSlopeVia via = viaIterator.next();
					if (catchIds.contains(via.getSlopePid())) {
						viaIterator.remove();
					}
				}
			}

		}

		for (RdSlope slope : slopes) {

			TreeMap<Integer, IVia> newVias = new TreeMap<Integer, IVia>();

			TreeMap<Integer, IVia> nextVias = new TreeMap<Integer, IVia>();

			RdLink preLink = null;
			List<RdLink> slinks = new ArrayList<RdLink>();
			for (RdLink link : newLinks) {
				if (slope.getNodePid() == link.getsNodePid()
						|| slope.getNodePid() == link.geteNodePid()) {
					preLink = link;
					break;
				}
			}
			slinks.add(preLink);
			this.caleRelationRdLink(slinks, newLinks, preLink);
			// 插入新的接续link信息
			for (int i = 0; i < slinks.size(); i++) {
				if (i == 0) {
					slope.changedFields()
							.put("linkPid", slinks.get(i).getPid());
					result.insertObject(slope, ObjStatus.UPDATE, slope.getPid());
					continue;
				}
				RdSlopeVia newVia = this.addRdslopeVia(slope.getPid(), slinks
						.get(i).getPid(), i, result);
				newVias.put(newVia.getSeqNum(), newVia);

			}
			// 维护原有的link信息
			nextVias = this.caleRdSlopeVia(slope, 0, newLinks.size(), result);

			if (newVias.size() > 0) {

				RdSlopeVia newVia = (RdSlopeVia) newVias.firstEntry()
						.getValue();
				String tableNamePid = newVia.tableName() + slope.getPid();

				result.breakVia(tableNamePid, 0, newVias, nextVias);
			}

		}

		// 如果打断的是接续线link
		for (RdSlopeVia via : rdSlopeVias) {

			TreeMap<Integer, IVia> newVias = new TreeMap<Integer, IVia>();

			TreeMap<Integer, IVia> nextVias = new TreeMap<Integer, IVia>();

			RdLink preLink = selector.loadBySeriesRelationLink(
					via.getSlopePid(), via.getSeqNum() - 1, true);
			RdSlope slope = (RdSlope) selector
					.loadById(via.getSlopePid(), true);
			List<RdLink> links = new ArrayList<RdLink>();

			if (preLink == null) {
				preLink = selector.loadByOutLinkBySlopePid(via.getSlopePid(),
						true);
			}
			this.caleRelationRdLink(links, newLinks, preLink);
			// 插入新的接续link信息
			for (int i = 0; i < links.size(); i++) {
				if (i == 0) {
					via.changedFields().put("linkPid", links.get(i).getPid());
					result.insertObject(via, ObjStatus.UPDATE,
							via.getSlopePid());
					newVias.put(via.getSeqNum(), via);
					continue;
				}
				RdSlopeVia newVia = this.addRdslopeVia(via.getSlopePid(), links
						.get(i).getPid(), via.getSeqNum() + i, result);
				newVias.put(newVia.getSeqNum(), newVia);
			}
			// 当link.size() == 0 时说明发生了节点分离 根据节点分离原则 分离点之后的接续link全部删除
			if (links.size() == 0) {
				Iterator<IRow> iterator = slope.getSlopeVias().iterator();
				while (iterator.hasNext()) {
					RdSlopeVia v = (RdSlopeVia) iterator.next();
					if (v.getSeqNum() >= via.getSeqNum()) {
						result.insertObject(v, ObjStatus.DELETE,
								v.getSlopePid());
						iterator.remove();
					}
				}
			}
			// 维护原有的link信息
			nextVias = this.caleRdSlopeVia(slope, via.getSeqNum(),
					newLinks.size(), result);

			String tableNamePid = via.tableName() + via.getSlopePid();

			result.breakVia(tableNamePid, via.getSeqNum(), newVias, nextVias);

		}
	}

	/***
	 * 增加坡度接续线信息
	 * 
	 * @param slopePid
	 * @param linkPid
	 * @param seqNum
	 * @param result
	 */
	private RdSlopeVia addRdslopeVia(int slopePid, int linkPid, int seqNum,
			Result result) {
		RdSlopeVia via = new RdSlopeVia();
		via.setSlopePid(slopePid);
		via.setLinkPid(linkPid);
		via.setSeqNum(seqNum);
		result.insertObject(via, ObjStatus.INSERT, via.getSlopePid());
		return via;
	}

	/***
	 * 维护坡度接续links信息
	 * 
	 * @param slope
	 * @param seqNum
	 * @param size
	 * @param result
	 */
	private TreeMap<Integer, IVia> caleRdSlopeVia(RdSlope slope, int seqNum,
			int size, Result result) {

		TreeMap<Integer, IVia> nextVias = new TreeMap<Integer, IVia>();
		for (IRow row : slope.getSlopeVias()) {
			RdSlopeVia rdSlopeVia = (RdSlopeVia) row;
			if (rdSlopeVia.getSeqNum() > seqNum) {
				rdSlopeVia.changedFields().put("seqNum",
						rdSlopeVia.getSeqNum() + size - 1);
				result.insertObject(rdSlopeVia, ObjStatus.UPDATE,
						rdSlopeVia.getSlopePid());
				nextVias.put(rdSlopeVia.getSeqNum(), rdSlopeVia);
			}
		}
		return nextVias;
	}

	/***
	 * 获取和退出线联通link组
	 * 
	 * @param resultLinks
	 *            新组成挂接link
	 * @param sourceLinks
	 *            原挂接link组
	 * @param preLink
	 */
	private void caleRelationRdLink(List<RdLink> resultLinks,
			List<RdLink> sourceLinks, RdLink preLink) {
		if (resultLinks.size() < sourceLinks.size()) {
			for (RdLink link : sourceLinks) {
				if (resultLinks.contains(link)) {
					continue;
				}
				if (preLink.getsNodePid() == link.getsNodePid()
						|| preLink.getsNodePid() == link.geteNodePid()
						|| preLink.geteNodePid() == link.getsNodePid()
						|| preLink.geteNodePid() == link.geteNodePid()) {
					resultLinks.add(link);
					caleRelationRdLink(resultLinks, sourceLinks, link);
					break;
				}
			}
		}
	}

	/**
	 * 分离节点，暂不考虑库跨图幅的情况
	 * 
	 * @param link
	 * @param nodePid
	 * @param rdlinks
	 * @param result
	 * @throws Exception
	 */
	public void departNode(RdLink link, int nodePid, List<RdLink> rdlinks,
			Result result) throws Exception {

		int linkPid = link.getPid();
		RdSlopeSelector selector = new RdSlopeSelector(this.conn);

		// node关联的RdSlope
		List<RdSlope> slopes = selector.loadByNode(nodePid, true);

		for (RdSlope slope : slopes) {

			result.insertObject(slope, ObjStatus.DELETE, slope.getPid());
		}

		// link为退出线的RdSlope
		slopes = selector.loadByOutLink(linkPid, true);

		for (RdSlope slope : slopes) {

			if (slope.getNodePid() == nodePid) {
				continue;
			}

			// 只删除接续link
			for (IRow row : slope.getSlopeVias()) {
				result.insertObject(row, ObjStatus.DELETE, slope.getPid());
			}
		}

		// link为接续link的RdSlope
		slopes = selector.loadByViaLink(linkPid, true);
		if (slopes.size() == 0) {
			return;
		}
		RdLinkSelector RdLinkSelector = new RdLinkSelector(this.conn);
		for (RdSlope slope : slopes) {
			int currSeqNum = 1;
			for (IRow Row : slope.getSlopeVias()) {
				RdSlopeVia via = (RdSlopeVia) Row;
				if (via.getLinkPid() == linkPid) {
					currSeqNum = via.getSeqNum();
					break;
				}
			}
			RdLink preLink = null;
			if (currSeqNum == 1) {
				preLink = (RdLink) RdLinkSelector.loadById(slope.getLinkPid(),
						true, true);
			} else {
				for (IRow Row : slope.getSlopeVias()) {
					RdSlopeVia via = (RdSlopeVia) Row;
					if (via.getSeqNum() == currSeqNum - 1) {
						preLink = (RdLink) RdLinkSelector.loadById(
								via.getLinkPid(), true, true);
						break;
					}
				}
			}
			int flagSeqNum = currSeqNum;
			if (preLink.getsNodePid() == nodePid
					|| preLink.geteNodePid() == nodePid) {
				flagSeqNum = currSeqNum - 1;
			}
			for (IRow row : slope.getSlopeVias()) {
				RdSlopeVia via = (RdSlopeVia) row;
				if (via.getSeqNum() > flagSeqNum) {
					result.insertObject(row, ObjStatus.DELETE, slope.getPid());
				}
			}
		}
	}

	public static void main(String[] args) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("111", "gggg");
		jsonObject.put("111", "KKKKK");
		System.out.println(jsonObject);
	}
}
