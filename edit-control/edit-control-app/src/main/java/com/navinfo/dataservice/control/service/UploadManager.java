package com.navinfo.dataservice.control.service;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.editplus.operation.imp.CollectorUploadPois;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: UploadManager
 * @author xiaoxiaowen4127
 * @date 2017年4月25日
 * @Description: UploadManager.java
 */
public class UploadManager {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	private String fileName;
	private boolean multiThread=false;
	public UploadManager(String fileName){
		this.fileName=fileName;
	}
	public UploadResult upload()throws Exception{
		//1.读取文件
		if(StringUtils.isEmpty(fileName)) throw new Exception("上传文件名为空");
		JSONArray rawPois = readPois();
		//2.将pois分发到各个大区
		//...
		//3.
		
		return null;
		
	}
	private JSONArray readPois() throws Exception {
		FileInputStream fis=null;
		Scanner scan = null;
		try{
			fis = new FileInputStream(fileName);
			scan = new Scanner(fis);
			JSONArray pois = new JSONArray();
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				if(StringUtils.isNotEmpty(line)){
					pois.add(JSONObject.fromObject(line));
				}
			}
			return pois;
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			if(fis!=null)fis.close();
			if(scan!=null)scan.close();
		}
	}
	private Map<Integer,CollectorUploadPois> distribute(JSONArray rawPois)throws Exception{
		Map<Integer,CollectorUploadPois> poiMap = new HashMap<Integer,CollectorUploadPois>();//key:大区dbid
		//计算poi所属图幅
		Map<String,List<JSONObject>> meshPoiMap=new HashMap<String,List<JSONObject>>();
		for (int i = 0; i < rawPois.size(); i++) {
			JSONObject jo = rawPois.getJSONObject(i);
			// 坐标确定mesh，mesh确定区库ID
			String wkt = jo.getString("geometry");
			
			Geometry point = new WKTReader().read(wkt);
			Coordinate[] coordinate = point.getCoordinates();
			String mesh = MeshUtils.point2Meshes(coordinate[0].x, coordinate[0].y)[0];
			if(!meshPoiMap.containsKey(mesh)){
				meshPoiMap.put(mesh, new ArrayList<JSONObject>());
			}
			meshPoiMap.get(mesh).add(jo);
		}
		//映射到对应的大区库上
		
		return null;
	}
}
