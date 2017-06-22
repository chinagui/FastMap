package com.navinfo.dataservice.dao.glm.selector;

import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculator;
import com.navinfo.dataservice.bizcommons.glm.GlmGridCalculatorFactory;
import com.navinfo.dataservice.bizcommons.glm.GlmGridRefInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.geolive.GeoliveHelper;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * Created by ly on 2017/5/24.
 */
public class SearchAllObject {

    private Connection conn;

    //主对象对应表名
    private String mainTableName = "";

    //查询对象对应表名
    private String searchTableName = "";

    //主键
    private String primaryKey = "";

    //外键
    private String foreignKey = "";

    //mainTableName + "." + primaryKey
    private String mainFlag = "";

    private String uuid = null;

    private List<Object> values = new ArrayList<>();

    public SearchAllObject(Connection conn) {

        this.conn = conn;
    }

    public SearchAllObject() {

    }

    public JSONObject getGeoLiveInfo(JSONObject json) throws Exception {

        String searchType = json.getString("searchType");

        String tableName = "";

        if (json.containsKey("tableName")) {

            tableName = json.getString("tableName");
        }

        JSONObject searchResult = new JSONObject();

        searchResult.put("type", searchType);

        GeoliveHelper geoliveHelper = GeoliveHelper.getIstance();

        if (searchType.equals("PARENT_TABLE_LABLE")) {

            searchResult.put("label", geoliveHelper.getParentLabel());

        } else if (searchType.equals("TABLE_LABLE")) {

            searchResult.put("tableName", tableName);

            searchResult.put("label", geoliveHelper.getSubLabel(tableName));

        } else if (searchType.equals("TABLE_INFO")) {

            searchResult.put("table", geoliveHelper.getTableInfo(tableName));
        }

        return searchResult;
    }

    /**
     * 按条件查询主要素；
     * 1、pids 返回主表primaryKey值
     * 2、有主键的分歧子表做查询条件，pids 返回分歧子表primaryKey值
     * 3、无主键的分歧子表做查询条件，pids 返回主表primaryKey值，rowIds返回分歧子表rowId值
     *
     * @param condition
     * @return
     * @throws Exception
     */
    public JSONObject loadByElementCondition(JSONObject condition) throws Exception {

        JSONObject searchResult = new JSONObject();

        if (conn == null || uuid != null) {

            return searchResult;
        }

        init(condition);

        List<Integer> mainPids = new ArrayList<>();

        int total =  getMainObjects(condition,mainPids);

        searchResult.put("uuid", uuid);
        searchResult.put("total", total);

        if (searchTableName.equals("RD_BRANCH_REALIMAGE")
                || searchTableName.equals("RD_SERIESBRANCH")) {

            handNoPrimaryKeyBranch(mainPids, searchResult);

            searchResult.put("geoLiveType", searchTableName.toUpperCase().replace("_", ""));

        } else {

            searchResult.put("geoLiveType", mainTableName.toUpperCase().replace("_", ""));

            JSONArray pids = new JSONArray();

            for (int pid : mainPids) {

                pids.add(pid);
            }

            searchResult.put("pids", pids);
        }

        return searchResult;
    }

    private void init(JSONObject json) throws Exception {

        mainTableName = json.getString("mainTableName").toUpperCase();

        searchTableName = json.getString("searchTableName").toUpperCase();

        initPrimaryKeyBranch();

        GeoliveHelper geoliveHelper = GeoliveHelper.getIstance();

        primaryKey = geoliveHelper.getPrimaryKey(mainTableName);

        if (!mainTableName.equals(searchTableName)) {

            foreignKey = geoliveHelper.getForeignKey(searchTableName, mainTableName);
        }

        uuid = json.getString("uuid");

        mainFlag = mainTableName + "." + primaryKey;
    }

