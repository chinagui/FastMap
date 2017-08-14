package com.navinfo.dataservice.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.scripts.model.Block;
import com.navinfo.dataservice.scripts.model.Block4Imp;
import com.navinfo.dataservice.scripts.model.City;
import com.navinfo.dataservice.scripts.model.City4Imp;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: ImportCityBlockByJson
 * @author xiaoxiaowen4127
 * @date 2017年2月26日
 * @Description: ImportCityBlockByJson.java
 */
public class ComputeCityBlockByJson {

	private String fileDir = "";
	private String rawBlockFile;
	private JSONArray rawBlocks;
	private Map<String,City> citys ;//key:city.city
	private Map<String,List<JSONObject>> cityBlockMap;
	private List<Block> blocks;
	
	public ComputeCityBlockByJson(String rawBlockFile){
		this.rawBlockFile = rawBlockFile;
	}
	
	public void compute()throws Exception{
		Connection conn = null;
		PreparedStatement stmt = null;
		try{
			//read
			readJsonFile();
			//city
			computeCitys();
			//block
			computeBlocks();
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			e.printStackTrace();
			throw e;
		}finally{
			DbUtils.closeQuietly(stmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	private void readJsonFile()throws Exception{
		//解析路径
		if(rawBlockFile.contains(File.separator)){
			fileDir = rawBlockFile.substring(0, rawBlockFile.lastIndexOf(File.separator));
		}
		Scanner scan = null;
		FileInputStream fis = null;
		try{
			rawBlocks = new JSONArray();
			fis = new FileInputStream(rawBlockFile);
			scan = new Scanner(fis);
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				if(line != null && StringUtils.isNotEmpty(line)){
					JSONObject json = JSONObject.fromObject(line);
					rawBlocks.add(json);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(fis!=null)fis.close();
			if(scan!=null)scan.close();
		}
	}
	
	private void writeJsonFile(JSONArray ja,String fileName) throws Exception {

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fileName);
			for (int j = 0; j < ja.size(); j++) {
				pw.println(ja.getJSONObject(j).toString());
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if(pw!=null){
				pw.close();
			}

		}
	}
	
	private void computeCitys()throws Exception{
		citys = new HashMap<String,City>();
		cityBlockMap = new HashMap<String,List<JSONObject>>();
		//记录已经被占用的meshId
		Set<String> usedmeshIds = new HashSet<String>();
		for(Object obj:rawBlocks){
			JSONObject jo = (JSONObject)obj;
            String cityVal = jo.getString("city");
			//geo 2 meshes
			String geoWkt=jo.getString("geometry");
			Geometry jtsGeo = JtsGeometryFactory.read(geoWkt);
			Set<String> meshIds = CompGeometryUtil.geoToMeshesWithoutBreak(jtsGeo);
			//过滤已使用的图幅
			meshIds.removeAll(usedmeshIds);//去除已经分配的meshId
			usedmeshIds.addAll(meshIds);//将已分配的meshId 放入已占用set
			if(meshIds.size()==0){
				continue;
			}
			
			if(citys.containsKey(cityVal)){
				citys.get(cityVal).getMeshIds().addAll(meshIds);
			}else{
				City city = new City();
				city.setName(jo.getString("Name"));
				city.setCity(cityVal);;
				city.setArea(jo.getString("Area"));
				city.setCounty(jo.getString("County"));
				city.setBlockCode(jo.getString("BlockCode"));
				city.setProvince(jo.getString("province"));
				city.setMeshIds(meshIds);
				city.setJob1(jo.getString("Job1"));
				city.setJob2(jo.getString("Job2"));
				city.setWorkProperty(jo.getString("work_property"));
				citys.put(cityVal, city);
			}
			if(cityBlockMap.containsKey(cityVal)){
				cityBlockMap.get(cityVal).add(jo);
			}else{
				List<JSONObject> subBlocks = new ArrayList<JSONObject>();
				subBlocks.add(jo);
				cityBlockMap.put(cityVal, subBlocks);
			}
		}
		JSONArray cityJsons = new JSONArray();
		//convert to json
		if(citys != null && citys.size() > 0){
			for(String cityName : citys.keySet()){
				City cityObj = citys.get(cityName);
				//添加数据到 JsonArray
				String wktStr = null;
				for(String meshId : cityObj.getMeshIds()){
					JSONObject meshObj = new JSONObject();
					meshObj.put("city", cityObj.getCity());
					meshObj.put("area", cityObj.getArea());
					meshObj.put("code", cityObj.getBlockCode());
					meshObj.put("county", cityObj.getCounty());
					meshObj.put("name", cityObj.getName());
					meshObj.put("province", cityObj.getProvince());
					meshObj.put("meshId", meshId);
					meshObj.put("job1", cityObj.getJob1());
					meshObj.put("job2", cityObj.getJob2());
					meshObj.put("workProperty", cityObj.getWorkProperty());
					//meshObj.put("", cityObj.get);
					
					if(meshId != null && StringUtils.isNotEmpty(meshId)){
						wktStr = MeshUtils.mesh2WKT(meshId); 
					}
					meshObj.put("geometry", wktStr);
		    
				    cityJsons.add(meshObj);
				 }
		    }
        }
		//write file
		writeJsonFile(cityJsons,fileDir+File.separator+"CITYS.JSON");
	}

	private void computeBlocks()throws Exception{
		blocks = new ArrayList<Block>();
		if(cityBlockMap==null||cityBlockMap.size()==0){
			System.out.println("city is empty. Do nothing.");
			return;
		}
		for(Entry<String,List<JSONObject>> entry:cityBlockMap.entrySet()){
			System.out.println("compute blocks in city:"+entry.getKey());
			//记录已经被占用的grid
			Map<String,Set<String>> meshes = new HashMap<String,Set<String>>();//key:mesh,value:grid set
			for(String m:citys.get(entry.getKey()).getMeshIds()){
				meshes.put(m, new HashSet<String>());
			}
			
			for(JSONObject jo:entry.getValue()){
				
				//geo 2 grids
				String geoWkt=jo.getString("geometry");
				Geometry jtsGeo = JtsGeometryFactory.read(geoWkt);
				Set<String> grids = CompGeometryUtil.geo2GridsWithoutBreak(jtsGeo);
				//过滤超过所属城市图幅的grid
				for(Iterator<String> it = grids.iterator();it.hasNext();){
					String g = it.next();
					String g2m = g.substring(0, g.length()-2);
					if(meshes.containsKey(g2m)){
						//往已计算的grid中添加，如果不成功则表示grid已分配
						if(!(meshes.get(g2m).add(g))){
							it.remove();
						}
					}else{
						it.remove();
					}
				}
				//生成block类
				Block block = new Block();
				block.setName(jo.getString("Name"));
				block.setCity(jo.getString("city"));;
				block.setArea(jo.getString("Area"));
				block.setCounty(jo.getString("County"));
				block.setBlockCode(jo.getString("BlockCode"));
				block.setProvince(jo.getString("province"));
				block.setGridIds(grids);
				block.setJob1(jo.getString("Job1"));
				block.setJob2(jo.getString("Job2"));
				block.setWorkProperty(jo.getString("work_property"));
				blocks.add(block);
			}
			//查找是否有gird未填充满city的所有图幅的情形
			//算法：通过未分配的grid的邻接grid所属的block(找到任意一个block就停止)，将图幅内的所有grid分配给该block
			for(Entry<String, Set<String>>  mEntry : meshes.entrySet()){
				if(mEntry.getValue().size()!=16){
					Block nb = null;//补充未分配grid到该block
					for(String g:CompGridUtil.mesh2Grid(mEntry.getKey())){//遍历图幅包含的所有grid
						if(!mEntry.getValue().contains(g)){//找到未分配的gird
							System.out.println("grid:"+g);
							String[] neiGrids = GridUtils.get9NeighborGrids(g);
							for(String ng:neiGrids){
								for(Block b:blocks){
									if(b.getGridIds().contains(ng)){
										nb = b;
										break;
									}
								}
								if(nb!=null){
									break;
								}
							}
						}
						if(nb!=null){
							break;
						}
					}
					//
					if(nb==null){
						//邻接一圈还没找到，就暂时不处理了
						//严谨的看，以上找block需要使用递归
						continue;
					}
					//分配
					for(String g:CompGridUtil.mesh2Grid(mEntry.getKey())){
						if(!mEntry.getValue().contains(g)){
							System.out.println("extra grid:"+g);
							nb.getGridIds().add(g);
						}
					}
				}
			}
		}
		JSONArray blockJsons = new JSONArray();
		//convert to json
		for(Block b:blocks){
			//添加数据到 JsonArray
			String wktStr = null;
			for(String gridId : b.getGridIds()){
				JSONObject gridObj = new JSONObject();
				gridObj.put("city", b.getCity());
				gridObj.put("area", b.getArea());
				gridObj.put("code", b.getBlockCode());
				gridObj.put("county", b.getCounty());
				gridObj.put("name", b.getName());
				gridObj.put("province", b.getProvince());
				gridObj.put("gridId", gridId);
				gridObj.put("job1", b.getJob1());
				gridObj.put("job2", b.getJob2());
				gridObj.put("workProperty", b.getWorkProperty());
				
				if(gridId != null && StringUtils.isNotEmpty(gridId)){
					wktStr = GridUtils.grid2Wkt(gridId); 
				}
				gridObj.put("geometry", wktStr);
    
				blockJsons.add(gridObj);
			 }
		}
		//write file
		writeJsonFile(blockJsons,fileDir+File.separator+"BLOCKS.JSON");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			if(args==null||args.length!=1){
				System.out.println("ERROR:need args:raw block file");
				return;
			}

			String rawBlockFile = args[0];
//			String rawBlockFile = "F:\\data\\blocksImp\\out2.json";
			
			ComputeCityBlockByJson com = new ComputeCityBlockByJson(rawBlockFile);
			com.compute();
			
			System.out.println("Over.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Oops, something wrong...");
			e.printStackTrace();

		}
	}
}
