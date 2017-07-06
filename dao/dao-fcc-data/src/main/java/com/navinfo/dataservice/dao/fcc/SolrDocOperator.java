package com.navinfo.dataservice.dao.fcc;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

/** 
 * @ClassName: SolrOperator
 * @author xiaoxiaowen4127
 * @date 2017年7月5日
 * @Description: SolrOperator.java
 */
public class SolrDocOperator {
	
	public void add(SolrClient client,SolrInputDocument doc)throws Exception{
		
		client.add(doc);
		client.commit();
		client.close();
	}
	
	public static void main(String[] args) {
		SolrClient client = null;
		try{
			client = SolrClientFactory.getInstance().getClient();
			
			
			
			SolrInputDocument doc = new SolrInputDocument();

			doc.addField("id", "Navinfo");
//			doc.addField("stage", 12);

//			doc.addField("wkt", "POINT(123.00123 30.12309)");
			
			client.add(doc);
			
			client.commit();
		}catch(Exception e){
			e.printStackTrace();
			try{
				client.rollback();
			}catch(Exception err){
				err.printStackTrace();
			}
		}finally{
			if(client!=null){
				try{
					client.close();
				}catch(Exception err){
					err.printStackTrace();
				}
			}
		}
		
	}
	
}
