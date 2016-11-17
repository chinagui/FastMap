package com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkKind;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNodePart;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameNodeSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Check {

	private Command command;

	public Check(Command command) {
		this.command = command;
	}

	public void checkNode(Connection conn) throws Exception {
		JSONArray nodeArray = this.command.getNodeArray();

		if (nodeArray == null) {
			throw new Exception("同一点关系组成node不能为空");
		} else if (nodeArray.size() < 2) {
			throw new Exception("同一点关系组成node不能少于2个");
		}

		RdSameNodeSelector sameNodeSelector = new RdSameNodeSelector(conn);

		// key：tableName,value:nodePid
		Map<String, StringBuilder> nodePids = new HashMap<>();

		for (int i = 0; i < nodeArray.size(); i++) {
			JSONObject obj = nodeArray.getJSONObject(i);

			int nodePid = obj.getInt("nodePid");

			String tableName = ReflectionAttrUtils.getTableNameByObjType(ObjType.valueOf(obj.getString("type")));

			if (nodePids.get(tableName) != null) {
				nodePids.get(tableName).append("," + nodePid);
			} else {
				nodePids.put(tableName, new StringBuilder().append(nodePid));
			}

			RdSameNodePart sameNodePart = sameNodeSelector.loadByNodePidAndTableName(nodePid, tableName, true);

			if (sameNodePart != null) {
				throw new Exception("node点：" + nodePid + "已经存在同一关系，不能重复创建");
			}
		}

		// 检查node是否属于某一单一link的起点和终点
		checkNodesForOneLink(nodePids, conn);

		// 检查node挂接的link中的LULINK
		checkNodesForLuLink(nodePids, conn);
	}

	/**
	 * 检查lu_node制作同一点关系
	 * @param nodePids
	 * @param conn
	 * @throws Exception
	 */
	private void checkNodesForLuLink(Map<String, StringBuilder> nodePids, Connection conn) throws Exception {
		if (nodePids.containsKey("LU_NODE")) {
			String luNodePids = nodePids.get("LU_NODE").toString();

			String[] luNodePidArray = luNodePids.split(",");
			
			Set<Integer> deFaultLuNodeKind = new HashSet<>();
			
			deFaultLuNodeKind.add(1);
			deFaultLuNodeKind.add(2);
			deFaultLuNodeKind.add(3);
			deFaultLuNodeKind.add(4);
			deFaultLuNodeKind.add(5);
			deFaultLuNodeKind.add(6);
			deFaultLuNodeKind.add(7);
			deFaultLuNodeKind.add(21);
			deFaultLuNodeKind.add(22);
			deFaultLuNodeKind.add(23);
			
			if (luNodePidArray.length > 2) {
				throw new Exception("土地利用要素NODE的个数不能超过2个");
			}
			else if(luNodePidArray.length == 2)
			{
				Map<Integer, Set<Integer>> nodeKindMap = new HashMap<>();

				if (luNodePidArray.length > 1) {
					LuLinkSelector selector = new LuLinkSelector(conn);

					List<LuLink> luLinkList = selector.loadByNodePids(luNodePids, true);

					for (LuLink luLink : luLinkList) {
						
						int sNodePid = luLink.getsNodePid();

						int eNodePid = luLink.geteNodePid();
						
						if(sNodePid == Integer.parseInt(luNodePidArray[0]) || sNodePid == Integer.parseInt(luNodePidArray[1]))
						{
							List<IRow> linkKindList = luLink.getLinkKinds();
							
							if (nodeKindMap.get(sNodePid) == null) {
								Set<Integer> nodeKindList = new HashSet<>();
								for (IRow row : linkKindList) {
									LuLinkKind kind = (LuLinkKind) row;

									nodeKindList.add(kind.getKind());
								}
								nodeKindMap.put(sNodePid, nodeKindList);
							} else {
								for (IRow row : linkKindList) {
									LuLinkKind kind = (LuLinkKind) row;

									nodeKindMap.get(sNodePid).add(kind.getKind());
								}
							}
						}
						if(eNodePid == Integer.parseInt(luNodePidArray[0])||eNodePid == Integer.parseInt(luNodePidArray[1]))
						{
							List<IRow> linkKindList = luLink.getLinkKinds();
							if (nodeKindMap.get(eNodePid) == null) {
								Set<Integer> nodeKindList = new HashSet<>();
								for (IRow row : linkKindList) {
									LuLinkKind kind = (LuLinkKind) row;

									nodeKindList.add(kind.getKind());
								}
								nodeKindMap.put(eNodePid, nodeKindList);
							} else {
								for (IRow row : linkKindList) {
									LuLinkKind kind = (LuLinkKind) row;

									nodeKindMap.get(eNodePid).add(kind.getKind());
								}
							}
						}
					}
				}
				Set<Integer> firstLuNodeSet = nodeKindMap.get(Integer.parseInt(luNodePidArray[0]));

				Set<Integer> secondLuNodeSet = nodeKindMap.get(Integer.parseInt(luNodePidArray[1]));
				
				if(deFaultLuNodeKind.containsAll(firstLuNodeSet) && deFaultLuNodeKind.containsAll(secondLuNodeSet))
				{
					//两条lu_link都不是bua边界线，不允许制作同一点
					if (!firstLuNodeSet.contains(21) && !secondLuNodeSet.contains(21)) {
						throw new Exception("种别为大学，购物中心，医院，体育场，公墓，停车场，工业区，邮区边界线，FM面边界线的土地利用link上挂接的node不允许互相创建同一点");
					}
					else if(firstLuNodeSet.contains(21) && firstLuNodeSet.size() == 1 && secondLuNodeSet.contains(21) && secondLuNodeSet.size() == 1 && nodePids.size() == 1)
					{
						throw new Exception("制作同一点关系的要素不能都是Bua边界线");
					}
					
					firstLuNodeSet.retainAll(secondLuNodeSet);
					
					if (firstLuNodeSet.size()>0) {
						throw new Exception("种别为大学，购物中心，医院，体育场，公墓，停车场，工业区，邮区边界线，FM面边界线的土地利用link上挂接的node不允许互相创建同一点");
					}
				}
				else
				{
					throw new Exception("参与同一点制作的土地利用Node只能为：link种别为大学，购物中心，医院，体育场，公墓，停车场，工业区， BUA边界线，邮区边界线，FM面边界线的node");
				}
			}
			else if(luNodePidArray.length == 1)
			{
				LuLinkSelector selector = new LuLinkSelector(conn);
				
				Set<Integer> nodeKindSet = new HashSet<>();
				
				List<LuLink> luLinkList = selector.loadByNodePids(luNodePids, true);
				
				for(LuLink luLink : luLinkList)
				{
					List<IRow> linkKindList = luLink.getLinkKinds();
					
					for(IRow row : linkKindList)
					{
						LuLinkKind kind = (LuLinkKind) row;

						nodeKindSet.add(kind.getKind());
					}
				}
				
				if(!deFaultLuNodeKind.containsAll(nodeKindSet))
				{
					throw new Exception("参与同一点制作的土地利用Node只能为：link种别为大学，购物中心，医院，体育场，公墓，停车场，工业区， BUA边界线，邮区边界线，FM面边界线的node");
				}
			}
		}
	}

	/**
	 * @param nodePids
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private void checkNodesForOneLink(Map<String, StringBuilder> nodePids, Connection conn) throws Exception {
		for (Map.Entry<String, StringBuilder> entry : nodePids.entrySet()) {
			String tableName = entry.getKey().replace("_NODE", "_LINK");

			String nodePidStr = entry.getValue().toString();

			RdSameNodeSelector selector = new RdSameNodeSelector(conn);

			List<Integer> linkPidList = selector.loadLinkByNodePids(tableName, nodePidStr, false);

			if (CollectionUtils.isNotEmpty(linkPidList)) {
				throw new Exception("同一条link的两个点不能做同一点");
			}
		}
	}
}
