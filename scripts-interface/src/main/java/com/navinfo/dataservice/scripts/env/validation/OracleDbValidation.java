package com.navinfo.dataservice.scripts.env.validation;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.DbConnectConfig;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.database.OracleSchema;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.scripts.env.validation.model.OracleDbType;
import com.navinfo.navicommons.database.QueryRunner;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author xiaoxiaowen4127
 * @ClassName: OracleDbValidation
 * @date 2017年8月7日
 * @Description: OracleDbValidation.java
 */
public class OracleDbValidation implements FosEnvValidation {
    private Logger log = LoggerRepos.getLogger(OracleDbValidation.class);
    private QueryRunner runner = new QueryRunner();
    private Set<String> sequenceSet = new HashSet<>();
    private Set<String> sequenceConfigSet = new HashSet<>();
    private Set<String> functionSet = new HashSet<>();
    private Set<String> functionConfigSet = new HashSet<>();
    private Set<String> procedureSet = new HashSet<>();
    private Set<String> procedureConfigSet = new HashSet<>();
    private Set<String> typeSet = new HashSet<>();
    private Set<String> typeConfigSet = new HashSet<>();
    private Set<String> packageSet = new HashSet<>();
    private Set<String> packageConfigSet = new HashSet<>();
    private Map<String, JSONObject> tableMap = new HashMap<>();
    private Map<String, JSONObject> tableConfigMap = new HashMap<>();

    private void prepare() {
        sequenceSet.clear();
        sequenceConfigSet.clear();
        functionSet.clear();
        functionConfigSet.clear();
        procedureSet.clear();
        procedureConfigSet.clear();
        typeSet.clear();
        typeConfigSet.clear();
        packageSet.clear();
        packageConfigSet.clear();
        tableMap.clear();
        tableConfigMap.clear();
    }

    private List<Connection> createConnection(OracleDbType dbType) throws Exception {
        List<Connection> connections = new ArrayList<>();
        switch (dbType) {
            case fmSys:
                connections.add(MultiDataSourceFactory.getInstance().getSysDataSource().getConnection());
                break;
            case fmMan:
            case fmTipsIdx:
            case metaRoad:
            case nationRoad:
            case fmRender:
            case dealership:
            case fmCheck:
                DbInfo dbinfo = DbService.getInstance().getOnlyDbByBizType(dbType.toString());
                OracleSchema schema = new OracleSchema(
                        DbConnectConfig.createConnectConfig(dbinfo.getConnectParam()));
                connections.add(schema.getPoolDataSource().getConnection());
                break;
            case pidServer:
                String pids = SystemConfigFactory.getSystemConfig().getValue(PropConstant.pidServers);
                for (String pid : pids.split(";")) {
                    DbConnectConfig dc = DbConnectConfig.createConnectConfig(pid, "pidServer");
                    connections.add(MultiDataSourceFactory.getInstance().getDataSource(dc).getConnection());
                }
                break;
            case regionRoad:
                List<DbInfo> dbInfos = DbService.getInstance().getDbsByBizType(dbType.toString());
                for (DbInfo dbInfo : dbInfos) {
                    OracleSchema oracleSchema = new OracleSchema(
                            DbConnectConfig.createConnectConfig(dbInfo.getConnectParam()));
                    connections.add(oracleSchema.getPoolDataSource().getConnection());
                }
                break;
            default:
                throw new Exception("OracleDbType not support:" + dbType);
        }
        return connections;
    }

