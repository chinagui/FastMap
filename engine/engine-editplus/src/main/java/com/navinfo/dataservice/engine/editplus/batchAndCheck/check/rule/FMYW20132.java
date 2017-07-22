package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * 检查条件：
 * 非删除POI对象
 * 检查原则：
 * 检查100米范围内，官方标准化中文名称、分类、品牌均相同的数据，报LOG：PID=**与PID=**存在重复
 * Created by ly on 2017/7/7.
 */
public class FMYW20132 extends BasicCheckRule {

    public final double distance = 100.0;

    @Override
    public void runCheck(BasicObj obj) throws Exception {

        if (!obj.objName().equals(ObjectName.IX_POI)) {

            return;
        }

        IxPoiObj poiObj = (IxPoiObj) obj;

        IxPoi poi = (IxPoi) poiObj.getMainrow();

        String chain = poi.getChain() == null ? "" : poi.getChain();

        String kindCode = poi.getKindCode() == null ? "" : poi.getKindCode();

        List<Long> pendingPids = getPendingPid(poi, kindCode, chain);

        if (pendingPids.size() == 0) {

            return;
        }

        Set<String> names = getOfficeStandardCHName(poiObj);

        if (names.size() == 0) {

            return;
        }

        List<BasicObj> pendingObjs = getBasicObjByPid(pendingPids);

        for (BasicObj pendingObj : pendingObjs) {

            IxPoiObj pendingPoiObj = (IxPoiObj) pendingObj;

            Set<String> pendingNames = getOfficeStandardCHName(pendingPoiObj);

            if (names.size() == 0) {

                continue;
            }

            pendingNames.retainAll(names);

            if (pendingNames.size() > 0) {

                IxPoi pendingPoi = (IxPoi) pendingPoiObj.getMainrow();

                String strLog = "PID=" + poi.getPid() + "与PID=" + pendingPoi.getPid() + "存在重复";

                setCheckResult(poi.getGeometry(), poiObj, poi.getMeshId(), strLog);
            }
        }
    }

    /**
     * 官方标准中文名称
     */
    public Set<String> getOfficeStandardCHName(IxPoiObj poiObj) {

        Set<String> standardName = new HashSet<>();

        List<IxPoiName> names = poiObj.getIxPoiNames();

        if (names == null || names.size() == 0) {

            return standardName;
        }

        for (IxPoiName br : names) {

            if (br.getNameClass() == 1 && br.getNameType() == 1
                    && (br.getLangCode().equals("CHI") || br.getLangCode().equals("CHT"))) {

                standardName.add(br.getName() == null ? "" : br.getName());
            }
        }

        return standardName;
    }

    /**
     * 根据pid获取BasicObj,仅加载IX_POI_NAME子表
     *
     * @param pids
     * @return
     * @throws Exception
     */
    private List<BasicObj> getBasicObjByPid(List<Long> pids) throws Exception {

        List<BasicObj> poiList = new ArrayList<>();

        Set<String> tabNames = new HashSet<>();

        tabNames.add("IX_POI_NAME");

        String objType = "IX_POI";

        for (Long pid : pids) {

            BasicObj obj = ObjSelector.selectByPid(getCheckRuleCommand().getConn(), objType, tabNames, false, pid, false);

            poiList.add(obj);
        }

        return poiList;
    }

    /**
     * 根据poi几何、分类、品牌获取待定poi的pid
     *
     * @param poi
     * @return
     * @throws Exception
     */
    private List<Long> getPendingPid(IxPoi poi, String kindCode, String chain)
            throws Exception {

        Geometry buffer = poi.getGeometry().buffer(GeometryUtils.convert2Degree(distance));

        String wktBuffer = GeoTranslator.jts2Wkt(buffer, 0.00001, 5);

        List<Long> pids = new ArrayList<>();

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        String strSql = "SELECT DISTINCT P.PID FROM IX_POI P WHERE SDO_WITHIN_DISTANCE(P.GEOMETRY, SDO_GEOMETRY( ?, 8307), 'mask=anyinteract') = 'TRUE' AND P.U_RECORD <> 2 AND NVL(P.KIND_CODE, -1) = NVL(?, -1) AND NVL(P.CHAIN, -1) = NVL(?, -1)";

        try {

            pstmt = getCheckRuleCommand().getConn().prepareStatement(strSql);

            pstmt.setString(1, wktBuffer);

            pstmt.setString(2, kindCode);

            pstmt.setString(3, chain);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                long pid = resultSet.getLong("pid");

                if (pid != poi.getPid()) {
                    pids.add(pid);
                }
            }

        } catch (Exception e) {

            throw e;

        } finally {

            DBUtils.closeResultSet(resultSet);

            DBUtils.closeStatement(pstmt);
        }

        return pids;
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {

    }
}