    /**
     * 将有主键的分歧子表当作主表查询（RD_BRANCH_DETAIL、RD_BRANCH_SCHEMATIC、RD_SIGNBOARD、RD_SIGNASREAL）
     */
    private void initPrimaryKeyBranch() {

        if (searchTableName.equals("RD_BRANCH_DETAIL")
                || searchTableName.equals("RD_BRANCH_SCHEMATIC")
                || searchTableName.equals("RD_SIGNBOARD")
                || searchTableName.equals("RD_SIGNASREAL")) {

            mainTableName = searchTableName;
        }
    }


    /**
     * 根据条件查询主对象Pid
     * @param json  条件json
     * @param mainPids 向前端返回的主要素pid
     * @return 符合查询条件的要素总个数
     * @throws Exception
     */
    private int getMainObjects(JSONObject json,List<Integer> mainPids) throws Exception {

        int total = 0;

        List<Integer> meshIds = new ArrayList<>(JSONArray.toCollection(json.getJSONArray("meshIds")));

        List<Integer> gridIds = new ArrayList<>(JSONArray.toCollection(json.getJSONArray("gridIds")));

        //按范围查询待定主对象Pids
        List<Integer> pendingPids = getPendingPids(meshIds, gridIds);

        //按范围未查到数据，直接返回
        if (pendingPids.size() < 1 && (meshIds.size() > 0 || gridIds.size() > 0)) {

            return total;
        }

        JSONArray jsonConditions = json.getJSONArray("conditions");

        //按照mainTableName与searchTableName关系组成查询语句
        String strSql = "SELECT DISTINCT " + mainFlag + " MAIN_PID , COUNT(DISTINCT "+mainFlag+" ) OVER(PARTITION BY 1) TOTAL FROM " + mainTableName;

        if (!mainTableName.equals(searchTableName)) {

            strSql += " , " + searchTableName;
        }

        strSql += " WHERE " + mainTableName + ".U_RECORD<>2 ";

        if (!mainTableName.equals(searchTableName)) {

            strSql += " AND " + searchTableName + ".U_RECORD<>2 AND " + searchTableName + "." + foreignKey + " = " + mainFlag;
        }

        //按待定主对象Pid组成查询条件
        if (pendingPids.size() > 0) {

            String ids = StringUtils.getInteStr(pendingPids);

            if (pendingPids.size() > 1000) {

                Clob pidClod = ConnectionUtil.createClob(conn);

                pidClod.setString(1, ids);

                strSql += " and " + mainFlag + " IN (select to_number(column_value) from table(clob_to_table(?))) ";

                values.add(pidClod);

            } else {

                strSql += " and " + mainFlag + " IN (" + ids + ") ";
            }
        }

        //按表属性字段条件组成查询条件
        for (int i = 0; i < jsonConditions.size(); i++) {

            JSONObject jsonCondition = jsonConditions.getJSONObject(i);

            strSql += interpreterCondition(jsonCondition);
        }

        strSql = splitPageSql(json, strSql);

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        try {
            pstmt = this.conn.prepareStatement(strSql);

            setPreparedStatement(pstmt);

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                int mainPid = resultSet.getInt("MAIN_PID");

                if (total == 0) {
                    total = resultSet.getInt("TOTAL");
                }

                mainPids.add(mainPid);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }

        return total;
    }

