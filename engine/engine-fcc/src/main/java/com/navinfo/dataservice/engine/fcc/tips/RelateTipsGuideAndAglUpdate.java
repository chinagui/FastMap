package com.navinfo.dataservice.engine.fcc.tips;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.model.TipsDao;
import com.navinfo.dataservice.dao.fcc.operator.TipsIndexOracleOperator;
import com.navinfo.dataservice.engine.fcc.tips.check.GdbDataQuery;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

/**
 * @ClassName: RelateTipsGuideAndAglUpdate.java
 * @author y
 * @date 2017-6-26 下午7:02:24
 * @Description: tips的引导坐标和角度计算
 */
public class RelateTipsGuideAndAglUpdate {

	private TipsDao json; // 关联tips信息（solr）

	private String sourceType = "";

	private List<TipsDao> linesAfterCut = null; // 打断后的测线
	
	Connection tipsConn=null; //tips 索引库连接
	
	private int dbId;

	public RelateTipsGuideAndAglUpdate(TipsDao json,
			List<TipsDao> linesAfterCut, int dbId, Connection tipsConn) {

		this.json = json;

		sourceType = json.getS_sourceType();

		this.linesAfterCut = linesAfterCut;
		
		this.dbId=dbId;
		
		this.tipsConn=tipsConn;
	}

	/**
	 * @Description:维护[显示坐标]、引导坐标、角度、[起点、终点、geo]
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-6-27 下午1:44:01
	 */
	public TipsDao excute() throws Exception {
		
		//说明：包含打断的线  才更新的哦  linesAfterCut
		
		//1.其他类型：修形不维护
		switch (sourceType) {
		case "2001"://测线
        case "1803"://挂接
        case "1806"://28.草图
        case "1102":// 11 .红绿灯   
        case "1901":// 25. 道路名 null
            return json;
           // return json;
        
        
        //2.打点类：维护g_guide维护：显示坐标到引导link的最近点
        case "1201":// 4.种别   f.id
        case "1203":// 5.道路通行方向    f.id
        case "1202":// 6. 车道数 f.id 
       // 7.SA、PA、匝道 f.id  g_location到f.id的
        case "1205": 
        case "1206": // 8 .PA f.id
        case "1207": // 9.匝道 f.id
        case "1211": // 10.IC\JCT  g_location到f.id
        case "1101":// 13. 点限速 f.id  g_location到f.id的垂足
        case "1706"://27.ADAS打点   g_location到f.id
        case "1702":// 24. 铁路道口  g_location到f.id
        case "2101":// 26.删除道路标记f.id
        case "1214":// 29.删除在建属性f.id
            return updateSimpleFPointTips();
        	
        	
        case "1107":// 12.收费站  g_location到in.id的垂足
        case "1301":// 14. 车道信息 g_location到in.id的垂足
        case "1302":// 15. 普通交限 复杂的 g_location到in.id的垂足
            return updateSimpleInPointTips();
        	
            
        // 4.范围线类
        //范围线，测线打断后，范围线的g_location：根据修形后的link重新维护几何坐标）。范围线的引导坐标是，范围线组成线组成的多形的中心。geo根据修形后的link重新计算几何中心点.g_guide=geo
        case "1604":// 23. 区域内道路   范围线
        case "1601":// 22. 环岛     范围线  
            return updateAreaLine();
            
    
         // 起终点类
        case "1507":// 22.步行街
        case "1508":// 23.公交专用道
        case "1510":// 24.桥 
        case "1511":// 25.隧道 
        case "1514":// 26.施工 
        case "1520":// 30.在建时间变更
            return updateStartEndPoint();
            
        case "1501":// 21.上下分离  
        	return updateSeparationLine();
        	
        case "1116"://立交
          	return updateGsc();
       
	    default:
		    return null;
	}

	}

