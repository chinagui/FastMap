package com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectrouteVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.directroute.RdDirectrouteSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;

public class Operation implements IOperation {

	private Command command;

	private Connection conn = null;

	public Operation(Command command) {
		this.command = command;
	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		msg = deleteRdDirectroute(result, command.getDirectroute());

		return msg;
	}

	public String deleteRdDirectroute(Result result, RdDirectroute directroute) {

		result.insertObject(directroute, ObjStatus.DELETE, directroute.pid());

		return null;
	}

	public void deleteByLink(RdLink oldLink, Result result) throws Exception {

		if (conn == null) {
			return;
		}

		RdDirectrouteSelector selector = new RdDirectrouteSelector(conn);

		
		List<RdDirectroute> directroutes = selector.loadByInOutLink(
				oldLink.getPid(), true);
		// 删除link做进入线或退出线的顺行
		for (RdDirectroute directroute : directroutes) {

			deleteRdDirectroute(result, directroute);
		}
		
		directroutes = selector.loadByPassLink(oldLink.getPid(), true);

		for (RdDirectroute directroute : directroutes) {

			DeletePassLink(directroute, oldLink, result);
		}
	}

	private void DeletePassLink(RdDirectroute directroute, RdLink oldLink,
			Result result) throws Exception {

		if (directroute.getVias().size() == 0) {

			return;
		}

		Map<Integer, List<RdDirectrouteVia>> viaGroupId = new HashMap<Integer, List<RdDirectrouteVia>>();

		for (IRow row : directroute.getVias()) {

			RdDirectrouteVia via = (RdDirectrouteVia) row;

			if (viaGroupId.get(via.getGroupId()) == null) {

				viaGroupId.put(via.getGroupId(),
						new ArrayList<RdDirectrouteVia>());
			}

			viaGroupId.get(via.getGroupId()).add(via);
		}
		
		//1：如果该经过线所属的经过线组对应的顺行信息只有这一组经过线，则删除该顺行信息、顺行信息所包括经过线信息等。
        //2：如果该经过线所属的经过线组对应的顺行信息有多组经过线，则删除该经过线所属经过线组（同组号）信息。
		int groupCount = viaGroupId.size();

		for (int key : viaGroupId.keySet()) {

			List<RdDirectrouteVia> value = viaGroupId.get(key);

			RdDirectrouteVia oldVia = null;

			for (RdDirectrouteVia via : value) {

				if (via.getLinkPid() == oldLink.getPid()) {

					oldVia = via;

					break;
				}
			}

			if (oldVia == null) {
				continue;
			}

			for (RdDirectrouteVia via : value) {

				result.insertObject(via, ObjStatus.DELETE, via.getPid());
			}

			groupCount--;
		}

		if (groupCount == 0) {

			// 清空子表，防止重复删除
			directroute.children().clear();

			result.insertObject(directroute, ObjStatus.DELETE,
					directroute.getPid());
		}
	}
}
