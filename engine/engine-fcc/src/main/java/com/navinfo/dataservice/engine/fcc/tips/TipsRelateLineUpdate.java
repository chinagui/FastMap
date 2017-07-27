package com.navinfo.dataservice.engine.fcc.tips;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import oracle.net.aso.f;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
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

	private TipsDao json; // tips信息（solr）
	private List<TipsDao> cutLines; // 打断后的测线
	private String sourceType = "";
	private String oldRowkey=""; //打断前测线的rowkey
	private int dbId;
	Connection tipsConn=null; //tips索引库连接
	
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
	 * @param dbId 
	 * @param tipsConn 
	 */
	public TipsRelateLineUpdate(String oldRowkey,TipsDao json2, List<TipsDao> resultArr, int dbId, Connection tipsConn) {
		this.json = json2;
		sourceType = json.getS_sourceType();
		cutLines=resultArr;
		this.oldRowkey=oldRowkey;
		this.dbId=dbId;
		this.tipsConn=tipsConn;
		
	}

	public TipsDao excute() throws Exception {

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

	private TipsDao updateJCTTips() {
		// TODO Auto-generated method stub
		return updateSimpleF();
	}



	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:10:36
	 */
	private TipsDao updateConstruction() {
		return updateFArray_Id();

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:10:04
	 */
	private TipsDao updateTunnel() {
		return updateFArray_Id();

	}

	/**
	 * @Description:TOOD [f_array].id
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:09:26
	 */
	private TipsDao updateBridgeTips() {
		return updateFArray_Id();

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:08:54
	 */
	private TipsDao updateLineAttrTips() {
		return updateFArray_Id();

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:08:42
	 */
	private TipsDao updateWalkStreetTips() {
		return updateFArray_Id();

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @throws Exception 
	 * @time:2017-4-13 上午10:08:20
	 */
	private TipsDao updateUpDownSeparateLine() throws Exception {
		
		JSONObject deep = JSONObject.fromObject(this.json.getDeep());
		
		List<Geometry> linkGeos=new ArrayList<Geometry>();
		
		String linePidStr=""; //测线的pid  因为有其他组成的测线，需要查询其坐标
		
		String linkPidStr=""; //link的pid

		int index = -1;//记录关联测线再关联数组中的位置
		
		JSONArray f_array = deep.getJSONArray("f_array");
		JSONArray f_array_new = new JSONArray(); // 一个新的f_array数组
		for(int i = 0; i < f_array.size(); i++){
			JSONObject fInfo = JSONObject.fromObject(f_array.get(i)); // 是个对象
			if(fInfo != null && fInfo.containsKey("type")) {
                int type = fInfo.getInt("type");
                String id = fInfo.getString("id");
                if (type == 2 && id.equals(oldRowkey)) {
                	index = i;
                	 for (TipsDao json : cutLines) {
            			 JSONObject newGeo = JSONObject.fromObject(json.getG_location());
            			 String idNew=json.getId();
            			 JSONObject newFInfo =JSONObject.fromObject(fInfo);//创建一个新的
            			 newFInfo.put("id", idNew);
            			 f_array_new.add(newFInfo); // 添加新对象到新数组
            			 Geometry geo = GeoTranslator.geojson2Jts(newGeo);
            			 linkGeos.add(geo);
            		}
                } 
                else{
                	 f_array_new.add(fInfo); 
                	//其他测线
                     if(type==2){
                     	
                     	linePidStr+=",'"+fInfo.getString("id")+"'";
                     }
                     //拿到关联线是link的pid
                     if(type == 1){
                     	linkPidStr+=","+fInfo.getString("id");
                     }
                }
                
            }
		}
			
		//更新g_location
		//==-1，说明就没有关联到当前测线，实际这样的情况应该不存在
		if(index ==-1)  return json;
		
		deep.put("f_array", f_array_new);
		json.setDeep(deep.toString());
		
		//RDLink的几何
		if(StringUtils.isNotEmpty(linkPidStr)){
			linkPidStr=linkPidStr.substring(1);
			List<Geometry> rdLinkGeos=getRdLinkGeoFromGdb(linkPidStr);
			linkGeos.addAll(rdLinkGeos);
		}
		
		//其他测线的几何
		if(StringUtils.isNotEmpty(linePidStr)){
			linePidStr=linePidStr.substring(1);
			List<Geometry> lineGeos=getLineGeoFromTipsOra(linePidStr);
			linkGeos.addAll(lineGeos);
		}

		
		////数据维护
		JSONObject gSLoc = JSONObject.fromObject(deep.getString("gSLoc"));
		JSONObject gELoc = JSONObject.fromObject(deep.getString("gELoc"));
		JSONObject gSLocNew=getNearlestLineId(gSLoc,linkGeos);
		JSONObject gELocNew = getNearlestLineId(gELoc,linkGeos);
		
		deep.put("gSLoc", gSLocNew);// 新的起点
		deep.put("gELoc", gELocNew);// 新的终点
		
		json.setDeep(deep.toString());
		json.setG_guide(gSLocNew.toString());
		json.setG_location(gSLocNew.toString());
		
		return json;

	}
	
	
	/**
	 * @Description:TOOD
	 * @param linePidStr
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-7-26 下午8:03:16
	 */
	private List<Geometry> getLineGeoFromTipsOra(String linePidStr) throws Exception {
		
		List<Geometry> result=new ArrayList<Geometry>();
		
		String sql="select * from  TIPS_INDEX  where id in("+linePidStr+")";
		
		TipsIndexOracleOperator op=new TipsIndexOracleOperator(tipsConn);
		
		List<TipsDao> daoList=op.query(sql,null);
		
		if(daoList!=null){
			
			for (TipsDao tipsDao : daoList) {
				JSONObject g_location = JSONObject.fromObject(tipsDao.getG_location());
				Geometry geo = GeoTranslator.geojson2Jts(g_location);
				result.add(geo);
			}
		}
		
		
		return result;
	}

	
	
	/**
	 * @Description:从大区库查询link的坐标
	 * @param linkPidStr:这里应该不会超过1000个，暂且不用clob
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-7-26 下午7:36:10
	 */
	private List<Geometry> getRdLinkGeoFromGdb(String linkPidStr) throws Exception {
		Connection conn=null;
		
		List<Geometry> geoList=new ArrayList<Geometry>();
		
		String sql="select geometry from rd_link  where link_pid in("+linkPidStr+")";
				
		try {
			conn = DBConnector.getInstance().getConnectionById(dbId);
			QueryRunner runner=new QueryRunner();
			ResultSetHandler<List<Geometry>> resultSetHandler = new ResultSetHandler<List<Geometry>>() {
				@Override
				public List<Geometry> handle(ResultSet rs)
						throws SQLException {
					List<Geometry>  geoList= new ArrayList<Geometry>();
					while (rs.next()) {
						STRUCT geometry=(STRUCT)rs.getObject("geometry");
						try {
							geoList.add(GeoTranslator.struct2Jts(geometry));
						} catch (Exception e) {
							throw new SQLException(e.getMessage());
						}
					}
					return geoList;
				}
			};
			
			geoList=runner.query(conn, sql, resultSetHandler);
			
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	
		return geoList;
	}
	
	
	
	/**
	 * @Description:获取离起点最近的点
	 * @param gSLoc
	 * @param linkGeos
	 * @return
	 * @author: y
	 * @time:2017-7-26 下午8:16:21
	 */
	private JSONObject getNearlestLineId(JSONObject gSLoc,
			List<Geometry> linkGeos) {
		Point point = (Point) GeoTranslator.geojson2Jts(gSLoc);
		Double minDistinct=null;
		Geometry nearlastLink=null;
		
		for (Geometry geometry : linkGeos) {
			double distinct=point.distance(geometry);
			 if(minDistinct==null||distinct<minDistinct){
				 minDistinct=distinct; 
				 nearlastLink = geometry;
			 }
		}
		
		return getNearLeastPoint(point,nearlastLink);
	}
	
	
	/**
	 * @Description:TOOD
	 * @param point
	 * @param lineGeo
	 * @return
	 * @author: y
	 * @time:2017-7-4 下午8:44:12
	 */
	private JSONObject getNearLeastPoint(Point point, Geometry lineGeo) {
		//打点tips到 测线的最近的点
		Coordinate targetPoint = GeometryUtils.GetNearestPointOnLine(GeoTranslator.transform(point,
	                0.00001, 5).getCoordinate(), GeoTranslator.transform(lineGeo, 0.00001, 5));
        JSONObject geoPoint = new JSONObject();
        geoPoint.put("type", "Point");
        geoPoint.put("coordinates", new double[]{targetPoint.x, targetPoint.y});
        
		return geoPoint;
	}

	/**
	 * @Description:1302 普通交限 in.id+[o_array].[out].id
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:08:04
	 */
	private TipsDao updateRestrictionTips() {

		boolean hasMeasuringLine = false;

		JSONObject deep = JSONObject.fromObject(this.json.getDeep());

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

                        	TipsDao nearLink = getNearlestLineId();
                            
                            out.put("id", nearLink.getId());

                            JSONObject  geo = JSONObject.fromObject(nearLink.getG_location());
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
            	
            	TipsDao nearLink = getNearlestLineId();
                String id = nearLink.getId();
                in.put("id", id);
                deep.put("in", in);// 新的
                hasMeasuringLine = true;
            }
        }

		// 如果有测线，则修改，并返回
		if (hasMeasuringLine) {

			json.setDeep(deep.toString());

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
	private TipsDao updateRdLaneTips() {

		boolean hasMeasuringLine = false;

		JSONObject deep = JSONObject.fromObject(this.json.getDeep());

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
                        	TipsDao nearLink = getNearlestLineId();
                            String id = nearLink.getId();
                            out.put("id", id);
                            JSONObject geo = JSONObject.fromObject(nearLink.getG_location());
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
            	TipsDao nearLink = getNearlestLineId();
                String id = nearLink.getId();
                in.put("id", id);
                deep.put("in", in);// 新的
                hasMeasuringLine = true;
            }
        }

		// 如果有测线，则修改，并返回
		if (hasMeasuringLine) {

			json.setDeep(deep.toString());

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
	private TipsDao updateSpeedLimitTips() {

		return updateSimpleF();

	}

	/**
	 * 简单的关联link修改 规格：deep.f.id
	 * 
	 * @return
	 */
	private TipsDao updateSimpleF() {
		JSONObject deep = JSONObject.fromObject(this.json.getDeep());

		JSONObject f = deep.getJSONObject("f");

		// 关联link是测线的
        if(f != null && f.containsKey("type")) {
            int type = f.getInt("type");
            String id = f.getString("id");
            if (type == 2 && id.equals(oldRowkey)) {
            	TipsDao nearLink = getNearlestLineId();
                String nearId = nearLink.getId();
                f.put("id", nearId);
                deep.put("f", f);
                json.setDeep(deep.toString());
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
	private TipsDao getNearlestLineId() {
		// tip的引导坐标
		JSONObject g_guide = JSONObject.fromObject(this.json.getG_guide());

		Point point = (Point) GeoTranslator.geojson2Jts(g_guide);

		// 打断后的测线显示坐标,计算 tips的引导坐标到显示坐标的距离，取最近的测线作为引导link
		
		 Double minDistinct=null;
		 TipsDao nearlastLink=null;
		 
		 for (TipsDao jsonObject : cutLines) {
			
			 JSONObject g_location1 = JSONObject.fromObject(jsonObject.getG_location());
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
	private TipsDao updateTollgateTips() {

		boolean hasMeasuringLine = false;

		JSONObject deep = JSONObject.fromObject(this.json.getDeep());


		// in.id
		JSONObject in = deep.getJSONObject("in");
		// 关联link是测线的
        if(in != null && in.containsKey("in")) {
            int inType = in.getInt("type");
            String inId = in.getString("id");
            if (inType == 2 && inId.equals(oldRowkey)) {
            	TipsDao nearLink = getNearlestLineId();
                String nearId = nearLink.getId();
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
            	TipsDao nearLink = getNearlestLineId();
                String nearId = nearLink.getId();
                out.put("id", nearId);
                deep.put("out", out);// 新的
                hasMeasuringLine = true;
            }
        }

		// 如果有测线，则修改，并返回
		if (hasMeasuringLine) {

			json.setDeep(deep.toString());

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
	private TipsDao updateTrafficSignalTips() {

		return updateFAarrary_F();

	}

	private TipsDao updateFAarrary_F() {
		boolean hasMeasuringLine = false;
        try {
            JSONObject deep = JSONObject.fromObject(this.json.getDeep());

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

                    	TipsDao nearLink = getNearlestLineId();
                        String nearId = nearLink.getId();
                        f.put("id", nearId);
                        JSONObject geo  = JSONObject.fromObject(nearLink.getG_location());
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

                json.setDeep(deep.toString());
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
	private TipsDao updateRampTips() {
		return updateSimpleF();

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:06:23
	 */
	private TipsDao updatePATips() {
		return updateSimpleF();

	}

	/**
	 * @Description:TOOD
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午10:06:20
	 */
	private TipsDao updateSATips() {
		return updateSimpleF();

	}

	/**
	 * @Description:1203 （道路方向） f.id
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午9:59:13
	 */
	private TipsDao updateKindLaneTips() {
		return updateSimpleF();

	}

	/**
	 * @Description:挂接 无关联link，不维护
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午9:58:32
	 */
	private TipsDao updateHookTips() {
		return null;

	}

	/**
	 * @Description:1116 立交 [f_array].id
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午9:58:25
	 */
	private TipsDao updateGSCTips() {

		return updateFArray_Id();

	}

	
	/**
	 * 特殊说明：起终点+范围线+立交，测线打断后需要 将讲的测线替换为打断后的所有测线
	 * @Description:TOOD
	 * @return
	 * @author: 
	 * @time:2017-6-22 上午9:19:53
	 */
	private TipsDao updateFArray_Id() {
		boolean hasMeasuringLine = false;

		JSONObject deep = JSONObject.fromObject(this.json.getDeep());

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
                	
                	 for (TipsDao json : cutLines) {
            			 JSONObject newGeo = JSONObject.fromObject(json.getG_location());
            			 String idNew=json.getId();
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

			json.setDeep(deep.toString()); //1.修改deep
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
	private TipsDao updateKindTips() {

		return updateSimpleF();

	}

	/**
	 * @Description:1203 f.id
	 * @author: y
	 * @return
	 * @time:2017-4-13 上午9:58:10
	 */
	private TipsDao updateLinkDirTips() {
		return updateSimpleF();

	}
}
