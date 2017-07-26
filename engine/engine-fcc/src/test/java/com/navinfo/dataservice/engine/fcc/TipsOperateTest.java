package com.navinfo.dataservice.engine.fcc;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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

		TipsOperator operate = new TipsOperator();

		try {
			operate.update("021901b0ad67e145be477bb1d2202181edfc84", 0, null,
					"m", 0 ,0 );
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
		
		b.put("rowkey", "021509E86F79D4FB174B728B9E552434D7D324");
		
		b.put("editStatus", 2);
		b.put("editMeth", 2);
		
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
			String rowkey = "220215153123fa4d7d62479fa7d35ab9def60fa8";
			 operate.update("0220019609FB3AFDD047EE9FE53BEF56496AAE", 123,
			 "1", "d", 1 ,1);

//			Connection hbaseConn = HBaseConnector.getInstance().getConnection();
//
//			Table htab = hbaseConn.getTable(TableName
//					.valueOf(HBaseConstant.tipTab));
//
//			Get get = new Get(rowkey.getBytes());
//
//			get.addColumn("data".getBytes(), "track".getBytes());
//
//			Result result = htab.get(get);
//
//			Put put = new Put(rowkey.getBytes());
//
//			JSONObject track = JSONObject.fromObject(new String(result
//					.getValue("data".getBytes(), "track".getBytes())));
//
//			JSONArray trackInfo = track.getJSONArray("t_trackInfo");
//
//			track.put("t_mStatus", 0);
//
//			track.put("t_dStatus", 0);
//
//			String date = StringUtils.getCurrentTime();
//
//			track.put("t_trackInfo", trackInfo);
//
//			track.put("t_date", date);
//
//			put.addColumn("data".getBytes(), "track".getBytes(), track
//					.toString().getBytes());
//
//			htab.put(put);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	 @Test
		public void testDel() {

		 String parameter = "{\"rowkey\":\"028001a693ba64bd1d455ca809e6486520f221\",\"subTaskId\":57}";
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

				EdgeMatchTipsOperator op = new EdgeMatchTipsOperator();

				PretreatmentTipsOperator op2 = new PretreatmentTipsOperator();

				delType = op2.getDelTypeByRowkeyAndUserId(rowkey, subTaskId);

				if(delType == 0 || delType == 1) {
					op.deleteByRowkey(rowkey, delType);
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
