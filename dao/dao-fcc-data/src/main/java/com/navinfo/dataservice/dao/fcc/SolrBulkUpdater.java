package com.navinfo.dataservice.dao.fcc;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONException;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.dao.fcc.connection.SolrClientFactory;


public class SolrBulkUpdater {

//	private ConcurrentUpdateSolrClient client;
	
	private SolrClient client;
	
	public SolrBulkUpdater(int queueSize, int threadCount) {

//		String address = SystemConfigFactory.getSystemConfig().getValue(
//				PropConstant.solrAddress);

//		client = new ConcurrentUpdateSolrClient(address, queueSize, threadCount);
		
		client = SolrClientFactory.getInstance().getClient();

	}

	public void addTips(JSONObject json) throws JSONException,
			SolrServerException, IOException {

		SolrInputDocument doc = new SolrInputDocument();

		doc.addField("id", json.getString("id"));

		doc.addField("wkt", json.getString("wkt"));

		doc.addField("stage", json.getInt("stage"));
		
		doc.addField("t_operateDate", json.getString("t_operateDate"));

		doc.addField("t_date", json.getString("t_date"));

		doc.addField("t_lifecycle", json.getInt("t_lifecycle"));

		doc.addField("t_command", json.getInt("t_command"));

		doc.addField("handler", json.getInt("handler"));
		
		doc.addField("t_cStatus", json.getInt("t_cStatus"));
		
		doc.addField("t_dStatus", json.getInt("t_dStatus"));
		
		doc.addField("t_mStatus", json.getInt("t_mStatus"));
		
		doc.addField("s_sourceCode", json.getInt("s_sourceCode"));

		doc.addField("s_sourceType", json.getString("s_sourceType"));

		doc.addField("g_location", json.getString("g_location"));

		doc.addField("g_guide", json.getString("g_guide"));

		doc.addField("deep", json.getString("deep"));
		
		doc.addField("feedback", json.getString("feedback"));
		
		doc.addField("s_reliability", json.getInt("s_reliability"));
		
		client.add(doc);
	}
	
	public void commit() throws SolrServerException, IOException{
		
		client.commit();
	}
	
	public void close() throws SolrServerException, IOException{
		
		client.close();
	}
	
	public void rollback() throws SolrServerException, IOException{
		
		client.rollback();
		
	}
}
