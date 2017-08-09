package com.navinfo.dataservice.impcore.deepinfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.photo.Photo;
import com.navinfo.dataservice.engine.photo.CollectorImport;
import com.navinfo.dataservice.impcore.exception.DataErrorException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class DeepPhotoImporter {

	private Logger logger = LoggerRepos.getLogger(this.getClass());

	public static String[] gas = new String[] { "230215", "230216", "230217" };

	public static String[] parking = new String[] { "230210", "230213", "230214" };

	public void run(String filePath, String outPath) throws Exception {

		File file = new File(filePath);

		InputStreamReader read = new InputStreamReader(new FileInputStream(file));

		BufferedReader reader = new BufferedReader(read);

		String line;

		Connection conn = DBConnector.getInstance().getMkConnection();

		conn.setAutoCommit(false);

		String querySql = "select 1 from ix_poi where pid=?";

		PreparedStatement pstmt = conn.prepareStatement(querySql);

		Statement stmt = conn.createStatement();

		ResultSet rs = null;

		List<Integer> pids = new ArrayList<Integer>();

		int photoCount = 0;

		int total = 0;

		int notfound = 0;
		
		int notfoundtag = 0;

		int cache = 0;

		Map<String, Map<String, Photo>> importPhoto = new HashMap<String, Map<String, Photo>>();

		while ((line = reader.readLine()) != null) {

			if (total % 10000 == 0) {
				logger.info("total:" + total + ",not found:" + notfound);
			}

			total++;

			JSONObject poi = JSONObject.fromObject(line);

			int pid = poi.getInt("pid");

			pstmt.setInt(1, pid);

			rs = pstmt.executeQuery();

			if (!rs.next()) {
				notfound++;
				if (isTag(poi) > 0) {
					notfoundtag++;
					pids.add(pid);
				}
				rs.close();
				continue;
			}

			rs.close();

			try {

				String kindCode = poi.getString("kindCode");

				JSONObject crObj = poi.getJSONObject("rental");

				JSONObject gasStation = poi.getJSONObject("gasStation");
				Set<String> gasSets = new HashSet<String>();
				CollectionUtils.addAll(gasSets, gas);

				JSONObject parkings = poi.getJSONObject("parkings");
				Set<String> parkSets = new HashSet<String>();
				CollectionUtils.addAll(parkSets, parking);
				
				// IX_POI_CARRENTAL//IX_POI_GASSTATION//IX_POI_PARKING
				if ((kindCode.equals("200201") && JSONUtils.isNull(crObj))
						&& (gasSets.contains(kindCode) && JSONUtils.isNull(gasStation))
						&& (parkSets.contains(kindCode) && JSONUtils.isNull(parkings))) {
					continue;
				}

				int res = PhotoImporter.run(conn, stmt, poi, importPhoto);
				if (res > 0) {
					cache++;
					photoCount++;
				}

			} catch (DataErrorException ex) {
				logger.error("pid " + pid + ":" + ex.getMessage());
			}

			if (cache > 3000) {
				stmt.executeBatch();
				
				for (Map.Entry<String, Map<String, Photo>> entry : importPhoto.entrySet()) {
					CollectorImport.importPhotoNew(entry.getValue(), entry.getKey());
				}
				
				importPhoto = new HashMap<>();
				
				cache = 0;
			}
		}

		if (cache > 0) {
			stmt.executeBatch();
			
			for (Map.Entry<String, Map<String, Photo>> entry : importPhoto.entrySet()) {
				CollectorImport.importPhotoNew(entry.getValue(), entry.getKey());
			}
			
			importPhoto = new HashMap<>();
		}

		stmt.close();

		pstmt.close();

		conn.commit();

		conn.close();

		PrintWriter pw = new PrintWriter(outPath);

		for (Integer pid : pids) {
			pw.println(pid);
		}

		pw.flush();

		pw.close();

		logger.info("total:" + total + ",not found:" + notfound);
		
		logger.info("not found tag:" + notfoundtag);

		logger.info("IX_POI_PHOTO count:" + photoCount);

		logger.info("DONE.");

		reader.close();
	}
	
	
	private int isTag(JSONObject poi){
		JSONArray array = poi.getJSONArray("attachments");

		if (array.size() == 0)
			return 0;

		int result = 0;

		for (int i = 0; i < array.size(); i++) {
			JSONObject obj = array.getJSONObject(i);

			int tag = obj.getInt("tag");

			int type = obj.getInt("type");

			if (tag != 7 || type != 1) {
				continue;
			}
			result ++;
		}
		return result;
	}
}
