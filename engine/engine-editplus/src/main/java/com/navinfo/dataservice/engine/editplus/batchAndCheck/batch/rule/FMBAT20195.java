package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiDetail;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;

import net.sf.json.JSONObject;

public class FMBAT20195 extends BasicBatchRule {
	
	private JSONObject chainWebsite = new JSONObject();
	
	private void chainWebsiteInit() {
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
	public void runBatch(BasicObj obj) throws Exception {
		IxPoiObj poiObj = (IxPoiObj) obj;
		IxPoi poi = (IxPoi) poiObj.getMainrow();
		if (poi.getHisOpType().equals(OperationType.DELETE)) {return;}
		if (poi.getKindCode().equals("150101")) {
			chainWebsiteInit();
			String chain = poi.getChain();
			if (chainWebsite.containsKey(chain)) {
				String webSite = chainWebsite.getString(chain);
				List<IxPoiDetail> poiDetails = poiObj.getIxPoiDetails();
				for (IxPoiDetail poiDetail : poiDetails) {
					String ixPoiWebSite = poiDetail.getWebSite();
					// 判断POI的website是否和规则的一致，一致不批，否则批处理
					if (!webSite.equals(ixPoiWebSite)) {
						poiDetail.setWebSite(webSite);
					}
				}
			}
		}
	}

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList) throws Exception {
		// TODO Auto-generated method stub

	}

}
