package com.navinfo.dataservice.engine.edit.operation.obj.cmg.building.create;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuilding;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildingSelector;

public class Check {
	public void PERMIT_CHECK_NO_REPEAT_FEATURE(Command command,Connection conn) throws Exception{
		for(Integer facePid:command.getFacePids()){
			CmgBuildingSelector selector=new CmgBuildingSelector(conn);
			List<CmgBuilding> buildings=selector.loadCmgBuildingByFacePid(facePid, false);
			
			if(buildings.size()>0){
				throw new Exception("不能重复创建feature");
			}
		}
	}
}
