package com.navinfo.dataservice.dao.fcc.connection;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;

/** 
 * @ClassName: SolrClientFactory
 * @author xiaoxiaowen4127
 * @date 2017年7月5日
 * @Description: SolrClientFactory.java
 */
public class SolrClientFactory {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	private volatile static SolrClientFactory instance = null;
	private volatile CloudSolrClient cloudClient = null;
	private volatile HttpSolrClient httpClient = null;
	public static SolrClientFactory getInstance(){
		if(instance==null){
			synchronized(SolrClientFactory.class){
				if(instance==null){
					instance=new SolrClientFactory();
				}
			}
		}
		return instance;
	}
	public SolrClient getClient(){
//		return getCloudClient();
		return getHttpSimpleClient();
	}
	private SolrClient getCloudClient(){
		if(cloudClient==null){
			synchronized(this){
				if(cloudClient==null){

					String address = SystemConfigFactory.getSystemConfig().getValue(
							PropConstant.solrCloudAddress);
//					String zk = "192.168.4.192:2181,192.168.4.193:2181,192.168.4.194:2181";
					cloudClient = new CloudSolrClient(address);
					cloudClient.setDefaultCollection("tips_sp10_dev");
					cloudClient.setParallelUpdates(true);
				}
			}
		}
		return cloudClient;
		
	}
	private  HttpSolrClient getHttpSimpleClient() {
		if (httpClient == null) {
			synchronized (this) {
				if (httpClient == null) {
					String address = SystemConfigFactory.getSystemConfig()
							.getValue(PropConstant.solrAddress);
					
//					String address = "http://192.168.3.124:8082/solr/tips_sp4/";

					httpClient = new HttpSolrClient(address);
					
					httpClient.setMaxTotalConnections(300);
					
					httpClient.setDefaultMaxConnectionsPerHost(300);
				}
			}
		}
		return httpClient;
	}
}
