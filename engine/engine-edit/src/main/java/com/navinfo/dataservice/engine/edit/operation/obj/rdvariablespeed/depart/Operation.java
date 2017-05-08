package com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.depart;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeedVia;
import com.navinfo.dataservice.dao.glm.selector.rd.variablespeed.RdVariableSpeedSelector;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;

/**
 * Created by chaixin on 2016/10/9 0009.
 */
public class Operation {
    private Connection conn;

    public Operation(Connection conn) {
        this.conn = conn;
    }

    
    /**
     * 维护上下线分离对可变限速的影响
     *
     * @param links      分离线
     * @param leftLinks  分离后左线
     * @param rightLinks 分离后右线
     * @param result     结果集
     * @return
     * @throws Exception
     */
    public String updownDepart(List<RdLink> links, Result result) throws Exception {
    	
        RdVariableSpeedSelector selector = new RdVariableSpeedSelector(conn);

		Map<Integer, RdVariableSpeed> delRdVariableSpeeds = new HashMap<Integer, RdVariableSpeed>();

		Map<Integer, RdVariableSpeed> updateRdVariableSpeeds = new HashMap<Integer, RdVariableSpeed>();
		
		Set<Integer> linkPids=new HashSet<Integer>();

		for (RdLink link : links) {
			
			linkPids.add(link.getPid());
			// 进入线或退出线
			List<RdVariableSpeed> variableSpeeds = selector
					.loadRdVariableSpeedByLinkPid(link.pid(), true);

			for (RdVariableSpeed variableSpeed : variableSpeeds) {
				
				delRdVariableSpeeds.put(variableSpeed.getPid(), variableSpeed);
			}
			
			// 接续link
			variableSpeeds = selector.loadRdVariableSpeedByViaLinkPid(
					link.pid(), true);

			for (RdVariableSpeed variableSpeed : variableSpeeds) {
				
				updateRdVariableSpeeds.put(variableSpeed.getPid(),
						variableSpeed);
			}
		}

		// 2.当目标link上的点已经参与制作可变限速
		List<Integer> nodePids = CalLinkOperateUtils.calNodePids(links);

		if (!nodePids.isEmpty()) {

			List<RdVariableSpeed> variableSpeeds = selector
					.loadRdVariableSpeedByNodePids(nodePids, true);

			for (RdVariableSpeed variableSpeed : variableSpeeds) {
				delRdVariableSpeeds.put(variableSpeed.getPid(), variableSpeed);
			}
		}		
		
		for (RdVariableSpeed variableSpeed : delRdVariableSpeeds.values())
		{
            result.insertObject(variableSpeed, ObjStatus.DELETE, variableSpeed.pid());
		}
		
		for (RdVariableSpeed variableSpeed : updateRdVariableSpeeds.values()) {
			
			if (delRdVariableSpeeds.containsKey(variableSpeed.pid())) {
				continue;
			}
			
			int minDelSeqNum = Integer.MAX_VALUE;

			for (IRow row : variableSpeed.getVias()) {

				RdVariableSpeedVia via = (RdVariableSpeedVia) row;

				if (linkPids.contains(via.getLinkPid())
						&& via.getSeqNum() < minDelSeqNum) {
					
					minDelSeqNum = via.getSeqNum();
				}
			}

			for (IRow row : variableSpeed.getVias()) {

				RdVariableSpeedVia via = (RdVariableSpeedVia) row;

				if (via.getSeqNum() >= minDelSeqNum) {

					result.insertObject(via, ObjStatus.DELETE,
							variableSpeed.pid());
				}
			}
		}
	
        return "";
    }
}
