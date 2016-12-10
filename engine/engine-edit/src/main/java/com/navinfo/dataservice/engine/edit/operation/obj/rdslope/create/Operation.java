package com.navinfo.dataservice.engine.edit.operation.obj.rdslope.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;

import com.navinfo.dataservice.dao.glm.iface.Result;

import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlopeVia;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;

/**
 * 新增坡度信息
 * 
 * @author 赵凯凯
 * 
 */
public class Operation implements IOperation {
	protected Logger log = Logger.getLogger(this.getClass());

	private Command command;
	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		this.createSlope(result);
		return null;
	}

	/**
	 * 新增坡度信息
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void createSlope(Result result) throws Exception {
		// 新增坡度
		RdSlope slope = new RdSlope();
		// 申请Pid
		slope.setPid(PidUtil.getInstance().applyRdSlopePid());
		// 进入点
		slope.setNodePid(this.command.getInNodePid());
		// 退出线
		slope.setLinkPid(this.command.getOutLinkPid());
		if (this.command.getSeriesLinkPids() != null) {

			// 添加坡度接续link信息
			if (this.command.getSeriesLinkPids().size() > 0) {
				// 特殊情况打断
				this.breakRelation(result);
				List<IRow> rdSlopeVias = new ArrayList<IRow>();
				for (int i = 0; i < this.command.getSeriesLinkPids().size(); i++) {
					rdSlopeVias.add(this.addSlopeVia(slope, i));
				}
				slope.setSlopeVias(rdSlopeVias);

			}
		}
		result.insertObject(slope, ObjStatus.INSERT, slope.pid());
	}

	/***
	 * 坡度添加接续link信息
	 * 
	 * @param slope
	 *            坡度信息
	 * @param seqNum
	 *            接续link序号
	 * @return
	 */
	private RdSlopeVia addSlopeVia(RdSlope slope, int seqNum) {
		RdSlopeVia slopeVia = new RdSlopeVia();
		slopeVia.setSlopePid(slope.getPid());
		slopeVia.setLinkPid(this.command.getSeriesLinkPids().get(seqNum));
		slopeVia.setSeqNum(seqNum + 1);
		return slopeVia;
	}

	/**
	 * 坡度打断: 以退出link挂接的第一根link为起始link， 沿着坡度点到坡度退出线的方向追踪计算，
	 * 追踪至退出link和接续link的长度总和大于100米小于150米处停止；
	 * 如果按照link既有的节点计算link长度大于150米，则在长度总和为130米处提示打断点位， 确认后在130米的点位处自动打断
	 * 
	 * @param result
	 * @throws Exception
	 */
	public RdLink breakRelation(Result result) throws Exception {
		RdLink resultLink =null;
		int size = this.command.getSeriesLinkPids().size();
		if (this.command.getLength() > 150) {
			// 在130m出做打断功能
			RdLinkSelector linkSelector = new RdLinkSelector(this.conn);
			// 获取最后接续link前一个link
			RdLink preLink = this.getPreRdLink(size, linkSelector);
			// 获取当前link
			IRow currentRow = linkSelector.loadByIdOnlyRdLink(this.command
					.getSeriesLinkPids().get(size - 1), true);
			RdLink currentlink = (RdLink) currentRow;
			// 获取打断位置长度单位米
			double breakLength = this.getBreaklength(preLink, currentlink);
			// 获取打断新生成的link加入到坡度的接续线
			 resultLink = this.getResultBreakLink(currentlink, preLink,
					breakLength, result);
			this.command.getSeriesLinkPids().set(size - 1, resultLink.getPid());
			

		}
		return resultLink;
	}

	/***
	 * 
	 * @param currentlink
	 * @param breakLength
	 * @param result
	 * @return
	 * @throws Exception
	 */
	private RdLink getResultBreakLink(RdLink currentlink, RdLink preLink,
			double breakLength, Result result) throws Exception {
		// 获取打断的线的几何
		LineString lineString = (LineString) GeoTranslator.transform(
				currentlink.getGeometry(), 0.00001, 5);
		// 获取打断点的位置
		Coordinate coordinate = GeometryUtils.getPointOnLineStringDistance(
				lineString, breakLength);
		// 获取打断后新生成的link
		List<RdLink> links = this.breakLink(result, currentlink.getPid(),
				coordinate);
		RdLink resultLink = null;
		for (RdLink link : links) {
			if (this.isIntersectionLink(preLink, link)) {
				resultLink = link;
				break;
			}
		}
		return resultLink;
	}

	/***
	 * 获取坡度最后接续link前一条link 可能是接续link 可能是退出线
	 * 
	 * @param size
	 * @param selector
	 * @return
	 * @throws Exception
	 */
	private RdLink getPreRdLink(int size, RdLinkSelector selector)
			throws Exception {
		IRow preRow = null;
		if (size == 1) {
			preRow = selector.loadByIdOnlyRdLink(this.command.getOutLinkPid(),
					true);
		} else {
			preRow = selector.loadByIdOnlyRdLink(this.command
					.getSeriesLinkPids().get(size - 2), true);
		}
		return (RdLink) preRow;

	}

	/***
	 * 获取打断的length
	 * 
	 * @param preLink
	 * @param currentlink
	 * @return
	 */
	private double getBreaklength(RdLink preLink, RdLink currentlink) {
		if (currentlink.getsNodePid() == preLink.getsNodePid()
				|| currentlink.getsNodePid() == preLink.geteNodePid()) {
			return currentlink.getLength() - this.command.getLength() +130;
		} else {
			return this.command.getLength() - 130;
		}
	}

	/***
	 * 判断两个link是否有共同nodePid
	 * 
	 * @param preLink
	 * @param nextLink
	 * @return
	 */
	private boolean isIntersectionLink(RdLink preLink, RdLink nextLink) {
		if (nextLink.getsNodePid() == preLink.getsNodePid()
				|| nextLink.getsNodePid() == preLink.geteNodePid()
				|| nextLink.geteNodePid() == preLink.getsNodePid()
				|| nextLink.geteNodePid() == preLink.geteNodePid()) {
			return true;

		}
		return false;
	}

	/**
	 * 
	 * @param result
	 * @param linkPid
	 *            打断link的pid
	 * @param coordinate
	 *            打断的点
	 * @return 返回打断links
	 * @throws Exception
	 */
	private List<RdLink> breakLink(Result result, int linkPid,
			Coordinate coordinate) throws Exception {
		// 组装打断link参数
		JSONObject breakJson = new JSONObject();
		breakJson.put("objId", linkPid);
		breakJson.put("dbId", command.getDbId());
		// 打断点的几何
		JSONObject data = new JSONObject();
		data.put("longitude", coordinate.x);
		data.put("latitude", coordinate.y);
		breakJson.put("data", data);
		// 调用打断API
		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command breakCommand = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Command(
				breakJson, breakJson.toString());
		com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process breakProcess = new com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint.Process(
				breakCommand, conn, result);
		return breakProcess.getInnerRun();
	}
}