package com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.rule;

import java.util.Collection;
import java.util.List;

import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoiCarrental;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.commons.util.StringUtils;

import net.sf.json.JSONObject;
/**
 * 汽车租赁网址和电话批处理 具体原则见：深度信息批处理配置表——sheet汽车租赁批处理
 * @author Gao Pengrong
 */
public class FMBAT20197 extends BasicBatchRule {
	private JSONObject chainWebsite = new JSONObject();
	private JSONObject chainContact = new JSONObject();
	

	@Override
	public void loadReferDatas(Collection<BasicObj> batchDataList)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	//月编无删除数据
	public void runBatch(BasicObj obj) throws Exception {
		// chainWebsite init
		chainWebsite.put("8007", "http://www.top1.cn/");
		chainWebsite.put("8006", "http://www.1hai.cn");
		chainWebsite.put("8004", "http://www.avis.cn");
		chainWebsite.put("8003", "http://www.zuche.com/");
		chainWebsite.put("3000", "http://www.izuche.com");
		chainWebsite.put("3854", "http://www.hnqzw.com.cn");
		chainWebsite.put("3902", "http://www.cdizu.com");
		chainWebsite.put("8005", "https://www.hertz.cn");
		chainWebsite.put("2FAA", "http://www.reocar.com");
		chainWebsite.put("36AD", "http://www.ezucoo.com");
		chainWebsite.put("36AE", "http://www.xinshengbo.com");
		chainWebsite.put("36AF", "http://www.dafang24.com");
		chainWebsite.put("3958", "http://www.green-go.cn");
		chainWebsite.put("3959", "http://www.evcardchina.com");
		// chainContact init chain=3000不批处理contact，所以不在chainContact
		chainContact.put("8007", "4006788588");
		chainContact.put("8006", "4008886608");
		chainContact.put("8004", "4008821119");
		chainContact.put("8003", "4006166666");
		chainContact.put("3854", "4008716166");
		chainContact.put("3902", "4008120556");
		chainContact.put("8005", "4009211138");
		chainContact.put("2FAA", "4007770888");
		chainContact.put("36AD", "4008308899");
		chainContact.put("36AE", "4006035155");
		chainContact.put("36AF", "4000600112");
		chainContact.put("3958", "4008080899");
		chainContact.put("3959", "4009208050");
		if(obj.objName().equals(ObjectName.IX_POI)){
			IxPoiObj poiObj=(IxPoiObj) obj;
			IxPoi poi=(IxPoi) poiObj.getMainrow();
			String kindCode= poi.getKindCode();
			long pid= poi.getPid();
			try {
				// 非汽车租赁分类，不进行汽车租赁网址和电话批处理
				if (!kindCode.equals("200201")) {
					return;
				}
				String chain = poi.getChain();
				List<IxPoiCarrental> carrentals = poiObj.getIxPoiCarrentals();
				// chain值在汽车租赁网址范围内的批
				if (chainWebsite.containsKey(chain) && StringUtils.isNotEmpty(chain)) {
					//POI有汽车租赁，则更新website,phone400
					if (carrentals.size() > 0) {
						for (IxPoiCarrental carrental : carrentals) {
							carrental.setWebSite(chainWebsite.getString(chain));
							//首汽租车 chain=3000,400电话不需要批
							if (!"3000".equals(chain)){
								carrental.setPhone400(chainContact.getString(chain));
							}
						}
					}else{
						//POI没有汽车租赁，则新增一条汽车租赁，并且设置website,phone400值
						IxPoiCarrental newCarrental= poiObj.createIxPoiCarrental();
						newCarrental.setPoiPid(pid);
						newCarrental.setWebSite(chainWebsite.getString(chain));
						if (!"3000".equals(chain)){
							newCarrental.setPhone400(chainContact.getString(chain));
						}
					}
				}

			} catch (Exception e) {
				throw e;
			}	
		}

	}

}