    /**
     * 根据meshId、gridId按范围查找待定主对象Pid
     * @param meshIds
     * @param gridIds
     * @return
     * @throws Exception
     */
    private List<Integer> getPendingPids(List<Integer> meshIds,List<Integer> gridIds) throws Exception {

        Set<Integer> pendingPids = new HashSet<>();

        List<String> meshWktStr = new ArrayList<>();

        for (int meshId : meshIds) {

            String wktStr = MeshUtils.mesh2WKT(String.valueOf(meshId));

            meshWktStr.add(wktStr);
        }

        List<String> gridWktStr = new ArrayList<>();

        for (int gridId : gridIds) {

            String wktStr = GridUtils.grid2Wkt(String.valueOf(gridId));

            gridWktStr.add(wktStr);
        }

        //处理特殊要素
        List<String> strSqls = new ArrayList<>();

        strSqls.addAll(getSpatialQuerySqlForSpecialObj());

        if (strSqls.size() > 0) {

            List<String> wktStrs = new ArrayList<>();

            wktStrs.addAll(meshWktStr);

            wktStrs.addAll(gridWktStr);

            for (String spatialQuerySql : strSqls) {

                for (String wktStr : wktStrs) {

                    pendingPids.addAll(selectPendingPids(spatialQuerySql, wktStr));
                }
            }

            return new ArrayList<>(pendingPids);
        }

        //按图幅处理
        if (meshIds.size() > 0) {

            String strMeshSql = getMeshIdQuerySql(meshIds);

            if (strMeshSql != null) {

                pendingPids.addAll(selectPendingPids(strMeshSql, null));

            } else {

                String spatialQuerySql = getSpatialQuerySql(null);

                for (String wktStr : meshWktStr) {

                    pendingPids.addAll(selectPendingPids(spatialQuerySql, wktStr));
                }
            }
        }

        //按grid处理
        if (gridIds.size() > 0) {

            String spatialQuerySql = getSpatialQuerySql(gridIds);

            for (String wktStr : gridWktStr) {

                pendingPids.addAll(selectPendingPids(spatialQuerySql, wktStr));
            }
        }

        return new ArrayList<>(pendingPids);
    }

    /**
     * 获取特殊要素按空间查询sql语句
     *
     * @return
     */
    private List<String> getSpatialQuerySqlForSpecialObj() {

        List<String> strSqls = new ArrayList<>();

        if (mainTableName.equals("RD_CROSS")) {

            strSqls.add("WITH TMP1 AS (SELECT DISTINCT NODE_PID FROM RD_NODE WHERE SDO_WITHIN_DISTANCE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE' AND U_RECORD != 2) SELECT DISTINCT PID PENDINGPID FROM RD_CROSS, TMP1 WHERE RD_CROSS.PID = TMP1.NODE_PID AND RD_CROSS.U_RECORD <> 2");

        } else if (mainTableName.equals("RD_SAMENODE")) {

            String strSql = "WITH TMP1 AS (SELECT DISTINCT NODE_PID FROM replace_tableName WHERE SDO_WITHIN_DISTANCE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE' AND U_RECORD != 2) SELECT DISTINCT RD_SAMENODE.GROUP_ID PENDINGPID FROM RD_SAMENODE, RD_SAMENODE_PART, TMP1 WHERE RD_SAMENODE_PART.TABLE_NAME = 'replace_tableName' AND RD_SAMENODE.GROUP_ID = RD_SAMENODE_PART.GROUP_ID AND RD_SAMENODE_PART.NODE_PID = TMP1.NODE_PID AND RD_SAMENODE.U_RECORD <> 2 AND RD_SAMENODE_PART.U_RECORD <> 2";

            strSqls.add(strSql.replace("replace_tableName", "RD_NODE"));

            strSqls.add(strSql.replace("replace_tableName", "AD_NODE"));

            strSqls.add(strSql.replace("replace_tableName", "RW_NODE"));

            strSqls.add(strSql.replace("replace_tableName", "ZONE_NODE"));

            strSqls.add(strSql.replace("replace_tableName", "LU_NODE"));

        } else if (mainTableName.equals("RD_SAMELINK")) {

            String strSql = "WITH TMP1 AS (SELECT DISTINCT LINK_PID FROM replace_tableName WHERE SDO_WITHIN_DISTANCE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE' AND U_RECORD != 2) SELECT DISTINCT RD_SAMELINK.GROUP_ID PENDINGPID FROM RD_SAMELINK, RD_SAMELINK_PART, TMP1 WHERE RD_SAMELINK_PART.TABLE_NAME = 'replace_tableName' AND RD_SAMELINK.GROUP_ID = RD_SAMELINK_PART.GROUP_ID AND RD_SAMELINK_PART.LINK_PID = TMP1.LINK_PID AND RD_SAMELINK.U_RECORD <> 2 AND RD_SAMELINK_PART.U_RECORD <> 2";

            strSqls.add(strSql.replace("replace_tableName", "RD_LINK"));

            strSqls.add(strSql.replace("replace_tableName", "AD_LINK"));

            strSqls.add(strSql.replace("replace_tableName", "RW_LINK"));

            strSqls.add(strSql.replace("replace_tableName", "ZONE_LINK"));

            strSqls.add(strSql.replace("replace_tableName", "LU_LINK"));

        } else if (searchTableName.equals("RD_BRANCH_DETAIL")
                || searchTableName.equals("RD_BRANCH_SCHEMATIC")
                || searchTableName.equals("RD_SIGNBOARD")
                || searchTableName.equals("RD_SIGNASREAL")) {

            String strSql = "WITH TMP1 AS (SELECT DISTINCT LINK_PID FROM RD_LINK WHERE SDO_WITHIN_DISTANCE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE' AND U_RECORD != 2) ";

            strSql += " SELECT DISTINCT " + primaryKey + " PENDINGPID FROM ";

            strSql += mainTableName + " , RD_BRANCH, TMP1 WHERE ";

            strSql += mainFlag + " = RD_BRANCH.BRANCH_PID AND RD_BRANCH.IN_LINK_PID = TMP1.LINK_PID AND ";

            strSql += mainTableName + ".U_RECORD <> 2 AND RD_BRANCH.U_RECORD <> 2";

            strSqls.add(strSql);
        }

        return strSqls;
    }


