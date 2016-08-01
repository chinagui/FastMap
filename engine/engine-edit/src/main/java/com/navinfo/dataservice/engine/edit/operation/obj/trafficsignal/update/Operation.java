package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.update;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.rd.trafficsignal.RdTrafficsignalSelector;

import net.sf.json.JSONObject;

/**
 * 
 * @ClassName: Operation
 * @author Zhang Xiaolong
 * @date 2016年7月20日 下午7:39:27
 * @Description: TODO
 */
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

		RdTrafficsignal rdTrafficsignal = this.command.getRdTrafficsignal();

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {
				result.insertObject(rdTrafficsignal, ObjStatus.DELETE, rdTrafficsignal.pid());

				return null;
			} else {
				boolean isChanged = rdTrafficsignal.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(rdTrafficsignal, ObjStatus.UPDATE, rdTrafficsignal.pid());
				}
			}
		}

		return null;
	}

	/**
	 * 打断link维护信号灯
	 * 
	 * @param oldLinkPid
	 *            被打断的link
	 * @param newLinks
	 *            新生成的link组
	 * @param result
	 * @param conn
	 * @throws Exception
	 */
	public void breakRdLink(int linkPid, List<RdLink> newLinks, Result result) throws Exception {
		if (conn == null) {
			return;
		}

		RdTrafficsignalSelector selector = new RdTrafficsignalSelector(conn);

		RdTrafficsignal rdTrafficsignal = selector.loadByLinkPid(true,linkPid).get(0);
		
		if(rdTrafficsignal != null)
		{
			for (RdLink link : newLinks) {

				if (link.getsNodePid() == rdTrafficsignal.getNodePid() || link.geteNodePid() == rdTrafficsignal.getNodePid()) {

					rdTrafficsignal.changedFields().put("linkPid", link.getPid());

					result.insertObject(rdTrafficsignal, ObjStatus.UPDATE, rdTrafficsignal.pid());
				}
			}
		}
	}
}
