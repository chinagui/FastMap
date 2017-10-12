package com.navinfo.dataservice.scripts;

import java.io.File;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.SingleBatchSelRsHandler;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

/** 
* @ClassName: BatchAddressSplit
* @author: zhangpengpeng
* @date: 2017年9月29日
* @Desc: 
* 1.对日落月未做地址拆分的数据执行该临时脚本
* 2.执行符合条件的pid(未执行日落月的拆分的pid,并且在精编状态表中满足条件1.中文地址拆分作业项FM-A09-01,commonHandler为空的
* 				 或者2.英文地址作业项FM-YW-20-018,commonHandler为空的)
* 3.执行FM-BAT-20-110,FM-BAT-20-125
*/
public class BatchAddressSplit {
	private static final Logger logger = LoggerRepos.getLogger(BatchAddressSplit.class);
	
	private BatchAddressSplit(){
	}

    private Integer dbId;
    
    private class TranslateHandler implements ResultSetHandler<List<Long>> {
        @Override
        public List<Long> handle(ResultSet rs) throws SQLException {
            List<Long> pids = new ArrayList<>();
            while (rs.next()) {
                pids.add(rs.getLong("PID"));
            }
            return pids;
        }
    }
    
    public List<Long> getPids(Connection conn)  throws Exception{
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append("WITH T AS ");
    	sb.append(" (SELECT N.POI_PID PID ");
    	sb.append("     FROM IX_POI_ADDRESS N");
    	sb.append("    WHERE N.FULLNAME IS NULL");
    	sb.append("  UNION ");
    	sb.append("  SELECT N1.POI_PID PID ");
    	sb.append("    FROM IX_POI_ADDRESS N1 ");
    	sb.append("   WHERE (N1.TOWN || N1.PLACE || N1.STREET || N1.LANDMARK) IS NULL ");
    	sb.append("     AND (N1.PREFIX || N1.HOUSENUM || N1.TYPE || N1.SUBNUM || N1.SURFIX || ");
    	sb.append("         N1.ESTAB || N1.BUILDING || N1.UNIT || N1.FLOOR || N1.ROOM || ");
    	sb.append("         N1.ADDONS) IS NULL ");
    	sb.append("     AND N1.LANG_CODE IN ('CHI', 'CHT')) ");
    	sb.append("select T.PID from T ");
    	sb.append(" where exists (select 1 ");
    	sb.append("                 from poi_column_status s ");
    	sb.append("                where s.pid = T.pid ");
    	sb.append("                  and s.common_handler is null ");
    	sb.append("                  and s.work_item_id in ('FM-A09-01', 'FM-YW-20-018')) ");
    	
    	return new QueryRunner().query(conn, sb.toString(), new TranslateHandler());
    }
    
