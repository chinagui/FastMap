package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;

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

	// 处理立交
	private void handleAdAdmin(List<AdAdmin> list) throws Exception {

		for (AdAdmin ad : list) {

			Map<String, Object> changedFields = ad.changedFields();
			// 代表点和线1的距离
			double line1Length = ad.getGeometry().distance(command.getLink1().getGeometry());

			// 代表点和线2的距离
			double line2Length = ad.getGeometry().distance(command.getLink2().getGeometry());

			// 代表点到线2的距离小于到线1的距离
			if (line1Length < line2Length) {
				changedFields.put("linkPid", command.getLink1().getPid());
			} else if (line2Length < line1Length) {
				changedFields.put("linkPid", command.getLink2().getPid());
			} else {
				// 代表点距离两条线距离相等，这种情况几率很小，随机取一条即可
				double pid[] = { command.getLink1().getPid(), command.getLink2().getPid() };
				changedFields.put("linkPid", pid[new Random().nextInt(2)]);
			}
			
			result.insertObject(ad, ObjStatus.UPDATE, ad.pid());
		}
	}

}
