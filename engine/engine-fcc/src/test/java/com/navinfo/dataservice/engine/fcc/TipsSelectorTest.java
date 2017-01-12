package com.navinfo.dataservice.engine.fcc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Test;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.engine.fcc.service.FccApiImpl;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class TipsSelectorTest {

	TipsSelector solrSelector = new TipsSelector();
	
	
	private static SolrController conn = new SolrController();
	
	
	/*	
	 * 这段就不需要
	 * ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
	    new String[] { "dubbo-consumer-datahub-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);*/

	//根据网格、类型、作业状态获取tips的snapshot列表（rowkey，点位，类型）
	@Test
	public void testGetSnapshot() {
		
		
	JSONArray grid = JSONArray
				.fromObject("[59564100,59564101,59564102,59564103,59564110,59564111,59564112,59564113,59564120,59564121,59564122,59564123,59564130,59564131,59564132,59564133]");
	
/*	parameter:{"grids":[59564100,59564101,59564102,59564103,59564110,59564111,59564112,59564113,59564120,59564121,59564122,59564123,59564130,59564131,59564132,59564133],
		"stage":[1,2],"mdFlag":"d","type":"2101","dbId":409}  */
/*		JSONArray grid = JSONArray
			.fromObject("[59567232,59567233]");*/

	
		System.out.println(grid.toString());
		JSONArray stage = new JSONArray();
		stage.add(1);
		stage.add(2);
		
		//红绿灯、红绿灯方位、大门、坡度、条件限速、车道限速、车道数、匝道、停车场出入口link、禁止穿行、禁止驶入、提左提右、一般道路方面、路面覆盖、测线
		//1102、1103 、1104、1106、1111、1113、1202
		int type = 2101;
		int dbId = 409;
		
		
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
		//types.add(8001);
	/*	types.add(1205);
		types.add(1401);
		types.add(1110);
		types.add(1515);
		types.add(1105);
		types.add(1806);
		types.add(1509);*/
		
		//{"gap":40,"mdFlag":"d","z":17,"x":107942,"y":49613}
		
		//{"gap":40,"mdFlag":"d","z":18,"x":215889,"y":99231}
		//parameter={"gap":40,"mdFlag":"d","z":18,"x":215894,"y":99196}
		
		//={"gap":40,"mdFlag":"d","z":18,"x":216035,"y":99004}
		
		//{"gap":40,"mdFlag":"d","z":17,"x":108022,"y":49665,types:[1806]}
		
		
		//返回的坐标和经纬度加载 {"gap":40,"mdFlag":"d","z":18,"x":216046,"y":99332}  rowkey:0215167ea4a06d6ebd4339a2cbb0f527482c3a.  路演环境数据
		
		//{"gap":40,"mdFlag":"d","z":19,"x":431790,"y":198467,"types":["1806"]}  19级别 应该返回数据，但是没有返回
		
		//{"gap":40,"mdFlag":"d","z":17,"x":107935,"y":49597}
		
		//{"gap":10,"mdFlag":"d","types":["8001"],"z":18,"x":215886,"y":99229}
		
		//{"gap":40,"mdFlag":"d","z":20,"x":863556,"y":396914}
		try {
			//{"gap":40,"mdFlag":"d","z":17,"x":107945,"y":49616}

			System.out.println(solrSelector.searchDataByTileWithGap(107945, 49616, 20,
					40, types,"d"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 */
	@Test
	public void testSearchDataByWkt() {
		JSONArray types = new JSONArray();
//		types.add(1202);

		//{"gap":40,"mdFlag":"d","z":17,"x":107942,"y":49613}
		try {
			String wkt = "POLYGON ((115.78478246015277 40.3580663376903, 117.06198634219226 40.3580663376903, 117.06198634219226 39.090405904000164, 115.78478246015277 39.090405904000164, 115.78478246015277 40.3580663376903))";
			solrSelector.searchDataByWkt(wkt, types,"d");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *
	 */
	@Test
	public void testOther() {

		try {
			//JSONObject obj=solrSelector.searchDataByRowkey("111503249654");
			
			/*JSONObject geojson = JSONObject.fromObject(obj
					.getString("g_location"));
			// 渲染的坐标都是屏幕坐标
			Geojson.coord2Pixel(geojson, 18, 5.5283968E7, 2.5481728E7);*/
			
			//System.out.println("geojson:"+geojson);
			
			
			
			
			String geo1="{\"coordinates\":[[116.48576,40.00849],[116.48582,40.00857],[116.48591,40.00866],[116.486,40.00876],[116.48613,40.00888],[116.48625,40.00902],[116.48633,40.00911],[116.48641,40.00918],[116.48645,40.00922]],\"type\":\"LineString\"}";
			String geo2="{\"coordinates\":[[116.48604,40.00812],[116.48617,40.00823],[116.48629,40.00837],[116.48643,40.00853],[116.48656,40.0087],[116.48669,40.00884],[116.48676,40.00893],[116.48681,40.00899],[116.48675,40.00902],[116.48655,40.00902],[116.48635,40.00901]],\"type\":\"LineString\"}";
			String geo3="{\"coordinates\":[[116.48577,40.00902],[116.48581,40.00897],[116.48594,40.00889],[116.48606,40.00879],[116.48619,40.00867],[116.4863,40.00855],[116.48637,40.00848],[116.48644,40.00841],[116.48649,40.00837],[116.48655,40.00834],[116.48652,40.00842],[116.48643,40.00856]],\"type\":\"LineString\"}";
			String geo4="{\"coordinates\":[[116.48617,40.00905],[116.48623,40.00901],[116.48635,40.00889],[116.48646,40.00874],[116.48654,40.00858],[116.48659,40.00842],[116.48659,40.00835]],\"type\":\"LineString\"}";
			String geo5="{\"coordinates\":[[116.48631,40.0081],[116.48639,40.00818],[116.4865,40.00827],[116.48664,40.00838],[116.48678,40.00851],[116.48692,40.00864],[116.48707,40.00876],[116.48725,40.00891],[116.48743,40.00905],[116.48762,40.00918]],\"type\":\"LineString\"}";
			        
				
			System.out.println(Geojson.geojson2Wkt(geo1)+",");
			System.out.println(Geojson.geojson2Wkt(geo2)+",");
			System.out.println(Geojson.geojson2Wkt(geo3)+",");
			System.out.println(Geojson.geojson2Wkt(geo4)+",");
			System.out.println(Geojson.geojson2Wkt(geo5));
			
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
//		@Test
		public void testSearchDataBySpatial() {
			try {
				JSONArray ja =
						solrSelector.searchDataBySpatial("POLYGON ((116.0625 40.14583, 116.09375 40.14583, 116.09375 40.125, 116.09375 40.10417, 116.0625 40.10417, 116.03125 40.10417, 116.03125 40.125, 116.03125 40.14583, 116.0625 40.14583))");
						//solrSelector.searchDataBySpatial("POLYGON ((113.70469 26.62879, 119.70818 26.62879, 119.70818 29.62948, 113.70469 29.62948, 113.70469 26.62879))");

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
					List<JSONObject> tips = conn.queryTipsWeb(wkt, type, stages,false);
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
		
		
		//@Test
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
		
		//修改一下solr中的所有数据的wkt
		public static void main(String[] args) {
			
			
			int fetchNum = Integer.MAX_VALUE;
			
			SolrQuery query = new SolrQuery();

			query.set("start", 0);

			query.set("rows", fetchNum); //1510  1507  1514  1512  1514
			
			//query.set("q", "s_sourceType:(1512 1510 1507 1514 1507) AND stage :(1 2 3) AND id:(\"021514d7a97574fe9e48f98a4dcdb4948cd321\")");
			
			//query.set("q", "s_sourceType:(1512 1510 1507 1514 1507 1511  ) AND stage :(1 2 3) ");
			
			//query.set("q", "s_sourceType:(8002 ) AND stage :(1 2 3) ");
			
			query.set("q", " stage :(1 2 3)  AND s_sourceType:( * NOT \"8001\" NOT \"8002\" NOT \"1501\")");
			
			
			HttpSolrClient client = SolrConnector.getInstance().getClient();

			QueryResponse response;
			try {
				response = client.query(query);
				
				SolrDocumentList sdList = response.getResults();

				long totalNum = sdList.getNumFound();

				if (totalNum <= fetchNum) {
					for (int i = 0; i < totalNum; i++) {
						SolrDocument doc = sdList.get(i);

						JSONObject snapshot = JSONObject.fromObject(doc);
						
						System.out.println(snapshot.get("id"));
						
						JSONObject  feedbacksO=snapshot.getJSONObject("feedback");
						
						JSONArray feedbacks=null;
						
						if(feedbacksO!=null){
							feedbacks=feedbacksO.getJSONArray("f_array");
						}
						
						String sourceType=snapshot.getString("s_sourceType");
						
						JSONObject g_location=JSONObject.fromObject(snapshot.getString("g_location"))  ;
						
						 String wkt=generateSolrWkt(snapshot.getString("id"),sourceType,g_location,feedbacks);
						
						// wkt=generateSolrWkt(sourceType,g_location,feedbacks);
						
						System.out.println("new wkt:"+wkt);
						
						boolean isUpdate=false;
						if(!wkt.equals(snapshot.getString("wkt"))){
							snapshot.put("wkt", wkt);
							isUpdate=true;
						}
						
						if(!snapshot.containsKey("t_inStatus")){
							snapshot.put("t_inStatus", 0);
							isUpdate=true;
						}
						
						if(!snapshot.containsKey("t_inMeth")){
							snapshot.put("t_inMeth", 0);
							isUpdate=true;
						}
						
						if(isUpdate){
							conn.addTips(snapshot);
						}
						

					}
				} else {
					// 暂先不处理
				}
				client.commit();
			} catch (Exception e) {
				
				e.printStackTrace();
			}

			


			
			
		}
		
		
		
		public static String generateSolrWkt(String rowkey , String sourceType, 
				JSONObject g_location, JSONArray feedbacks) throws Exception {
			List<Geometry> geos = new ArrayList<Geometry>();

			GeometryFactory factory = new GeometryFactory();
			
			if (sourceType.equals("1501")) {
				return null;
			} //8002的g_location是feedback中坐标的第一个点，可以不取，如果取了，则坐标相交，solr计算结果返回错误
			else  if(! sourceType.equals("8002") ){

				Geometry g = GeoTranslator.geojson2Jts(g_location);
				
				int glen=g.getNumGeometries();
				
				for (int i = 0; i < glen; i++) {
					
					if (!g.isValid()) {
						throw new Exception("invalid g_location");
					}
					
					geos.add(g.getGeometryN(i));
					
				}
			}

			for (int i = 0; i < feedbacks.size(); i++) {
				JSONObject feedback = feedbacks.getJSONObject(i);

				if (feedback.getInt("type") == 6) {
					// 草图
					JSONArray content = feedback.getJSONArray("content");

					for (int j = 0; j < content.size(); j++) {

						JSONObject geo = content.getJSONObject(j);

						Geometry g = GeoTranslator.geojson2Jts(geo
								.getJSONObject("geo"));

						geos.add(g);
						
					}

					break;
				}
			}

			if (geos.size() == 1) {
				return GeoTranslator.jts2Wkt(geos.get(0));
			} else {
				/**
				 * 20161026修改，如果复杂几何中存在相同的坐标，则保留一个，否则solr计算wkt相交有问题（
				 * 和王磊确认目前采集端不限制多线几何重复或者自相交的情况）
				 **/
				Geometry[] gArray = null;
				// 去重处理
				Set<Geometry> gSet = new TreeSet<Geometry>();
				for (int i = 0; i < geos.size(); i++) {
					gSet.add(geos.get(i));
				}

				gArray = new Geometry[gSet.size()];

				Iterator<Geometry> it = gSet.iterator();// 先迭代出来
				int i = 0;
				while (it.hasNext()) {// 遍历
					gArray[i] = it.next();
					i++;
				}
				Geometry g = factory.createGeometryCollection(gArray);

				return GeoTranslator.jts2Wkt(g);
			}

		}

		


}
