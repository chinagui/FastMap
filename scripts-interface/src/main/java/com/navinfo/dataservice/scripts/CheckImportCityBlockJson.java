package com.navinfo.dataservice.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.scripts.model.Block4Imp;
import com.navinfo.dataservice.scripts.model.City4Imp;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: CheckImportCityBlockJson
 * @author zl 
 * @date 2017.06.12
 * @Description: CheckImportCityBlockJson.java
 */
public class CheckImportCityBlockJson {
	private static Logger log = LoggerRepos.getLogger(CheckImportCityBlockJson.class);
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			if(args==null||args.length!=3){
				log.error("ERROR:need args:cityFile blockFile");
				return;
			}

			String initBlockFile = args[0];
			String cityFile = args[1];
			String blockFile = args[2];
			
//			String initBlockFile="F:\\newblock.txt";
//			String cityFile = "F:\\CITYS.JSON";
//			String blockFile = "F:\\BLOCKS.JSON";

			check(initBlockFile,cityFile,blockFile);
			
			log.info("Over.");
			System.exit(0);
		} catch (Exception e) {
			log.error("Oops, something wrong...", e );
			e.printStackTrace();
		}
	}
	
	public static void check(String initBlockFile,String cityFile,String blockFile)throws Exception{
		try{
			
			Map<String,City4Imp> citys = parseCity(cityFile);
			
			//write citys
			/*for(City4Imp city:citys.values()){
				Geometry cityGeo = MeshUtils.meshes2Jts(city.getMeshes());
				cityGeo.getGeometryType();
			}*/
			Map<String,Block4Imp> blocks = parseBlock(citys,blockFile);
			
			for(Map.Entry<String,Block4Imp> block:blocks.entrySet()){
				
				Geometry blockGeo = CompGridUtil.grids2Jts(block.getValue().getGrids());
				//判断是否出现复杂多边形
//				String geometryType = blockGeo.getGeometryType();
//				System.out.println("geometryType: "+geometryType);
				
				if (blockGeo instanceof MultiPolygon) {
					JSONArray jsonarray = JSONArray.fromObject(block.getValue().getGrids()); 
					//判断geometry 是否是复杂多边形
					log.info(blockFile+" 检查: "+"此block 几何含复杂多边形!");
					log.info(block.getKey()+": 此block 范围内 grids: "+jsonarray);
				}
			}
			
			parseInitBlock(initBlockFile,citys.size(),blocks.size());

		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
		}
	}
	
	
	public static Map<String,City4Imp> parseCity(String cityFile)throws Exception{
		BufferedReader reader = null;
		try{
			System.out.println("Starting read city file...");
			Map<String,City4Imp> citys = new HashMap<String,City4Imp>();
			File file = new File(cityFile);

			InputStreamReader read = new InputStreamReader(
					new FileInputStream(file));

			reader = new BufferedReader(read);
			String line=null;
			while ((line = reader.readLine()) != null){
				JSONObject cityJson = JSONObject.fromObject(line);
				String cityName = cityJson.getString("city");
				if(citys.containsKey(cityName)){
					citys.get(cityName).getMeshes().add(cityJson.getString("meshId"));
				}else{
					City4Imp newCity = new City4Imp();
					newCity.setCityName(cityName);
					newCity.setAdminId(Integer.parseInt(cityJson.getString("code").substring(0, 6)));
					newCity.setProvName(cityJson.getString("province"));
					Set<String> meshes = new HashSet<String>();
					meshes.add(cityJson.getString("meshId"));
					newCity.setMeshes(meshes);
					citys.put(cityName, newCity);
				}
				
//				cityCount+=1;
			}
			System.out.println("read city file over. city size:"+citys.size());
			return citys;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(reader!=null){
				reader.close();
			}
		}
	}

	public static Map<String,Block4Imp> parseBlock(Map<String,City4Imp> citys,String blockFile)throws Exception{
		BufferedReader reader = null;
		try{
			System.out.println("Starting read block file...");
			Map<String,Block4Imp> blocks = new HashMap<String,Block4Imp>();
			File file = new File(blockFile);

			List<String> allGrids = new ArrayList<String>();
			List<String> appliedGrids = new ArrayList<String>();

			Map<String,String> meshCity = new HashMap<String,String>();
			
			InputStreamReader read = new InputStreamReader(
					new FileInputStream(file));

			reader = new BufferedReader(read);
			String line=null;
			while ((line = reader.readLine()) != null){
				JSONObject blockJson = JSONObject.fromObject(line);
				//先判断计算的grid是否超过了城市界定的图幅，如果超过了，忽略
				String cityName = blockJson.getString("city");
				String grid = blockJson.getString("gridId");
				//添加到全部的grid 集合中
				allGrids.add(grid);
				String mesh = grid.substring(0, 6);
				if(meshCity.containsKey(mesh)){
					//图幅已经有对应的city  比较已有city 与现有city是否一致
					String oldCityName = meshCity.get(mesh);
					if(!cityName.equals(oldCityName)){
						//同一个图幅的grid 被分配到了不同的 city
						log.info(blockFile+" 检查: "+"同一个图幅的grid 被分配到了不同的 city ");
						log.info(" 当前grid: "+grid+" 当前city: "+cityName+" 同组grid分配city: "+oldCityName);
					}
				}else{
					meshCity.put(mesh, cityName);
				}
				if(citys.containsKey(cityName)&&citys.get(cityName).getMeshes().contains(mesh)){

					String blockCode = blockJson.getString("code");
					if(blocks.containsKey(blockCode)){
						blocks.get(blockCode).getGrids().add(grid);
						//添加到已分配的grid 集合中
						appliedGrids.add(grid);
					}else{
						Block4Imp newBlock = new Block4Imp();
						newBlock.setCityName(cityName);
						String blockName =blockJson.getString("province")+cityName+blockJson.getString("county")+blockJson.getString("job1")+blockJson.getString("job2");
						newBlock.setBlockName(blockName);
						newBlock.setWrokProperty(blockJson.getString("workProperty"));
						Set<String> grids = new HashSet<String>();
						grids.add(grid);
						newBlock.setGrids(grids);
						blocks.put(blockCode, newBlock);
						//添加到已分配的grid 集合中
						appliedGrids.add(grid);
					}
				}
				
//				blockCount+=1;
			}
			
			//未分配给任何block的grid 集合
			allGrids.removeAll(appliedGrids);
			if(allGrids.size() > 0){
				JSONArray jsonarray = JSONArray.fromObject(allGrids); 
				log.info(blockFile+" 检查: "+"未分配给任何block的grids : "+jsonarray);
			}
			System.out.println("read block file over. block size:"+citys.size());
			return blocks;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(reader!=null){
				reader.close();
			}
		}
		
	}
	
	private static void parseInitBlock(String initBlockFile,int cityCount,int blockCount) throws Exception {
		Set<String> cityImps = null;
		Set<String> blockImps = null;
		int lineCount = 0;
		BufferedReader reader = null;
		try{
			System.out.println("Starting read initBlockFile file...");
			cityImps = new HashSet<String>(); 
			blockImps = new HashSet<String>(); 
			
			File file = new File(initBlockFile);

			InputStreamReader read = new InputStreamReader(
					new FileInputStream(file));

			reader = new BufferedReader(read);
			String line=null;
			while ((line = reader.readLine()) != null){
				JSONObject blockJson = JSONObject.fromObject(line);
				//先判断计算的grid是否超过了城市界定的图幅，如果超过了，忽略
				cityImps.add(blockJson.getString("city"));
				blockImps.add(blockJson.getString("BlockCode"));
				lineCount+=1;
			}
			
			if(cityImps.size() != cityCount){
				log.info(initBlockFile+" 检查:"+" 导入的city ("+cityImps.size()+")和原始文件中的city("+cityCount+") 数量不一致!");
			}
			
			if(blockImps.size() != blockCount){
				log.info(initBlockFile+" 检查:"+" 导入的block ("+blockImps.size()+")和原始文件中的block("+blockCount+") 数量不一致!");
			}
			
			System.out.println("read initBlockFile file over. initBlockFile size:"+lineCount);
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			if(reader!=null){
				reader.close();
			}
		}
	}

}