	/**
	 * @Description:立交修形维护g_location及Geo
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-8-15 下午1:31:30
	 */
	private TipsDao updateGsc() throws Exception {
		Connection oraConn=null;
		boolean hasMeasuringLine = false;
		try{
			oraConn = DBConnector.getInstance().getConnectionById(dbId);
	
			JSONObject deep = JSONObject.fromObject(this.json.getDeep());
	
			JSONArray f_array = deep.getJSONArray("f_array");
	
			JSONArray f_array_new = new JSONArray(); // 一个新的f_array数组
			
			//>2则说明三条线以上，三条线以上，则必须是原原来交点相同，修行前有判断，所以这里不用在维护
			if(f_array.size()>2){
				return json;
			}
			
			JSONObject lineLocation=null;//测线的location
			String lineRowkey=null;//测线的id
			
			JSONObject newFInfo1=null; //测线修行后的，一个新的fInfo 测线的
			
			JSONObject newFInfo2=null; //测线修行后的，一个新的fInfo 另一个组成线的  另一个组成线，因为交点变了，所以也需要重新计算弧段
			
			String otherLineId=null;//立交的另一条组成线的id
			
			int otherLineType=0;//立交的另一条组成线类型
			
			
			//只有一条说明就是原有测线本身，是修形，替换g_location,如果》1说明是跨图幅打断了，打断，已经在打断中维护过了
			if(linesAfterCut.size()==1){
				lineRowkey= linesAfterCut.get(0).getId();
				lineLocation=JSONObject.fromObject(linesAfterCut.get(0).getG_location()) ;//测线的id
				
				for (Object object : f_array) {
					JSONObject fInfo = JSONObject.fromObject(object); // 是个对象
					// 关联link是测线的
		            if(fInfo != null && fInfo.containsKey("type")) {
		                int type = fInfo.getInt("type");
		                String id = fInfo.getString("id");
		            	
		                if (type == 2 && id.equals(lineRowkey)) {
		                	 hasMeasuringLine=true;
			               	 newFInfo1 =JSONObject.fromObject(fInfo);//创建一个新的
		                }
		                else{
		                	 newFInfo2 =JSONObject.fromObject(fInfo);//创建一个新的
		                	 otherLineId=id;
		                	 otherLineType=type;
		                }
		            }
				}
			}
			
			 //2.查询其他关联线的几何
			 Geometry otherLineGeo=null; //另一个组成线的几何
			 GdbDataQuery oraQuery=new GdbDataQuery(oraConn);
			 //测线，索引库查wktlocation 就可以
			 //1 道路LINK；2 测线；3 铁路LINK
			 if(2==otherLineType){
				List<Geometry> tipsGeo=new ArrayList<Geometry>();
				 String sql="SELECT * FROM tips_index  d WHERE ID IN(?)";
				 Clob  pidClob=ConnectionUtil.createClob(tipsConn,otherLineId);
				 List<TipsDao>  tipsList=new TipsIndexOracleOperator(tipsConn).query(sql, pidClob);
				 for (TipsDao tipsDao : tipsList) {
					tipsGeo.add(tipsDao.getWktLocation());
				 }
			 }
			 //铁路
			 if(3==otherLineType){
				 List<Geometry> list=oraQuery.queryLineGeometry("RW_LINK",otherLineId);
				 if(list.size()>0){
					 otherLineGeo=list.get(0);
				 }
			 }
			 //rd_link
			 if(1==otherLineType){
				 List<Geometry> list=oraQuery.queryLineGeometry("RD_LINK",otherLineId);
				 if(list.size()>0){
					 otherLineGeo=list.get(0);
				 }
			 }
			 
			 //3.获取到新的交点
			 
			 Geometry lineGeomtry = (Geometry) GeoTranslator.geojson2Jts(lineLocation);
			 Point interGeo=(Point)lineGeomtry.intersection(otherLineGeo);//交点
			 
			 JSONObject newLocationJson = GeoTranslator.jts2Geojson(interGeo);
			 
			 //计算，打断后组成线和立交几何，左右2米的一个几何弧端
			 Geometry geoLine=TipsGeoUtils.getGscLineGeo(lineGeomtry,interGeo);
			 JSONObject  newGeo= GeoTranslator.jts2Geojson(geoLine);
		     newFInfo1.put("geo", newGeo); //更新geo
			 
		     
			 Geometry geoTheOtherLine=TipsGeoUtils.getGscLineGeo(otherLineGeo,interGeo);
			 JSONObject  newGeoTheOther= GeoTranslator.jts2Geojson(geoTheOtherLine);
		     newFInfo2.put("geo", newGeoTheOther); //更新geo
			 
			 f_array_new.add(newFInfo1); // 添加新对象到新数组
			 f_array_new.add(newFInfo2); // 添加新对象到新数组
			 
			 
			// 如果有测线，则修改，并返回
			if (hasMeasuringLine) {
				json.setG_location(newLocationJson.toString());
				deep.put("f_array", f_array_new);// 新的
				json.setDeep(deep.toString()); //1.修改deep
				return json;
			}
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(oraConn);
			throw e;
		}finally{
			DbUtils.commitAndCloseQuietly(oraConn);
		}

		return json;
		
	}

