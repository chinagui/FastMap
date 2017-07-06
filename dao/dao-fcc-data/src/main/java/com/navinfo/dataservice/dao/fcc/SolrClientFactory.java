package com.navinfo.dataservice.dao.fcc;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.CloudSolrClient;

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
		String zk = "192.168.4.192:2181,192.168.4.193:2181,192.168.4.194:2181";
		CloudSolrClient client = new CloudSolrClient(zk);
		client.setDefaultCollection("tips_sp10");
		return client;
		
	}
}
