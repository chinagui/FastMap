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

		String address = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.solrAddress);

		client = new ConcurrentUpdateSolrClient(address, queueSize, threadCount);
		
//		client = SolrClientFactory.getInstance().getClient();

	}

	public void addTips(JSONObject json) throws JSONException,
			SolrServerException, IOException {

        SolrInputDocument doc = new SolrInputDocument();

        doc.addField("id", json.getString("id"));

        doc.addField("wkt", json.getString("wkt"));

        //这个主要是g_location:目前只用于tips的下载和渲染
        doc.addField("wktLocation", json.getString("wktLocation"));

        doc.addField("stage", json.getInt("stage"));

        //doc.addField("t_operateDate", json.getString("t_operateDate"));

        //doc.addField("t_date", json.getString("t_date"));

        //doc.addField("t_lifecycle", json.getInt("t_lifecycle"));

        //doc.addField("t_command", json.getInt("t_command"));

        doc.addField("handler", json.getInt("handler"));

        //doc.addField("s_sourceCode", json.getInt("s_sourceCode"));

        doc.addField("s_sourceType", json.getString("s_sourceType"));

        //doc.addField("g_location", json.getString("g_location"));

        //doc.addField("g_guide", json.getString("g_guide"));

        //doc.addField("deep", json.getString("deep"));

        //doc.addField("feedback", json.getString("feedback"));

        //doc.addField("s_reliability", json.getInt("s_reliability"));

        doc.addField("t_tipStatus", json.getInt("t_tipStatus"));
        doc.addField("t_dEditStatus", json.getInt("t_dEditStatus"));
        //doc.addField("t_dEditMeth", json.getInt("t_dEditMeth"));
        //doc.addField("t_mEditStatus", json.getInt("t_mEditStatus"));
        //doc.addField("t_mEditMeth", json.getInt("t_mEditMeth"));

//        if (json.containsKey("tipdiff")) {
//
//            doc.addField("tipdiff", json.getString("tipdiff"));
//        }

        doc.addField("s_qTaskId", json.getInt("s_qTaskId"));

        doc.addField("s_mTaskId", json.getInt("s_mTaskId"));

        doc.addField("s_qSubTaskId", json.getInt("s_qSubTaskId"));

//        if(json.containsKey("s_project") && StringUtils.isNotEmpty(json.getString("s_project"))) {
//            doc.addField("s_project", json.getString("s_project"));
//        }

        doc.addField("s_mSubTaskId", json.getInt("s_mSubTaskId"));

        //doc.addField("relate_links", json.getString("relate_links"));

        //doc.addField("relate_nodes", json.getString("relate_nodes"));

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
