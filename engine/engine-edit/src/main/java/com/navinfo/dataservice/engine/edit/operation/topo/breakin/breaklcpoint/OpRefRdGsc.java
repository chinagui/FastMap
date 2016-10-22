package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breaklcpoint;

import java.sql.Connection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.lc.LcLink;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
import com.navinfo.dataservice.dao.glm.selector.lc.LcLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.RdGscOperateUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONObject;

public class OpRefRdGsc implements IOperation {

	private Command command;

	private Result result;

	private Connection connection;

	public OpRefRdGsc(Command command, Connection connection) {
		this.command = command;

		this.connection = connection;
	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		List<RdGsc> rdGscList = command.getRdGscs();

		if (CollectionUtils.isNotEmpty(rdGscList)) {
			this.handleRdGsc(rdGscList);
		}

		return null;
	}

	// 处理立交
	private void handleRdGsc(List<RdGsc> list) throws Exception {

		for (RdGsc rr : list) {

			List<IRow> rdGscLinkList = rr.getLinks();

			// 判断是否是自相交立交
			boolean isSelfGsc = RdGscOperateUtils.checkIsSelfInter(rdGscLinkList);

			if (isSelfGsc) {
				handleSelfGscOnBreak(rr);
			} else {
				handleNotSelfGscOnBreak(rr);
			}

		}
	}
	
	/**
	 * 打断LINK后维护自相交的立交关系
	 * @param rr 立交对象
	 * @throws Exception
	 */
	private void handleSelfGscOnBreak(RdGsc rr) throws Exception {
		
		Geometry gscGeo = rr.getGeometry();

		RdGscLink gscLink = (RdGscLink) rr.getLinks().get(0);

		LcLinkSelector linkSelector = new LcLinkSelector(connection);

		LcLink link = (LcLink) linkSelector.loadById(gscLink.getLinkPid(), true);

		JSONObject geojson = GeoTranslator.jts2Geojson(link.getGeometry());
		
		Geometry point = GeoTranslator.transform(command.getPoint(), 100000, 0);

		int type = RdGscOperateUtils.calCoordinateBySelfInter(geojson, gscGeo,point);

		// 每个立交至少由两条线组成，循环遍历每条link
		for (int i = 0; i < rr.getLinks().size(); i++) {
			RdGscLink rdGscLink = (RdGscLink) rr.getLinks().get(i);

			// 找到打断的那条link
			if (rdGscLink.getLinkPid() == command.getLinkPid()) {
				// 打断后，立交的link和link序号需要重新计算

				List<Integer> shpSeqNum = null;

				JSONObject updateContent = new JSONObject();

				LcLink tmpLink = null;

				switch (type) {
				case 1:
					tmpLink = command.getsLcLink();
					break;
				case 2:
					if (i == 0) {
						tmpLink = command.getsLcLink();
					} else {
						tmpLink = command.geteLcLink();
					}
					break;
				case 3:
					tmpLink = command.getsLcLink();
					break;

				default:
					throw new Exception("打断LINK失败：自相交LINK的立交关系维护失败");
				}
				
				//截取精度，防止位置序号计算错误
				Geometry tmpLinkGeo = GeoTranslator.transform(tmpLink.getGeometry(), 1, 0);
				
				shpSeqNum = RdGscOperateUtils.calcShpSeqNum(gscGeo, tmpLinkGeo.getCoordinates());
				
				updateContent.put("shpSeqNum", shpSeqNum.get(0));

				updateContent.put("linkPid", tmpLink.getPid());

				boolean changed = rdGscLink.fillChangeFields(updateContent);

				if (changed) {
					result.insertObject(rdGscLink, ObjStatus.UPDATE, rdGscLink.parentPKValue());
				}
			}
		}

	}

	/**
	 * 处理非自相交的立交的组成线被打断
	 * 
	 * @param rdGsc 立交对象
	 * @throws Exception
	 */
	private void handleNotSelfGscOnBreak(RdGsc rdGsc) throws Exception {
		// 每个立交至少由两条线组成，循环遍历每条link
		for (IRow row : rdGsc.getLinks()) {
			RdGscLink rdGscLink = (RdGscLink) row;

			// 找到打断的那条link
			if (rdGscLink.getLinkPid() == command.getLinkPid()) {

				Geometry link1Geo = command.getsLcLink().getGeometry();

				Geometry link2Geo = command.geteLcLink().getGeometry();

				LcLink link = null;

				// 当立交和新生成的link距离为0，代表该新生成的link为立交的组成link
				if (rdGsc.getGeometry().distance(link1Geo) == 0) {
					link = command.getsLcLink();
				}
				if (rdGsc.getGeometry().distance(link2Geo) == 0) {
					link = command.geteLcLink();
				}

				if (link != null) {
					// 打断后，立交的link和link序号需要重新计算
					List<Integer> shpSeqNum = RdGscOperateUtils.calcShpSeqNum(rdGsc.getGeometry(), link.getGeometry().getCoordinates());

					JSONObject updateContent = new JSONObject();

					updateContent.put("shpSeqNum", shpSeqNum.get(0));

					updateContent.put("linkPid", link.getPid());

					boolean changed = rdGscLink.fillChangeFields(updateContent);

					if (changed) {
						result.insertObject(rdGscLink, ObjStatus.UPDATE, rdGscLink.parentPKValue());
					}
				}

			}
		}
	}
}
