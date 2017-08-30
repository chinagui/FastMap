package com.navinfo.dataservice.engine.fcc;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.navinfo.dataservice.bizcommons.sys.SysLogConstant;
import com.navinfo.dataservice.bizcommons.sys.SysLogOperator;
import com.navinfo.dataservice.bizcommons.sys.SysLogStats;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.fcc.TaskType;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.engine.fcc.track.TrackLinesUpload;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;
import net.sf.json.JsonConfig;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.SolrController;
import com.navinfo.dataservice.engine.audio.Audio;
import com.navinfo.dataservice.engine.fcc.patternImage.PatternImageImporter;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;
import com.navinfo.dataservice.engine.fcc.tips.TipsUpload;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TipsSelectorTest extends InitApplication {

	TipsSelector solrSelector = new TipsSelector();


	private static SolrController conn = new SolrController();


	@Override
	@Before
	public void init() {
		initContext();
	}

	
	
	/*	
	 * 这段就不需要
	 * ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
	    new String[] { "dubbo-consumer-datahub-test.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);*/

	//根据网格、类型、作业状态获取tips的snapshot列表（rowkey，点位，类型）
	@Test
	public void testGetSnapshot() {
		String parameter = "{\"grids\":[59564100,59564101,59564102,59564103,59564110,59564111,59564112,59564113,59564120,59564121,59564122,59564123,59564130,59564131,59564132,59564133],\n" +
				"\t\t\"stage\":[1,2],\"mdFlag\":\"d\",\"type\":\"2101\",\"dbId\":409}";
		try {
			System.out.println(solrSelector.getSnapshot(parameter));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    @Test
	public void testTrackPoints() throws Exception{
        TrackLinesUpload trackUploader = new TrackLinesUpload();
        trackUploader.run("F:\\FCC\\track_collection.json", HBaseConstant.trackLineTab, 1);
        System.exit(0);
    }

	@Test
	public void testQueryByCodeAndGrid(){

//		JSONArray grids = JSONArray
//				.fromObject("[60560301,60560302,60560303,60560304,60560311,60560312,60560313,60560314]");
	/*	JSONArray grids = JSONArray
				.fromObject("[60566132,60566122,60566120,60566133,60566123,60566112,60566113,60566130,60566131]");
		*/


		JSONArray grids = JSONArray
				.fromObject("[60565710]");
	/*	JSONArray grids = JSONArray
				.fromObject("[60566132,60566122,60566120,60566133,60566123,60566112,60566113,60566130,60566131]");
		*/

		//f是预处理渲染，如果不是，则需要过滤没有提交的预处理tips
		boolean isPre=false;

		JSONArray stages = new JSONArray();
		stages.add(0);
		stages.add(1);
		stages.add(2);
		stages.add(3);

		JSONArray types = new JSONArray();
		types.add(1501);
		types.add(1502);
		types.add(1503);
		types.add(1504);
		types.add(1505);
		types.add(1506);
		types.add(1507);
		types.add(1508);
		types.add(1509);
		types.add(1510);
		types.add(1511);
		types.add(1512);
		types.add(1513);
		types.add(1514);
		types.add(1515);
		types.add(1516);
		types.add(1517);

		int type=1509;



		try{
			String wkt = GridUtils.grids2Wkt(grids);
			List<JSONObject> tips = null;//conn.queryTipsWeb(wkt, type, stages,isPre);

			for (JSONObject jsonObject : tips) {
				System.out.println("kind:"+jsonObject.getString("s_sourceType")+"-------"+jsonObject.get("id"));
			}

			System.out.println("--------------------");
		}catch (Exception e) {
			e.printStackTrace();
		}



	}


	//根据瓦片扩圈获取Tips数据
	@Test
	public void testSearchDataByTileWithGap() {
		
		try{
			String parameter= "{\"pType\":\"fc\",\"gap\":10,\"types\":[\"1107\",\"1201\",\"1202\",\"1203\",\"1702\",\"2001\",\"1901\",\"2101\",\"1601\",\"1803\",\"1301\",\"1507\"],\"x\":215851,\"y\":99298,\"z\":18}";
			parameter = "{\"mdFlag\":\"d\",\"gap\":10,\"pType\":\"sl\",\"types\":[\"1107\",\"1201\",\"1102\",\"1202\",\"1101\",\"1205\",\"1206\",\"1207\",\"1203\",\"1702\",\"1501\",\"1508\",\"1514\",\"1901\",\"2001\",\"2101\",\"1601\",\"1803\",\"1301\",\"1604\"],\"x\":107901,\"y\":49676,\"z\":17}";
            //web 区域粗编 subtaskId=395. 有2个1601.返回空则ok
			parameter="{\"subtaskId\":395,\"mdFlag\":\"d\",\"gap\":10,\"types\":[\"8002\",\"1403\",\"1510\",\"1508\",\"1506\",\"1606\",\"1803\",\"1509\",\"2101\",\"1804\",\"1202\",\"1503\",\"8001\",\"1104\",\"1706\",\"1407\",\"1116\",\"1410\",\"1301\",\"1404\",\"2001\",\"1514\",\"1501\",\"1513\",\"1304\",\"1305\",\"1302\",\"1405\",\"1701\",\"1504\",\"1705\",\"1208\",\"1502\",\"1507\",\"1605\",\"1702\",\"1207\",\"1604\",\"1515\",\"1101\",\"1704\",\"1703\",\"1203\",\"1901\",\"1206\",\"1205\",\"1201\",\"1601\",\"1209\",\"1607\",\"1516\",\"1512\",\"1806\",\"1106\",\"1602\",\"1111\",\"1107\",\"1102\",\"1511\",\"1505\",\"1517\",\"1105\",\"1109\",\"1110\",\"1112\",\"1113\",\"1114\",\"1115\",\"1204\",\"1303\",\"1306\",\"1308\",\"1310\",\"1311\",\"1401\",\"1402\",\"1406\",\"1409\",\"1707\",\"2002\",\"1708\",\"1518\",\"1709\",\"2201\",\"2102\",\"1211\"],\"workStatus\":[0],\"x\":108076,\"y\":49648,\"z\":17}";
			//web 区域粗编 subtaskId=395.  有2个1601+1个8001返回一个8001则ok
			parameter="{\"subtaskId\":395,\"mdFlag\":\"d\",\"gap\":10,\"types\":[\"8002\",\"1403\",\"1510\",\"1508\",\"1506\",\"1606\",\"1803\",\"1509\",\"2101\",\"1804\",\"1202\",\"1503\",\"8001\",\"1104\",\"1706\",\"1407\",\"1116\",\"1410\",\"1301\",\"1404\",\"2001\",\"1514\",\"1501\",\"1513\",\"1304\",\"1305\",\"1302\",\"1405\",\"1701\",\"1504\",\"1705\",\"1208\",\"1502\",\"1507\",\"1605\",\"1702\",\"1207\",\"1604\",\"1515\",\"1101\",\"1704\",\"1703\",\"1203\",\"1901\",\"1206\",\"1205\",\"1201\",\"1601\",\"1209\",\"1607\",\"1516\",\"1512\",\"1806\",\"1106\",\"1602\",\"1111\",\"1107\",\"1102\",\"1511\",\"1505\",\"1517\",\"1105\",\"1109\",\"1110\",\"1112\",\"1113\",\"1114\",\"1115\",\"1204\",\"1303\",\"1306\",\"1308\",\"1310\",\"1311\",\"1401\",\"1402\",\"1406\",\"1409\",\"1707\",\"2002\",\"1708\",\"1518\",\"1709\",\"2201\",\"2102\",\"1211\"],\"workStatus\":[0],\"x\":108073,\"y\":49646,\"z\":17}";
			// web grid粗编。晶任务号换为575  有2个1601+1个8001   返回 有2个1601则ok
			parameter="{\"subtaskId\":123,\"mdFlag\":\"d\",\"gap\":10,\"types\":[\"8002\",\"1403\",\"1510\",\"1508\",\"1506\",\"1606\",\"1803\",\"1509\",\"2101\",\"1804\",\"1202\",\"1503\",\"8001\",\"1104\",\"1706\",\"1407\",\"1116\",\"1410\",\"1301\",\"1404\",\"2001\",\"1514\",\"1501\",\"1513\",\"1304\",\"1305\",\"1302\",\"1405\",\"1701\",\"1504\",\"1705\",\"1208\",\"1502\",\"1507\",\"1605\",\"1702\",\"1207\",\"1604\",\"1515\",\"1101\",\"1704\",\"1703\",\"1203\",\"1901\",\"1206\",\"1205\",\"1201\",\"1601\",\"1209\",\"1607\",\"1516\",\"1512\",\"1806\",\"1106\",\"1602\",\"1111\",\"1107\",\"1102\",\"1511\",\"1505\",\"1517\",\"1105\",\"1109\",\"1110\",\"1112\",\"1113\",\"1114\",\"1115\",\"1204\",\"1303\",\"1306\",\"1308\",\"1310\",\"1311\",\"1401\",\"1402\",\"1406\",\"1409\",\"1707\",\"2002\",\"1708\",\"1518\",\"1709\",\"2201\",\"2102\",\"1211\"],\"workStatus\":[0,11],\"x\":108073,\"y\":49646,\"z\":17}";

			
			parameter = "{\"mdFlag\":\"d\",\"gap\":10,\"pType\":\"sl\",\"types\":[\"1510\",\"1508\",\"1803\",\"2101\",\"1202\",\"1006\",\"1301\",\"2001\",\"1514\",\"1501\",\"1302\",\"1507\",\"1002\",\"1207\",\"1604\",\"1101\",\"1203\",\"1901\",\"1206\",\"1205\",\"1201\",\"1601\",\"1806\",\"1107\",\"1102\",\"1511\",\"1211\",\"1116\",\"1214\"],\"x\":215622,\"y\":99084,\"z\":18}";
			
			
			parameter="{\"pType\":\"fc\",\"gap\":10,\"types\":[\"1105\"],\"workStatus\":[0,2,1],\"x\":111610,\"y\":46704,\"z\":17}";
			//{"mdFlag":"d","gap":10,"pType":"sl","types":["1510","1508","1803","2101","1202","1006","1301","2001","1514","1501","1302","1507","1002","1207","1604","1101","1203","1901","1206","1205","1201","1601","1806","1107","1102","1511","1211","1116","1214"],"x":215622,"y":99084,"z":18}
			System.out.println(parameter);

			parameter = "{\"mdFlag\":\"d\",\"gap\":10,\"types\":[\"8002\",\"1403\",\"1510\",\"1508\",\"1506\",\"1606\",\"1803\",\"1509\",\"2101\",\"1804\"" +
					",\"1202\",\"1503\",\"8001\",\"1104\",\"1706\",\"1407\",\"1116\",\"1410\",\"1301\",\"1404\",\"2001\",\"1514\",\"1501\",\"1513\",\"1304\"" +
					",\"1305\",\"1302\",\"1405\",\"1701\",\"1504\",\"1705\",\"1208\",\"1502\",\"1507\",\"1605\",\"1702\",\"1207\",\"1604\",\"1515\",\"1101\"" +
					",\"1704\",\"1703\",\"1203\",\"1901\",\"1206\",\"1205\",\"1201\",\"1601\",\"1209\",\"1607\",\"1516\",\"1512\",\"1806\",\"1106\",\"1602\"" +
					",\"1111\",\"1107\",\"1102\",\"1511\",\"1505\",\"1517\",\"1105\",\"1109\",\"1110\",\"1112\",\"1113\",\"1114\",\"1115\",\"1204\",\"1303\"" +
					",\"1306\",\"1308\",\"1310\",\"1311\",\"1401\",\"1402\",\"1406\",\"1409\",\"1707\",\"2002\",\"1708\",\"1518\",\"1709\",\"2201\",\"2102\"" +
					",\"1211\",\"1117\",\"1214\"],\"workStatus\":[11],\"subtaskId\":720,\"x\":215725,\"y\":99050,\"z\":18}";
			parameter = "{\"mdFlag\":\"d\",\"gap\":10,\"pType\":\"sl\",\"types\":[\"1510\",\"1508\",\"1803\",\"2101\",\"1202\",\"1006\",\"1301\",\"2001\",\"1514\",\"1501\",\"1302\",\"1507\",\"1002\",\"1207\",\"1604\",\"1101\",\"1203\",\"1901\",\"1206\",\"1205\",\"1201\",\"1601\",\"1806\",\"1107\",\"1102\",\"1511\",\"1211\",\"1116\",\"1214\",\"1520\",\"1706\"],\"x\":431579,\"y\":198488,\"z\":19}";
			parameter = "{\"mdFlag\":\"d\",\"gap\":10,\"types\":[\"8002\",\"1403\",\"1510\",\"1508\",\"1506\",\"1606\",\"1803\",\"1509\",\"2101\",\"1804\"" +
                    ",\"1202\",\"1503\",\"1104\",\"1706\",\"1407\",\"1116\",\"1410\",\"1301\",\"1404\",\"2001\",\"1514\",\"1501\",\"1513\",\"1304\",\"1305\"" +
                    ",\"1302\",\"1405\",\"1701\",\"1504\",\"1705\",\"1208\",\"1502\",\"1507\",\"1605\",\"1702\",\"1207\",\"1604\",\"1515\",\"1101\",\"1704\"" +
                    ",\"1703\",\"1203\",\"1901\",\"1206\",\"1205\",\"1201\",\"1601\",\"1209\",\"1607\",\"1516\",\"1512\",\"1806\",\"1106\",\"1602\",\"1111\"" +
                    ",\"1107\",\"1102\",\"1511\",\"1505\",\"1517\",\"1105\",\"1109\",\"1110\",\"1112\",\"1113\",\"1114\",\"1115\",\"1204\",\"1303\",\"1306\"" +
                    ",\"1308\",\"1310\",\"1311\",\"1401\",\"1402\",\"1406\",\"1409\",\"1707\",\"2002\",\"1708\",\"1518\",\"1709\",\"2201\",\"2102\",\"1211\"" +
                    ",\"1117\",\"1214\"],\"workStatus\":[0,11],\"subtaskId\":810,\"x\":107926,\"y\":49626,\"z\":17}";
			parameter = "{\"mdFlag\":\"d\",\"gap\":10,\"pType\":\"sl\",\"types\":[\"1510\",\"1508\",\"1803\",\"2101\",\"1202\",\"1706\",\"1301\",\"2001\",\"1514\",\"1501\",\"1302\",\"1507\",\"1702\",\"1207\",\"1604\",\"1101\",\"1203\",\"1901\",\"1206\",\"1205\",\"1201\",\"1601\",\"1806\",\"1107\",\"1102\",\"1511\",\"1211\",\"1116\",\"1214\",\"1520\"],\"workStatus\":[0,1,2],\"x\":215794,\"y\":99243,\"z\":18}";
			//parameter = "{\"pType\":\"ms\",\"mdFlag\":\"f\",\"noQFilter\":[1,2],\"gap\":10,\"types\":[\"8002\",\"1403\",\"1510\",\"1508\",\"1506\",\"1606\",\"1803\",\"1509\",\"2101\",\"1804\",\"1202\",\"1503\",\"8001\",\"1104\",\"1706\",\"1407\",\"1116\",\"1410\",\"1301\",\"1404\",\"2001\",\"1514\",\"1501\",\"1513\",\"1304\",\"1305\",\"1302\",\"1405\",\"1701\",\"1504\",\"1705\",\"1208\",\"1502\",\"1507\",\"1605\",\"1702\",\"1207\",\"1604\",\"1515\",\"1101\",\"1704\",\"1703\",\"1203\",\"1901\",\"1206\",\"1205\",\"1201\",\"1601\",\"1209\",\"1607\",\"1516\",\"1512\",\"1806\",\"1106\",\"1602\",\"1111\",\"1107\",\"1102\",\"1511\",\"1505\",\"1517\",\"1105\",\"1109\",\"1110\",\"1112\",\"1113\",\"1114\",\"1115\",\"1204\",\"1303\",\"1306\",\"1308\",\"1310\",\"1311\",\"1401\",\"1402\",\"1406\",\"1409\",\"1707\",\"2002\",\"1708\",\"1518\",\"1709\",\"2201\",\"2102\"],\"x\":830,\"y\":414,\"z\":10}";
			System.out.println("**********************************************");
            System.out.println("**********************************************");
            System.out.println("**********************************************");
            System.out.println("**********************************************");
            System.out.println("reusut:----------------------------------\n"
			
					+solrSelector.searchDataByTileWithGap(parameter));
			
			

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
			JSONArray grids=new JSONArray();
			grids.add(60560302);
			String wkt = "POLYGON ((115.78478246015277 40.3580663376903, 117.06198634219226 40.3580663376903, 117.06198634219226 39.090405904000164, 115.78478246015277 39.090405904000164, 115.78478246015277 40.3580663376903))";
			wkt = GridUtils.grids2Wkt(grids);
			System.out.println(wkt);
//			JSONArray tips = solrSelector.searchDataByWkt(wkt, types,"d","wkt");
//			System.out.println(tips);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

	//根据网格获取tips统计
	@Test
	public void testGetStats() {
		String parameter = "{\"subtaskId\":57,\"mdFlag\":\"d\",\"workStatus\":0}";

//		String parameter = "{\"grids\":[59567311,59567312],\"subtaskId\":188,\"workStatus\":9}";
//
		try {
			System.out.println(solrSelector.getStats(parameter));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	//根据rowkey获取单个tips的详细信息
	@Test
	public void testSearchDataByRowkey() {
		try {
//			System.out.println("sorl by rowkey:");
//			System.out.println(solrSelector.searchDataByRowkey("73c0077b-e950-4079-9d3b-c7454c4109f9"));
            Set<Integer> collectTaskIds = new HashSet<>();
            collectTaskIds.add(5799999);
			List<TipsDao> tipsList = solrSelector.queryCollectTaskTips(collectTaskIds,
					TaskType.PROGRAM_TYPE_Q);
			Map<String, int[]> statsMap = new HashMap<>();
			for (TipsDao tip : tipsList) {
				JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);
				JSONObject snapshot = JSONObject.fromObject(tip, jsonConfig);
				JSONObject geoJson = snapshot.getJSONObject("wkt");// 统计坐标
				Geometry point = GeometryUtils.getPointFromGeo(GeoTranslator.geojson2Jts(geoJson));
				Coordinate coordinate = point.getCoordinates()[0];
                System.out.println("*******************************************************");
                System.out.println(coordinate.x);
				String gridId = CompGridUtil
						.point2Grids(coordinate.x, coordinate.y)[0];
				int tipStatus = snapshot.getInt("t_tipStatus");
				int dEditStatus = snapshot.getInt("t_dEditStatus");
				if (statsMap.containsKey(gridId)) {
					int[] statsArray = statsMap.get(gridId);
					if (tipStatus == 2 && dEditStatus != 2) {// 未完成
						statsArray[0] += 1;
					} else if (tipStatus == 2 && dEditStatus == 2) {// 已完成
						statsArray[1] += 1;
					}
				} else {
					int[] statsArray = new int[] { 0, 0 };
					if (tipStatus == 2 && dEditStatus != 2) {// 未完成
						statsArray[0] += 1;
					} else if (tipStatus == 2 && dEditStatus == 2) {// 已完成
						statsArray[1] += 1;
					}
					statsMap.put(gridId, statsArray);
				}
			}
			List<Map> list = new ArrayList<>();
			if (statsMap.size() > 0) {
				for (String gridId : statsMap.keySet()) {
					Map<String, Integer> map = new HashMap<>();
					map.put("gridId", Integer.valueOf(gridId));
					int[] statsArray = statsMap.get(gridId);
					map.put("finished", statsArray[1]);
					map.put("unfinished", statsArray[0]);
					list.add(map);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	//根据wkt范围获取tips的snapshot列表
	@Test
	public void testSearchDataBySpatial() {
		try {
//			JSONArray stages = new JSONArray();
//			stages.add(1);
//			stages.add(2);
//			JSONArray ja =
//					solrSelector.searchDataBySpatial("POLYGON ((116.25 39.75, 116.375 39.75, 116.375 39.83333, 116.25 39.83333, 116.25 39.75))",10,1901,stages);
//			//solrSelector.searchDataBySpatial("POLYGON ((113.70469 26.62879, 119.70818 26.62879, 119.70818 29.62948, 113.70469 29.62948, 113.70469 26.62879))");
//
//			System.out.println(ja.size()+"  "+ja.toString());

//            String parameter = "{\"subTaskId\":198,\"programType\":1}";
//            System.out.println(solrSelector.statInfoTask(parameter));

            solrSelector.checkUpdate(
                    "59567233" ,"20160101010101");
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






	/**
	 * 修改tips 删除old
	 *
	 * @param rowkey
	 * @return
	 * @throws Exception
	 */
	public  boolean updateOld2Null(String rowkey)
			throws Exception {

		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

		Put put = new Put(rowkey.getBytes());

		put.addColumn("data".getBytes(), "old".getBytes(),"{}".getBytes());

		htab.put(put);

		htab.close();

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

    @Test
    public void testListInfoTipsByPage() throws Exception {
        String parameter = "{\"subTaskId\":772,\"programType\":4,\"curPage\":1,\"pageSize\":10,\"tipStatus\":1}";
        TipsSelector selector = new TipsSelector();
        JSONObject jsonObject = selector.listInfoTipsByPage(parameter);
        System.out.println("*********************************************");
        System.out.println("*********************************************");
        System.out.println("*********************************************");
        System.out.println(jsonObject.toString());
    }

	@Test
	public void testImport() {
		String parameter = "{\"jobId\":74}";
		try {

			JSONObject jsonReq = JSONObject.fromObject(parameter);

			int jobId = jsonReq.getInt("jobId");

			int subtaskId = 0;

			//外业，有可能没有任务号
			if(jsonReq.containsKey("subtaskId")){

				subtaskId=jsonReq.getInt("subtaskId");
			}
			
			int userId=2922;

			//UploadService upload = UploadService.getInstance();

			// String filePath = upload.unzipByJobId(jobId); //服务测试

			//E:\03 ni_robot\Nav_Robot\10测试数据\01上传下载\音频测试数据\2677  2677道路名
			String filePath = "E:\\03 ni_robot\\Nav_Robot\\10测试数据\\robot\\3Dtips"; // 本地测试用

			//	String filePath = "E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\模式图测试数据\\548"; // 本地测试用

			// String
			filePath="E:\\03 ni_robot\\Nav_Robot\\10测试数据\\01上传下载\\74";

			Map<String, Photo> photoMap = new HashMap<String, Photo>();

			Map<String, Audio> audioMap = new HashMap<String, Audio>();

			TipsUpload tipsUploader = new TipsUpload(subtaskId);

			tipsUploader.run("F:\\FCC\\tips.txt", photoMap, audioMap,userId);

//			tipsUploader.run(filePath + "\\tips.txt", photoMap, audioMap);

			//tipsUploader.run(filePath + "\\tips.txt", photoMap, audioMap);

			//CollectorImport.importPhoto(photoMap, filePath);

			//AudioImport.importAudio(audioMap, filePath);

			JSONArray patternImageResultImpResult=PatternImageImporter.importImage(filePath + "/"+ "JVImage.txt",filePath +"/JVImage"); //JVImage为模式图的文件夹

			JSONObject result = new JSONObject();

			result.put("total", tipsUploader.getTotal());

			result.put("failed", tipsUploader.getFailed());

			result.put("reasons", tipsUploader.getReasons());

			result.put("regionResults", tipsUploader.getRegionResults());

			result.put("JVImageResult", patternImageResultImpResult);

			 //记录上传日志。不抛出异常
			insertStatisticsInfoNoException(jobId, subtaskId, userId,
					tipsUploader);

			System.out.println("开始上传tips完成，jobId:" + jobId + "\tresult:"
					+ result);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @Description:记录上传日志
	 * @param jobId
	 * @param subtaskId
	 * @param userId
	 * @param tipsUploader
	 * @throws Exception
	 * @author: y
	 * @time:2017-8-9 上午11:09:43
	 */
	private void insertStatisticsInfoNoException(int jobId, int subtaskId,
			long userId, TipsUpload tipsUploader)  {
		try{
			SysLogStats log = new SysLogStats();
			log.setLogType(SysLogConstant.TIPS_UPLOAD_TYPE);
			log.setLogDesc(SysLogConstant.TIPS_UPLOAD_DESC+",jobId :"+jobId+",subtaskId:"+subtaskId);
			log.setFailureTotal(tipsUploader.getFailed());
			log.setSuccessTotal(tipsUploader.getTotal()-tipsUploader.getFailed());  
			log.setTotal(tipsUploader.getTotal());
			log.setBeginTime(DateUtils.getSysDateFormat());
			log.setEndTime(DateUtils.getSysDateFormat());
			log.setErrorMsg(tipsUploader.getReasons().toString());
			log.setUserId(String.valueOf(userId));
			SysLogOperator.getInstance().insertSysLog(log);
		
		}catch (Exception e) {
			System.out.println("记录日志出错："+e.getMessage());
			e.printStackTrace();
		}
	}

	// @Test
	public void testGetByRowkey(){
		TipsSelector selector = new TipsSelector();

		try {

			JSONObject data = selector.searchDataByRowkey("11151515030481");
			System.out.println(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testGetByRowkeyNew(){
		TipsSelector selector = new TipsSelector();

		try {

			JSONObject data = selector.searchDataByRowkeyNew("028001542ba6f3a372404fbd40a87566643c93");
			System.out.println(data);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}



	// @Test
	public void testGetByRowkeys(){
		TipsSelector selector = new TipsSelector();

		try {
			JSONArray data = JSONArray.fromObject("[\"11151515030481\",\"11151515030491\"]");
			JSONArray resut = selector.searchDataByRowkeyArr(data);
			System.out.println(resut);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSelectorPoi(){
		TipsSelector selector = new TipsSelector();
		
		try{
			//JSONArray result = selector.searchPoiRelateTips("0220014a20f45489b740e6a93122e1f2c94f4e", 0, 30, 330);
			JSONObject obj = new JSONObject ();
			obj.put("programType", 1);
			obj.put("order", "lifecycle-asc");
			obj.put("dbId", 13);
			//JSONObject result = selector.searchGpsAndDeleteLinkTips(649, "2017-07-28", "2017-07-31", 20, 1,obj);
			
			JSONArray result = selector.searchPoiRelateTips("02200198b6885076094a3391a2702025daee56", 649, 30, 13, 1);
			System.out.println(result.toString());
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		TipsSelectorTest t=new TipsSelectorTest();

		try{
			t.updateOld2Null("022001A2C0E7831C8C4C0699BAA8CC4F08B702");
			t.updateOld2Null("0220017123EA6859E84566AE72B7058B366467");
			t.updateOld2Null("02200146D339648F224EC8A4A6352E3472C61A");
			t.updateOld2Null("02200125F29A320BC949CB8E9D70C8E1C2FF5F");
			t.updateOld2Null("02200127892B41A2A5495A8A7CDDA21245DDA8");
			t.updateOld2Null("022001A63B1B60F59245AC89546AE53363815D");
			t.updateOld2Null("0220013603D1856C9649FB8662DB05D88B8CF4");
			t.updateOld2Null("022001EF202DB44A73436EB9BB2F69EBA0C79E");
			t.updateOld2Null("022001564ECA95B24B47C982F62F769F017438");
			t.updateOld2Null("022001225E04332E9C423F804467C183969A1F");
			t.updateOld2Null("022001DE7AF5158F2647FFB02B4E6B8FAF293D");
			t.updateOld2Null("022001121BA3EC40874B62A3BF9948CE9EBC22");
			t.updateOld2Null("022001B552721C48FE466D8B868B42154F7291");
			t.updateOld2Null("022001A7E62AB367304E31850D8DF7E8CAB5E7");
			t.updateOld2Null("022001A6C9EF6746C14295B1C0FCD6535377FC");
			t.updateOld2Null("0220010DB186904E4D418FB95702141349F9FC");
			t.updateOld2Null("022001D4FE76FF544F4937AFBECB7E6135288E");
			t.updateOld2Null("022001083A12495E144138AA6FBBDF43E4F520");
			t.updateOld2Null("022001083A12495E144138AA6FBBDF43E4F520");
			t.updateOld2Null("02200100F5331EB0044B3BA3021BD039BB6653");
			t.updateOld2Null("0220016A54CC1989CD4633BD1B8FC7CAE22452");
			t.updateOld2Null("02200144980BB18C9B47E1BB1FA148BF6085CF");
			t.updateOld2Null("022001A4C9D4493BAF41C5BD00184A6F36C514");
			t.updateOld2Null("022001794791602C44485FB994989F57B16D52");
			t.updateOld2Null("022001E578359DD084411290FE08DF424F5F13");
			t.updateOld2Null("02200149019B5204B9484D9973D3621327260E");
			t.updateOld2Null("02200164EE3A45BB364FE7B03EEBFAFB87CF46");
			t.updateOld2Null("0220011ABF6D39A919403BA4F159EDC2532491");
		}catch (Exception e) {
			e.printStackTrace();
		}

	}


}