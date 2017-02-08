package com.navinfo.dataservice.engine.fcc;

import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Test;

import com.navinfo.dataservice.dao.fcc.SolrController;

/** 
 * @ClassName: SolrTest.java
 * @author y
 * @date 2017-1-6 上午11:32:49
 * @Description: TODO
 *  
 */
public class SolrTest  {
	
	@Test
	public void testEdit() {

		SolrController solr = new SolrController();
		JSONArray stages=new JSONArray();
		stages.add(1);
		stages.add(2);
		//stages.add(5);
		try {
			List<JSONObject> datas=solr.queryTipsWeb("POLYGON ((-142.94007897377014 83.34101832624152,-142.93962836265564 83.34101832624152,-142.93962836265564 83.3410705787633,-142.94007897377014 83.3410705787633,-142.94007897377014 83.34101832624152))", stages);
			for (JSONObject jsonObject : datas) {
				if(jsonObject.containsKey("t_inStatus")){
					jsonObject.remove("t_inStatus");
				}
				 if(!jsonObject.containsKey("t_pStatus")){
					jsonObject.put("t_pStatus", 0);
					jsonObject.put("t_mInProc", 0);
					jsonObject.put("t_dInProc", 0);
				}
				 if(!jsonObject.containsKey("t_inMeth")){
						jsonObject.put("t_inMeth", 0);
					}
				if(jsonObject.containsKey("feedback")){
					try{
						JSONArray arr=JSONArray.fromObject(jsonObject.get("feedback")) ;
						JSONObject  newFeedBObject=new JSONObject();
						newFeedBObject.put("f_array", arr);
						
						jsonObject.put("feedback", newFeedBObject);
						System.out.println(jsonObject.get("id"));
					}catch (Exception e) {
						// TODO: handle exception
					}
				}
				System.out.println(jsonObject.get("id"));
				solr.addTips(jsonObject);
			}
			
			System.out.println("处理完毕");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
