package com.navinfo.dataservice.engine.fcc;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.navicommons.database.sql.DBUtils;
import com.navinfo.navicommons.geo.computation.GridUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.engine.audio.Audio;
import com.navinfo.dataservice.engine.fcc.tips.EdgeMatchTipsOperator;
import com.navinfo.dataservice.engine.fcc.tips.PretreatmentTipsOperator;
import com.navinfo.dataservice.engine.fcc.tips.TipsOperator;
import com.navinfo.dataservice.engine.fcc.tips.TipsUtils;

/**
 * @ClassName: TipsOperateTest.java
 * @author y
 * @date 2016-7-2下午1:56:04
 * @Description: TODO
 * 
 */
public class TipsOperateTest extends InitApplication {

	@Override
	@Before
	public void init() {
		initContext();
	}

	@Test
	public void testEdit() {
		
		String  parameter="{\"mdFlag\":\"d\",\"handler\":1176,\"rowkey\":\"0212016086f35d16434fad8038541a01533abf\",\"pid\":508000041,\"editStatus\":0,\"editMeth\":2}";
		
		

		TipsOperator operate = new TipsOperator();

		try {
			 if (StringUtils.isEmpty(parameter)) {
	                throw new IllegalArgumentException("parameter参数不能为空。");
	            }
			  JSONObject jsonReq = JSONObject.fromObject(parameter);

	            String rowkey = jsonReq.getString("rowkey");

	            //int stage = jsonReq.getInt("stage");

	            int handler = jsonReq.getInt("handler");

	            String mdFlag= jsonReq.getString("mdFlag");

	            int editStatus = jsonReq.getInt("editStatus");
	            int editMeth = jsonReq.getInt("editMeth");

	            if (StringUtils.isEmpty(rowkey)) {
	                throw new IllegalArgumentException("参数错误:rowkey不能为空");
	            }

	            if (StringUtils.isEmpty(mdFlag)) {
	                throw new IllegalArgumentException("参数错误:mdFlag不能为空");
	            }

	            //值域验证
	            if(!"m".equals(mdFlag)&&!"d".equals(mdFlag)){
	                throw new IllegalArgumentException("参数错误:mdflag值域错误。");
	            }


	            String pid = null;

	            if (jsonReq.containsKey("pid")) {
	                pid = jsonReq.getString("pid");
	            }

	            TipsOperator op = new TipsOperator();

	            op.update(rowkey, handler, pid, mdFlag, editStatus, editMeth);
	            
	            System.out.println("----------------修改成功");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 批量修改tips状态
	 * @Description:TOOD
	 * @author: y
	 * @time:2017-2-9 上午9:45:51
	 */
	@Test
	public void testBatchEdit() {

		TipsOperator operate = new TipsOperator();
		
		//JSONArray data, int handler, String mdFlag
		
		JSONArray data=new JSONArray();
		
		JSONObject b=new JSONObject();
		
		b.put("rowkey", "02110417AAB99AD0124E91A3FE0076C14A8E8B");
		
		b.put("editStatus",0);
		b.put("editMeth", 1);
		
		data.add(b);
		
//		JSONObject a=new JSONObject();
//
//		a.put("rowkey", "1115024070073");
//
//		a.put("status", "2");
//
//		data.add(a);
		
		int handler=2922;
		
		String mdFlag="d";

		try {
			operate.batchUpdateStatus(data, handler, mdFlag);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Test
	public void testBatchEditStatus2() throws Exception {
		
		try{

		// String parameter="{\"mdFlag\":\"d\",\"handler\":02922,\"data\":[{\"rowkey\":\"1115023838453\",\"status\":1},{\"rowkey\":\"1115024070073\",\"status\":1}]}";
		    String parameter=" {\"mdFlag\":\"d\",\"handler\":\"2922\",\"data\":[{\"rowkey\":\"0280019713755270f140bc92bed694cd4f5663\",\"status\":1}]}";
		    parameter = "{\"mdFlag\":\"d\",\"handler\":1315,\"data\":[{\"rowkey\":\"028002b4eb017e06fd4690a2473409a33bc39b\",\"editStatus\"" +
                    ":2,\"editMeth\":1}]}";
            if (StringUtils.isEmpty(parameter)) {
                 throw new IllegalArgumentException("parameter参数不能为空。");
            }

            JSONObject jsonReq = JSONObject.fromObject(parameter);

            //{mdflag:'',handler:'',data:[{rowkey:'',status:''}]}

            JSONArray data = jsonReq.getJSONArray("data");

            int handler = jsonReq.getInt("handler");

            String mdFlag= jsonReq.getString("mdFlag");

             if (data==null||data.size()==0) {
                    throw new IllegalArgumentException("参数错误:data不能为空");
             }

             if (StringUtils.isEmpty(mdFlag)) {
                    throw new IllegalArgumentException("参数错误:mdFlag不能为空");
             }

                  //值域验证
             if(!"m".equals(mdFlag)&&!"d".equals(mdFlag)){
                 throw new IllegalArgumentException("参数错误:mdflag值域错误。");
             }

			TipsOperator op = new TipsOperator();

			op.batchUpdateStatus(data,handler,mdFlag);

			//return new ModelAndView("jsonView", success());
		}catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	

	@Test
	public void testUpdateAll() {

		TipsOperator operate = new TipsOperator();

		try {
//			String rowkey = "220215153123fa4d7d62479fa7d35ab9def60fa8";
//			 operate.update("0220019609FB3AFDD047EE9FE53BEF56496AAE", 123,
//			 "1", "d", 1 ,1);
            String parameter = "{taskId:77}";

            java.sql.Connection oracleConn = null;
            try {
                oracleConn = DBConnector.getInstance().getTipsIdxConnection();

                JSONObject jsonReq = JSONObject.fromObject(parameter);

                if(!jsonReq.containsKey("taskId")) {
                    throw new IllegalArgumentException("参数错误:midTaskId不能为空。");
                }

                int midTaskId = jsonReq.getInt("taskId");
                if(midTaskId == 0) {
                    throw new IllegalArgumentException("参数错误:midTaskId不能为空。");
                }

                ManApi manApi = (ManApi) ApplicationContextUtil.getBean("manApi");
                JSONArray gridList = manApi.getGridIdsByTaskId(midTaskId);
                String wkt = GridUtils.grids2Wkt(gridList);
                TipsOperator tipsOperator = new TipsOperator();
                long totalNum = tipsOperator.batchNoTaskDataByMidTask(wkt, midTaskId);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("total", totalNum);

            } catch (Exception e) {
                DBUtils.rollBack(oracleConn);
            } finally {
                DBUtils.closeConnection(oracleConn);
            }

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	 @Test
		public void testDel() {

		 String parameter = "{\"rowkey\":\"111101102446\",\"subTaskId\":57}";
			try {
				if (StringUtils.isEmpty(parameter)) {
					throw new IllegalArgumentException("parameter参数不能为空。");
				}

				JSONObject jsonReq = JSONObject.fromObject(parameter);

				String rowkey = jsonReq.getString("rowkey");
				
				int delType=0; //默认物理删除。0：逻辑删除；1：物理删除

                int subTaskId = jsonReq.getInt("subTaskId");

				if (StringUtils.isEmpty(rowkey)) {
					throw new IllegalArgumentException("参数错误：rowkey不能为空。");
				}

				PretreatmentTipsOperator op2 = new PretreatmentTipsOperator();

				delType = op2.getDelTypeByRowkeyAndUserId(rowkey, subTaskId);

				if(delType != PretreatmentTipsOperator.TIP_NOT_DELETE) {
					op2.deleteByRowkey(rowkey, delType, subTaskId);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	
	

	//造数据
	public static void main(String[] args) {

		int  count=1200;
		Scanner scanner = null;
		PrintWriter pw=null;
		try {
			scanner = new Scanner(
					new FileInputStream(
							"E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\音频测试数据\\1425_1\\tips.txt"));
			
			JSONObject json =null;
			if (scanner.hasNextLine()) {

				String line = scanner.nextLine();

				json = JSONObject.fromObject(line);
				
			}
			
			 pw = new PrintWriter("E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\音频测试数据\\1425_1\\tip_1000.txt");
			for (int i = 0; i < count; i++) {
				String rowkey=TipsUtils.getNewRowkey("2001");
				json.put("rowkey", rowkey);
				json.put("t_handler", "2922");
				json.put("t_operateDate", DateUtils.dateToString(new Date(),
						DateUtils.DATE_COMPACTED_FORMAT));
				pw.println(json.toString());
				
			}
			
			System.out.println("造数据完成");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			scanner.close();
			pw.close();
		}

	}


}
