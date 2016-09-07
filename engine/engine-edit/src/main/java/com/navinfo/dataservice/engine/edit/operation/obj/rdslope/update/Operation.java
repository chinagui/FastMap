package com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlopeVia;
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

	@Override
	public String run(Result result) throws Exception {
		this.updateRdSlope(result);
		this.handRdSlopeVia(result);
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
	 */
	private void handRdSlopeVia(Result result) {
		if (this.command.getSeriesLinkPids() != null) {
			if (this.command.getOutLinkPid() != 0
					&& this.command.getOutLinkPid() != this.command.getSlope()
							.getLinkPid()) {
				for (IRow row : this.command.getSlope().getSlopeVias()) {
					result.insertObject(row, ObjStatus.DELETE,
							this.command.getPid());
				}
				for (int i = 0; i < this.command.getSeriesLinkPids().size(); i++) {
					this.addRdSlope(result, i + 1);
				}

			} else {
				int sourceSize = this.command.getSlope().getSlopeVias().size();
				int currentSize = this.command.getSeriesLinkPids().size();

				if (sourceSize > currentSize) {
					for (int i = currentSize; i <= sourceSize; i++) {
						result.insertObject(this.command.getSlope()
								.getSlopeVias().get(i), ObjStatus.DELETE,
								this.command.getPid());

					}
				}
				if (sourceSize < currentSize) {
					for (int i = sourceSize; i <= currentSize; i++) {
						this.addRdSlope(result, i + 1);
					}
				}

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
		rdSlopeVia.setSeqNum(seqNum);
		result.insertObject(rdSlopeVia, ObjStatus.INSERT, this.command.getPid());
	}

	/**
	 * 打断link维护坡度
	 * 
	 * @param oldLinkPid
	 *            被打断的link
	 * @param newLinks
	 *            新生成的link组
	 * @param result
	 * @param conn
	 * @throws Exception
	 */
	public void breakRdLink(int linkPid, List<RdLink> newLinks, Result result)
			throws Exception {
		if (conn == null) {
			return;
		}
		RdSlopeSelector selector = new RdSlopeSelector(conn);
		// 如果打断的是退出线link
		List<RdSlope> slopes = selector.loadByOutLink(linkPid, true);
		// 如果删除的是接续 LINK需要维护 坡度接续 LINK 表
		List<RdSlopeVia> rdSlopeVias = selector.loadBySeriesLink(linkPid, true);

		for (RdSlope slope : slopes) {
			RdLink preLink = null;
			List<RdLink> slinks = new ArrayList<RdLink>();
			for (RdLink link : newLinks) {
				if (slope.getNodePid() == link.getsNodePid()
						|| slope.getNodePid() == link.geteNodePid()) {
					preLink = link;
					break;
				}
			}
			this.caleRelationRdLink(slinks, newLinks, preLink);
			// 插入新的接续link信息
			for (int i = 0; i < slinks.size(); i++) {
				if (i == 0) {
					slope.changedFields()
							.put("linkPid", slinks.get(i).getPid());
					result.insertObject(slope, ObjStatus.UPDATE, slope.getPid());
					continue;
				}
				this.addRdslopeVia(slope.getPid(), slinks.get(i).getPid(), i,
						result);

			}
			// 维护原有的link信息
			this.caleRdSlopeVia(slope, 0, newLinks.size(), result);
		}
		// 如果打断的是接续线link
		for (RdSlopeVia via : rdSlopeVias) {
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
					continue;
				}
				this.addRdslopeVia(via.getSlopePid(), links.get(i).getPid(),
						via.getSeqNum() + i, result);
			}
			// 维护原有的link信息
			this.caleRdSlopeVia(slope, via.getSeqNum(), newLinks.size(), result);

		}
	}

	private void addRdslopeVia(int slopePid, int linkPid, int seqNum,
			Result result) {
		RdSlopeVia via = new RdSlopeVia();
		via.setSlopePid(slopePid);
		via.setLinkPid(linkPid);
		via.setSeqNum(seqNum);
		result.insertObject(via, ObjStatus.INSERT, via.getSlopePid());
	}

	/***
	 * 维护坡度接续links信息
	 * 
	 * @param slope
	 * @param seqNum
	 * @param size
	 * @param result
	 */
	private void caleRdSlopeVia(RdSlope slope, int seqNum, int size,
			Result result) {
		for (IRow row : slope.getSlopeVias()) {
			RdSlopeVia rdSlopeVia = (RdSlopeVia) row;
			if (rdSlopeVia.getSeqNum() > seqNum) {
				rdSlopeVia.changedFields().put("seqNum",
						rdSlopeVia.getSeqNum() + size - 1);
				result.insertObject(rdSlopeVia, ObjStatus.UPDATE,
						rdSlopeVia.getSlopePid());
			}
		}
	}

	/***
	 * 获取和退出线联通link组
	 * 
	 * @param resultLinks
	 * @param sourceLinks
	 * @param preLink
	 */
	private void caleRelationRdLink(List<RdLink> resultLinks,
			List<RdLink> sourceLinks, RdLink preLink) {
		while (resultLinks.size() < sourceLinks.size()) {
			for (RdLink link : sourceLinks) {

				if (resultLinks.contains(link)) {
					continue;
				}
				if (preLink.getsNodePid() == link.getsNodePid()
						|| preLink.getsNodePid() == link.geteNodePid()
						|| preLink.geteNodePid() == link.getsNodePid()
						|| preLink.geteNodePid() == link.geteNodePid()) {
					resultLinks.add(link);
					preLink = link;
					break;
				}

			}
		}
	}

}
