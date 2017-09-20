package com.navinfo.dataservice.engine.edit.operation.edge;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.service.DbMeshInfoUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.dao.glm.iface.*;
import com.navinfo.dataservice.dao.glm.model.rd.crf.*;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoadLink;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.glm.selector.SelectorUtils;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.node.RdNodeSelector;
import com.navinfo.dataservice.engine.edit.utils.Constant;
import com.navinfo.dataservice.engine.edit.utils.GeometryUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.*;

/**
 * @Title: EdgeUtil
 * @Package: com.navinfo.dataservice.engine.edit.operation
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 2017/9/13
 * @Version: V1.0
 */
public class EdgeUtil {

    private static final Logger LOGGER = Logger.getLogger(EdgeUtil.class);

    private EdgeUtil() {
    }

    public static Set<Integer> calcDbIds(IRow iRow) throws Exception {
        Set<Integer> dbIds = new HashSet<>();

        Geometry geometry = GeometryUtils.loadGeometry(iRow);
        dbIds.addAll(DbMeshInfoUtil.calcDbIds(geometry));

        return dbIds;
    }

    public static Set<Integer> calcNewDbIds(IRow iRow) throws Exception {
        Set<Integer> dbIds = new HashSet<>();

        Geometry geometry = GeometryUtils.loadGeometry(iRow);
        if (iRow.changedFields().containsKey("geometry")) {
            geometry = GeoTranslator.geojson2Jts((JSONObject) iRow.changedFields().get("geometry"));
        }
        dbIds.addAll(DbMeshInfoUtil.calcDbIds(geometry));

        return dbIds;
    }

    public static Set<Integer> calcCrfDbIds(EdgeResult edge, Connection conn) throws Exception {
        Set<Integer> dbIds = new HashSet<>();
        for (Geometry geometry : getCRFGeom(edge.getSourceResult().getAddObjects(), edge.getSourceDb(), conn)) {
            dbIds.addAll(DbMeshInfoUtil.calcDbIds(geometry));
        }
        for (Geometry geometry : getCRFGeom(edge.getSourceResult().getUpdateObjects(), edge.getSourceDb(), conn)) {
            dbIds.addAll(DbMeshInfoUtil.calcDbIds(geometry));
        }
        for (Geometry geometry : getCRFGeom(edge.getSourceResult().getDelObjects(), edge.getSourceDb(), conn)) {
            dbIds.addAll(DbMeshInfoUtil.calcDbIds(geometry));
        }

        return dbIds;
    }

