package com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLinkForm;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.sql.DBUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * @Title: GLM60226
 * @Package: com.navinfo.dataservice.engine.editplus.batchAndCheck.check.rule
 * @Description:
 *  POI的显示坐标与引导坐标之间的连线（以显示坐标和引导坐标为端点的线段），不应与道路Link相交，否则报log
 *  特殊说明：
 *  1.若与该POI自身的引导Link相交，不报log；
 *  2.若与连线相交的道路为高架、隧道、公交专用道路、跨线天桥、跨线地道属性，不报log；
 *  3.若与连线相交的道路为高速（1级道路）、城市高速（2级道路）、10级路、或8级道路（非辅路），不报log。
 *  4.若POI的种别230126、230127、230128，230105不报log。
 *  5.若POI的显示和引导坐标跨未验证道路，不报LOG。
 *  6.POI的种别为区域性12类主点（主点：父子关系中的父）：动物园（180308）、植物园（180309）、高尔夫球场（180105）、高尔夫练习场（180106）、游乐园（180307）
 *    、公园（180304）、港口（230125）、火车站（230103）、大学（160105）、景区（180400）、滑雪场（180104），不报log。
 * @Author: Crayeres
 * @Date: 7/7/2017
 * @Version: V1.0
 */
public class GLM60226 extends BasicCheckRule {

    /**
     * 允许作为父关系的POI种别
     */
    private final static List<String> ALLOW_PARENT_KINDCODE = new ArrayList(){{
        add("180308"); add("180309"); add("180105"); add("180106"); add("180307"); add("180304");
        add("230125"); add("230103"); add("160105"); add("180400"); add("180104");
    }};

    /**
     * 允许相交的RDLINK类别
     */
    private final static List<Integer> ALLOW_LINK_KIND = new ArrayList(){{
        add(1); add(2); add(10);
    }};

    /**
     * 允许相交的RDLINK形态
     */
    private final static List<Integer> ALLOW_LINK_FORM = new ArrayList() {{
        add(16); add(17); add(22); add(31); add(32);
    }};

    @Override
    public void runCheck(BasicObj obj) throws Exception {
    }

    @Override
    public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
    }

    @Override
    public void run() throws Exception {
        Map<Long, IxPoiObj> map = new HashMap<>();

        for (Map.Entry<Long, BasicObj> entry : getRowList().entrySet()) {
            BasicObj basicObj = entry.getValue();

            if (basicObj.objName().equals(ObjectName.IX_POI) && !basicObj.opType().equals(OperationType.DELETE)) {
                IxPoiObj poiObj = (IxPoiObj) basicObj;
                IxPoi poi = (IxPoi) poiObj.getMainrow();

                if (StringUtils.isNotEmpty(poi.getKindCode())) {
                    map.put(basicObj.objPid(), poiObj);
                }
            }
        }

        if (map.isEmpty()) {
            return;
        }

        Connection conn = getCheckRuleCommand().getConn();

        List<Clob> values = new ArrayList<>();

        String pidString;
        if (map.size() > 1000) {
            Clob clob = ConnectionUtil.createClob(conn);
            clob.setString(1, StringUtils.join(map.keySet(), ","));
            pidString = " PID IN (select to_number(column_value) from table(clob_to_table(?)))";
            values.add(clob);
        } else {
            pidString = " PID IN (" + StringUtils.join(map.keySet(), ",") + ")";
        }

        String sql = "SELECT T1.LINK_PID, T1.KIND, T1.IS_VIADUCT, T1.DEVELOP_STATE, T2.KIND_CODE, T2.PID" +
                "  FROM RD_LINK T1, IX_POI T2" + 
                " WHERE T2." + pidString +
                "   AND T2.KIND_CODE NOT IN ('230126', '230127', '230128', '230105')" + 
                "   AND T1.LINK_PID <> T2.LINK_PID" + "   AND T1.U_RECORD <> 2" + 
                "   AND SDO_RELATE(T1.GEOMETRY," + 
                "                  SDO_GEOMETRY('LINESTRING(' || T2.GEOMETRY.SDO_POINT.X || ' ' ||" + 
                "                               T2.GEOMETRY.SDO_POINT.Y || ' , ' ||" + 
                "                               T2.X_GUIDE || ' ' || T2.Y_GUIDE || ')'," + 
                "                               8307)," + 
                "                  'mask=011001111+001011111+101011111+100011011') = 'TRUE'" + 
                "   AND T2.U_RECORD <> 2";

        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try {
            pstmt = conn.prepareStatement(sql);
            if (CollectionUtils.isNotEmpty(values)) {
                for (int i = 0; i < values.size(); i++) {
                    pstmt.setClob(i + 1, values.get(i));
                }
            }

            resultSet = pstmt.executeQuery();

            Set<String> filters = new HashSet<>();

            while (resultSet.next()) {
                boolean flag = true;

                int kind = resultSet.getInt("KIND");
                if (ALLOW_LINK_KIND.contains(Integer.valueOf(kind))) {
                    flag = false;
                }

                int isViaduct = resultSet.getInt("IS_VIADUCT");
                if (1 == isViaduct) {
                    flag = false;
                }

                int developState = resultSet.getInt("DEVELOP_STATE");
                if (2 == developState) {
                    flag = false;
                }

                int linkPid = resultSet.getInt("LINK_PID");
                List<IRow> forms = new AbstractSelector(RdLinkForm.class, conn).loadRowsByParentId(linkPid, false);


                if (8 == kind) {
                    boolean hasRoads = false;
                    for (IRow row : forms) {
                        RdLinkForm form = (RdLinkForm) row;
                        if (34 == form.getFormOfWay()) {
                            hasRoads = true;
                        }
                    }

                    if (!hasRoads) {
                        flag = false;
                    }
                }

                for (IRow row : forms) {
                    RdLinkForm form  = (RdLinkForm) row;
                    if (ALLOW_LINK_FORM.contains(Integer.valueOf(form.getFormOfWay()))) {
                        flag = false;
                    }
                }

                String kindCode = resultSet.getString("KIND_CODE");

                Long pid = resultSet.getLong("PID");

                if (ALLOW_PARENT_KINDCODE.contains(kindCode)) {
                    IxPoiObj poiObj = map.get(pid);
                    if (CollectionUtils.isNotEmpty(poiObj.getIxPoiChildrens())) {
                        flag = false;
                    }
                }

                if (flag) {
                    String targets = String.format("[IX_POI,%s]", pid);
                    if (!filters.contains(targets)) {
                        filters.add(targets);
                        setCheckResult("", targets, 0);
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
    }
}
