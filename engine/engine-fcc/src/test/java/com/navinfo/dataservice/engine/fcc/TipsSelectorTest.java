package com.navinfo.dataservice.engine.fcc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.junit.Test;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.engine.fcc.service.FccApiImpl;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;

public class TipsSelectorTest {

	TipsSelector solrSelector = new TipsSelector();
	
	
	private SolrController conn = new SolrController();
	
	
	/*	
	 * 这段就不需要
	 * ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
	    new String[] { "dubbo-consumer-datahub-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);*/

	//根据网格、类型、作业状态获取tips的snapshot列表（rowkey，点位，类型）
	//@Test
	public void testGetSnapshot() {
		
		
	JSONArray grid = JSONArray
				.fromObject("[59567513,59567512,59567503]");
	    
/*		JSONArray grid = JSONArray
			.fromObject("[59567232,59567233]");*/

	
		System.out.println(grid.toString());
		JSONArray stage = new JSONArray();
		stage.add(1);
		stage.add(2);
		
		//红绿灯、红绿灯方位、大门、坡度、条件限速、车道限速、车道数、匝道、停车场出入口link、禁止穿行、禁止驶入、提左提右、一般道路方面、路面覆盖、测线
		//1102、1103 、1104、1106、1111、1113、1202
		int type = 1501;
		int dbId = 9;
		
		
		try {
			System.out.println(solrSelector.getSnapshot(grid, stage, type,
					dbId,"d"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
    //根据瓦片扩圈获取Tips数据
	@Test
	public void testSearchDataByTileWithGap() {
		JSONArray types = new JSONArray();
		//types.add(1202);
	/*	types.add(1205);
		types.add(1401);
		types.add(1110);
		types.add(1515);
		types.add(1105);
		types.add(1806);
		types.add(1509);*/
		
		//{"gap":40,"mdFlag":"d","z":17,"x":107942,"y":49613}
		
		//{"gap":40,"mdFlag":"d","z":18,"x":215889,"y":99231}
		try {
			System.out.println(solrSelector.searchDataByTileWithGap(215889, 99231, 18,
					40, types,"d"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//根据网格获取tips统计
	//	@Test
		public void testGetStats() {
			JSONArray grid = JSONArray
					.fromObject("[59567101,59567102,59567103,59567104,59567201,60560301,60560302,60560303,60560304]");
			JSONArray stage = new JSONArray();
			try {
				System.out.println(solrSelector.getStats(grid, stage));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		//根据rowkey获取单个tips的详细信息
		//@Test
		public void testSearchDataByRowkey() {
			try {
				System.out.println("sorl by rowkey:");
				System.out.println(solrSelector.searchDataByRowkey("11111146543817"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		//根据wkt范围获取tips的snapshot列表
	//	@Test
		public void testSearchDataBySpatial() {
			try {
				JSONArray ja =
						solrSelector.searchDataBySpatial("POLYGON ((113.70469 26.62879, 119.70818 26.62879, 119.70818 29.62948, 113.70469 29.62948, 113.70469 26.62879))");

				System.out.println(ja.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		//@Test
		public void testGeoUtis() {
			try {
				//1.坐标转换为图幅
				
				System.out.println(MeshUtils.point2Meshes(117.04654,40.27268).length);
				System.out.println(MeshUtils.point2Meshes(117.04654,40.27268)[0]);
				//2.全角转半角
				System.out.println("---２３４５----");
				System.out.println(ExcelReader.f2h("---２３４５----"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		/**
		 * 已经测试成功的取出正则表达式匹配的字符串         
	         * @author erikas
	         * @throws Exception
		 */
		//@org.junit.Test
		public void testReg() throws Exception {
		//  ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅩⅪⅫ⑴⑵⑶
			
			//String str = "[ X , I , V , IIII , IV , VI , X , XII , C , CI , IX , IV ]";
			//String str = " I xa II  as III as  IV  as  V  as X  as C as CII as CXXI ";
			String str="ⅠasⅡasⅢ asⅥasVassVIasⅫ";
			str=str.replace("Ⅰ", "  I  ");
			str=str.replace("Ⅱ", "  II  ");
			str=str.replace("Ⅲ", "  III  ");
			str=str.replace("Ⅵ", "  IV  ");
			
			System.out.println(str);
			System.out.println(str.replace("Ⅰ", "I"));
			String result="";
			//匹配罗马数字的正则,但是由于每一个都可能是0个 空字符串也会被匹配出来 需要后期在程序里再处理
			String regex = "(-| +|^)M{0,9}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})( +|$)"; 
			Pattern p = Pattern.compile(regex);
			Matcher matcher = p.matcher(str);
			List<String> list = new ArrayList<String>();
			while (matcher.find()) {
				//matcher.find()返回true表示匹配到了结果 但是执行了之后如果后面没有再匹配成功的 会马上变成false
				String srcStr = matcher.group();
				//String value=r2a(srcStr);
				if(!" ".equals(srcStr)){
					System.out.println(srcStr+":"+r2a(srcStr)+"----");
					str=str.replace(srcStr,r2a(srcStr));
					System.out.println("替换后："+str);
				}
				//把取出的结果 放入list
				list.add(srcStr);
			}
			//System.out.println(str);
			System.out.println(list);
		}
		
		//罗马数字转阿拉伯数字：
	    // 从前往后遍历罗马数字，如果某个数比前一个数小，则把该数加入到结果中；
	    // 反之，则在结果中两次减去前一个数并加上当前这个数；
	    // I、V、X、   L、   C、     D、     M
	    // 1．5、10、50、100、500、1000
	    private static String r2a(String in){
	        int graph[] = new int[400];
	        graph['I'] = 1;
	        graph['V']=5;
	        graph['X']=10;
	        graph['L']=50;
	        graph['C']=100;
	        graph['D']=500;
	        graph['M']=1000;
	        char[] num = in.toCharArray();
	        // 遍历这个数，用sum来总计和
	        int sum = graph[num[0]];
	        for(int i=0; i<num.length-1; i++){
	            // 如果，i比i+1大的话，直接相加
	            if(graph[num[i]] >= graph[num[i+1]]){
	                sum += graph[num[i+1]];
	            }
	            // 如果i比i+1小的话，则将总和sum减去i这个地方数的两倍，同时加上i+1
	            // 就相当于后边的数比左边的数大，则用右边的数减左边的数
	            else{
	                sum = sum + graph[num[i+1]] - 2*graph[num[i]];
	            }
	        }
	        return String.valueOf(sum);
	    }
	    
	  // @Test
	    public  void  testQuerySolr(){
	    	System.out.println("查询rowkey");
	    	/*JSONArray grids = JSONArray
					.fromObject("[60560301,60560302,60560303,60560311,60560312,60560313,60560322,60560323,60560331,60560332,60560333,60560320,60560330,60560300,60560321,60560310]");
	    	*/
	    	
	    	JSONArray grids = JSONArray
					.fromObject("[59567233]");
			
	    	//[59567303,59567313]
	    /*	JSONArray grids = JSONArray
			.fromObject("[60560303,60560311,60560312,60560313,60560322]");
	    	*/
	    	
	    	System.out.println(grids.toString());
			JSONArray stages = new JSONArray();
			stages.add(0);
			stages.add(1);
			stages.add(2);
			stages.add(3);
			stages.add(4);
			//没找到：1113  1202
			//红绿灯、红绿灯方位、大门、坡度、条件限速、车道限速、车道数、匝道、停车场出入口link、禁止穿行、禁止驶入、提左提右、一般道路方面、路面覆盖、测线、2001
			//1102、1103 、1104、1106、1111、1113、1202、1207、1208、1304、1305、1404、1405、1502
			
			//int [] types={1102,1103,1104,1106,1111,1113,1202,1207,1208,1304,1305,1404,1405,1502};
			
			int [] types={1507,1512,1511,1516,1517,1605,1606,1601,1602,1804};
			
			
			//int [] types={1202,1207,1304,1305};
		
			for (int i = 0; i < types.length; i++) {
				int type = types[i];
	    		String wkt;
				try {
					wkt = GridUtils.grids2Wkt(grids);
					List<JSONObject> tips = conn.queryTipsWeb(wkt, type, stages);
					if(tips==null||tips.size()==0){
						System.out.println("type:"+type+"在"+grids+"没有找到");
					}
					int count=0;
					String ids="";
		    		for (JSONObject json : tips) {
		    			ids+=","+json.get("id");
		    			update(json.get("id").toString());
		    			count++;
		    			if(count==10)  break;
		    		}
		    		if(StringUtils.isNotEmpty(ids)){
		    			System.out.println("type:"+type+"找到数据rowkeys:"+ids);
		    		}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

	    		
	    }
	    
	    
	    
	    
	    /**
		 * 修改tips(增加三个字段)
		 * 
		 * @param rowkey
		 * @param mdFlag 
		 * @param content
		 * @return
		 * @throws Exception
		 */
		public  boolean update(String rowkey)
				throws Exception {

			Connection hbaseConn = HBaseConnector.getInstance().getConnection();

			Table htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

			Get get = new Get(rowkey.getBytes());

			get.addColumn("data".getBytes(), "track".getBytes());

			Result result = htab.get(get);

			if (result.isEmpty()) {
				return false;
			}

			Put put = new Put(rowkey.getBytes());

			JSONObject track = JSONObject.fromObject(new String(result.getValue(
					"data".getBytes(), "track".getBytes())));


			JSONArray trackInfo = track.getJSONArray("t_trackInfo");
			
			int i=0;
			
			for (Object obj:trackInfo) {
				
				JSONObject info=JSONObject.fromObject(obj);
				i++;
				
				info.put("stage", 1);
				trackInfo.add(info);
				
				if(i==1) break;
				
				
			}
			
			
			track.put("t_lifecycle", 2);
			
			track.put("t_cStatus", 1);
			
			track.put("t_dStatus", 0);
			
			track.put("t_mStatus", 0);
			
			String date = StringUtils.getCurrentTime();

			track.put("t_trackInfo", trackInfo);

			track.put("t_date", date);
			

			put.addColumn("data".getBytes(), "track".getBytes(), track.toString()
					.getBytes());

			htab.put(put);
			
			
			JSONObject solrIndex = conn.getById(rowkey);

			solrIndex.put("t_lifecycle", 2);

			solrIndex.put("t_date", date);
			
			solrIndex.put("t_cStatus", 1);
			
			solrIndex.put("t_dStatus", 0);
			
			solrIndex.put("t_mStatus", 0);
			
			solrIndex.put("stage", 1);
			
			conn.addTips(solrIndex);


			return true;
		}

		//@Test
		public void testGrid2Location(){
			
			double [] l=GridUtils.grid2Location("59567233");
			System.out.println("grid2Location_____________");
			for (double d : l) {
				System.out.println(d);
			}
		}
		
	/*	public static void main(String[] args) {
			
			TipsSelectorTest test=new TipsSelectorTest();
			try {
				test.update("0220011d8405593377421c984adc368b877abe");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
		
		
		@Test
		public void testSubTaskCount(){
			FccApiImpl imp=new  FccApiImpl();
			JSONArray grids=new JSONArray();
			grids.add(60560302);
			grids.add(59567332);
			grids.add(59567322);
			JSONObject result;
			try {
				result = imp.getSubTaskStats(grids);
				System.out.println(result);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		


}
