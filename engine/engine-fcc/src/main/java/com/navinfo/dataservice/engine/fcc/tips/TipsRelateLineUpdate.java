package com.navinfo.dataservice.engine.fcc.tips;

import java.util.List;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: TipsRelateLineUpdate.java
 * @author y
 * @date 2017-4-13 上午9:54:08
 * @Description: 测线跨图幅打断、测线打断。维护测线上关联要素的 ：关联id、显示坐标（起终点和范围线需要维护）
 * 
 */
public class TipsRelateLineUpdate {

	private JSONObject json; // tips信息（solr）
	private List<JSONObject> cutLines; // 打断后的测线
	private String sourceType = "";
	private String oldRowkey=""; //打断前测线的rowkey
	
	/**
	 * @param json
	 *            :要维护的tips
	 * @param line1
	 * @param line2
	 */
	public TipsRelateLineUpdate(JSONObject json, JSONObject line1,
			JSONObject line2) {
		super();
	
	}

	/*
	 * 28类情报的tips 1.道路形状（测线） 2001 2.道路挂接 1803 3.立交(分层) 1116 4.道路种别 1201
	 * 5.道路方向（含时间段单方向道路） 1203 6.车道数 1202 7.SA 1205 8.PA 1206 9.匝道 1207 10.IC\JCT
	 * 1211 11.红绿灯（点属性） 1102 12.收费站（点属性） 1107 13.点限速（点属性） 1101 14.车道信息（关系属性）
	 * 1301 15.交通限制（关系属性）nk 1302 16.上下分离 1501 17.步行街 1507 18.公交专用道 1508 19.桥
	 * 1510 20.隧道 1511 21.施工 1514 22.环岛 1601 23.区域内道路 1604 24.铁路道口 1702 25.道路名
	 * 1901 26.删除标记 2101 27. ADAS打点1706 28. 草图 1806
	 */

	/**
	 * @param json2
	 * @param resultArr
	 */
	public TipsRelateLineUpdate(String oldRowkey,JSONObject json2, List<JSONObject> resultArr) {
		this.json = json2;
		sourceType = json.getString("s_sourceType");
		cutLines=resultArr;
		this.oldRowkey=oldRowkey;
		
	}

	public JSONObject excute() {

		switch (sourceType) {
            case "1803":// 2.挂接 null
                return updateHookTips();
            case "1116":// 3.立交 [f_array].id
                return updateGSCTips();
            case "1201":// 4.种别 f.id
                return updateKindTips();
            case "1203":// 5.道路通行方向 f.id
                return updateLinkDirTips();
            case "1202":// 6. 车道数 f.id
                return updateKindLaneTips();
                // 7.SA、PA、匝道 f.id
            case "1205":
                return updateSATips();
            case "1206": // 8 .PA f.id
                return updatePATips();
            case "1207": // 9.匝道 f.id
                return updateRampTips();
            case "1211": // 10.IC\JCT f.id
                return updateJCTTips();
            case "1102":// 11 .红绿灯 [f_array].f
                return updateTrafficSignalTips();
            case "1107":// 12.收费站 in.id+out.id 复杂的----
                return updateTollgateTips();
            case "1101":// 13. 点限速 f.id
                return updateSpeedLimitTips();
            case "1301":// 14. 车道信息（车信） 复杂的----
                return updateRdLaneTips();
            case "1302":// 15. 普通交限 复杂的----
                return updateRestrictionTips();
            case "1501": // 16. 上下线分离 [f_array].id
                return updateUpDownSeparateLine();
            case "1507":// 17.步行街 [f_array].id
                return updateWalkStreetTips();
            case "1508":// 18.公交专用道 [f_array].id
                return updateLineAttrTips();
                // 起终点类
            case "1510":// 19. 桥 [f_array].id
                return updateBridgeTips();
            case "1511":// 20. 隧道 [f_array].id
                return updateTunnel();
            case "1514":// 21.施工 [f_array].id
                return updateConstruction();
                // 范围线类
            case "1601":// 22. 环岛 [f_array].id
                return updateFArray_Id();
            case "1604":// 23. 区域内道路 [f_array].id
                return updateFArray_Id();
            case "1702":// 24. 铁路道口 f.id
                return updateSimpleF();
            case "1901":// 25. 道路名 null
                return null;
            case "2101":// 26.删除道路标记 null
            	return updateSimpleF();
            case "1706"://27.ADAS打点 f.id
                return updateSimpleF();
            case "1806"://28.草图
                return null;
		    default:
			    return null;
		}

	}