    private JSONObject loadConfig(OracleDbType dbType, String folder) throws Exception {
        log.info("load config file start");
        URL url = OracleDbValidation.class.getResource(folder + dbType + ".json");
        if (url == null) {
            return null;
        }
        String path = url.getPath();
        byte[] bytes = Files.readAllBytes(new File(path).toPath());
        String line = new String(bytes);
        JSONObject data = JSONObject.fromObject(line);
        JSONArray array = data.getJSONArray("tables");
        for (int i = 0; i < array.size(); i++) {
            JSONObject table = array.getJSONObject(i);
            String name = table.getString("name");
            tableConfigMap.put(name, table);
        }
        array = data.getJSONArray("types");
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            typeConfigSet.add(json.getString("name"));
        }
        array = data.getJSONArray("sequences");
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            sequenceConfigSet.add(json.getString("name"));
        }
        array = data.getJSONArray("functions");
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            functionConfigSet.add(json.getString("name"));
        }
        array = data.getJSONArray("packages");
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            packageConfigSet.add(json.getString("name"));
        }
        array = data.getJSONArray("procedures");
        for (int i = 0; i < array.size(); i++) {
            JSONObject json = array.getJSONObject(i);
            procedureConfigSet.add(json.getString("name"));
        }

        log.info("load config file end");
        return data;
    }

    private void loadTables(Connection conn) throws Exception {
        String indexSql = "select table_name,count(1) count from user_indexes group by table_name";
        String columnSql = "select table_name,count(1) count from user_tab_columns group  by table_name";

        ResultSetHandler<Map<String, Integer>> rsHandler = new ResultSetHandler<Map<String, Integer>>() {
            public Map<String, Integer> handle(ResultSet rs) throws SQLException {
                Map<String, Integer> indexMap = new HashMap<>();
                while (rs.next()) {
                    String name = rs.getString("table_name");
                    if (name.startsWith("TMP") || name.startsWith("TEMP") || name.startsWith("MD")
                            || name.startsWith("BAK_") || name.endsWith("_BAK") || name.contains("$")) {
                        continue;
                    }
                    int count = rs.getInt("count");
                    indexMap.put(name, count);
                }
                return indexMap;
            }
        };
        Map<String, Integer> indexMap = runner.query(conn, indexSql, rsHandler);
        Map<String, Integer> columnMap = runner.query(conn, columnSql, rsHandler);

        for (String tableName : columnMap.keySet()) {
            JSONObject table = new JSONObject();
            table.put("columnCount", columnMap.get(tableName));
            if (indexMap.containsKey(tableName)) {
                table.put("indexCount", indexMap.get(tableName));
            } else {
                table.put("indexCount", 0);
            }
            tableMap.put(tableName, table);
        }
    }

    private void loadOthers(Connection conn) throws Exception {
        String sql = "select object_name,object_type from user_objects";
        ResultSetHandler<Integer> resultSetHandler = new ResultSetHandler<Integer>() {
            public Integer handle(ResultSet rs) throws SQLException {
                while (rs.next()) {
                    String name = rs.getString("object_name");
                    String type = rs.getString("object_type");

                    if (name.startsWith("MDRS") || name.startsWith("SYS_PLSQL_")) {
                        continue;
                    }

                    if (type.equals("SEQUENCE")) {
                        sequenceSet.add(name);
                    } else if (type.equals("TYPE")) {
                        typeSet.add(name);
                    } else if (type.equals("FUNCTION")) {
                        functionSet.add(name);
                    } else if (type.equals("PROCEDURE")) {
                        procedureSet.add(name);
                    } else if (type.equals("PACKAGE")) {
                        packageSet.add(name);
                    }

                }
                return 1;
            }
        };
        runner.query(conn, sql, resultSetHandler);
    }

    private int getTableSize(String tableName, Connection conn) throws Exception {
        String sql = "select count(1) count from " + tableName;
        return runner.query(conn, sql, new ResultSetHandler<Integer>() {
            @Override
            public Integer handle(ResultSet rs) throws SQLException {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    return count;
                }
                return 0;
            }
        });
    }

    private void loadDBSchema(Connection conn) throws Exception {
        log.info("load schema start");
        loadTables(conn);
        loadOthers(conn);
        log.info("load schema end");
    }

    private void checkTables(Connection conn, ValidationResult validationResult) throws Exception {
        log.info("check tables start");
        for (String tableName : tableMap.keySet()) {
            JSONObject table = tableMap.get(tableName);
            table.getInt("indexCount");
            if (!tableConfigMap.containsKey(tableName)) {
                validationResult.errs.add(conn + ":table should not exist: " + tableName);
                continue;
            }
            JSONObject json = tableConfigMap.get(tableName);
            if (table.getInt("indexCount") != json.getInt("indexCount")) {
                validationResult.errs.add(conn + ":table index count not match: " + tableName);
            }
            if (table.getInt("columnCount") != json.getInt("columnCount")) {
                validationResult.errs.add(conn + ":table column count not match: " + tableName);
            }
            int size = json.getInt("rowCount");
            if (size > 0) {
                int rowCount = getTableSize(tableName, conn);
                if (rowCount != size) {
                    validationResult.errs.add(conn + ":table row count not match: " + tableName);
                }
            }
        }
        for (String tableName : tableConfigMap.keySet()) {
            if (!tableMap.containsKey(tableName)) {
                validationResult.errs.add(conn + ":table missing: " + tableName);
            }
        }

        log.info("check tables end");
    }

    private void checkTypes(Connection conn, ValidationResult validationResult) {
        log.info("check types start");
        for (String name : typeSet) {
            if (!typeConfigSet.contains(name)) {
                validationResult.errs.add(conn + ":type should not exist: " + name);
                continue;
            }
        }
        for (String name : typeConfigSet) {
            if (!typeSet.contains(name)) {
                validationResult.errs.add(conn + ":type missing: " + name);
            }
        }

        log.info("check types end");
    }

    private void checkSequences(Connection conn, ValidationResult validationResult) {
        log.info("check sequences start");
        for (String name : sequenceSet) {
            if (!sequenceConfigSet.contains(name)) {
                validationResult.errs.add(conn + ":sequence should not exist: " + name);
                continue;
            }
        }
        for (String name : sequenceConfigSet) {
            if (!sequenceSet.contains(name)) {
                validationResult.errs.add(conn + ":sequence missing: " + name);
            }
        }

        log.info("check sequences end");
    }

    private void checkFunctions(Connection conn, ValidationResult validationResult) {
        log.info("check functions start");
        for (String name : functionSet) {
            if (!functionConfigSet.contains(name)) {
                validationResult.errs.add(conn + ":function should not exist: " + name);
                continue;
            }
        }
        for (String name : functionConfigSet) {
            if (!functionSet.contains(name)) {
                validationResult.errs.add(conn + ":function missing: " + name);
            }
        }

        log.info("check functions end");
    }

    private void checkProcedures(Connection conn, ValidationResult validationResult) {
        log.info("check procedures start");
        for (String name : procedureSet) {
            if (!procedureConfigSet.contains(name)) {
                validationResult.errs.add(conn + ":procedure should not exist: " + name);
                continue;
            }
        }
        for (String name : procedureConfigSet) {
            if (!procedureSet.contains(name)) {
                validationResult.errs.add(conn + ":procedure missing: " + name);
            }
        }

        log.info("check procedures end");
    }

    private void checkPackages(Connection conn, ValidationResult validationResult) {
        log.info("check packages start");
        for (String name : packageSet) {
            if (!packageConfigSet.contains(name)) {
                validationResult.errs.add(conn + ":package should not exist: " + name);
                continue;
            }
        }
        for (String name : packageConfigSet) {
            if (!packageSet.contains(name)) {
                validationResult.errs.add(conn + ":packag missing: " + name);
            }
        }

        log.info("check packages end");
    }

    private void validation(OracleDbType dbType, ValidationResult result) throws Exception {
        log.info("start to validate " + dbType);

        List<Connection> connections = createConnection(dbType);

        for (Connection conn : connections) {
            prepare();
            JSONObject data = loadConfig(dbType, "/com/navinfo/dataservice/scripts/resources/dbconfig/");
            if (data == null) {
                data = loadConfig(dbType, "/");
            }
            if (data == null) {
                throw new Exception("config file not found");
            }
            loadDBSchema(conn);
            checkTables(conn, result);
            checkFunctions(conn, result);
            checkPackages(conn, result);
            checkProcedures(conn, result);
            checkTypes(conn, result);
            checkSequences(conn, result);
        }
        log.info("validate end for " + dbType);
    }

    @Override
    public ValidationResult validation() throws Exception {
        ValidationResult result = new ValidationResult();
        for (OracleDbType dbType : OracleDbType.values()) {
            validation(dbType, result);
        }
        return result;
    }
}