	/**
	 * @Description:打点tips维护1
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-7-4 下午8:16:36
	 */
	private TipsDao updateSimpleFPointTips() throws Exception {
		
		//打点tips的显示坐标.
		JSONObject g_location  =JSONObject.fromObject(json.getG_location()) ; 
		Point point = (Point) GeoTranslator.geojson2Jts(g_location);
		
		//打点tips的关联link_id
		JSONObject deep = JSONObject.fromObject(this.json.getDeep());
		JSONObject f = deep.getJSONObject("f");
		String lineId=f.getString("id");
		
		//测线的坐标
		JSONObject lineLocation=null;
		
		//测线的坐标，如果只有一条，则就就是当前测线的坐标。如果是多条。则是tips中记录的关联测线
		for (TipsDao line : linesAfterCut) {
			
			if(lineId.equals(line.getId())){
				
				lineLocation=JSONObject.fromObject(line.getG_location());
				
				break;
			}
		}
		
		Geometry lineGeo= GeoTranslator.geojson2Jts(lineLocation);
		
		JSONObject  g_guide= getNearLeastPoint(point, lineGeo);
		
		json.setG_guide(g_guide.toString());
		
		//1706（GPS打点） 1702  铁路道口需要同时维护g_location=g_guide
		if("1706".equals(sourceType)||"1702".equals(sourceType)){
			
			json.setG_location(g_guide.toString());
			
		}
		
		updateAgl(lineLocation, g_guide); //更新角度
		
		return json;
	}
	
	
	/**
	 * @Description:打点tips维护2
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-7-4 下午8:16:36
	 */
	private TipsDao updateSimpleInPointTips() throws Exception {
		
		//打点tips的显示坐标.
		JSONObject g_location  =JSONObject.fromObject(json.getG_location()) ; 
		Point point = (Point) GeoTranslator.geojson2Jts(g_location);
		
		//打点tips的关联link_id
		JSONObject deep = JSONObject.fromObject(this.json.getDeep());
		JSONObject f = deep.getJSONObject("in");
		String lineId=f.getString("id");
		
		//测线的坐标
		JSONObject lineLocation=null;
		
		//测线的坐标，如果只有一条，则就就是当前测线的坐标。如果是多条。则是tips中记录的关联测线
		for (TipsDao line : linesAfterCut) {
			
			if(lineId.equals(line.getId())){
				
				lineLocation=JSONObject.fromObject(line.getG_location());			
				break;
			}
		}
		
		Geometry lineGeo= GeoTranslator.geojson2Jts(lineLocation);
		
		JSONObject  g_guide= getNearLeastPoint(point, lineGeo);
		
		json.setG_guide(g_guide.toString());
		
		double agl=calAngle(lineLocation, g_guide);
		
		if(deep.containsKey("agl")){
			
			deep.put("agl", agl);
			
			json.setDeep(deep.toString());
		}
		
		return json;
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
	 * @Description:范围线，修形维护
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-6-27 下午1:46:33
	 */
	private TipsDao updateAreaLine() throws Exception {
		
		JSONArray geoArr=new JSONArray();
		
		JSONObject deep = JSONObject.fromObject(this.json.getDeep());

		JSONArray f_array = deep.getJSONArray("f_array");
		
		JSONArray f_array_new = new JSONArray(); //一个新的数组
		
		boolean hasOldLine=false;
		
		//只有一条说明就是原有测线本身，是修形，替换g_location
		if(linesAfterCut.size()==1){
			String oldRowkey= linesAfterCut.get(0).getId();
			
			for (Object object : f_array) {
				JSONObject fInfo = JSONObject.fromObject(object); // 是个对象
				
				// 关联link是测线的
	            if(fInfo != null && fInfo.containsKey("type")) {
	                int type = fInfo.getInt("type");
	                String id = fInfo.getString("id");
	                JSONObject geoF=fInfo.getJSONObject("geoF");//组成线的几何
	                
	                //是当前线，则替换
	                if (type == 2 && id.equals(oldRowkey)) {
	                	hasOldLine=true;
	                    geoArr.add(linesAfterCut.get(0).getG_location()); //新的
	                    fInfo.put("geoF", linesAfterCut.get(0).getG_location()); //替换fInfo.geoF
	                    f_array_new.add(fInfo); 
	                }else{
	                	 geoArr.add(geoF);
	                	 f_array_new.add(fInfo);
	                }
	            }
			}
		}
		
		// 1.如果是测线修形（未打断），则需要更新g_location.(已打断的，再打断时G_location已经更新)
		if (hasOldLine) {
			
			//1.更新deep.f_array
			deep.put("f_array", f_array_new); //替换 [f_array]
			
			//2.更新g_location
			json=GLocationUpdate.updateAreaLineLocation(geoArr,json); //更新g_location
			
			//3.如果是范围线：更新geo、更新g_guide=geo
				
			JSONObject gLocation=JSONObject.fromObject(json.getG_location());
			
			Geometry geometry=GeoTranslator.geojson2Jts(gLocation);
			
			Point point=(Point)GeometryUtils.getPointFromGeo(geometry); //中心
			
			JSONObject pointGeo = GeoTranslator.jts2Geojson(point);
			
			//2.1更新geo
			deep.put("geo", pointGeo);
			
			json.setDeep(deep.toString()); //1.修改deep
			
			//2.2更新guide
			json.setG_guide(pointGeo.toString());
			
		}

		return json;
	}
	
	/**
	 * @Description:起终点类
	 * @return
	 * @author: jiayong
	 * @time:2017-7-4 下午4:29:09
	 */
	private TipsDao updateStartEndPoint(){
		JSONObject deep = JSONObject.fromObject(this.json.getDeep());
		
		if(linesAfterCut.size() == 1){
			String rowkey = linesAfterCut.get(0).getId();
			
			int index = -1;//记录关联测线再关联数组中的位置
			JSONArray f_array = deep.getJSONArray("f_array");
			for(int i = 0; i < f_array.size(); i++){
				JSONObject fInfo = JSONObject.fromObject(f_array.get(i)); // 是个对象
				if(fInfo != null && fInfo.containsKey("type")) {
	                int type = fInfo.getInt("type");
	                String id = fInfo.getString("id");
	                if (type == 2 && id.equals(rowkey)) {
	                	index = i;
	                	break;
	                }                
	            }
			}
			
			if(index > -1){
				//更新g_location
				json = GLocationUpdate.updateStartEndPointLocation(index,json,sourceType,linesAfterCut); 
			}
			
		}

		if( "1507".equals(sourceType) || "1508".equals(sourceType)
				 || "1510".equals(sourceType) || "1511".equals(sourceType) || "1514".equals(sourceType)||"1520".equals(sourceType)){
			JSONObject g_location = JSONObject.fromObject(this.json.getG_location());
			JSONObject g_guide = JSONObject.fromObject(this.json.getG_guide());
			
			JSONObject gSLoc = JSONObject.fromObject(deep.getString("gSLoc"));
			JSONObject gELoc = JSONObject.fromObject(deep.getString("gELoc"));
			
			JSONObject gSLocNew = getNearlestLineId(gSLoc,g_location);
			JSONObject gELocNew = getNearlestLineId(gELoc,g_location);
			
			deep.put("gSLoc", gSLocNew);// 新的起点
			deep.put("gELoc", gELocNew);// 新的终点
			json.setDeep(deep.toString());
			
			json.setG_guide(gSLocNew.toString());
			//json.put("g_guide", g_guide);
		}
		
		return json;
	}
	
	
	
	/**
	 * @Description:起终点类
	 * @return
	 * @author: jiayong
	 * @throws Exception 
	 * @time:2017-7-4 下午4:29:09
	 */
	private TipsDao updateSeparationLine() throws Exception{
		
		JSONObject deep = JSONObject.fromObject(this.json.getDeep());
		
		
		List<Geometry> linkGeos=new ArrayList<Geometry>();
		
		String linePidStr=""; //测线的pid  因为有其他组成的测线，需要查询其坐标
		
		String linkPidStr=""; //link的pid
		

		int index = -1;//记录关联测线再关联数组中的位置
		
		//==1是判断 只有修形 ，且没有跨图幅打断的情况
		if(linesAfterCut.size() == 1){
		    String rowkey = linesAfterCut.get(0).getId();
			JSONArray f_array = deep.getJSONArray("f_array");
			for(int i = 0; i < f_array.size(); i++){
				JSONObject fInfo = JSONObject.fromObject(f_array.get(i)); // 是个对象
				if(fInfo != null && fInfo.containsKey("type")) {
	                int type = fInfo.getInt("type");
	                String id = fInfo.getString("id");
	                if (type == 2 && id.equals(rowkey)) {
	                	index = i;
	                } 
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
		//==-1，说明打断维护过了，或者没找到关联（理论不存在）
		if(index ==-1)  return json;
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
		
		//当前测线的几何
		/*JSONObject g_location = JSONObject.fromObject(linesAfterCut.get(0).getG_location());
		Geometry geo = GeoTranslator.geojson2Jts(g_location);
		linkGeos.add(geo);*/
		
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
	 * @Description:TOOD
	 * @return
	 * @author: jiayong
	 * @time:2017-7-4 下午5:49:09
	 */
	private JSONObject getNearlestLineId(JSONObject jsonPoint,JSONObject mutiLines) {
		Point point = (Point) GeoTranslator.geojson2Jts(jsonPoint);
		Double minDistinct=null;
		Geometry nearlastLink=null;
		Geometry geo = GeoTranslator.geojson2Jts(mutiLines);
		//取最近jsonPoint的线几何
		MultiLineString lines = (MultiLineString) geo;
		
		for (int i = 0; i < lines.getNumGeometries(); i++) {
			
			LineString line = (LineString) lines.getGeometryN(i);
			double distinct=point.distance(line);
			 if(minDistinct==null||distinct<minDistinct){
				 minDistinct=distinct; 
				 nearlastLink = line;
			 }
		}
		return getNearLeastPoint(point,nearlastLink);
	}
	

	/**
	 * @Description:更新tips的角度
	 * @param lineLocation
	 * @param guide
	 * @return
	 * @author: y
	 * @throws Exception
	 * @time:2017-6-27 上午10:43:24
	 */
	private TipsDao updateAgl(JSONObject lineLocation, JSONObject guide)
			throws Exception {

		JSONObject deep = JSONObject.fromObject(json.getDeep());

		if (deep != null && deep.containsKey("agl")) {

			double agl = calAngle(lineLocation, guide);

			deep.put("agl", agl);

			json.setDeep(deep.toString());
		}
		return json;
	}

	/**
	 * @Description:计算tips的角度
	 * @param lineLocation
	 *            ：测线的显示坐标
	 * @param guide
	 *            ：引导坐标
	 * @return
	 * @throws Exception
	 * @author: y
	 * @time:2017-6-27 上午10:42:42
	 */
	private double calAngle(JSONObject lineLocation, JSONObject guide)
			throws Exception {
		double calAngle = 0;

		Point guidePoint = (Point) GeoTranslator.geojson2Jts(guide);

		Geometry line = GeoTranslator.geojson2Jts(lineLocation);

		// 查询和引导坐标最近的一条线段
		LineString nearLeastline = getLineComposedByRecentTwoPoint(line,
				guidePoint);

		double[] points = new double[4];
		Coordinate[] cs = nearLeastline.getCoordinates();
		points[0] = cs[0].x;
		points[1] = cs[0].y;
		points[2] = cs[1].x;
		points[3] = cs[1].y;

		calAngle = TipsGeomUtils.calAngle(points);

		return calAngle;
	}

	/**
	 * 找某条link上离指定点位最近的两个形状点
	 * 
	 * @param guidePoint
	 * @param line
	 * @param rdLink
	 * @param geoPoint
	 * @return LineString 返回两形状点组成的线
	 * @throws Exception
	 */
	protected LineString getLineComposedByRecentTwoPoint(Geometry line,
			Point guidePoint) throws Exception {
		Double minDis = null;
		LineString newLine = null; // 离引导坐标最近的两个形状点的组成的线段

		int geoNum = line.getNumGeometries();
		for (int i = 0; i < geoNum; i++) {
			Geometry subGeo = line.getGeometryN(i);
			if (subGeo instanceof LineString) {
				Coordinate[] c_array = subGeo.getCoordinates();
				int num = -1; // 存放离的最近的形状点的顺序号
				for (int k = 0; k < c_array.length; k++) {
					double tmpDis = GeometryUtils.getDistance(
							guidePoint.getCoordinate(), c_array[k]);
					if (minDis == null || tmpDis < minDis) {
						minDis = tmpDis;
						num = k;
					}
				}
				Coordinate c_start = null;
				Coordinate c_end = null;

				if (num != -1) {
					if (num == 0) {
						c_start = c_array[0];
						c_end = c_array[1];
					} else if (num == c_array.length - 1) {
						c_start = c_array[c_array.length - 2];
						c_end = c_array[num];
					} else {
						double dis_last = GeometryUtils.getDistance(
								guidePoint.getCoordinate(), c_array[num - 1]);
						double dis_next = GeometryUtils.getDistance(
								guidePoint.getCoordinate(), c_array[num + 1]);
						if (dis_last < dis_next) {
							c_start = c_array[num - 1];
							c_end = c_array[num];
						} else {
							c_start = c_array[num];
							c_end = c_array[num + 1];
						}
					}

					Coordinate[] coordinates = new Coordinate[] { c_start,
							c_end };

					newLine = JtsGeometryFactory.createLineString(coordinates);
				}
			}
		}

		return newLine;
	}

//	public static void main(String[] args) {
//
//		SolrController conn = new SolrController();
//
//		// 关联的tips
//		JSONObject solrIndex;
//		try {
//			solrIndex = conn.getById("0216045BC2F25E98B54D4991F63B57FD9EE7F6");
//			// 测线
//			JSONObject solrLine = conn
//					.getById("022001091D1890CA8849EC9908F32A1667C2C2");
//
//			JSONObject lineLocation = JSONObject.fromObject(solrLine
//					.getString("g_location"));
//
//			JSONObject guide = JSONObject.fromObject(solrIndex
//					.getString("g_guide"));
//
//			RelateTipsGuideAndAglUpdate u = new RelateTipsGuideAndAglUpdate(
//					solrIndex,null);
//			u.updateAgl(lineLocation, guide);
//
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	//}

}
