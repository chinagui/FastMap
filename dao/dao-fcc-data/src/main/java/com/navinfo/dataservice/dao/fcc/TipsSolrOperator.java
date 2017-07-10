package com.navinfo.dataservice.dao.fcc;

import java.util.Collection;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.SolrInputDocument;

import com.navinfo.dataservice.commons.util.DateUtils;

/** 
 * @ClassName: SolrOperator
 * @author xiaoxiaowen4127
 * @date 2017年7月5日
 * @Description: SolrOperator.java
 */
public class TipsSolrOperator {
	
	public List<TipsSolrDao> listByQTask(SolrClient client,Collection<Integer> qtaskIds)throws Exception{
		
		
		return null;
	}
	
	public void add(SolrClient client,TipsSolrDao tips)throws Exception{
		SolrInputDocument doc = new SolrInputDocument();
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
			doc.addField("stage", 12);
			doc.addField("t_date", DateUtils.getCurrentTimestamp());
			doc.addField("t_operateDate", DateUtils.getCurrentTimestampWithMilis());
			doc.addField("t_lifecycle", 1);
			doc.addField("t_command", 0);
			doc.addField("handler", 4127);
			doc.addField("s_sourceType", "1301");
			doc.addField("s_sourceCode", 11);
			doc.addField("g_location", "A");
			doc.addField("g_guide", "A");
			doc.addField("wkt", "POINT(123.00123 30.12309)");
			doc.addField("deep", "A");
			doc.addField("feedback", "A");
			doc.addField("s_reliability", 80);
			doc.addField("t_cStatus", 1);
			doc.addField("t_dStatus", 1);
			doc.addField("t_mStatus", 1);
			doc.addField("t_inMeth",1 );
			doc.addField("t_pStatus", 1);
			doc.addField("t_dInProc", 1);
			doc.addField("t_mInProc", 1);
//			doc.addField("tipdiff", );
			doc.addField("s_qTaskId", 123);
			doc.addField("s_mTaskId", 234);
			doc.addField("t_fStatus", 1);
			doc.addField("s_qSubTaskId", 12);
			doc.addField("s_mSubTaskId", 23);
			doc.addField("wktLocation", "POINT (113.38127 22.51341)");
			doc.addField("relate_links", "");
			doc.addField("relate_nodes", "");
			
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
