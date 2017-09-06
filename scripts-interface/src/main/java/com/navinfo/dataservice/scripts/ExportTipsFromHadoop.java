package com.navinfo.dataservice.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;

import com.alibaba.druid.util.StringUtils;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

public class ExportTipsFromHadoop {

	public static void main(String[] args) throws Exception{
		
		org.apache.hadoop.hbase.client.Connection hbaseConn = null;

		Table htab = null;
		
		String path = args[0];

		try {

			JobScriptsInterface.initContext();

			Map<String, String> taskInfoes = getTaskIds(path);

			hbaseConn = HBaseConnector.getInstance().getConnection();

			htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

			for (Map.Entry<String, String> taskInfo : taskInfoes.entrySet()) {

				handleDataByPoolAndTask(taskInfo,htab);
				
			}//for
			
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			
			e.printStackTrace();
		}
		finally{				
			htab.close();
			
			hbaseConn.close();
			
			System.out.println("Success, export is over...");
		}
	}

	/**
	 * 根据输入的配置文件，输出快线和中线任务号
	 * 
	 * @param path
	 * @return
	 * @throws Exception
	 */
	private static Map<String, String> getTaskIds(String path) throws Exception {
		
		Map<String, String> taskInfo = new HashMap<>();

		String line;

		File file = new File(path);

		InputStreamReader read = new InputStreamReader(new FileInputStream(file));

		BufferedReader reader = new BufferedReader(read);

		while ((line = reader.readLine()) != null) {
			String[] lineInfo = line.split(":");

			if (lineInfo.length < 2 || StringUtils.isEmpty(lineInfo[1])) {
				continue;
			}

			String[] taskIds = lineInfo[1].split(",");

			for (int i = 0; i < taskIds.length; i++) {
				taskInfo.put(lineInfo[0] + i, taskIds[i]);
			}
		}

		reader.close();
		
		return taskInfo;
	}

	/**
	 * 根据任务号，连接solar，确定rowkeys
	 * 
	 * @param taskInfo
	 * @return
	 * @throws Exception
	 */
	private static List<Get> getSelectedRowKeys(Map.Entry<String, String> taskInfo) throws Exception {
		Connection oracleConn = null;

		QueryRunner run = new QueryRunner();

		List<Get> result = new ArrayList<>();

		try {
			oracleConn = DBConnector.getInstance().getTipsIdxConnection();
			
			String taskType = taskInfo.getKey().substring(0, taskInfo.getKey().lastIndexOf('D') + 1);

			String sql = String.format("SELECT ID FROM TIPS_INDEX WHERE %s = %s", taskType,
					taskInfo.getValue());

			ResultSetHandler<List<Get>> resultSetHandler = new ResultSetHandler<List<Get>>() {
				@Override
				public List<Get> handle(ResultSet rs) throws SQLException {

					List<Get> rowkeys = new ArrayList<>();

					while (rs.next()) {
						
						Get dao = new Get(rs.getString(1).getBytes());
						rowkeys.add(dao);
					}

					return rowkeys;
				}
			};

			result = run.query(oracleConn, sql.toString(), resultSetHandler);

		} catch (Exception e) {
			e.printStackTrace();
			
		} finally {
			DbUtils.close(oracleConn);
		}
		
		return result;
	}
	
	/**
	 * 优化：多线程运算，加快速度
	 * 
	 * @param taskInfo
	 * @param htab
	 * @throws Exception
	 */
	private static void handleDataByPoolAndTask(Map.Entry<String, String> taskInfo,Table htab) throws Exception{

		PrintWriter pw = new PrintWriter(taskInfo.getValue() + "_tips_info_record.txt");
		
	    final int maxRowKeySize = 1000;
	    
        ExecutorService pool = Executors.newFixedThreadPool(10);
        
        List<Get> rowkeys = getSelectedRowKeys(taskInfo);
        
        int loopSize = rowkeys.size() % maxRowKeySize == 0 ? rowkeys.size() / maxRowKeySize : rowkeys.size() / maxRowKeySize + 1;
        
        ArrayList<Future<Result[]>> results = new ArrayList<Future<Result[]>>();
        		
        for (int loop = 0; loop < loopSize; loop++)
        {
            int end = (loop + 1) * maxRowKeySize > rowkeys.size() ? rowkeys.size() : (loop + 1) * maxRowKeySize;
                
            List<Get> partRowKeys = rowkeys.subList(loop * maxRowKeySize, end);
            
            HbaseDataGetter hbaseDataGetter = new HbaseDataGetter(partRowKeys,htab);
            
            Future<Result[]> result = pool.submit(hbaseDataGetter);
            
            results.add(result);
        }
        
        pool.shutdown();
        
        for(Future<Result[]> item : results){
    		for (Result result : item.get()) {
    			   			
    			if (result.isEmpty()) {
    				continue;
    			}
    			
    			JSONObject jsonObj = new JSONObject();

    			jsonObj.put("rowkey", new String(result.getRow()));

    			for (KeyValue rowKV : result.raw()) {
    				jsonObj.put(new String(rowKV.getQualifier()), new String(rowKV.getValue()));
    			}

    			pw.println(jsonObj);
    		} // for
        }//for
		
		pw.flush();

		pw.close();
	}
	
}

class HbaseDataGetter implements Callable<Result[]>
{
    private List<Get> rowkeys;
    
    private Table htab = null;;
    
    public HbaseDataGetter(List<Get> rowKeys,Table htab)
    {
        this.rowkeys = rowKeys;
        this.htab = htab;
    }

    @Override
	public Result[] call() throws Exception {
		Result[] partresults = new Result[rowkeys.size()];

		try {
			partresults = htab.get(rowkeys);

		} catch (Exception e) {
			e.printStackTrace();

		}

		return partresults;
	}
}

