package com.navinfo.dataservice.engine.photo.mass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;
import com.navinfo.dataservice.dao.photo.mass.HbaseTipsController;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class TipsPhotoGetter {
		
	
	//二代中查询mark的历史照片
	public byte[] getMarkHisPhotoByRowkey(String oldRowkey, String type) throws Exception {
		
		
		//1.根据rowkey查询mass_photos_tips_idx_url表，得到对应的photo的rowkey
		HbaseTipsController hbaseTipsController = new HbaseTipsController();
		String newRowkey = hbaseTipsController.getNewRowkey4Photo(oldRowkey);
		
		//2.根据新rowkey到mass_photos_tips查询得到photo
		byte[] tipsPhoto = hbaseTipsController.getPhotoByRowkey(newRowkey);
		
		if ("origin".equals(type)) {
			return FileUtils.rotateOrigin(tipsPhoto);
//			return photo;
		} else {
			return FileUtils.makeSmallImage(tipsPhoto);
		}
		
	}
	//三代中查询tips的历史照片
	public List<Map<String, Object>> getTipsHisPhotoByRowkey(String rowkey) throws Exception {
		List<Map<String, Object>> photoRowkeyList = new ArrayList<>();
		
		
		String[] queryColNames = {"geometry"};
		Table htab = HBaseConnector.getInstance().getConnection()
                .getTable(TableName.valueOf(HBaseConstant.tipTab));
		
		//1.查询该tips的geometry列的内容
		JSONObject geometry = HbaseTipsQuery.getHbaseTipsByRowkey(htab, rowkey,
	   			queryColNames);
		
		//2.根据该tips的geometry列信息，查询tips引导坐标g_guide
		JSONObject jsonObject1 = geometry.getJSONObject("geometry");
		JSONObject jsonObject = jsonObject1.getJSONObject("g_guide");
		JSONArray gGuide = jsonObject.getJSONArray("coordinates");
		
		//3.根据引导坐标计算出该tips的Geohash		
		Double lon = gGuide.getDouble(0);
		Double lat = gGuide.getDouble(1);		
		GeoHash g = new GeoHash(lat,lon);
		g.sethashLength(9);
		String geohash = g.getGeoHashBase32();
		
		//4.根据Geohash，在mass_photos_tips表中过滤查询到对应的rowkey
		HbaseTipsController hbaseTipsController = new HbaseTipsController();
		JSONArray array = hbaseTipsController.getPhotoRowkeyByGeohash(geohash);
		
		//5.根据rowkey在mass_photos_tips表中查询到对应的照片
		for (Object object : array) {
			String photoRowkey = object.toString();
			byte[] tipsPhoto = hbaseTipsController.getPhotoByRowkey(photoRowkey);
			Map<String, Object> map = new HashMap<>();
			map.put("rowkey", object);	
			map.put("photo", tipsPhoto);
			photoRowkeyList.add(map);
		}				
		return photoRowkeyList;		
	}

}
