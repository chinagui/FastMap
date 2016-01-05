package com.navinfo.dataservice.commons.db;

import org.apache.solr.client.solrj.SolrClient;

/**
 * solr连接类
 */
public class SolrAddress {

	/**
	 * 初始化solr连接
	 * 
	 * @return
	 */
	public static boolean initSolrAddress() {

		String address = ConfigLoader.getConfig().getString("solr_address");

		return false;
	}

	/**
	 * 获取solr连接
	 * 
	 * @return
	 */
	public static SolrClient getSolrClient() {
		return null;
	}

}
