package com.navinfo.dataservice.impcore.deepinfo;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiCarrental;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.operator.BasicOperator;

import com.navinfo.dataservice.impcore.exception.DataErrorException;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class DetailImporter {
	
	public static String[] kcs=new String[]{"180308","180309","180304","180400","160206","160205","170100","170101","170102","150101","110103","110102","130501","130105","110200","120101","120101","120101","120102","230215","230216","230217","200201"};
	
	public static int run(Result result, Connection conn,
			Statement stmt, JSONObject poi) throws Exception {


		if (JSONUtils.isNull(poi)) {
			return 0;
		}

		Set<String> kcSets = new HashSet<String>();
		CollectionUtils.addAll(kcSets, kcs);
		String kindCode = poi.getString("kindCode");
		if(!kcSets.contains(kindCode)){
			return 0;
		}

		IxPoiDetail det = new IxPoiDetail();
		
		det.setPoiPid(poi.getInt("pid"));
		
		if(!"200201".equals(kindCode)){
			det.setWebsite(JsonUtils.getString(poi, "website"));
		}
		
		BasicOperator operator = new BasicOperator(conn,
				det);

		operator.insertRow2Sql(stmt);

		return 1;
	}
}
