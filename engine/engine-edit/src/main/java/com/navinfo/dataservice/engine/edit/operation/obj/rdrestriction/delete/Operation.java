package com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;

public class Operation implements IOperation {

	private RdRestriction restrict;

	public Operation()
	{
	}
	
	public Operation(Command command, RdRestriction restrict) {
		this.restrict = restrict;
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(restrict, ObjStatus.DELETE, restrict.pid());

		return null;
	}

	/**
	 * 删除link对交限的更新影响分析
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getUpdateResInfectData(int linkPid, Connection conn) throws Exception {
		RdRestrictionSelector restriction = new RdRestrictionSelector(conn);
		// 获取退出线为该link

		List<RdRestriction> restrictions2 = restriction.loadRdRestrictionByOutLinkPid(linkPid, true);

		List<RdRestriction> outLinkUpdateResList = new ArrayList<>();

		for (RdRestriction rdRestriction : restrictions2) {
			List<IRow> details = rdRestriction.getDetails();

			if (details.size() > 1) {
				outLinkUpdateResList.add(rdRestriction);
			}
		}

		List<AlertObject> alertList = new ArrayList<>();

		for (RdRestriction rdRestriction : outLinkUpdateResList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdRestriction.objType());

			alertObj.setPid(rdRestriction.getPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			alertList.add(alertObj);
		}

		return alertList;
	}

	/**
	 * 删除进入link对交限的删除影响分析
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteInLinkResInfectData(int linkPid, Connection conn) throws Exception {
		RdRestrictionSelector restriction = new RdRestrictionSelector(conn);

		List<RdRestriction> restrictions = restriction.loadRdRestrictionByLinkPid(linkPid, true);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdRestriction rdRestriction : restrictions) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdRestriction.objType());

			alertObj.setPid(rdRestriction.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}

	/**
	 * 删除退出link对交限的删除影响分析
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteOutLinkResInfectData(int linkPid, Connection conn) throws Exception {

		RdRestrictionSelector restriction = new RdRestrictionSelector(conn);
		// 获取退出线为该link

		List<RdRestriction> restrictions2 = restriction.loadRdRestrictionByOutLinkPid(linkPid, true);

		List<RdRestriction> outLinkDeleteResList = new ArrayList<>();

		for (RdRestriction rdRestriction : restrictions2) {
			List<IRow> details = rdRestriction.getDetails();

			if (details.size() == 1) {
				outLinkDeleteResList.add(rdRestriction);
			}
		}

		List<AlertObject> alertList = new ArrayList<>();

		for (RdRestriction rdRestriction : outLinkDeleteResList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdRestriction.objType());

			alertObj.setPid(rdRestriction.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}

	/**
	 * 删除路口对交限的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteCrossRestrictInfectData(List<RdRestriction> restrictions) throws Exception {

		List<AlertObject> alertList = new ArrayList<>();

		for (RdRestriction restriction : restrictions) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(restriction.objType());

			alertObj.setPid(restriction.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			alertList.add(alertObj);
		}

		return alertList;
	}
}
