package com.navinfo.dataservice.engine.editplus.diff;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.edit.model.IxDealershipResult;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiAddress;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiContact;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiName;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiRecommender {

	public static  Connection conn = null;

	public static List<BasicObj> loadPoi(Geometry geometry) throws Exception {
		List<BasicObj> poiList = new ArrayList<BasicObj>();
		String sql = "SELECT pid FROM ix_poi p WHERE sdo_within_distance(p.geometry, sdo_geometry(:1  , 8307), 'mask=anyinteract') = 'TRUE'";
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			Geometry buffer = geometry.buffer(GeometryUtils.convert2Degree(2000));
		
		    String wkt = GeoTranslator.jts2Wkt(buffer);
			Clob geom = ConnectionUtil.createClob(conn);			
			geom.setString(1, wkt);
		    pstmt.setClob(1,geom);
			
//			pstmt.setString(1, wkt);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", null,false,resultSet.getInt("pid"), false);
				poiList.add(obj);
			}
		}catch(Exception e){
			throw new Exception(e.getMessage(),e);
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		return poiList;
	}
	
	public static FastPoi buildFastPoi(BasicObj obj){
		
		IxPoi p=(IxPoi) obj.getMainrow();
		IxPoiObj poiObj=(IxPoiObj) obj;
		
		IxPoiName poiName=poiObj.getOfficeStandardCHName();
		IxPoiAddress poiAddr=poiObj.getChiAddress();
		
		FastPoi fp=new FastPoi();
		JSONArray array =GeoTranslator.jts2JSONArray(p.getGeometry());
		 
		fp.setX(array.getDouble(0));
		fp.setY(array.getDouble(1));
		if(poiAddr==null || poiAddr.getAddrname()==null){fp.setAddr("");}
		if(poiName==null ||poiName.getName()==null){fp.setName("");}
		
		StringBuffer sb = new StringBuffer();
		String telephone = "";
		for(IxPoiContact c:poiObj.getIxPoiContacts()){
			sb.append(c.getContact()).append(";");
		}
		if(sb.length() > 0) telephone = sb.toString().substring(0, sb.toString().length()-1);
		telephone = StringUtil.sortPhone(telephone);
		
		fp.setTel(telephone);
		fp.setPostCode(p.getPostCode());
		fp.setPoiNum(p.getPoiNum());
		return fp;
	}
	
	public static FastResult buildFastResult(IxDealershipResult dealResult){
		FastResult fr=new FastResult();
		JSONArray array =GeoTranslator.jts2JSONArray(dealResult.getGeometry());
		fr.setX(array.getDouble(0));
		fr.setY(array.getDouble(1));
		fr.setAddr(dealResult.getAddress());
		fr.setName(dealResult.getName());
		fr.setTel(StringUtil.sortPhone(StringUtil.contactFormat(dealResult.getPoiTel())));
		fr.setPostCode(dealResult.getPostCode());
		return fr;
	}

	//推荐匹配poi
	public static void recommenderPoi(IxDealershipResult dealResult) throws Exception{
//		JSONObject loc=new JSONObject();
//		if (dealResult.getAddress()!=null){
//			loc=BaiduGeocoding.geocoder(dealResult.getAddress());
//		}else{
//			loc=BaiduGeocoding.geocoder(dealResult.getName());
//		}
//		if(loc==null){
//			throw new Exception("result数据名称和地址都为空，无法Geocoding");
//		}
//		Geometry pointWkt = GeoTranslator.point2Jts(loc.getDouble("lng"),loc.getDouble("lat"));
//		dealResult.setGeometry(pointWkt);
		FastResult fr=buildFastResult(dealResult);
		//外扩两公里查询poi
		List<BasicObj> poiList = loadPoi(dealResult.getGeometry());
		Map<String, Double> matchPoi=new HashMap<String, Double>();
		for(BasicObj obj:poiList){
		   FastPoi fp=buildFastPoi(obj);
		   double sim=similarity(fr,fp);
		   if(sim>0.3){
			   matchPoi.put(fp.getPoiNum(), sim);
		   }
		}
		List<Entry<String, String>> matchPoiList=sortMap(matchPoi);
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<matchPoiList.size();i++){ 
			Map.Entry<String,String> mapping=(Entry<String, String>) matchPoiList.get(i);
			sb.append(mapping.getValue()).append(";");
			if(i==0){
				dealResult.setPoiNum1(mapping.getKey());
				dealResult.setCfmPoiNum(mapping.getKey());;
			}
			else if(i==1){
				dealResult.setPoiNum2(mapping.getKey());
			}
			else if(i==2){
				dealResult.setPoiNum3(mapping.getKey());
			}
			else if(i==3){
				dealResult.setPoiNum4(mapping.getKey());
			}
			else if(i==4){
				dealResult.setPoiNum5(mapping.getKey());
				break;
			} 
       } 
		dealResult.setSimilarity(sb.toString());
		
	}
	
	public static List<Entry<String, String>> sortMap(Map matchPoi){
		 //这里将map.entrySet()转换成list
        List<Map.Entry<String,String>> list = new ArrayList<Map.Entry<String,String>>(matchPoi.entrySet());
        //然后通过比较器来实现排序
        Collections.sort(list,new Comparator<Map.Entry<String,String>>() {
            //升序排序
            public int compare(Entry<String, String> o1,
                    Entry<String, String> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }

        });
        return list;
    }

	
	public static double similarity(FastResult s1, FastPoi s2) {

		if (s1 == null || s2 == null)
			return 0;

		double r1, r2, r3, r4, r5, sim1, sim2, q = 0;
		double x1, y1, x2, y2;
		double d;
		String tel1 = "", tel2 = "";
		x1 = s1.getX();
		y1 = s1.getY();
		x2 = s2.getX();
		y2 = s2.getY();
		d = LngLat.distanceByLngLat(y1, x1, y2, x2);

		r1 = 1 / (1 + d);

		if (s1.getName() == null || s2.getName() == null
				|| s1.getName().equals("") || s2.getName().equals(""))
			r2 = 0;
		else
			r2 = LevenshteinExtend.getSimilarityRatio(s1.getName(),
					s2.getName());

		if (s1.getAddr() == null || s2.getAddr() == null
				|| s1.getAddr().equals("") || s2.getAddr().equals(""))
			r3 = 0;
		else
			r3 = LevenshteinExtend.getSimilarityRatio(s1.getAddr(),
					s2.getAddr());

		if (s1.getPostCode() == null || s2.getPostCode() == null
				|| s1.getPostCode().equals("") || s2.getPostCode().equals(""))
			r4 = 0;
		else
			r4 = LevenshteinExtend.getSimilarityRatio(s1.getPostCode(),
					s2.getPostCode());
		String[] tels1 = s1.getTel().split(";");
		String[] tels2 = s2.getTel().split(";");
		boolean flag = false;
		r5 = 1;
		if (tels1 != null)
			for (int i = 0; i < tels1.length; i++) {
				double tempR = 0;
				if (tels2 != null)
					for (int j = 0; j < tels2.length; j++) {
						tel1 = tels1[i];
						tel2 = tels2[j];
						tel1 = tel1.replace("+86", "");
						tel1 = tel1.replaceAll("\\D", "");
						tel2 = tel2.replace("+86", "");
						tel2 = tel2.replaceAll("\\D", "");
						double t = LevenshteinExtend.getSimilarityRatio(tel1,
								tel2);

						if (!(tels1[i] == null || tels2[j] == null
								|| tels1[i].equals("") || tels2[j].equals(""))) {
							if (t > tempR)
								tempR = t;
							flag = true;
						}
					}
				r5 *= (1 - tempR);
			}
		r5 = 1 - r5;
		if (r5 < 1)
			r5 = 0;
		sim1 = 1 - (1 - r1) * (1 - r2) * (1 - 0.9 * r3 - 0.1 * r4) * (1 - r5);
		if (r1 != 0)
			q += 0.1;
		if (r2 != 0)
			q += 0.33;
		if (r3 != 0)
			q += 0.31;
		if (r4 != 0)
			q += 0.03;
		if (flag)
			q += 0.23;
		sim2 = (0.1 * r1 + 0.33 * r2 + 0.31 * r3 + 0.03 * r4 + 0.23 * r5);
		if (q != 0 && q > 0.3)
			sim2 /= q;
		return (sim1 + sim2) / 2;
	}

	public static double similarityDealership(FastResult s1, FastPoi s2) {

		if (s1 == null || s2 == null)
			return 0;
		if (s1.getProvnm() != null && s2.getProvnm() != null
				&& (!s1.getProvnm().equals("")) && (!s2.getProvnm().equals(""))
				&& (!s1.getProvnm().equals(s2.getProvnm())))
			return 0;
		double r1, r2, r3, r4, r5, sim1, sim2, q = 0;
		double x1, y1, x2, y2;
		double d;
		String tel1 = "", tel2 = "";
		x1 = s1.getX();
		y1 = s1.getY();
		x2 = s2.getX();
		y2 = s2.getY();
		d = LngLat.distanceByLngLat(y1, x1, y2, x2);

		r1 = 1 / (1 + d);

		if (s1.getName() == null || s2.getName() == null
				|| s1.getName().equals("") || s2.getName().equals(""))
			r2 = 0;
		else
			r2 = LevenshteinExtend.getSimilarityRatio(s1.getName(),
					s2.getName());

		if (s1.getAddr() == null || s2.getAddr() == null
				|| s1.getAddr().equals("") || s2.getAddr().equals(""))
			r3 = 0;
		else
			r3 = LevenshteinExtend.getSimilarityRatio(s1.getAddr(),
					s2.getAddr());

		if (s1.getPostCode() == null || s2.getPostCode() == null
				|| s1.getPostCode().equals("") || s2.getPostCode().equals(""))
			r4 = 0;
		else
			r4 = LevenshteinExtend.getSimilarityRatio(s1.getPostCode(),
					s2.getPostCode());
		String[] tels1 = s1.getTel().split(";");
		String[] tels2 = s2.getTel().split(";");

		r5 = 1;
		boolean flag = false;
		if (tels1 != null)
			for (int i = 0; i < tels1.length; i++) {
				double tempR = 0;
				if (tels2 != null)
					for (int j = 0; j < tels2.length; j++) {
						tel1 = tels1[i];
						tel2 = tels2[j];
						tel1 = tel1.replace("+86", "");
						tel1 = tel1.replaceAll("\\D", "");
						tel2 = tel2.replace("+86", "");
						tel2 = tel2.replaceAll("\\D", "");
						double t = LevenshteinExtend.getSimilarityRatio(tel1,
								tel2);

						if (!(tels1[i] == null || tels2[j] == null
								|| tels1[i].equals("") || tels2[j].equals(""))) {
							if (t > tempR)
								tempR = t;
							flag = true;
						}
					}

				r5 *= (1 - tempR);
			}

		r5 = 1 - r5;
		if (r5 != 1)
			r5 = 0;

		sim1 = 1 - (1 - r1) * (1 - r2) * (1 - 0.9 * r3 - 0.1 * r4) * (1 - r5);
		if (r1 != 0)
			q += 0.2;
		if (r2 != 0)
			q += 0.3;
		if (r3 != 0)
			q += 0.27;
		if (r4 != 0)
			q += 0.03;
		if (flag)
			q += 0.2;
		sim2 = (0.2 * r1 + 0.3 * r2 + 0.27 * r3 + 0.03 * r4 + 0.2 * r5);
		if (q != 0 && q > 0.3)
			sim2 /= q;
		return (sim1 + sim2) / 2;
	}
	
}