    /**
     * 获取按空间查询sql语句
     *
     * @return
     */
    private String getMeshIdQuerySql(List<Integer> meshIds) {

        if (meshIds.size() < 1) {

            return null;
        }

        GlmGridRefInfo glmGridRefInfo = getGlmGridRefInfo();

        List<String[]> refInfos = glmGridRefInfo.getRefInfo();


        //refInfos为空的对象有GEOMETRY字段，返回查询sql语句
        if (CollectionUtils.isEmpty(refInfos)) {

            String strSubSql = getMeshIdSql(mainTableName, meshIds);

            if (StringUtils.isEmpty(strSubSql)) {

                return null;
            }

            return "SELECT DISTINCT " + primaryKey + " pendingPid  FROM " + mainTableName + " WHERE U_RECORD != 2 " + strSubSql;
        }

        //获取与主对象关联且包含GEOMETRY字段的对象信息
        String geoTableName = refInfos.get(refInfos.size() - 1)[0];

        String geoKey = refInfos.get(refInfos.size() - 1)[1];

        String strSubSql = getMeshIdSql(geoTableName, meshIds);

        if (StringUtils.isEmpty(strSubSql)) {

            return null;
        }

        //关联几何要素查询sql
        String strQuerySql = "WITH TMP1 AS ( SELECT DISTINCT " + geoKey + " FROM " + geoTableName + " WHERE U_RECORD != 2 " + strSubSql + " ) ";

        strQuerySql += " SELECT DISTINCT " + mainFlag + " pendingPid FROM " + mainTableName;
        //from 添加关联表TableName，包含GEOMETRY字段的表以TMP1替换
        strQuerySql += " , TMP1  ";

        for (int i = 0; i < refInfos.size() - 1; i++) {

            strQuerySql += " , " + refInfos.get(i)[0];
        }

        String ferKey = glmGridRefInfo.getGridRefCol();

        //根据refInfos添加各表之间的关联条件
        strQuerySql += " where " + mainTableName + "." + ferKey + " = ";

        //多表关联时，处理中间关联表
        for (int i = 0; i < refInfos.size() - 1; i++) {

            strQuerySql += refInfos.get(i)[0] + "." + refInfos.get(i)[1] + " and " + refInfos.get(i)[0] + "." + refInfos.get(i)[2] + " = ";
        }

        strQuerySql += " TMP1." + geoKey + " ";

        //排除各表U_RECORD=2的数据
        strQuerySql += " and " + mainTableName + ".U_RECORD<>2 ";

        for (int i = 0; i < refInfos.size() - 1; i++) {

            strQuerySql += " and  " + refInfos.get(i)[0] + ".U_RECORD<>2 ";
        }

        return strQuerySql;
    }


