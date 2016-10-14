package com.navinfo.dataservice.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.GeometryExceptionWithContext;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.model.Block;
import com.navinfo.dataservice.api.man.model.City;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.DbServerType;
import com.navinfo.dataservice.commons.database.oracle.MyPoolGuardConnectionWrapper;
import com.navinfo.dataservice.commons.database.oracle.MyPoolableConnection;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.datahub.service.DbService;
import com.navinfo.dataservice.expcore.ExportConfig;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class ImportBlockGrid {
	
	public static JSONObject execute(JSONObject request) throws Exception{
		JSONObject response = new JSONObject();
		BufferedReader bufferedReader = null;
		try {
			String blockFile = (String) request.get("blockFile");
			Assert.notNull(blockFile, "blockFile不能为空");

			File f = new File(blockFile);

			InputStreamReader read = new InputStreamReader(new FileInputStream(f));

			bufferedReader = new BufferedReader(read);
			String line = null;
			
			WKTReader reader=new WKTReader();
			Set<String> usedGrids = new HashSet<String>();

			JSONArray ja = new JSONArray();
			while ((line = bufferedReader.readLine()) != null) {
				
				JSONObject json = JSONObject.fromObject(line);
				String code=json.getString("code");
				String geometry =json.getString("geometry");
				
				Geometry geo = reader.read(geometry);
				Set<String> grids = CompGeometryUtil.polygon2GridsWithoutBreak((Polygon)geo);

				if(grids.size()==0){
					System.out.println("block lost:"+code);
					continue;
				}
				
				grids.removeAll(usedGrids);//去除已经分配的grid
				usedGrids.addAll(grids);//
				
				json.put("grids", grids);
				ja.add(json);
	 		}
			response.put("blocks", ja);
		} catch (Exception e) {
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}finally{
			if(bufferedReader!=null)bufferedReader.close();
		}
		return response;
	}

}
