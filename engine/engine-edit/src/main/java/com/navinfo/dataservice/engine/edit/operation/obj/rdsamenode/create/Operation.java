package com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.create;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdLink;
import com.navinfo.dataservice.dao.glm.model.ad.zone.ZoneLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLink;
import com.navinfo.dataservice.dao.glm.model.lu.LuLinkKind;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.rw.RwLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNodePart;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.ad.geo.AdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.ad.zone.ZoneLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.lu.LuLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.rw.RwLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameNodeSelector;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Operation implements IOperation {

    private Logger log = Logger.getLogger(Operation.class);

    private Command command;

    private Connection conn;

    public Operation(Command command, Connection conn) {
        this.command = command;
        this.conn = conn;
    }

    @Override
    public String run(Result result) throws Exception {
        String msg = null;

        createRdSameNode(result);

        return msg;
    }

    /**
     * @param result
     * @throws Exception
     */
    private void createRdSameNode(Result result) throws Exception {

        RdSameNode rdSameNode = new RdSameNode();

        rdSameNode.setPid(PidUtil.getInstance().applyRdSameNodePid());

        JSONArray nodeArray = this.command.getNodeArray();

        // 主点pid
        int mainNodePid = 0;

        String mainTableName = "";

        // 设置子表rd_samenode_part
        for (int i = 0; i < nodeArray.size(); i++) {
            JSONObject obj = nodeArray.getJSONObject(i);

            int nodePid = obj.getInt("nodePid");

            // 将类似RDNODE转为表名"RD_NODE"
            ObjType objType = ObjType.valueOf(obj.getString("type"));

            String tableName = ReflectionAttrUtils.getTableNameByObjType(objType);

            int isMain = obj.getInt("isMain");

            if (isMain == 1) {
                mainNodePid = nodePid;

                mainTableName = tableName;
            } 

            RdSameNodePart sameNodePart = new RdSameNodePart();

            sameNodePart.setGroupId(rdSameNode.getPid());

            sameNodePart.setNodePid(nodePid);

            sameNodePart.setTableName(tableName);

            rdSameNode.getParts().add(sameNodePart);
        }

        result.insertObject(rdSameNode, ObjStatus.INSERT, rdSameNode.getPid());

        // 更新点的坐标
        updateNodeGeo(mainNodePid, mainTableName.toUpperCase(), nodeArray, result);
    }

    
    
	/**
	 * 维护点的坐标
	 * 
	 * @param mainNodePid
	 * @param tableName
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	private void updateNodeGeo(int mainNodePid, String tableName,
			JSONArray nodeArray, Result result) throws Exception {

		if (mainNodePid == 0 || StringUtils.isEmpty(tableName)) {
			return;
		}

		RdSameNodeSelector sameNodeSelector = new RdSameNodeSelector(conn);

		Geometry nodeGeo = sameNodeSelector.getGeoByNodePidAndTableName(
				mainNodePid, tableName, true);

		// 组装参数
		JSONObject updateContent = new JSONObject();

		updateContent.put("dbId", command.getDbId());

		JSONObject data = new JSONObject();

		data.put("longitude", nodeGeo.getCoordinate().x);

		data.put("latitude", nodeGeo.getCoordinate().y);

		updateContent.put("data", data);

		for (int i = 0; i < nodeArray.size(); i++) {

			JSONObject obj = nodeArray.getJSONObject(i);

			int isMain = obj.getInt("isMain");

			if (isMain == 1) {

				continue;
			}

			int nodePid = obj.getInt("nodePid");

			ObjType objType = ObjType.valueOf(obj.getString("type"));

			Geometry moveNode = sameNodeSelector.getGeoByNodePidAndTableName(
					nodePid,
					ReflectionAttrUtils.getTableNameByObjType(objType), true);

			if (moveNode.distance(nodeGeo) != 0) {

				updateContent.put("objId", nodePid);

				// 调用移动接口
				moveNode(objType, updateContent, result);
			}
		}
	}

    /**
     * 调用点的移动接口，维护点的坐标
     *
     * @param type
     * @throws Exception
     */
    private void moveNode(ObjType type, JSONObject updateContent, Result result) throws Exception {
        switch (type) {
            case RDNODE:
                com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Command updatecommand = new com
                        .navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Command(updateContent, "");
                com.navinfo.dataservice.engine.edit.operation.topo.move.moverdnode.Process process = new com.navinfo
                        .dataservice.engine.edit.operation.topo.move.moverdnode.Process(updatecommand, result, conn);
                process.innerRun();
                break;
            case ADNODE:
                com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Command adCommand = new com
                        .navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Command(updateContent, null);
                com.navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Process adProcess = new com
                        .navinfo.dataservice.engine.edit.operation.topo.move.moveadnode.Process(adCommand, result,
                        conn);
                adProcess.innerRun();
                break;

            case RWNODE:
                com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode.Command rwCommand = new com
                        .navinfo.dataservice.engine.edit.operation.topo.move.moverwnode.Command(updateContent, null);
                com.navinfo.dataservice.engine.edit.operation.topo.move.moverwnode.Process rwProcess = new com
                        .navinfo.dataservice.engine.edit.operation.topo.move.moverwnode.Process(rwCommand, result,
                        conn);
                rwProcess.innerRun();
                break;
            case ZONENODE:
                com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode.Command zoneCommand = new com
                        .navinfo.dataservice.engine.edit.operation.topo.move.movezonenode.Command(updateContent, null);
                com.navinfo.dataservice.engine.edit.operation.topo.move.movezonenode.Process zoneProcess = new com
                        .navinfo.dataservice.engine.edit.operation.topo.move.movezonenode.Process(zoneCommand,
                        result, conn);
                zoneProcess.innerRun();
                break;
            case LUNODE:
                com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode.Command luCommand = new com
                        .navinfo.dataservice.engine.edit.operation.topo.move.movelunode.Command(updateContent, null);
                com.navinfo.dataservice.engine.edit.operation.topo.move.movelunode.Process luProcess = new com
                        .navinfo.dataservice.engine.edit.operation.topo.move.movelunode.Process(luCommand, result,
                        conn);
                luProcess.innerRun();
                break;
            default:
                break;
        }
    }

    /**
     * 给打断同一线link调用的方法，在打断点位置需要创建同一点关系
     *
     * @param result       结果集
     * @param sameNodeList 同一点集合
     * @throws Exception
     */
    public void breakSameLink(Result result, List<IRow> sameNodeList) throws Exception {
        if (CollectionUtils.isNotEmpty(sameNodeList)) {
            RdSameNode rdSameNode = new RdSameNode();

            rdSameNode.setPid(PidUtil.getInstance().applyRdSameNodePid());

            for (IRow row : sameNodeList) {
                RdSameNodePart sameNodePart = new RdSameNodePart();

                sameNodePart.setGroupId(rdSameNode.getPid());

                sameNodePart.setNodePid(row.parentPKValue());

                sameNodePart.setTableName(ReflectionAttrUtils.getTableNameByObjType(row.objType()));

                rdSameNode.getParts().add(sameNodePart);
            }
            result.insertObject(rdSameNode, ObjStatus.INSERT, rdSameNode.getPid());
        }
    }

    /**
     * 移动主要素，其他从要素跟随移动：优先级顺序（RDNODE>ADNODE>RWNODE>ZONENODE>LUNODE）
     *
     * @param updateContent
     * @param type
     * @param result
     * @throws Exception
     */
    public void moveMainNodeForTopo(JSONObject updateContent, ObjType type, Result result) throws Exception {

        int nodePid = updateContent.getInt("objId");

        RdSameNodeSelector sameNodeSelector = new RdSameNodeSelector(conn);

        String tableName = ReflectionAttrUtils.getTableNameByObjType(type);

        List<RdSameNode> sameNodeList = sameNodeSelector.loadSameNodeByNodePids(String.valueOf(nodePid), tableName,
                true);

        //不是同一node组成点
        if (CollectionUtils.isEmpty(sameNodeList)) {

            return;
        }
        if (sameNodeList.size() != 1) {
            throw new Exception(type.toString() + "点:" + nodePid + "的同一点关系不唯一");
        }

        RdSameNode sameNode = sameNodeList.get(0);

        List<IRow> sameNodePart = sameNode.getParts();

        // 定义需要移动的partNode
        Map<ObjType, JSONObject> movePartNodeMap = new HashMap<>();

        for (IRow row : sameNodePart) {
            RdSameNodePart part = (RdSameNodePart) row;

            Object obj = JSONObject.toBean(updateContent);

            JSONObject updateJson = JSONObject.fromObject(obj);

            ObjType partType = ReflectionAttrUtils.getObjTypeByTableName(part.getTableName());

            updateJson.element("objId", part.getNodePid());

            updateJson.element("type", partType);

            updateJson.put("mainType", type);

            movePartNodeMap.put(partType, updateJson);
        }

        handleMovePartNodeMap(nodePid, type, movePartNodeMap, result);

        editLogs( sameNode, nodePid,  type,  result);

    }


    private  void editLogs(RdSameNode sameNode,int nodePid, ObjType type, Result result)
    {
        try {
            RdSameLinkSelector sameLinkSelector = new RdSameLinkSelector(conn);
            Class clazz = result.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (!"logs".equals(field.getName()))
                    continue;
                field.setAccessible(true);
                Object object = field.get(result);
                if (object instanceof JSONArray) {
                    JSONArray logs = (JSONArray) object;
                    JSONObject log = new JSONObject();
                    log.put("type", sameNode.objType());
                    log.put("pid", sameNode.pid());
                    log.put("op", "修改");
                    logs.add(log);

                    switch (type) {
                        case RDNODE:
                            List<RdLink> rdLinks = new RdLinkSelector(conn).loadByNodePid(nodePid, false);
                            for (RdLink link : rdLinks) {
                                RdSameLinkPart sameLinkPart = sameLinkSelector.loadLinkPartByLink(link.pid(),
                                        "RD_LINK", false);
                                if (null == sameLinkPart)
                                    continue;
                                log = new JSONObject();
                                log.put("type", "RDSAMELINK");
                                log.put("pid", sameLinkPart.getGroupId());
                                log.put("op", "修改");
                                logs.add(log);
                            }
                            break;
                        case ADNODE:
                            List<AdLink> adLinks = new AdLinkSelector(conn).loadByNodePid(nodePid, false);
                            for (AdLink link : adLinks) {
                                RdSameLinkPart sameLinkPart = sameLinkSelector.loadLinkPartByLink(link.pid(),
                                        "AD_LINK", false);
                                if (null == sameLinkPart)
                                    continue;
                                log = new JSONObject();
                                log.put("type", "RDSAMELINK");
                                log.put("pid", sameLinkPart.getGroupId());
                                log.put("op", "修改");
                                logs.add(log);
                            }
                            break;
                        case LUNODE:
                            List<LuLink> luLinks = new LuLinkSelector(conn).loadByNodePid(nodePid, false);
                            for (LuLink link : luLinks) {
                                RdSameLinkPart sameLinkPart = sameLinkSelector.loadLinkPartByLink(link.pid(),
                                        "LU_LINK", false);
                                if (null == sameLinkPart)
                                    continue;
                                log = new JSONObject();
                                log.put("type", "RDSAMELINK");
                                log.put("pid", sameLinkPart.getGroupId());
                                log.put("op", "修改");
                                logs.add(log);
                            }
                            break;
                        case ZONENODE:
                            List<ZoneLink> zoneLinks = new ZoneLinkSelector(conn).loadByNodePid(nodePid, false);
                            for (ZoneLink link : zoneLinks) {
                                RdSameLinkPart sameLinkPart = sameLinkSelector.loadLinkPartByLink(link.pid(),
                                        "ZONE_LINK", false);
                                if (null == sameLinkPart)
                                    continue;
                                log = new JSONObject();
                                log.put("type", "RDSAMELINK");
                                log.put("pid", sameLinkPart.getGroupId());
                                log.put("op", "修改");
                                logs.add(log);
                            }
                            break;
                        case RWNODE:
                            List<RwLink> rwLinks = new RwLinkSelector(conn).loadByNodePid(nodePid, false);
                            for (RwLink link : rwLinks) {
                                RdSameLinkPart sameLinkPart = sameLinkSelector.loadLinkPartByLink(link.pid(),
                                        "RW_LINK", false);
                                if (null == sameLinkPart)
                                    continue;
                                log = new JSONObject();
                                log.put("type", "RDSAMELINK");
                                log.put("pid", sameLinkPart.getGroupId());
                                log.put("op", "修改");
                                logs.add(log);
                            }
                            break;
                    }
                }
                break;
            }
        } catch (Exception e) {
            log.info("该功能用于移动Node时返回同一点渲染信息，出错部分已忽略");
            log.error("错误日志：" + e.getMessage(), e);
        }
    }


    /**
     * （RDNODE>ADNODE>RWNODE>ZONENODE>LUNODE）
     *
     * @param nodePid
     * @param type
     * @param movePartNodeMap
     * @throws Exception
     */
    private void handleMovePartNodeMap(int nodePid, ObjType type, Map<ObjType, JSONObject> movePartNodeMap, Result
            result) throws Exception {

        if (movePartNodeMap.size() > 0) {
            switch (type) {
                case RDNODE:
                    moveNodeFromNodeMap(type, nodePid, movePartNodeMap, result);
                    break;
                case ADNODE:
                    if (movePartNodeMap.containsKey(ObjType.RDNODE)) {
                        throw new Exception("node不是该同一关系中的主要素，不允许移动操作");
                    } else {
                        moveNodeFromNodeMap(type, nodePid, movePartNodeMap, result);
                    }
                    break;
                case RWNODE:
                    if (movePartNodeMap.containsKey(ObjType.RDNODE)||movePartNodeMap.containsKey(ObjType.ADNODE)) {
                        throw new Exception("node不是该同一关系中的主要素，不允许移动操作");
                    } else {
                        moveNodeFromNodeMap(type, nodePid, movePartNodeMap, result);
                    }
                    break;
                case ZONENODE:
                    if (movePartNodeMap.containsKey(ObjType.RDNODE) || movePartNodeMap.containsKey(ObjType.ADNODE) ||
                            movePartNodeMap.containsKey(ObjType.RWNODE)) {
                        throw new Exception("node不是该同一关系中的主要素，不允许移动操作");
                    } else {
                        moveNodeFromNodeMap(type, nodePid, movePartNodeMap, result);
                    }
                    break;
                case LUNODE:
                    LuLinkSelector selector = new LuLinkSelector(conn);
                    if (movePartNodeMap.containsKey(ObjType.RDNODE) || movePartNodeMap.containsKey(ObjType.ADNODE) ||
                            movePartNodeMap.containsKey(ObjType.RWNODE) || movePartNodeMap.containsKey(ObjType
                            .ZONENODE)) {
                        throw new Exception("node不是该同一关系中的主要素，不允许移动操作");
                    } else {
                        List<LuLink> luLinkList = selector.loadByNodePid(nodePid, true);

                        boolean isMoveMainNode = false;

                        for (LuLink luLink : luLinkList) {
                            for (IRow luLinkKind : luLink.getLinkKinds()) {
                                LuLinkKind kind = (LuLinkKind) luLinkKind;

                                if (kind.getKind() == 21) {
                                    isMoveMainNode = true;
                                    break;
                                }
                            }
                            if (isMoveMainNode) {
                                break;
                            }
                        }

                        if (!isMoveMainNode) {
                            throw new Exception("node不是该同一关系中的主要素，不允许移动操作");
                        } else {
                            moveNodeFromNodeMap(type, nodePid, movePartNodeMap, result);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void moveNodeFromNodeMap(ObjType type, int nodePid, Map<ObjType, JSONObject> movePartNodeMap, Result
            result) throws Exception {
        for (Map.Entry<ObjType, JSONObject> nodeMap : movePartNodeMap.entrySet()) {

            ObjType partType = nodeMap.getKey();

            JSONObject updateContent = nodeMap.getValue();

            if (!(type.equals(partType) && updateContent.getInt("objId") == nodePid)) {
                moveNode(partType, updateContent, result);
            }
        }
    }
}