	private JSONObject updateJCTTips() {
		// TODO Auto-generated method stub
		return updateSimpleF();
	}



	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:10:36
	 */
	private JSONObject updateConstruction() {
		return updateFArray_Id();

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:10:04
	 */
	private JSONObject updateTunnel() {
		return updateFArray_Id();

	}

	/**
	 * @Description:TOOD [f_array].id
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:09:26
	 */
	private JSONObject updateBridgeTips() {
		return updateFArray_Id();

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:08:54
	 */
	private JSONObject updateLineAttrTips() {
		return updateFArray_Id();

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:08:42
	 */
	private JSONObject updateWalkStreetTips() {
		return updateFArray_Id();

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:08:20
	 */
	private JSONObject updateUpDownSeparateLine() {
		return updateFArray_Id();

	}

	/**
	 * @Description:1302 普通交限 in.id+[o_array].[out].id
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:08:04
	 */
	private JSONObject updateRestrictionTips() {

		boolean hasMeasuringLine = false;

		JSONObject deep = JSONObject.fromObject(this.json.getString("deep"));

		//out:[o_array].[out].id
		JSONArray o_array = deep.getJSONArray("o_array");

		JSONArray o_array_new = new JSONArray(); // 一个新的o_array数组
        try {
            for (Object object : o_array) {

                JSONObject o_object = JSONObject.fromObject(object);

                JSONArray ourArr = o_object.getJSONArray("out");

                JSONArray ourArr_new = new JSONArray(); // 一个新的out数组

                for (Object object2 : ourArr) {

                    JSONObject out = JSONObject.fromObject(object2);

                    // 关联link是测线的
                    if(out != null && out.containsKey("type")) {
                        int outType = out.getInt("type");
                        String outId = out.getString("id");
                        if (outType == 2 && outId.equals(oldRowkey)) {

                            JSONObject nearLink = getNearlestLineId();
                            
                            out.put("id", nearLink.getString("id"));

                            JSONObject  geo = nearLink.getJSONObject("g_location");
                            Geometry lineGeo = GeoTranslator.geojson2Jts(geo);
                            Geometry midGeo = GeometryUtils.getMidPointByLine(lineGeo);
                            out.put("geo", GeoTranslator.jts2Geojson(midGeo));

                            out.put("out", out);// 新的

                            hasMeasuringLine = true;
                        }
                    }
                    ourArr_new.add(out);

                }
                o_object.put("out", ourArr_new);

                o_array_new.add(o_object);

            }

            deep.put("o_array", o_array);
        }catch (Exception e) {
            e.printStackTrace();
        }
		
		// in:in.id
		JSONObject in = deep.getJSONObject("in");

		// 关联link是测线的
        if(in != null && in.containsKey("type")) {
            int inType = in.getInt("type");
            String inId = in.getString("id");
            if (inType == 2 && inId.equals(oldRowkey)) {
            	
                JSONObject nearLink = getNearlestLineId();
                String id = nearLink.getString("id");
                in.put("id", id);
                deep.put("in", in);// 新的
                hasMeasuringLine = true;
            }
        }

		// 如果有测线，则修改，并返回
		if (hasMeasuringLine) {

			json.put("deep", deep);

			return json;
		}

		return null;

	}

	/**
	 * @Description:1301 车道信息(车信) in.id+[o_array].[d_array].out.id(out是对象)
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:07:50
	 */
	private JSONObject updateRdLaneTips() {

		boolean hasMeasuringLine = false;

		JSONObject deep = JSONObject.fromObject(this.json.getString("deep"));

		// out:[o_array].[d_array].out.id(out是对象)
		JSONArray o_array = deep.getJSONArray("o_array");

		JSONArray o_array_new = new JSONArray(); // 一个新的o_array数组
        try {
            for (Object object : o_array) {

                JSONObject o_array_info = JSONObject.fromObject(object); // 一个o_array对象

                JSONArray d_array = o_array_info.getJSONArray("d_array");

                JSONArray d_array_new = new JSONArray(); // 一个新的d_array数组

                for (Object object2 : d_array) {

                    JSONObject dInfo = JSONObject.fromObject(object2);

                    JSONObject out = dInfo.getJSONObject("out");

                    // 关联link是测线的
                    if(out != null && out.containsKey("type")) {
                        int outType = out.getInt("type");
                        String outId = out.getString("id");
                        if (outType == 2 && outId.equals(oldRowkey)) {
                            JSONObject nearLink = getNearlestLineId();
                            String id = nearLink.getString("id");
                            out.put("id", id);
                            JSONObject geo = nearLink.getJSONObject("g_location");
                            Geometry lineGeo = GeoTranslator.geojson2Jts(geo);
                            Geometry midGeo = GeometryUtils.getMidPointByLine(lineGeo);
                            out.put("geo", GeoTranslator.jts2Geojson(midGeo));
                            dInfo.put("out", out);// 新的
                            hasMeasuringLine = true;
                        }
                    }

                    d_array_new.add(dInfo);
                }

                o_array_info.put("d_array", d_array_new);// 新的 d_array_new
                o_array_new.add(o_array_info);
            }

            deep.put("o_array", o_array_new); // 新的
        }catch (Exception e) {
            e.printStackTrace();
        }

		// in:in.id
		JSONObject in = deep.getJSONObject("in");

		// 关联link是测线的
        if(in != null && in.containsKey("type")) {
            int inType = in.getInt("type");
            String inId = in.getString("id");
            if (inType == 2 && inId.equals(oldRowkey)) {
                JSONObject nearLink = getNearlestLineId();
                String id = nearLink.getString("id");
                in.put("id", id);
                deep.put("in", in);// 新的
                hasMeasuringLine = true;
            }
        }

		// 如果有测线，则修改，并返回
		if (hasMeasuringLine) {

			json.put("deep", deep);

			return json;
		}

		return null;

	}

	/**
	 * @Description:1101 点限速，关联测线修改 f.id
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:07:31
	 */
	private JSONObject updateSpeedLimitTips() {

		return updateSimpleF();

	}

	/**
	 * 简单的关联link修改 规格：deep.f.id
	 * 
	 * @return
	 */
	private JSONObject updateSimpleF() {
		JSONObject deep = JSONObject.fromObject(this.json.getString("deep"));

		JSONObject f = deep.getJSONObject("f");

		// 关联link是测线的
        if(f != null && f.containsKey("type")) {
            int type = f.getInt("type");
            String id = f.getString("id");
            if (type == 2 && id.equals(oldRowkey)) {
                JSONObject nearLink = getNearlestLineId();
                String nearId = nearLink.getString("id");
                f.put("id", nearId);
                deep.put("f", f);
                json.put("deep", deep);
                return json;
            }
        }

		// 关联的不是测线，则不返回
	    return null;
	}

	/**
	 * @Description:TOOD
	 * @return
	 * @author: y
	 * @time:2017-4-17 下午5:55:08
	 */
	private JSONObject getNearlestLineId() {
		// tip的引导坐标
		JSONObject g_guide = JSONObject.fromObject(this.json
				.getString("g_guide"));

		Point point = (Point) GeoTranslator.geojson2Jts(g_guide);

		// 打断后的测线显示坐标,计算 tips的引导坐标到显示坐标的距离，取最近的测线作为引导link
		
		 Double minDistinct=null;
		 JSONObject nearlastLink=null;
		 
		 for (JSONObject jsonObject : cutLines) {
			
			 JSONObject g_location1 = jsonObject.getJSONObject("g_location");
			 Geometry geo1 = GeoTranslator.geojson2Jts(g_location1);
			 double distinct=point.distance(geo1);
			 if(minDistinct==null||distinct<minDistinct){
				 minDistinct=distinct; 
				 nearlastLink=jsonObject;
			 }
		}
		return nearlastLink;
	}

	/**
	 * @Description:1107 收费站 in.id+out.id
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:07:15
	 */
	private JSONObject updateTollgateTips() {

		boolean hasMeasuringLine = false;

		JSONObject deep = JSONObject.fromObject(this.json.getString("deep"));


		// in.id
		JSONObject in = deep.getJSONObject("in");
		// 关联link是测线的
        if(in != null && in.containsKey("in")) {
            int inType = in.getInt("type");
            String inId = in.getString("id");
            if (inType == 2 && inId.equals(oldRowkey)) {
                JSONObject nearLink = getNearlestLineId();
                String nearId = nearLink.getString("id");
                in.put("id", nearId);
                deep.put("in", in);// 新的
                hasMeasuringLine = true;
            }
        }

		// out.id
		JSONObject out = deep.getJSONObject("out");

		// 关联link是测线的
        if(out != null && out.containsKey("id")) {
            int outType = out.getInt("type");
            String outId = out.getString("id");
            if (outType == 2 && outId.equals(oldRowkey)) {
                JSONObject nearLink = getNearlestLineId();
                String nearId = nearLink.getString("id");
                out.put("id", nearId);
                deep.put("out", out);// 新的
                hasMeasuringLine = true;
            }
        }

		// 如果有测线，则修改，并返回
		if (hasMeasuringLine) {

			json.put("deep", deep);

			return json;
		}
		return null;

	}

	/**
	 * @Description:"1102":// 红绿灯 [f_array].f.id (f唯一是对象)
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:06:58
	 */
	private JSONObject updateTrafficSignalTips() {

		return updateFAarrary_F();

	}

	private JSONObject updateFAarrary_F() {
		boolean hasMeasuringLine = false;
        try {
            JSONObject deep = JSONObject.fromObject(this.json.getString("deep"));

            JSONArray f_array = deep.getJSONArray("f_array");

            JSONArray f_array_new = new JSONArray(); // 一个新的f_array数组

            for (Object object : f_array) {

                JSONObject fInfo = JSONObject.fromObject(object);

                JSONObject f = fInfo.getJSONObject("f"); // 是个对象

                if(f != null && f.containsKey("type")) {
                    int type = f.getInt("type");
                    String id = f.getString("id");
                    // 关联link是测线的
                    if (type == 2 && id.equals(oldRowkey)) {

                        JSONObject nearLink = getNearlestLineId();
                        String nearId = nearLink.getString("id");
                        f.put("id", nearId);
                        JSONObject geo  = nearLink.getJSONObject("g_location");
                        Geometry lineGeo = GeoTranslator.geojson2Jts(geo);
                        Geometry midGeo = GeometryUtils.getMidPointByLine(lineGeo);
                        fInfo.put("geo", GeoTranslator.jts2Geojson(midGeo));
                        fInfo.put("f", f);

                        hasMeasuringLine = true;

                    }
                }

                f_array_new.add(fInfo); // 添加到新数组

            }

            if (hasMeasuringLine) {
                deep.put("f_array", f_array_new);// 新的

                json.put("deep", deep);
                return json;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
		return null;
	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:06:25
	 */
	private JSONObject updateRampTips() {
		return updateSimpleF();

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:06:23
	 */
	private JSONObject updatePATips() {
		return updateSimpleF();

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:06:20
	 */
	private JSONObject updateSATips() {
		return updateSimpleF();

	}

	/**
	 * @Description:1203 （道路方向） f.id
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午9:59:13
	 */
	private JSONObject updateKindLaneTips() {
		return updateSimpleF();

	}

	/**
	 * @Description:挂接 无关联link，不维护
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午9:58:32
	 */
	private JSONObject updateHookTips() {
		return null;

	}

	/**
	 * @Description:1116 立交 [f_array].id
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午9:58:25
	 */
	private JSONObject updateGSCTips() {

		return updateFArray_Id();

	}

	
	/**
	 * 特殊说明：起终点+范围线+立交，测线打断后需要 将讲的测线替换为打断后的所有测线
	 * @Description:TOOD
	 * @return
	 * @author: 
	 * @time:2017-6-22 上午9:19:53
	 */
	private JSONObject updateFArray_Id() {
		boolean hasMeasuringLine = false;

		JSONObject deep = JSONObject.fromObject(this.json.getString("deep"));

		JSONArray f_array = deep.getJSONArray("f_array");

		JSONArray f_array_new = new JSONArray(); // 一个新的f_array数组
		
		int index=-1; //旧测线在数组中的位置，用户更新g_location(范围线使用)
		
		JSONArray geoArr=new JSONArray();//范围线使用
		
		int i=-1;
		for (Object object : f_array) {
			
			i++;

			JSONObject fInfo = JSONObject.fromObject(object); // 是个对象

			// 关联link是测线的
            if(fInfo != null && fInfo.containsKey("type")) {
                int type = fInfo.getInt("type");
                String id = fInfo.getString("id");
            	
	           	JSONObject geoF=null;
	           	 
	           	 if(fInfo.containsKey("geoF")){
	           		 
	           		geoF=fInfo.getJSONObject("geoF");
	           	 }
                if (type == 2 && id.equals(oldRowkey)) {
                	
                	 for (JSONObject json : cutLines) {
            			 JSONObject newGeo = json.getJSONObject("g_location");
            			 String idNew=json.getString("id");
            			 JSONObject newFInfo =JSONObject.fromObject(fInfo);//创建一个新的
            			 newFInfo.put("id", idNew);
            			 //只有立交有geo
            			 if(newFInfo.containsKey("geo")){
            				 newFInfo.put("geo", newGeo); 
            			 }
            			 //范围线
            			 if(newFInfo.containsKey("geoF")){
            				 newFInfo.put("geoF", newGeo); 
            			 }
            			 f_array_new.add(newFInfo); // 添加新对象到新数组
            			 geoArr.add(newGeo);
            		}
                    hasMeasuringLine = true;
                    index=i;
                }
                //如果关联的不是当前测线，则将原来的也添加到新数组
                else{
                	
                	f_array_new.add(fInfo); // 添加到新数组
                	
                	geoArr.add(geoF);
                }
            }

		}
		
		// 如果有测线，则修改，并返回
		if (hasMeasuringLine) {

			deep.put("f_array", f_array_new);// 新的

			json.put("deep", deep); //1.修改deep
			//int index,JSONObject  json2Update,String sourceType,List<JSONObject> cutLines
			//json=GLocationUpdate.updateAreaLineLocation(index,json,sourceType,cutLines);
			
		   //范围线的，需要重新计算范围线的g_location
			if(index!=-1&&("1601".equals(sourceType)||"1604".equals(sourceType))){
				
				json=GLocationUpdate.updateAreaLineLocation(geoArr,json);
			}
		
			//起终点的，需要替换g_location.将旧的坐标替换为新的两条或者多条线的坐标
			if(index != -1 && ("1507".equals(sourceType) || "1508".equals(sourceType)
					 || "1510".equals(sourceType) || "1511".equals(sourceType) || "1514".equals(sourceType))){
				
				json=GLocationUpdate.updateStartEndPointLocation(index,json,sourceType,cutLines);
				
			}
			
			return json;
		}

		return json;
	}

	
	/**
	 * @Description:种别 f.id
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午9:58:15
	 */
	private JSONObject updateKindTips() {

		return updateSimpleF();

	}

	/**
	 * @Description:1203 f.id
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午9:58:10
	 */
	private JSONObject updateLinkDirTips() {
		return updateSimpleF();

	}
}
