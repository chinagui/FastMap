package com.navinfo.dataservice.engine.edit.operation.topo.repair.repaircmglink;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildfaceSelector;
import com.navinfo.dataservice.engine.edit.operation.topo.repair.repaircmglink.Command;

import net.sf.json.JSONObject;

public class Check {
	//背景：前检查“不允许对构成面的Link的端点处形状点，进行修形操作”
	public void PERMIT_MODIFICATE_POLYGON_ENDPOINT(Command command, Connection conn) throws Exception {
		int linkPid = command.getCmglink().getPid();
		CmgBuildfaceSelector selector = new CmgBuildfaceSelector(conn);
		List<CmgBuildface> faces = selector.listTheAssociatedFaceOfTheLink(linkPid, false);

		if(command.getCatchInfos()==null){
			return;
		}
		
		for (int i = 0; i < command.getCatchInfos().size(); i++) {
			JSONObject obj = command.getCatchInfos().getJSONObject(i);
			int nodePid = obj.getInt("nodePid");
			if (faces.size() > 0 && nodePid != 0) {
				throwException("不允许对构成面的Link的端点处形状点，进行修形操作");
			}
		}
	}
	
	private void throwException(String msg) throws Exception {
		throw new Exception(msg);
	}
}
