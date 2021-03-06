package com.navinfo.dataservice.engine.edit.operation.obj.rdslope.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlopeVia;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.slope.RdSlopeSelector;

/***
 * 删除坡度信息
 * 
 * @author zhaokk
 * 
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
		this.deleteRdSlope(result);
		return null;
	}

	private void deleteRdSlope(Result result) {
		result.insertObject(this.command.getSlope(), ObjStatus.DELETE, this.command.getSlope().getPid());
	}

	/**
	 * 删除link维护信息
	 * @param result
	 * @param linkPid
	 * @throws Exception
	 */
	public void deleteByLink(Result result, int linkPid) throws Exception {

		if (conn == null || linkPid == 0) {
			return;
		}
		// 如果删除的是退出线 则对应的坡度被删除
		RdSlopeSelector selector = new RdSlopeSelector(conn);

		List<RdSlope> slopes = selector.loadByOutLink(linkPid, true);

		for (RdSlope slope : slopes) {

			result.insertObject(slope, ObjStatus.DELETE, slope.getPid());
		}
		// 如果删除的是接续 LINK需要维护 坡度接续 LINK 表
		List<RdSlopeVia> rdSlopeVias = selector.loadBySeriesLink(linkPid, true);

		for (RdSlopeVia via : rdSlopeVias) {
			List<IRow> vias = new AbstractSelector(RdSlopeVia.class, conn).loadRowsByParentId(via.getSlopePid(), true);
			for (IRow row : vias) {
				RdSlopeVia slopeVia = (RdSlopeVia) row;
				if (slopeVia.getSeqNum() >= via.getSeqNum()) {
					result.insertObject(slopeVia, ObjStatus.DELETE, slopeVia.getSlopePid());
				}
			}
		}
	}

	/**
	 * 删除link对坡度的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteRdSlopeInfectData(int linkPid, Connection conn) throws Exception {

		RdSlopeSelector selector = new RdSlopeSelector(conn);

		List<RdSlope> slopes = selector.loadByOutLink(linkPid, true);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdSlope slope : slopes) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(slope.objType());

			alertObj.setPid(slope.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}

	/**
	 * 删除link对坡度的更新影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getUpdateRdSlopeInfectData(int linkPid, Connection conn) throws Exception {

		RdSlopeSelector selector = new RdSlopeSelector(conn);

		List<RdSlopeVia> rdSlopeVias = selector.loadBySeriesLink(linkPid, true);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdSlopeVia via : rdSlopeVias) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(via.objType());

			alertObj.setPid(via.getSlopePid());

			alertObj.setStatus(ObjStatus.UPDATE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}

	/**
	 * 删除links时 维护坡度
	 *
	 * @param result
	 * @param linkPids
	 * @throws Exception
	 */
	public void deleteByLinks(Result result, List<Integer> linkPids) throws Exception {

		if (conn == null || linkPids.size() == 0) {

			return;
		}

		RdSlopeSelector selector = new RdSlopeSelector(conn);

		List<RdSlope> slopes = selector.loadByLinks(linkPids, true);

		for (RdSlope slope : slopes) {
			//被删link作为退出线，删除link删除坡度
			if (linkPids.contains(slope.getLinkPid())) {

				result.insertObject(slope, ObjStatus.DELETE, slope.getPid());

				continue;
			}

			int minSeqNum = Integer.MAX_VALUE;

			List<RdSlopeVia> vias = new ArrayList<>();

			for (IRow row : slope.getSlopeVias()) {

				RdSlopeVia slopeVia = (RdSlopeVia) row;

				vias.add(slopeVia);

				if (!linkPids.contains(slopeVia.getLinkPid())) {

					continue;
				}

				if (minSeqNum > slopeVia.getSeqNum()) {

					minSeqNum = slopeVia.getSeqNum();
				}
			}

			//被删link作为接续线，删除最小接续线以及后续接续线
			for (RdSlopeVia via : vias) {

				if (via.getSeqNum() >= minSeqNum) {

					result.insertObject(via, ObjStatus.DELETE, slope.getPid());
				}
			}
		}
	}
}
