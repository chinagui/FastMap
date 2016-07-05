package com.navinfo.dataservice.dao.fcc;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     * @throws IOException 
     * @throws SolrServerException 
     */
    public void testApp() throws Exception
    {
    	HttpSolrClient client = SolrConnector.getInstance().getClient();
    	SolrInputDocument doc = new SolrInputDocument();
    	String id = java.util.UUID.randomUUID().toString();
    	doc.addField("id", id);
    	doc.addField("t_lifecycle", 1);
    	doc.addField("s_sourceCode", 1);
    	doc.addField("t_mStatus", 1);
    	doc.addField("t_operateDate", new java.text.SimpleDateFormat("yyyymmddhhMMss").format(new java.util.Date()));
    	doc.addField("t_command", 1);
    	doc.addField("g_guide", "MULTIPOLYGON (((115.90625 26.02083, 115.90625 26.04167, 115.9375 26.04167, 115.9375 26.02083, 115.90625 26.02083)), ((110.53125 2.16667, 110.53125 2.1875, 110.5625 2.1875, 110.5625 2.16667, 110.53125 2.16667)))");
    	doc.addField("g_location", "MULTIPOLYGON (((115.90625 26.02083, 115.90625 26.04167, 115.9375 26.04167, 115.9375 26.02083, 115.90625 26.02083)), ((110.53125 2.16667, 110.53125 2.1875, 110.5625 2.1875, 110.5625 2.16667, 110.53125 2.16667)))");
    	doc.addField("t_dStatus", 1);
    	doc.addField("stage", 2);
    	doc.addField("feedback", "");
    	doc.addField("t_cStatus", 0);
    	doc.addField("s_reliability", 0);
    	doc.addField("t_date", new java.text.SimpleDateFormat("yyyymmddhhMMss").format(new java.util.Date()));
    	doc.addField("s_sourceType", 0);
    	doc.addField("deep", 0);
    	doc.addField("handler", "0");
    	client.add(doc);
    	client.commit();
//    	client.deleteById(id);
//    	client.commit();
    	Map<String, String> paraMap = new HashMap<String, String>();
    	paraMap.put("id", id.toString());
		SolrParams params=new  MapSolrParams(paraMap );
		QueryResponse response = client.query(params);
		SolrDocumentList results = response.getResults();
		Iterator<SolrDocument> ite = results.iterator();
		while(ite.hasNext()){
			System.out.println(ite.next());
		}
    }
}
