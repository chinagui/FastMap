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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
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

    /**
     * 计算本次操作跨大区CRF影响
     * @param edge
     */
    public static void handleCrf(EdgeResult edge) {
        Result sourceResult = edge.getSourceResult();

    }

    public static Set<Integer> calcDbIds(IRow iRow, Integer sourceDb, List<IRow> iRows) throws Exception{
        Set<Integer> dbIds = new HashSet<>();

        if (Constant.CRF_TYPES.contains(iRow)) {
            List<Geometry> geometries = getCRFGeom(iRows, sourceDb);
        } else {
            Geometry geometry = GeometryUtils.loadGeometry(iRow);
            dbIds.addAll(DbMeshInfoUtil.calcDbIds(geometry));
        }

        return dbIds;
    }

    public static List<Geometry> getCRFGeom(List<IRow> rows, Integer sourceDb) throws Exception{
        List<Geometry> geoms = new ArrayList<>();

        Connection conn = null;
        try {
            conn = DBConnector.getInstance().getConnectionById(sourceDb);

            Set<Integer> currInterPids = new HashSet<>();
            Set<Integer> currRoadPids = new HashSet<>();
            Set<Integer> currObjectPids = new HashSet<>();

            for (IRow row : rows) {
                if (row instanceof RdInter || row instanceof RdInterLink || row instanceof RdInterNode) {
                    currInterPids.add(getCRFPid(row));
                } else if (row instanceof RdRoad || row instanceof RdRoadLink) {
                    currRoadPids.add(getCRFPid(row));
                } else if (row instanceof RdObject || row instanceof RdObjectInter || row instanceof RdObjectRoad || row instanceof RdObjectNode || row instanceof RdObjectName || row instanceof RdObjectLink) {

                    currObjectPids.add(getCRFPid(row));
                }
            }

            Set<Integer> inters = new HashSet<>(currInterPids);
            Set<Integer> roads = new HashSet<>(currRoadPids);
            Set<Integer> links = new HashSet<>();
            Set<Integer> nodes = new HashSet<>();

            AbstractSelector selector = new AbstractSelector(RdObject.class, conn);
            List<IRow> rowsTmp = null;
            try {
                rowsTmp = selector.loadByIds(new ArrayList<>(currObjectPids), true, true);
                for (IRow row : rows) {
                    if (row instanceof RdObject && row.status().equals(ObjStatus.INSERT)) {
                        rowsTmp.add(row);
                    }
                }
            } catch (Exception e) {
                LOGGER.error(String.format("获取RdObject出错[ids: %s]", Arrays.toString(currObjectPids.toArray())), e);
            }

            for (IRow rowObj : rowsTmp) {
                RdObject obj = (RdObject) rowObj;
                for (IRow row : obj.getInters()) {
                    inters.add(((RdObjectInter) row).getInterPid());
                }
                for (IRow row : obj.getRoads()) {
                    roads.add(((RdObjectRoad) row).getRoadPid());
                }
                for (IRow row : obj.getLinks()) {
                    links.add(((RdObjectLink) row).getLinkPid());
                }
                for (IRow row : obj.getNodes()) {
                    nodes.add(((RdObjectNode) row).getNodePid());
                }
            }

            selector = new AbstractSelector(RdInter.class, conn);
            try {
                rowsTmp = selector.loadByIds(new ArrayList<>(inters), true, true);
                for (IRow row : rows) {
                    if (row instanceof RdInter && row.status().equals(ObjStatus.INSERT)) {
                        rowsTmp.add(row);
                    }
                }
            } catch (Exception e) {
                LOGGER.error(String.format("获取RdInter出错[ids: %s]", Arrays.toString(inters.toArray())), e);
            }

            for (IRow rowInter : rowsTmp) {
                RdInter obj = (RdInter) rowInter;
                for (IRow row : obj.getLinks()) {
                    links.add(((RdInterLink) row).getLinkPid());
                }
                for (IRow row : obj.getNodes()) {
                    nodes.add(((RdInterNode) row).getNodePid());
                }
            }

            selector = new AbstractSelector(RdRoad.class, conn);
            try {
                rowsTmp = selector.loadByIds(new ArrayList<>(roads), true, true);
                for (IRow row : rows) {
                    if (row instanceof RdRoad && row.status().equals(ObjStatus.INSERT)) {
                        rowsTmp.add(row);
                    }
                }
            } catch (Exception e) {
                LOGGER.error(String.format("获取RdRoad出错[ids: %s]", Arrays.toString(roads.toArray())), e);
            }

            for (IRow rowRoad : rowsTmp) {
                RdRoad obj = (RdRoad) rowRoad;
                for (IRow row : obj.getLinks()) {
                    links.add(((RdRoadLink) row).getLinkPid());
                }
            }


            selector = new AbstractSelector(RdLink.class, conn);
            try {
                rowsTmp = selector.loadByIds(new ArrayList<>(links), true, false);
                if (rowsTmp.size() != links.size() && rowsTmp.size() > 0) {
                    int minus = links.size() - rowsTmp.size();

                    RdLink link = (RdLink) rowsTmp.get(0);
                    Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(GeoTranslator.jts2Wkt(link.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION), 3);
                    for (Integer dbId : dbIds) {
                        if (dbId.equals(sourceDb)) {
                            continue;
                        }

                        Connection connection = null;
                        try {
                            connection = DBConnector.getInstance().getConnectionById(dbId);
                            List<RdLink> rdLinks = new RdLinkSelector(connection).loadByPids(new ArrayList<>(links), false);
                            rowsTmp.addAll(rdLinks);
                            if (rdLinks.size() == minus) {
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            DBUtils.closeConnection(connection);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(String.format("获取RdLink出错[ids: %s]", Arrays.toString(links.toArray())), e);
            }

            for (IRow rowLink : rowsTmp) {
                RdLink obj = (RdLink) rowLink;
                geoms.add(obj.getGeometry());
            }

            selector = new AbstractSelector(RdNode.class, conn);
            try {
                rowsTmp = selector.loadByIds(new ArrayList<>(nodes), true, false);
                if (rowsTmp.size() != nodes.size() && rowsTmp.size() > 0) {
                    int minus = nodes.size() - rowsTmp.size();

                    RdNode node = (RdNode) rowsTmp.get(0);
                    Set<Integer> dbIds = DbMeshInfoUtil.calcDbIds(GeoTranslator.jts2Wkt(node.getGeometry(), Constant.BASE_SHRINK, Constant.BASE_PRECISION), 3);
                    for (Integer dbId : dbIds) {
                        if (dbId.equals(sourceDb)) {
                            continue;
                        }

                        Connection connection = null;
                        try {
                            connection = DBConnector.getInstance().getConnectionById(dbId);
                            List<IRow> rdNodes = new RdNodeSelector(connection).loadByIds(new ArrayList<>(links), false, false);
                            rowsTmp.addAll(rdNodes);
                            if (rdNodes.size() == minus) {
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            DBUtils.closeConnection(connection);
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(String.format("获取RdNode出错[ids: %s]", Arrays.toString(nodes.toArray())), e);
            }

            for (IRow rowNode : rowsTmp) {
                RdNode obj = (RdNode) rowNode;
                geoms.add(obj.getGeometry());
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DbUtils.closeQuietly(conn);
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

}
