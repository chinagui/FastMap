package com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.selector.rd.gsc.RdGscSelector;

/**
 * 
* @Title: Operation.java 
* @Description: 删除立交操作类
* @author 张小龙   
* @date 2016年4月18日 下午3:06:03 
* @version V1.0
 */
public class Operation implements IOperation {

	private RdGsc rdGsc;

	public Operation(Command command, RdGsc rdGsc) {

		this.rdGsc = rdGsc;

	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(rdGsc, ObjStatus.DELETE, rdGsc.pid());
				
		return null;
	}
	
	/**
	 * 删除link对立交的删除影响
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getDeleteRdGscInfectData(int linkPid,Connection conn) throws Exception {
		
		RdGscSelector selector = new RdGscSelector(conn);

		List<RdGsc> rdGscList = selector.loadRdGscLinkByLinkPid(linkPid, "RD_LINK", true);
		
		List<AlertObject> alertList = new ArrayList<>();

		for (RdGsc rdGsc : rdGscList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdGsc.objType());

			alertObj.setPid(rdGsc.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
}
