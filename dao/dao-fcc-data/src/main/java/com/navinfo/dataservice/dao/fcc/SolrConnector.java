package com.navinfo.dataservice.dao.fcc;

import org.apache.solr.client.solrj.impl.HttpSolrClient;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;


@Deprecated
public class SolrConnector {

	private static class SingletonHolder {
		private static final SolrConnector INSTANCE = new SolrConnector();
	}

	public static final SolrConnector getInstance() {
		return SingletonHolder.INSTANCE;
	}

	private HttpSolrClient client;
	
	public HttpSolrClient getClient() {
		if (client == null) {
			synchronized (this) {
				if (client == null) {
					String address = SystemConfigFactory.getSystemConfig()
							.getValue(PropConstant.solrAddress);

					client = new HttpSolrClient(address);
					
					client.setMaxTotalConnections(400);
					
					client.setDefaultMaxConnectionsPerHost(400);
				}
			}
		}
		return client;
	}

}