    /**
     * 获取Grid映射关系信息
     *
     * @return
     */
    private GlmGridRefInfo getGlmGridRefInfo() {
        String gdbVersion = SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion);

        GlmGridCalculator gridCalculator = GlmGridCalculatorFactory.getInstance().create(gdbVersion);

        GlmGridRefInfo glmGridRefInfo = gridCalculator.getGlmGridRefInfo(mainTableName);

        return glmGridRefInfo;
    }

    /**
     * 获取按空间查询sql语句，grid列表有数据时，按照guid所在图幅筛选
     *
     * @param gridIds grid列表
     * @return
     */
    private String getSpatialQuerySql(List<Integer> gridIds) {

        List<Integer> meshIds = new ArrayList<>();

        if (gridIds != null) {

            for (int gridId : gridIds) {

                int meshId = gridId / 100;

                if (!meshIds.contains(meshId)) {

                    meshIds.add(meshId);
                }
            }
        }

        GlmGridRefInfo glmGridRefInfo = getGlmGridRefInfo();

        List<String[]> refInfos = glmGridRefInfo.getRefInfo();

        String strSubSql = getMeshIdSql(mainTableName, meshIds);

        //refInfos为空的对象有GEOMETRY字段，返回查询sql语句
        if (CollectionUtils.isEmpty(refInfos)) {

            return "SELECT DISTINCT " + primaryKey + " pendingPid  FROM " + mainTableName + " WHERE SDO_WITHIN_DISTANCE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE' AND U_RECORD != 2 " + strSubSql;
        }

        //获取与主对象关联且包含GEOMETRY字段的对象信息
        String geoTableName = refInfos.get(refInfos.size() - 1)[0];

        String geoKey = refInfos.get(refInfos.size() - 1)[1];

        strSubSql = getMeshIdSql(geoTableName, meshIds);

        //关联几何要素查询sql
        String strQuerySql = "WITH TMP1 AS ( SELECT DISTINCT " + geoKey + " FROM " + geoTableName + " WHERE SDO_WITHIN_DISTANCE(GEOMETRY, SDO_GEOMETRY(:1, 8307), 'DISTANCE=0') = 'TRUE' AND U_RECORD != 2 " + strSubSql + " )  ";

        strQuerySql += " SELECT DISTINCT " + mainFlag + " pendingPid FROM " + mainTableName;
        //from 添加关联表TableName，包含GEOMETRY字段的表以TMP1替换
        strQuerySql += " , TMP1  ";

        for (int i = 0; i < refInfos.size() - 1; i++) {

            strQuerySql += " , " + refInfos.get(i)[0];
        }

        String ferKey = glmGridRefInfo.getGridRefCol();

        //根据refInfos添加各表之间的关联条件
        strQuerySql += " where " + mainTableName + "." + ferKey + " = ";

        //多表关联时，处理中间关联表
        for (int i = 0; i < refInfos.size() - 1; i++) {

            strQuerySql += refInfos.get(i)[0] + "." + refInfos.get(i)[1] + " and " + refInfos.get(i)[0] + "." + refInfos.get(i)[2] + " = ";
        }

        strQuerySql += " TMP1." + geoKey + " ";

        //排除各表U_RECORD=2的数据
        strQuerySql += " and " + mainTableName + ".U_RECORD<>2 ";

        for (int i = 0; i < refInfos.size() - 1; i++) {

            strQuerySql += " and  " + refInfos.get(i)[0] + ".U_RECORD<>2 ";
        }

        return strQuerySql;
    }

    /**
     * 获取按MeshId查询的sql语句条件
     *
     * @param tableName 表名
     * @param meshIds   图幅号列表
     * @return
     */
    private String getMeshIdSql(String tableName, List<Integer> meshIds) {

        String strSql = "";

        if (meshIds.size() < 0) {

            return strSql;
        }

        if (tableName.toUpperCase().equals("RD_LINK")
                || tableName.toUpperCase().equals("RW_LINK")
                || tableName.toUpperCase().equals("IX_POI")
                || tableName.toUpperCase().equals("AD_FACE")
                || tableName.toUpperCase().equals("ZONE_FACE")
                || tableName.toUpperCase().equals("LC_FACE")
                || tableName.toUpperCase().equals("LU_FACE")
                || tableName.toUpperCase().equals("CMG_BUILDFACE")
                ) {
            strSql = " and " + tableName + ".MESH_ID IN ";
        } else if (tableName.toUpperCase().equals("RW_NODE")
                || tableName.toUpperCase().equals("AD_NODE")
                || tableName.toUpperCase().equals("ZONE_NODE")
                || tableName.toUpperCase().equals("LC_NODE")
                || tableName.toUpperCase().equals("LU_NODE")
                || tableName.toUpperCase().equals("CMG_BUILDNODE")
                ) {
            strSql = " and " + tableName + ".NODE_PID=" + tableName + "_MESH.NODE_PID " + tableName + "_MESH.MESH_ID IN ";
        } else if (tableName.toUpperCase().equals("AD_LINK")
                || tableName.toUpperCase().equals("ZONE_LINK")
                || tableName.toUpperCase().equals("LC_LINK")
                || tableName.toUpperCase().equals("LU_LINK")
                || tableName.toUpperCase().equals("CMG_BUILDLINK")
                ) {
            strSql = " and " + tableName + ".LINK_PID=" + tableName + "_MESH.LINK_PID " + tableName + "_MESH.MESH_ID IN ";
        }

        if (StringUtils.isNotEmpty(strSql)) {

            strSql += " ( " + StringUtils.getInteStr(meshIds) + " )";
        }

        return strSql;
    }

    /**
     * 查询待定主要素Pid
     *
     * @param spatialQuerySql 查询语句
     * @param wktStr          查询范围
     * @return
     * @throws Exception
     */
    private List<Integer> selectPendingPids(String spatialQuerySql, String wktStr) throws Exception {

        PreparedStatement pstmt = null;

        ResultSet resultSet = null;

        List<Integer> pendingPids = new ArrayList<>();

        try {

            pstmt = this.conn.prepareStatement(spatialQuerySql);

            if (wktStr != null) {

                pstmt.setString(1, wktStr);
            }

            resultSet = pstmt.executeQuery();

            while (resultSet.next()) {

                int pendingPid = resultSet.getInt("pendingPid");

                pendingPids.add(pendingPid);
            }
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);

            DBUtils.closeStatement(pstmt);
        }

        return pendingPids;
    }

    /**
     * 解析表属性字段条件
     *
     * @param jsonCondition
     * @return
     * @throws Exception
     */
    private String interpreterCondition(JSONObject jsonCondition) throws Exception {

        //字段类型
        String fieldType = jsonCondition.getString("fieldType");

        //操作符
        String operator = jsonCondition.getString("operator");

        //字段名
        String fieldName = jsonCondition.getString("fieldName");

        String strSql = "";

        if (operator.equals("IN")) {

            JSONArray valueArray = jsonCondition.getJSONArray("value");

            if (valueArray.size() > 0) {

                strSql = " and " + searchTableName + "." + fieldName + " " + operator + " ( ? ";

                for (int i = 0; i < valueArray.size() - 1; i++) {

                    strSql += " , ? ";
                }

                strSql += " ) ";
            }

            handleValues(jsonCondition, fieldType);

        } else {

            strSql = " and " + searchTableName + "." + fieldName + " " + operator + " ? ";

            handleValue(jsonCondition, fieldType);
        }

        return strSql;
    }


    //处理一组值
    private void handleValues(JSONObject jsonCondition, String fieldType) throws Exception {

        JSONArray jsonValues = jsonCondition.getJSONArray("value");

        for (int i = 0; i < jsonValues.size(); i++) {

            if (fieldType.equals("Integer")) {

                values.add(jsonValues.getInt(i));

            } else if (fieldType.equals("Double")) {

                values.add(jsonValues.getDouble(i));

            } else if (fieldType.equals("String")) {

                values.add(jsonValues.getString(i));
            } else {
                throw new Exception("不支持的数据类型：" + fieldType);
            }
        }
    }

    /**
     * 处理单值
     *
     * @param jsonCondition
     * @param fieldType
     * @throws Exception
     */
    private void handleValue(JSONObject jsonCondition, String fieldType) throws Exception {

        if (fieldType.equals("Integer")) {

            values.add(jsonCondition.getInt("value"));

        } else if (fieldType.equals("Double")) {

            values.add(jsonCondition.getDouble("value"));

        } else if (fieldType.equals("String")) {

            values.add(jsonCondition.getString("value"));

        } else {

            throw new Exception("不支持的数据类型：" + fieldType);
        }
    }

    /**
     * 组成分页sql
     *
     * @param json
     * @param strSql
     * @return
     */
    private String splitPageSql(JSONObject json, String strSql) {

        int pageNum = json.getInt("pageNum");

        int pageSize = json.getInt("pageSize");

        int startRow = (pageNum - 1) * pageSize + 1;

        int endRow = pageNum * pageSize;

        // 排序
        strSql += " order by MAIN_PID desc ";

        //pageSize
        String strSqlSize = " Select TMP.MAIN_PID ,TMP.TOTAL,ROWNUM ROWNO from ( " + strSql + " ) TMP WHERE ROWNUM <= " + endRow + " ";

        //pageNum
        String StrSqlNum = " Select TABLE_ALIAS.MAIN_PID,TABLE_ALIAS.TOTAL from ( " + strSqlSize + " ) TABLE_ALIAS WHERE TABLE_ALIAS.ROWNO >= " + startRow;

        return StrSqlNum;
    }

    /**
     * PreparedStatement添加参数值
     *
     * @param pstmt
     * @throws Exception
     */
    private void setPreparedStatement(PreparedStatement pstmt) throws Exception {

        for (int i = 0; i < values.size(); i++) {

            Object obj = values.get(i);

            if (obj instanceof Integer) {

                pstmt.setInt(i + 1, Integer.parseInt(String.valueOf(obj)));

            } else if (obj instanceof Double) {

                pstmt.setDouble(i + 1, Double.parseDouble(String.valueOf(obj)));

            } else if (obj instanceof String) {

                pstmt.setString(i + 1, String.valueOf(obj));

            } else if (obj instanceof Clob) {

                pstmt.setClob(i + 1, (Clob) obj);
            } else {

                throw new Exception("不支持的数据类型：" + obj.toString());
            }
        }
    }

    /**
     * 处理无主键的分歧子表（RD_BRANCH_REALIMAGE、RD_SERIESBRANCH）
     *
     * @param mainPids
     * @return
     * @throws Exception
     */
    private void handNoPrimaryKeyBranch(List<Integer> mainPids, JSONObject json) throws Exception {

        JSONArray rowIds = new JSONArray();

        JSONArray pids = new JSONArray();

        RdBranchSelector selector = new RdBranchSelector(this.conn);

        List<IRow> rows = new ArrayList<>();

        if (searchTableName.equals("RD_BRANCH_REALIMAGE")) {

            rows = selector.getsubTable(ObjType.RDBRANCHREALIMAGE, mainPids, false);

        } else if (searchTableName.equals("RD_SERIESBRANCH")) {

            rows = selector.getsubTable(ObjType.RDSERIESBRANCH, mainPids, false);
        }

        //一个分歧最多只能有一条子表数据（RD_BRANCH_REALIMAGE、RDSERIESBRANCH），不考虑错误数据的情况
        Map<Integer, String> rowMap = new HashMap<>();

        for (IRow row : rows) {

            rowMap.put(row.parentPKValue(), row.rowId());
        }

        //保持mainPids顺序
        for (int mainPid : mainPids) {

            if (rowMap.containsKey(mainPid)) {

                rowIds.add(rowMap.get(mainPid));

                pids.add(mainPid);
            }
        }

        json.put("pids", pids);

        json.put("rowIds", rowIds);
    }
}
