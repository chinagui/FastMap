package com.navinfo.dataservice.dao.fcc;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;

import com.navinfo.dataservice.commons.log.LoggerRepos;

/** 
 * @ClassName: SolrClientUtils
 * @author xiaoxiaowen4127
 * @date 2017年7月8日
 * @Description: SolrClientUtils.java
 */
public class SolrClientUtils {
	
	public static Logger log = LoggerRepos.getLogger(SolrClientUtils.class);
	
	public static void close(SolrClient client) throws IOException{
		client.close();
	}

	public static void closeQuietly(SolrClient client) {
		try{
			client.close();
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}

	public static void commitAndCloseQuietly(SolrClient client) {
		try{
			client.close();
		}catch(Exception e){
			log.error(e.getMessage(),e);
		}
	}
}
