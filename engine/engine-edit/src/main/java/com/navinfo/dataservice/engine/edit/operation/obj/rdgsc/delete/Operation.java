package com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGsc;
import com.navinfo.dataservice.dao.glm.model.rd.gsc.RdGscLink;
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

	private Connection conn;

	public Operation(Connection conn) {
		this.conn = conn;
	}

	public Operation(Command command, RdGsc rdGsc) {

		this.rdGsc = rdGsc;

	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(rdGsc, ObjStatus.DELETE, rdGsc.pid());

		return null;
	}

	/**
	 * 删除link维护立交（包含多线立交）
	 * 
	 * @param linkPidList
	 * @param result
	 * @throws Exception
	 */
	public void deleteByLinkPid(List<? extends IRow> linkRow, Result result) throws Exception {
		List<RdGsc> allDelRdGscList = new ArrayList<>();

		List<RdGscLink> allDelRdGscLinkList = new ArrayList<>();
		
		List<RdGscLink> updateLevelGscLinkList = new ArrayList<>();

		// 计算需要删除主表和子表的立交数据
		calcDelLinkOnGsc(linkRow, allDelRdGscList, allDelRdGscLinkList,updateLevelGscLinkList);

		for (RdGsc gsc : allDelRdGscList) {
			result.insertObject(gsc, ObjStatus.DELETE, gsc.getPid());
		}

		for (RdGscLink gscLink : allDelRdGscLinkList) {
			result.insertObject(gscLink, ObjStatus.DELETE, gscLink.getPid());
		}
		
		for(RdGscLink gscLink : updateLevelGscLinkList)
		{
			gscLink.changedFields().put("zlevel", gscLink.getZlevel() - 1);
			
			result.insertObject(gscLink, ObjStatus.UPDATE, gscLink.getPid());
		}
		
	}

	/**
	 * 
	 * @param allDelRdGscList
	 * @param allDelRdGscLinkList
	 * @throws Exception
	 */
	private void calcDelLinkOnGsc(List<? extends IRow> linkRow, List<RdGsc> allDelRdGscList, List<RdGscLink> allDelRdGscLinkList,List<RdGscLink> updateLevelGscLinkList)
			throws Exception {
		RdGscSelector selector = new RdGscSelector(conn);

		for (IRow link : linkRow) {
			int linkPid = link.parentPKValue();

			String tableName = link.tableName().toUpperCase();

			List<RdGsc> gscList = selector.loadRdGscLinkByLinkPid(linkPid, tableName, true);

			for (RdGsc gsc : gscList) {
				if (!allDelRdGscList.contains(gsc)) {
					// key:层级level，value:rdgsclink对象
					Map<Integer, List<RdGscLink>> levelGscLinkMap = new HashMap<>();

					List<RdGscLink> delGscLinkList = new ArrayList<>();

					for (IRow row : gsc.getLinks()) {
						RdGscLink gscLink = (RdGscLink) row;

						int level = gscLink.getZlevel();

						if (linkPid == gscLink.getLinkPid()) {
							delGscLinkList.add(gscLink);
						}

						List<RdGscLink> levelGscLinkList = levelGscLinkMap.get(level);

						if (levelGscLinkList == null) {
							levelGscLinkList = new ArrayList<>();

							levelGscLinkList.add(gscLink);

							levelGscLinkMap.put(level, levelGscLinkList);
						} else {
							levelGscLinkList.add(gscLink);
						}
					}

					boolean flag = checkRdGscHas2Del(linkRow, levelGscLinkMap,updateLevelGscLinkList);

					if (flag) {
						allDelRdGscList.add(gsc);
					} else {
						allDelRdGscLinkList.addAll(delGscLinkList);
					}
				}
			}
		}
	}

	/**
	 * 检查删除link后立交是否要被删除
	 * 
	 * @param linkRow
	 * @param linkTypeMap
	 * @param levelGscLink
	 * @return
	 */
	private boolean checkRdGscHas2Del(List<? extends IRow> linkRow, Map<Integer, List<RdGscLink>> levelGscLinkMap,List<RdGscLink> updateLevelLink) {
		boolean flag = false;

		// 需要删除的level
		List<Integer> delLevel = new ArrayList<>();

		for (Map.Entry<Integer, List<RdGscLink>> entry : levelGscLinkMap.entrySet()) {
			int level = entry.getKey();

			// 需要删除的gsclink放在该list中，最后和该level的对比是否该level下不存在link
			List<RdGscLink> delRdgscList = new ArrayList<>();

			List<RdGscLink> gscLinkList = entry.getValue();
			for (IRow row : linkRow) {
				int linkPid = row.parentPKValue();

				String tableName = row.tableName().toUpperCase();

				for (RdGscLink gscLink : gscLinkList) {
					if (gscLink.getLinkPid() == linkPid && gscLink.getTableName().equals(tableName)
							&& !delRdgscList.contains(gscLink)) {
						delRdgscList.add(gscLink);
					}
				}
			}

			if (delRdgscList.containsAll(gscLinkList)) {
				delLevel.add(level);
			}
		}

		// 如果删除link后立交level小于二级则需要删除立交整体(不考虑特殊立交和自相交立交)
		if (levelGscLinkMap.size() - delLevel.size() < 2) {
			flag = true;
		}
		else if(levelGscLinkMap.size() >2 && delLevel.size() == 1)
		{
			int delLev = delLevel.get(0);
			for (Map.Entry<Integer, List<RdGscLink>> entry : levelGscLinkMap.entrySet()) {
				int level = entry.getKey();
				if(!delLevel.contains(level) && level>delLev)
				{
					List<RdGscLink> gscLinkList = entry.getValue();
					
					updateLevelLink.addAll(gscLinkList);
				}
			}
		}

		return flag;
	}

	/**
	 * 删除link对立交的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteRdGscInfectData(List<? extends IRow> linkRow) throws Exception {

		List<RdGsc> allDelRdGscList = new ArrayList<>();

		List<RdGscLink> allDelRdGscLinkList = new ArrayList<>();
		
		List<RdGscLink> updateGscLinkList = new ArrayList<>();

		calcDelLinkOnGsc(linkRow, allDelRdGscList, allDelRdGscLinkList,updateGscLinkList);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdGsc rdGsc : allDelRdGscList) {
			
			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(rdGsc.objType());

			alertObj.setPid(rdGsc.getPid());

			alertObj.setStatus(ObjStatus.DELETE);
			
			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}
		for (RdGscLink rdGscLink : allDelRdGscLinkList) {
			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(ObjType.RDGSC);

			alertObj.setPid(rdGscLink.getPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
