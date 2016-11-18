package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiDetail;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: PoiBatchProcessorFM_BAT_20_195
 * @author: zhangpengpeng
 * @date: 2016年11月11日
 * @Desc: 深度信息通用银行网址批处理 具体原则见：深度信息批处理配置表——sheet通用银行网址批处理
 */
public class PoiBatchProcessorFM_BAT_20_195 implements IBatch {

	private JSONObject chainWebsite = new JSONObject();

	public PoiBatchProcessorFM_BAT_20_195() {
		//chainWebsite init
		chainWebsite.put("6003", "http://www.icbc.com.cn");
		chainWebsite.put("6045", "http://www.ccb.com");
		chainWebsite.put("6002", "http://www.abchina.com");
		chainWebsite.put("6001", "http://www.boc.cn");
		chainWebsite.put("6000", "http://www.bankcomm.com");
		chainWebsite.put("6028", "http://www.cmbchina.com");
		chainWebsite.put("6046", "http://bank.ecitic.com");
		chainWebsite.put("6025", "http://www.cmbc.com.cn");
		chainWebsite.put("602C", "http://www.cebbank.com");
		chainWebsite.put("602B", "http://www.hxb.com.cn");
		chainWebsite.put("6047", "http://www.psbc.com");
		chainWebsite.put("602A", "http://www.cgbchina.com.cn");
		chainWebsite.put("6026", "http://www.cib.com.cn");
		chainWebsite.put("6027", "http://www.spdb.com.cn");
		chainWebsite.put("602D", "http://www.bankofbeijing.com.cn");
		chainWebsite.put("6021", "http://www.bankofshanghai.com");
		chainWebsite.put("601C", "http://www.hkbea.com.cn");
		chainWebsite.put("6015", "http://www.citibank.com.cn");
		chainWebsite.put("6016", "http://www.hangseng.com.cn");
		chainWebsite.put("6014", "http://www.hsbc.com.cn");
		chainWebsite.put("600D", "http://www.standardchartered.com.cn");
		chainWebsite.put("600E", "http://www.whbcn.com");
		chainWebsite.put("600F", "http://www.dbs.com");
	}

	@Override
	public JSONObject run(IxPoi poi, Connection conn, JSONObject json, EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		try {
			String kindCode = poi.getKindCode();
			int uRecord = poi.getuRecord();
			// 不是银行分类的不批
			if (!kindCode.equals("150101") || (uRecord == 2)) {
				return result;
			}

			String chain = poi.getChain();
			JSONArray dataArray = new JSONArray();
			if (chainWebsite.containsKey(chain)) {
				String webSite = chainWebsite.getString(chain);
				// 获取POI的poiDetails
				List<IRow> poiDetails = poi.getDetails();
				if (poiDetails.size() > 0) {
					for (IRow poiDetail : poiDetails) {
						IxPoiDetail ixPoiDetail = (IxPoiDetail) poiDetail;
						String ixPoiWebSite = ixPoiDetail.getWebSite();
						// 判断POI的website是否和规则的一致，一致不批，否则批处理
						if (!webSite.equals(ixPoiWebSite)) {
							JSONObject data = new JSONObject();
							data.put("rowId", ixPoiDetail.getRowId());
							data.put("webSite", webSite);
							data.put("objStatus", ObjStatus.UPDATE.toString());
							dataArray.add(data);
						}
					}
				}
			}

			if (dataArray.size() > 0) {
				result.put("details", dataArray);
			}

			return result;
		} catch (Exception e) {
			throw e;
		}
	}
}
