package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.vividsolutions.jts.geom.Geometry;

public class OpRefAdAdmin implements IOperation {

	private Command command;

	private Result result;

	public OpRefAdAdmin(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		List<AdAdmin> adAdminList = command.getAdAdmins();

		if (CollectionUtils.isNotEmpty(adAdminList)) {
			this.handleAdAdmin(adAdminList);
		}

		return null;
	}

	// 处理行政区划代表点
	private void handleAdAdmin(List<AdAdmin> list) throws Exception {

		for (AdAdmin ad : list) {

			Map<String, Object> changedFields = ad.changedFields();

			List<RdLink> linkList = command.getNewLinks();

			if (CollectionUtils.isNotEmpty(linkList)) {
				Geometry adGeo = ad.getGeometry();

				// 默认第一个link最近
				int guideLinkPid = linkList.get(0).getPid();
				double line1Length = adGeo.distance(linkList.get(0).getGeometry());

				// 计算代表点和生成的线最近的link
				for (int i = 1; i < linkList.size(); i++) {
					RdLink link = linkList.get(i);

					// 代表点和线的距离
					double line2Length = ad.getGeometry().distance(link.getGeometry());

					if (line2Length < line1Length) {
						guideLinkPid = link.getPid();

						line1Length = line2Length;
					}
				}
				changedFields.put("linkPid", guideLinkPid);
				result.insertObject(ad, ObjStatus.UPDATE, ad.pid());
			}
		}
	}

}