    private static List<Geometry> getCRFGeom(List<IRow> rows, Integer sourceDb, Connection conn) throws Exception {
        List<Geometry> geoms = new ArrayList<>();

        List<Integer> currentInter = new ArrayList<>();
        List<Integer> currentRoad = new ArrayList<>();
        List<Integer> currentObject = new ArrayList<>();

        for (IRow iRow : rows) {
            if (Constant.CRF_INTER.contains(iRow.objType())) {
                currentInter.add(getCRFPid(iRow));
            }
            if (Constant.CRF_ROAD.contains(iRow.objType())) {
                currentRoad.add(getCRFPid(iRow));
            }
            if (Constant.CRF_OBJECT.contains(iRow.objType())) {
                currentObject.add(getCRFPid(iRow));
            }
        }

        List<Integer> interPids = new ArrayList<>(currentInter);
        List<Integer> roadPids = new ArrayList<>(currentRoad);
        List<Integer> linkPids = new ArrayList<>();
        List<Integer> nodePids = new ArrayList<>();

        AbstractSelector selector = new AbstractSelector(RdObject.class, conn);
        List<IRow> iRows;
        try {
            iRows = selector.loadByIds(currentObject, false, true);
            for (IRow iRow : rows) {
                if (iRow instanceof RdObject && ObjStatus.INSERT.equals(iRow.status())) {
                    iRows.add(iRow);
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format("获取RdObject出错[ids: %s]", Arrays.toString(currentObject.toArray())), e.fillInStackTrace());
            throw e;
        }

        for (IRow rowObj : iRows) {
            RdObject obj = (RdObject) rowObj;
            for (IRow row : obj.getInters()) {
                interPids.add(((RdObjectInter) row).getInterPid());
            }
            for (IRow row : obj.getRoads()) {
                roadPids.add(((RdObjectRoad) row).getRoadPid());
            }
            for (IRow row : obj.getLinks()) {
                linkPids.add(((RdObjectLink) row).getLinkPid());
            }
            for (IRow row : obj.getNodes()) {
                nodePids.add(((RdObjectNode) row).getNodePid());
            }
        }

        selector = new AbstractSelector(RdInter.class, conn);
        try {
            iRows = selector.loadByIds(interPids, false, true);
            for (IRow iRow : rows) {
                if (iRow instanceof RdInter && ObjStatus.INSERT.equals(iRow.status())) {
                    iRows.add(iRow);
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format("获取RdInter出错[ids: %s]", Arrays.toString(interPids.toArray())), e.fillInStackTrace());
            throw e;
        }

        for (IRow rowInter : iRows) {
            RdInter obj = (RdInter) rowInter;
            for (IRow row : obj.getLinks()) {
                linkPids.add(((RdInterLink) row).getLinkPid());
            }
            for (IRow row : obj.getNodes()) {
                nodePids.add(((RdInterNode) row).getNodePid());
            }
        }

        selector = new AbstractSelector(RdRoad.class, conn);
        try {
            iRows = selector.loadByIds(roadPids, false, true);
            for (IRow iRow : rows) {
                if (iRow instanceof RdRoad && ObjStatus.INSERT.equals(iRow.status())) {
                    iRows.add(iRow);
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format("获取RdRoad出错[ids: %s]", Arrays.toString(roadPids.toArray())), e.fillInStackTrace());
            throw e;
        }

        for (IRow rowRoad : iRows) {
            RdRoad obj = (RdRoad) rowRoad;
            for (IRow row : obj.getLinks()) {
                linkPids.add(((RdRoadLink) row).getLinkPid());
            }
        }


        selector = new AbstractSelector(RdLink.class, conn);
        try {
            iRows = selector.loadByIds(linkPids, false, false);
            if (CollectionUtils.isNotEmpty(iRows) && iRows.size() != linkPids.size()) {
                String wkt = GeoTranslator.jts2Wkt(GeometryUtils.loadGeometry(iRows.iterator().next()), Constant.BASE_SHRINK, Constant.BASE_PRECISION);

                Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(wkt, 3);
                Iterator<Integer> iterator = dbIds.iterator();
                do {
                    Integer dbId = iterator.next();
                    if (!sourceDb.equals(dbId)) {
                        Connection connection = null;
                        try {
                            connection = DBConnector.getInstance().getConnectionById(dbId);
                            List<IRow> nodes = new RdLinkSelector(connection).loadByIds(linkPids, false, false);
                            iRows.addAll(nodes);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            DBUtils.closeConnection(connection);
                        }
                    }
                } while (iRows.size() != linkPids.size() && iterator.hasNext());
            }
        } catch (Exception e) {
            LOGGER.error(String.format("获取RdLink出错[ids: %s]", Arrays.toString(linkPids.toArray())), e.fillInStackTrace());
            throw e;
        }

        for (IRow rowLink : iRows) {
            RdLink obj = (RdLink) rowLink;
            geoms.add(obj.getGeometry());
        }

        selector = new AbstractSelector(RdNode.class, conn);
        try {
            iRows = selector.loadByIds(nodePids, true, false);
            if (CollectionUtils.isNotEmpty(iRows) && iRows.size() != nodePids.size()) {
                String wkt = GeoTranslator.jts2Wkt(GeometryUtils.loadGeometry(iRows.iterator().next()), Constant.BASE_SHRINK, Constant.BASE_PRECISION);
                Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(wkt, 3);

                Iterator<Integer> iterator = dbIds.iterator();
                do {
                    Integer dbId = iterator.next();
                    if (!sourceDb.equals(dbId)) {
                        Connection connection = null;
                        try {
                            connection = DBConnector.getInstance().getConnectionById(dbId);
                            List<IRow> nodes = new RdNodeSelector(connection).loadByIds(nodePids, false, false);
                            iRows.addAll(nodes);
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            DBUtils.closeConnection(connection);
                        }
                    }
                } while (iRows.size() != nodePids.size() && iterator.hasNext());
            }
        } catch (Exception e) {
            LOGGER.error(String.format("获取RdNode出错[ids: %s]", Arrays.toString(nodePids.toArray())), e.fillInStackTrace());
            throw e;
        }

        for (IRow rowNode : iRows) {
            RdNode obj = (RdNode) rowNode;
            geoms.add(obj.getGeometry());
        }
        return geoms;
    }

    private static Integer getCRFPid(IRow iRow) {
        Integer pid;
        switch (iRow.objType()) {
            case RDINTER: pid = ((RdInter) iRow).getPid(); break;
            case RDINTERLINK: pid = ((RdInterLink) iRow).getPid(); break;
            case RDINTERNODE: pid = ((RdInterNode) iRow).getPid(); break;
            case RDROAD: pid = ((RdRoad) iRow).getPid(); break;
            case RDROADLINK: pid = ((RdRoadLink) iRow).getPid(); break;
            case RDOBJECT: pid = ((RdObject) iRow).getPid(); break;
            case RDOBJECTINTER: pid = ((RdObjectInter) iRow).getPid(); break;
            case RDOBJECTROAD: pid = ((RdObjectRoad) iRow).getPid(); break;
            case RDOBJECTLINK: pid = ((RdObjectLink) iRow).getPid(); break;
            case RDOBJECTNODE: pid = ((RdObjectNode) iRow).getPid(); break;
            case RDOBJECTNAME: pid = ((RdObjectName) iRow).getPid(); break;
            default: pid = 0;
        }
        return pid;
    }

    public static void extentions(List<IRow> roots, List<IRow> iRows) {
        List<IRow> subRows = new ArrayList<>();

        for (IRow iRow : iRows) {
            if (iRow instanceof IObj) {
                continue;
            }

            if (containsParent(roots, iRow)) {
                subRows.add(iRow);
            }
        }
        roots.addAll(subRows);
    }

    private static boolean containsParent(List<IRow> roots, IRow row) {
        for (IRow iRow : roots) {
            if (iRow instanceof IObj) {
                IObj root = (IObj) iRow;
                if (row.parentPKName().equals(root.tableName()) && (root.pid() == row.parentPKValue())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean contains(List<IRow> sources, IRow iRow) {
        for (IRow source : sources) {
            if (source == iRow) {
                return true;
            }
            if (source.rowId() == iRow.rowId()) {
                return true;
            }
        }
        return false;
    }

    public static boolean notContains(List<IRow> sources, IRow iRow) {
        return !contains(sources, iRow);
    }


    /**
     * 填充AddObject数据的RowId，防止跨库数据不一致
     *
     * @param addedData
     */
    public static void initalizeRowId(List<IRow> addedData) {
        for (IRow iRow : addedData) {
            String tableName = SelectorUtils.getObjTableName(iRow);
            if (StringUtils.isNotEmpty(iRow.rowId())) {
                if (!tableName.equals("IX_POI")) {
                    iRow.setRowId(UuidUtils.genUuid());
                }
            } else {
                iRow.setRowId(UuidUtils.genUuid());
            }
            if (CollectionUtils.isNotEmpty(iRow.children())) {
                for (List<IRow> list : iRow.children()) {
                    initalizeRowId(list);
                }
            }
        }
    }

    public static boolean isEmptyResult(Result result) {
        return CollectionUtils.isEmpty(result.getAddObjects()) && CollectionUtils.isEmpty(result.getUpdateObjects()) &&
                CollectionUtils.isEmpty(result.getDelObjects());
    }

    public static Map<Integer, ObjStatus> diffDb(Set<Integer> sourceDb, Set<Integer> newDb) {
        Map<Integer, ObjStatus> map = new HashMap<>();
        for (Integer dbId : sourceDb) {
            if (newDb.contains(dbId.intValue())) {
                map.put(dbId, ObjStatus.UPDATE);
            } else {
                map.put(dbId, ObjStatus.DELETE);
            }
        }
        for (Integer dbId : newDb) {
            if (!map.containsKey(dbId.intValue())) {
                map.put(dbId, ObjStatus.INSERT);
            }
        }

        return map;
    }
}
