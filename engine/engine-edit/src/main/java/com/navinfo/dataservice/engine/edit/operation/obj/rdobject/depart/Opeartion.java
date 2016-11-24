package com.navinfo.dataservice.engine.edit.operation.obj.rdobject.depart;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObject;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectLink;
import com.navinfo.dataservice.dao.glm.model.rd.crf.RdObjectRoad;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdObjectSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdRoadLinkSelector;

import java.sql.Connection;
import java.util.*;

/**
 * 用于维护节点分离对CRF对象的影响
 * Created by chaixin on 2016/9/19 0019.
 */
public class Opeartion {

    private Connection conn;

    public Opeartion(Connection conn) {
        this.conn = conn;
    }

    public void depart(int nodePid, RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {
        // 加载RdRoadLinkSelector、rdObjectSelector
        RdObjectSelector rdObjectSelector = new RdObjectSelector(this.conn);
        RdRoadLinkSelector rdRoadLinkSelector = new RdRoadLinkSelector(this.conn);
        // 查询linkPid相关RdRoad
        List<RdRoadLink> roadLinks = rdRoadLinkSelector.loadByLinks(Arrays.asList(new Integer[]{oldLink.pid()}), true);
        // 创建Map用于确定RdObject是否需要删除
        Map<Integer, RdObject> deleteMap = new HashMap<Integer, RdObject>();
        // 查询RdRoad相关的RdObject
        Map<String, RdObject> roadObject = null;
        if (!roadLinks.isEmpty()) {
            int roadPid = roadLinks.get(0).getPid();
            roadObject = rdObjectSelector.loadRdObjectByPidAndType(roadPid + "", ObjType.RDROAD, true);
            // 删除RdObject对象以及数据库中对应的RdRoad
            for (String key : roadObject.keySet()) {
                RdObject rdObject = getRdObject(deleteMap, roadObject, key);
                Iterator<IRow> iterator = rdObject.getRoads().iterator();
                while (iterator.hasNext()) {
                    RdObjectRoad objectRoad = (RdObjectRoad) iterator.next();
                    if (roadPid == objectRoad.getRoadPid()) {
                        result.insertObject(objectRoad, ObjStatus.DELETE, roadPid);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        // 查询RdObjectLink相关的RdObject
        Map<String, RdObject> linkObject = rdObjectSelector.loadRdObjectByPidAndType(oldLink.pid() + "", ObjType.RDLINK, true);
        // 删除RdObject对象以及数据库中对应的RdObjectLink
        for (String key : linkObject.keySet()) {
            RdObject rdObject = getRdObject(deleteMap, linkObject, key);
            Iterator<IRow> iterator = rdObject.getLinks().iterator();
            while (iterator.hasNext()) {
                RdObjectLink objectLink = (RdObjectLink) iterator.next();
                if (oldLink.pid() == objectLink.getLinkPid()) {
                    result.insertObject(objectLink, ObjStatus.DELETE, objectLink.getPid());
                    iterator.remove();
                    break;
                }
            }
        }
        // 判断RdObject对象是否需要删除
        for (Integer pid : deleteMap.keySet()) {
            RdObject rdObject = deleteMap.get(pid);
            if (rdObject.getLinks().isEmpty() && rdObject.getRoads().isEmpty() && rdObject.getInters().isEmpty()) {
                result.insertObject(rdObject, ObjStatus.DELETE, rdObject.pid());
            }
        }
    }

    /**
     * 获取RdObject，如果已存在于已处理deleteMap中则取出使用同意对象，如不存在则放入deleteMap中
     *
     * @param deleteMap 已处理RdObject
     * @param objectMap 查询结果集RdObject
     * @param key       主键
     * @return
     */
    private RdObject getRdObject(Map<Integer, RdObject> deleteMap, Map<String, RdObject> objectMap, String key) {
        RdObject rdObject = objectMap.get(key);
        if (null == deleteMap.get(rdObject.pid())) {
            deleteMap.put(rdObject.pid(), rdObject);
        } else {
            rdObject = deleteMap.get(rdObject.getPid());
        }
        return rdObject;
    }
}