    private Map<Long, BasicObj> loadData(Connection conn, List<Long> pids) throws Exception{
    	Map<Long, BasicObj> objs = new HashMap<>();
		Set<String> tabNames = new HashSet<>();
		tabNames.add("IX_POI_ADDRESS");
		objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, tabNames, false, pids, false, false);
		return objs;
    }
    
    public void setBatchHandler(Connection conn, List<Long> pids)throws Exception {
    	
    	try{
	    	String sql = "update poi_column_status s set s.HANDLER=9999999 where s.pid ";
			if(pids.size()<=1000){
				sql += " IN (" + org.apache.commons.lang.StringUtils.join(pids,",") + ") ";
			}else{
				sql += " IN (select to_number(column_value) from table(clob_to_table(?))) ";
			}
			sql += " and s.work_item_id in ('FM-A09-01', 'FM-YW-20-018') ";
			
			if(pids.size()>1000){
				Clob clobPids=ConnectionUtil.createClob(conn);
				clobPids.setString(1, org.apache.commons.lang.StringUtils.join(pids, ","));
				new QueryRunner().update(conn, sql,clobPids);
			}else{
				new QueryRunner().update(conn, sql);
			}
		}catch(Exception e){
			if(conn != null){
				conn.rollback();
			}
			throw new Exception("更新批处理的数据的handler异常:" + e.getMessage());
		}finally{
			if(conn != null){
				conn.commit();
			}
		}
    }
    
    public void cancelBatchHandler(Connection conn, List<Long> pids)throws Exception {
    	String sql = "update poi_column_status s set s.HANDLER=0 where s.pid ";
		if(pids.size()<=1000){
			sql += " IN (" + org.apache.commons.lang.StringUtils.join(pids,",") + ") ";
		}else{
			sql += " IN (select to_number(column_value) from table(clob_to_table(?))) ";
		}
		sql += " and s.work_item_id in ('FM-A09-01', 'FM-YW-20-018') ";

		if(pids.size()>1000){
			Clob clobPids=ConnectionUtil.createClob(conn);
			clobPids.setString(1, org.apache.commons.lang.StringUtils.join(pids, ","));
			new QueryRunner().update(conn, sql,clobPids);
		}else{
			new QueryRunner().update(conn, sql);
		}
    }
    
    public void execute(JSONObject request) throws Exception {
    	Connection conn = null;
    	try{
	    	logger.info("batch addrSplit start...");
	        Long timeStart = System.currentTimeMillis();
	        dbId = request.optInt("dbId", Integer.MIN_VALUE);
            if (dbId != Integer.MIN_VALUE) {
                conn = DBConnector.getInstance().getConnectionById(dbId);
            } else {
                conn = DBConnector.getInstance().getMkConnection();
            }
	        OperationResult operationResult=new OperationResult();
	        
	        List<Long> pids = new ArrayList<>();
	        String StrPids = request.getString("pids");
	        if(StringUtils.isNotEmpty(StrPids)){
		        String[] pidArray = StrPids.split(",");
		        if(pidArray.length > 0){
			        for (String pidStr: pidArray){
			        	pids.add(Long.parseLong(pidStr));
			        }
		        }
	        }else{
	        	pids = getPids(conn);
	        }

	        if(pids == null || pids.size() == 0){
	        	logger.info("batch addrSplit end...no data need batch");
	        	return ;
	        }
	        // 批处理之前先将数据的hander置为特殊的"999999"标记,防止月编别的人申请数据作业,导致冲突
	        setBatchHandler(conn, pids);
	        
	        try{
		        Map<Long, BasicObj> poiObjs = loadData(conn, pids);
		        if(poiObjs == null || poiObjs.isEmpty()){
		        	logger.info("batch addrSplit end...no data need batch");
		        	return ;
		        }
		        Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadAllLog(conn, pids);
		        // 将poi对象与履历合并起来
				ObjHisLogParser.parse(poiObjs, logs);
				operationResult.putAll(poiObjs.values());
				
				BatchCommand batchCommand=new BatchCommand();
				batchCommand.setRuleId("FM-BAT-20-110");
				batchCommand.setRuleId("FM-BAT-20-125");
				Batch batch=new Batch(conn,operationResult);
				batch.operate(batchCommand);
				batch.persistChangeLog(1, 2);
			}catch (Exception e){
				// 取消打特殊标记
				cancelBatchHandler(conn, pids);
				throw e;
			}
	        // 取消打特殊标记
	        cancelBatchHandler(conn, pids);
			logger.info("batch addrSplit end...");
	        Long timeEnd = System.currentTimeMillis();
	        logger.info("batch addrSplit time:" + (timeEnd-timeStart) + " ms...");
	        
    	}catch(Exception e){
    		e.printStackTrace();
    		logger.error("Batch addrSplit Error:" + e.getMessage());
    		DbUtils.rollbackAndCloseQuietly(conn);
    	}finally{
    		DbUtils.commitAndCloseQuietly(conn);
    	}
    }

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Map<String, String> paraMap = new HashMap<String, String>();
		try{
	        if(args.length%2!=0){
	            System.out.println("ERROR:need args:-irequest xxx");
	            return;
	        }
	        for(int i=0; i<args.length;i+=2){
	        	paraMap.put(args[i], args[i+1]);
	        }
	        String irequest = paraMap.get("-irequest");
	        if(StringUtils.isEmpty(irequest)){
	            System.out.println("ERROR:need args:-irequest xxx");
	            return;
	        }
	        JSONObject request=null;
//	        String dir = "W:\\test\\";
	        String dir = SystemConfigFactory.getSystemConfig().getValue("scripts.dir");
	        //初始化context
	        JobScriptsInterface.initContext();
	        //
	        request = ToolScriptsInterface.readJson(dir+"request"+ File.separator+irequest);
	
	        BatchAddressSplit split = new BatchAddressSplit();
	        split.execute(request);
	
	        logger.debug("Over.");
	        System.exit(0);
		}catch(Exception e){
	        System.out.println("Error, something wrong...");
	        e.printStackTrace();
		}
    }
}
