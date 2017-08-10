package com.navinfo.dataservice.engine.fcc.tips.direct;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.GeometryExceptionWithContext;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

import org.apache.commons.dbutils.DbUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;

import com.navinfo.dataservice.commons.timedomain.TimeDecoder;
import com.navinfo.dataservice.commons.util.DisplayUtils;
import com.navinfo.dataservice.dao.fcc.SolrBulkUpdater;
import com.navinfo.dataservice.engine.fcc.tips.TipsImportUtils;

public class DirectTipsBuilder {

	private static final WKT wkt = new WKT();

	private static String sql = "select a.link_pid,a.direct,b.time_domain,a.geometry "
			+ "from rd_link a,rd_link_limit b "
			+ "where a.link_pid = b.link_pid "
			+ " and b.type=1 and time_domain is not null";

	private static String type = "1203";

	/**
	 * 导入入口程序块
	 * 
	 * @param fmgdbConn
	 * @param htab
	 */
	public static void importTips(java.sql.Connection fmgdbConn, Table htab)
			throws Exception {
		SolrBulkUpdater solrConn = new SolrBulkUpdater(TipsImportUtils.QueueSize,TipsImportUtils.ThreadCount);
		Statement stmt = null;

		ResultSet resultSet = null;
		try{
			stmt = fmgdbConn.createStatement();
	
			resultSet = stmt.executeQuery(sql);
	
			resultSet.setFetchSize(5000);
	
			List<Put> puts = new ArrayList<Put>();
	
			int num = 0;
	
			String uniqId = null;
	
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String date = sdf.format(new Date());
	
			while (resultSet.next()) {
				num++;
	
				uniqId = resultSet.getString("link_pid");
	
				String rowkey = TipsImportUtils.generateRowkey(uniqId, type);
	
				String source = TipsImportUtils.generateSource(type);
	
				String track = TipsImportUtils.generateTrack(date);
				
				String feedback = TipsImportUtils.generateFeedback();
	
				JSONObject geometry = generateGeometry(resultSet);
	
				String deep = generateDeep(resultSet);
	
				Put put = new Put(rowkey.getBytes());
	
				put.addColumn("data".getBytes(), "source".getBytes(),
						source.getBytes());
	
				put.addColumn("data".getBytes(), "track".getBytes(),
						track.getBytes());
	
				put.addColumn("data".getBytes(), "geometry".getBytes(),
						geometry.toString().getBytes());
	
				put.addColumn("data".getBytes(), "deep".getBytes(), deep.getBytes());
				
				put.addColumn("data".getBytes(), "feedback".getBytes(), feedback.getBytes());
	
				puts.add(put);
				
				JSONObject solrIndexJson = TipsImportUtils.assembleSolrIndex(rowkey, 0, date, type, deep.toString(), geometry.getJSONObject("g_location"), geometry.getJSONObject("g_guide"), "[]");
	
				solrConn.addTips(solrIndexJson);
				
				if (num % 5000 == 0) {
					htab.put(puts);
	
					puts.clear();
	
				}
			}
	
			htab.put(puts);
	
			solrConn.commit();
			
			solrConn.close();
		}catch (Exception e) {
			throw e;
		}finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(stmt);
		}
		
	}

	private static JSONObject generateGeometry(ResultSet resultSet)
			throws SQLException, GeometryExceptionWithContext {
		STRUCT struct1 = (STRUCT) resultSet.getObject("geometry");

		JGeometry geom1 = JGeometry.load(struct1);

		int direct = resultSet.getInt("direct");

		double[] point = DisplayUtils.get1_4Point(geom1, direct);

		JSONObject json = new JSONObject();

		json.put("type", "Point");

		json.put("coordinates", point);

		JSONObject geometry = new JSONObject();

		geometry.put("g_location", json);

		geometry.put("g_guide", json);

		return geometry;
	}

	private static String generateDeep(ResultSet resultSet) throws Exception {

		JSONObject json = new JSONObject();

		JSONObject jsonF = new JSONObject();

		jsonF.put("id", resultSet.getString("link_pid"));

		jsonF.put("type", 1);

		json.put("f", jsonF);

		STRUCT struct1 = (STRUCT) resultSet.getObject("geometry");

		JGeometry geom1 = JGeometry.load(struct1);

		String linkWkt = new String(wkt.fromJGeometry(geom1));

		int direct = resultSet.getInt("direct");

		json.put("agl", DisplayUtils.calIncloudedAngle(linkWkt, direct));

		json.put("dr", direct);
		
		TimeDecoder decoder = new TimeDecoder();
		
		json.put("time", decoder.decode(resultSet.getString("time_domain")));

		return json.toString();
	}

}
