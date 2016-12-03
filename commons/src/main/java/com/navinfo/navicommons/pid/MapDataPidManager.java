package com.navinfo.navicommons.pid;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import oracle.jdbc.OracleTypes;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
import com.navinfo.navicommons.pid.PidResponse.PidSeg;
import com.thoughtworks.xstream.XStream;

/**
 * PID管理类
 *
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-7-26
 */


public class MapDataPidManager {
    private static final transient Logger log = Logger.getLogger(MapDataPidManager.class);

    private DataSource dmsDataSource;


    public MapDataPidManager(DataSource dmsDataSource) {
        this.dmsDataSource = dmsDataSource;
    }

    /**
     * 回收pid
     *
     * @param pidSegs
     * @param clientId
     * @throws SQLException
     */
    public void recyclePid(List<PidResponse.PidSeg> pidSegs, String clientId) throws SQLException {
        Connection conn = dmsDataSource.getConnection();
        QueryRunner runner = new QueryRunner();
        PreparedStatement stmt = null;
        try {
            String uuid = UUID.randomUUID().toString().replace("-", "");
            runner.update(conn, "INSERT INTO PID_APPLY_RECORD" +
                    " (RECORD_ID, CLIENT_ID, APPLY_TIME, LIMIT, TABLE_NAME,RECYCLE)" +
                    "    VALUES(?,?,sysdate,0,?,1) ", uuid, clientId, "not defined");
            String sql = "insert into PID_SEGMENT_USAGE(USAGE_ID,RECORD_ID,TABLE_NAME,START_NUM,END_NUM,STATUS)" +
                    "values(?,?,?,?,?,0)";

            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < pidSegs.size(); i++) {
                PidResponse.PidSeg pidSeg = pidSegs.get(i);
                String x = UUID.randomUUID().toString().replace("-", "");
                stmt.setString(1, x);
                stmt.setString(2, uuid);
                stmt.setString(3, pidSeg.getPidType());
                stmt.setLong(4, pidSeg.getStartNum());
                stmt.setLong(5, pidSeg.getEndNum());
                stmt.addBatch();
                if (i % 100 == 0) {
                    stmt.executeBatch();
                    stmt.clearBatch();
                }

            }
            stmt.executeBatch();
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.commitAndCloseQuietly(conn);
        }

    }

    private List<String> getAllPIDTable(Connection conn) throws SQLException {
        String sql = "SELECT TABLE_NAME FROM PID_MAX_PER_TABLE";
        QueryRunner runner = new QueryRunner();
        List<String> tableNames;
        tableNames = runner.query(conn, sql, new ResultSetHandler<List<String>>() {
            public List<String> handle(ResultSet rs) throws SQLException {
                List<String> tableNames = new ArrayList<String>();
                while (rs.next()) {
                    tableNames.add(rs.getString(1));
                }
                return tableNames;
            }
        });

        return tableNames;
    }

    public List<PidResponse.PidSeg> applyPid(String clientId, String tableName, int limit) throws Exception {
        return this.applyPid(clientId, tableName, limit, null, null);
    }

    /**
     * PID申请
     *
     * @param clientId
     * @param tableName
     * @param limit
     * @return
     */
    public List<PidResponse.PidSeg> applyPid(Connection conn, String clientId, String tableName, int limit) throws Exception {
       return this.applyPid(conn, clientId, tableName, limit, null, null);

    }
    /**
     * PID 分配
     * @param clientId
     * @param tableName
     * @param limit
     * @return
     * @throws Exception
     */
    public String pidDistribute(String clientId, String tableName, int limit,String remark) throws Exception {
        PidDistributeResponse response = PidDistributeResponse.getDefaultResponse();
        List<PidResponse.PidSeg> allPidSegs = new ArrayList<PidResponse.PidSeg>();
        Connection conn = null;
        String pidDistributeXml=null;
        try {
            conn = dmsDataSource.getConnection();
            if (StringUtils.isBlank(tableName)) {
                List<String> tables = getAllPIDTable(conn);
                for (int i = 0; i < tables.size(); i++) {
                    tableName = tables.get(i);

                    //共用一个PID类型
                    if("TMC_POINT".equals(tableName) || "TMC_LINE".equals(tableName) || "TMC_AREA".equals(tableName)){
                        continue;
                    }
                    List<PidResponse.PidSeg> pidSegs = applyPid(conn, clientId, tableName, limit);
                    allPidSegs.addAll(pidSegs);
                }
            } else 
            {
                    String [] tableNames = tableName.split(",");
                    for(String aTableName : tableNames)
                    {
                    List<PidResponse.PidSeg> pidSegs = applyPid(conn, clientId, aTableName, limit);
                    allPidSegs.addAll(pidSegs);
                    }
            }
            response.setPids(allPidSegs);
            response.setLimit(limit);
            response.setClientId(clientId);
            Date date = new java.util.Date();
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            response.setCreateTime(sdf.format(date));
            response.setRemark(remark);
            response.setDistributeId(UUID.randomUUID().toString().replaceAll("-", ""));
            //保存分配历史
            pidDistributeXml = saveDistributeHistory(conn,response);
        } catch (Exception e) {
            DbUtils.rollback(conn);
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }


        return response.getDistributeId();
    
    }
    
    
    /**
     * PID 分配,(fastMap使用)
     * @param clientId
     * @param tableName
     * @param limit
     * @return
     * @throws Exception
     */
    public List<PidResponse.PidSeg> pidDistribute2fastMap(String clientId, String tableName, int limit) throws Exception {
        PidDistributeResponse response = PidDistributeResponse.getDefaultResponse();
        List<PidResponse.PidSeg> allPidSegs = new ArrayList<PidResponse.PidSeg>();
        Connection conn = null;
        String pidDistributeXml=null;
        try {
            conn = dmsDataSource.getConnection();
            if (StringUtils.isBlank(tableName)) {
                List<String> tables = getAllPIDTable(conn);
                for (int i = 0; i < tables.size(); i++) {
                    tableName = tables.get(i);

                    //共用一个PID类型
                    if("TMC_POINT".equals(tableName) || "TMC_LINE".equals(tableName) || "TMC_AREA".equals(tableName)){
                        continue;
                    }
                    List<PidResponse.PidSeg> pidSegs = applyPid(conn, clientId, tableName, limit);
                    allPidSegs.addAll(pidSegs);
                }
            } else 
            {
                    String [] tableNames = tableName.split(",");
                    for(String aTableName : tableNames)
                    {
                    List<PidResponse.PidSeg> pidSegs = applyPid(conn, clientId, aTableName, limit);
                    allPidSegs.addAll(pidSegs);
                    }
            }
            response.setPids(allPidSegs);
            response.setLimit(limit);
            response.setClientId(clientId);
            Date date = new java.util.Date();
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            response.setCreateTime(sdf.format(date));
            response.setDistributeId(UUID.randomUUID().toString().replaceAll("-", ""));
            //保存分配历史
            pidDistributeXml = saveDistributeHistory(conn,response);
        } catch (Exception e) {
            DbUtils.rollback(conn);
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }


        return allPidSegs ;
    
    }
    public String saveDistributeHistory(Connection conn,PidDistributeResponse response) throws SQLException{
        
        XStream xs = new XStream();  
        xs.processAnnotations(PidDistributeResponse.class);  
        StringBuffer buf= new StringBuffer("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        String xml=buf.append(xs.toXML(response)).toString();
        QueryRunner query=new QueryRunner();
        query.update(conn, "insert into pid_distribute_record(distribute_id,create_time,client_id,limit,remark,xml)values(?,to_date(?,'yyyy-MM-dd HH24:mi:ss'),?,?,?,?)",
                response.getDistributeId(),response.getCreateTime(),response.getClientId(),response.getLimit(),response.getRemark(),xml);
        return xml;
    }
    @SuppressWarnings("unchecked")
    public String getDistributeXml(String distributeId) throws SQLException{
        String xml=null;
        QueryRunner query=new QueryRunner();
        xml=query.query(dmsDataSource,"select * from pid_distribute_record t where t.distribute_id=?", 1,new ResultSetHandler<String>(){

            public String handle(ResultSet rs) throws SQLException {
                String xml=null;
                if(rs.next()){
                    xml=rs.getString("xml");
                }
                return xml;
            }},distributeId);
        return xml;
    }
    @SuppressWarnings("unchecked")
    public Page pidDistributeList(int currentPageNum) throws Exception{
        QueryRunner query=new QueryRunner();
        final Page page = new Page(currentPageNum);
        query.query(currentPageNum, 20, dmsDataSource, "select distribute_id,create_time,client_id,limit,remark from pid_distribute_record order by create_time desc", new ResultSetHandler(){

            public Object handle(ResultSet rs) throws SQLException {
                List list = new ArrayList();
                page.setResult(list);
                SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                while(rs.next()){
                    HashMap map = new HashMap();
                    page.setTotalCount(rs.getInt(QueryRunner.TOTAL_RECORD_NUM));
                    map.put("distribute_id", rs.getString("distribute_id"));
                    map.put("create_time", sdf.format(rs.getTimestamp("create_time")));
                    map.put("client_id", rs.getString("client_id"));
                    map.put("limit", rs.getString("limit"));
                    map.put("remark", rs.getString("remark"));
                    list.add(map);
                }
                return null;
            }
            
        });
        return page;
    }

    public static void main(String[] args) {


    }

    @SuppressWarnings("unchecked")
    public void pidImport(String distributeId, String xml,final List<String> returnPidRepeatList) throws Exception {
        Connection conn=null;
        QueryRunner query=new QueryRunner();
        //解析xml
        XStream xs = new XStream();  
        xs.processAnnotations(PidDistributeResponse.class);  
        final PidDistributeResponse pidDistributeResponse=new PidDistributeResponse(1,"");
        xs.fromXML(xml,pidDistributeResponse);
        //检查分配批次是否已存在
        int exists = query.queryForInt(dmsDataSource, "select count(1) from pid_distribute_record where distribute_id=?", distributeId);
        if(exists>0){
            throw new ServiceException("分配批次号已存在，不能导入");
        }
        try {
            conn=dmsDataSource.getConnection();
            List<PidSeg> pids = pidDistributeResponse.getPids();
            //检查PID段是否与存在的号段重叠
            for (final PidSeg pid : pids ) {
                query.query(conn, "select t.table_name,t.start_num,t.end_num from pid_segment_usage t " + 
                        "      where t.status=1 and t.table_name=? and ((t.start_num between ? and ? ) or (t.end_num between ? and ?))", 10,new ResultSetHandler() {

                            public Object handle(ResultSet rs) throws SQLException {
                                while(rs.next()){
                                    String msg="导入号段： "+pid.getPidType()+":"+pid.getStartNum()+"->"+pid.getEndNum();
                                    msg+="，库中的号段： "+rs.getString("table_name")+":"+rs.getString("start_num")+"->"+rs.getString("end_num");
                                    returnPidRepeatList.add(msg);
                                }
                                return null;
                            }
                        },
                        pid.getPidType(),pid.getStartNum(),pid.getEndNum(),pid.getStartNum(),pid.getEndNum());
            }
            if(returnPidRepeatList.size()>0){
                throw new ServiceException("导入的号段与库中的号段重复，不能导入");
            }
            //写入分配批次号
            query.update(conn, "insert into pid_distribute_record(distribute_id,create_time,client_id,limit,remark,xml)values(?,to_date(?,'yyyy-MM-dd HH24:mi:ss'),?,?,?,?)",
                    pidDistributeResponse.getDistributeId(),pidDistributeResponse.getCreateTime(),pidDistributeResponse.getClientId(),pidDistributeResponse.getLimit(),pidDistributeResponse.getRemark(),xml);
            
            for (final PidSeg pid : pids ) {
                String record_id=UUID.randomUUID().toString().replaceAll("-", "");
                String usage_id=UUID.randomUUID().toString().replaceAll("-", "");
                //写入申请记录
                query.update(conn, "insert into pid_apply_record(record_id,client_id,apply_time,limit,table_name,recycle)" +
                        "values(?,?,?,?,?,?)",record_id,"PID导入",new java.sql.Timestamp(new Date().getTime()),(pid.getEndNum()-pid.getStartNum()+1),pid.getPidType(),"1");
                //写入号段表
                query.update(conn, "insert into pid_segment_usage(usage_id,record_id,table_name,start_num,end_num,status)" +
                        "values(?,?,?,?,?,?)",usage_id,record_id,pid.getPidType(),pid.getStartNum(),pid.getEndNum(),0);
                //更新表最大值
                query.update(conn, "update pid_max_per_table t set pid_max_value=?  where t.table_name=? and t.pid_max_value<?",
                        pid.getEndNum(),pid.getPidType(),pid.getEndNum());
                
            }
        } catch (Exception e) {
            DbUtils.rollback(conn);
            log.error("pid 导入出错："+e.getMessage(),e);
            throw e;
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
        
    }

    public List<PidSeg> applyPid(String clientId, String tableName, Integer limit, String clientIp, String clientTaskId) throws Exception {
        List<PidResponse.PidSeg> allPidSegs = new ArrayList<PidResponse.PidSeg>();
        Connection conn = null;
        try {
            conn = dmsDataSource.getConnection();
            if (StringUtils.isBlank(tableName)) {
                List<String> tables = getAllPIDTable(conn);
                for (int i = 0; i < tables.size(); i++) {
                    tableName = tables.get(i);
                    List<PidResponse.PidSeg> pidSegs = applyPid(conn, clientId, tableName, limit,clientIp,clientTaskId);
                    allPidSegs.addAll(pidSegs);
                }
            } else {
                List<PidResponse.PidSeg> pidSegs = applyPid(conn, clientId, tableName, limit,clientIp,clientTaskId);
                allPidSegs.addAll(pidSegs);
            }
        } catch (Exception e) {
            DbUtils.rollback(conn);
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }


        return allPidSegs;
    }
    final static String SEGMENT_CONTINUE_NO = "no";
    private List<PidSeg> applyPid(Connection conn, String clientId, String tableName, Integer limit, String clientIp,
        String clientTaskId) throws Exception {
        tableName = tableName.toUpperCase();
        CallableStatement cs = null;
        List<PidResponse.PidSeg> result = new ArrayList<PidResponse.PidSeg>();
        String sqlStr = "{call DMS_PID_MAN.APPLY_PID(?,?,?,?,?,?,?)}";

        ResultSet rs = null;
        try {

            cs = conn.prepareCall(sqlStr);
            cs.setString(1, tableName);
            cs.setInt(2, limit);
            cs.setString(3, clientId);
            cs.registerOutParameter(4, OracleTypes.CURSOR);            
            cs.setString(5, SEGMENT_CONTINUE_NO);
            cs.setString(6, clientIp);
            cs.setString(7, clientTaskId);
            cs.execute();
            rs = (ResultSet) cs.getObject(4);
            if (rs != null && !rs.isClosed()) {
                while (rs.next()) {
                    int startNum = rs.getInt(1);
                    int endNum = rs.getInt(2);
                    log.debug(tableName + " PID 段" + startNum + "->" + endNum);
                    PidResponse.PidSeg pidSeg = new PidResponse.PidSeg(tableName, startNum, endNum);
                    result.add(pidSeg);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;

        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(cs);
        }


        return result;
    }


}
