package com.navinfo.dataservice.scripts;

import java.io.PrintWriter;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.util.Assert;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryOperators;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import org.apache.commons.lang.StringUtils;

public class InitMongoDataExp {

	private static Logger logger = LoggerRepos
			.getLogger(InitMongoDataExp.class);

	private static Set<String> deepkcs = null;
	private static Set<String> charingkcs = null;

	// private static List<String> collectionNm = Arrays.asList("poi_detail",
	// "poi_parking", "poi_rental");

	public static JSONObject execute(JSONObject request) throws Exception {
		JSONObject response = new JSONObject();

		try {

			String mongodbHost = (String) request.get("mongodbHost");
			Assert.notNull(mongodbHost, "mongodbHost不能为空");
			int mongoPort = (int) request.get("mongoPort");
			int expType = (int) request.get("expType");
			String admincodes = (String) request.get("admincodes");
			Assert.notNull(admincodes, "admincodes不能为空");
			queryFromMongo(mongodbHost, mongoPort, expType, admincodes);
			logger.info("数据导出完毕");
			return response;

		} catch (Exception e) {
			response.put("msg", "ERROR:" + e.getMessage());
			throw e;
		}

	}

	public static void main(String[] args) {

		/*
		 * MongoClient mongoClient; MongoDatabase db; MongoCollection<Document>
		 * collection; System.out.println("-----------------------------");
		 * 
		 * try {
		 * 
		 * mongoClient = new MongoClient("192.168.4.220", 30000);
		 * 
		 * db = mongoClient.getDatabase("edit_charge"); initKcs(); BasicDBObject
		 * condition = new BasicDBObject(); condition.put("kindCode", new
		 * BasicDBObject(QueryOperators.IN, charingkcs));
		 * 
		 * condition.put("lifecycle", new BasicDBObject(QueryOperators.NE, 1));
		 * 
		 * collection = db.getCollection("poi"); MongoCursor<Document> cur =
		 * collection.find(condition).iterator(); while (cur.hasNext()) {
		 * 
		 * System.out.println(JSONObject.fromObject(cur.next().toString())
		 * .toString());
		 * 
		 * }
		 * 
		 * } catch (Exception e) { logger.info(e.getMessage(), e);
		 * System.out.println(e.getMessage() + "-------------------------");
		 * e.printStackTrace(); }
		 */
		String mongodbHost = "192.168.4.220";
		int mongoPort = 30000;
		int expType = 1;
		String admincodes = "310000";
		try {
			queryFromMongo(mongodbHost, mongoPort, expType, admincodes);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	public static void queryFromMongo(String mongodbHost, int mongoPort,
			int expType, String admincodes) throws Exception {

		logger.info("MongoDB init start");

		MongoClient mongoClient;
		MongoDatabase db;
		MongoCollection<Document> collection;
		try {
			Map<String, Collection<String>> adminMeshes = parseMeshes(admincodes);
			mongoClient = new MongoClient(mongodbHost, mongoPort);
			String mongoDbName = "";
			if (expType == 0) {
				mongoDbName = "deepinfo";
			}
			if (expType == 1) {
				mongoDbName = "edit_charge";

			}
			db = mongoClient.getDatabase(mongoDbName);

			logger.info("MongoDB init success");

			int total = 0;

			int count = 0;
			logger.info("MongoDB find data");
			for (String admimcode : adminMeshes.keySet()) {
				logger.info("admimcode ==  " + admimcode);

				total++;

				if (total % 10000 == 0) {
					System.out.println("total:" + total + ",output:" + count);
				}
				PrintWriter pw = null;

				System.out.println("starting exp " + admimcode);

				BasicDBObject condition = new BasicDBObject();

				BasicDBObject fields = new BasicDBObject();
				fields.put("_id", 0);

				if (expType == 0) {
					initdeepKcs();
					condition.put("kindCode", new BasicDBObject(
							QueryOperators.IN, deepkcs));
				}
				if (expType == 1) {
					initKcs();
					condition.put("kindCode", new BasicDBObject(
							QueryOperators.IN, charingkcs));
				}
				condition.put("meshid", new BasicDBObject(QueryOperators.IN,
						adminMeshes.get(admimcode)));
				condition.put("lifecycle", new BasicDBObject(QueryOperators.NE,
						1));
				if (expType == 0) {
					String fileName = admimcode + "_poi_deep.txt";
					pw = new PrintWriter(fileName);
					// for (String col : collectionNm) {
					collection = db.getCollection("poi");
					MongoCursor<Document> curDeep = collection
							.find(condition)
							.projection(
									Projections.include("pid", "kindCode",
											"businessTime", "gasStation",
											"parkings", "rental", "website",
											"contacts", "attachments",
											"hwEntryExit", "hotel", "hospital"))
							.iterator();
					while (curDeep.hasNext()) {
						count++;

						pw.println(curDeep.next().toJson());
					}
				}
				// }
				if (expType == 1) {
					String fileName = admimcode + "_poi_charging.txt";
					pw = new PrintWriter(fileName);
					collection = db.getCollection("poi");
					MongoCursor<Document> cur = collection
							.find(condition)
							.projection(
									Projections.include("pid", "kindCode",
											"chargingPole", "chargingStation"))
							.iterator();
					while (cur.hasNext()) {
						count++;

						pw.println(cur.next().toJson());

					}

				}
				pw.flush();
				pw.close();

				System.out
						.println("Total count of " + admimcode + ": " + total);
				System.out.println("Output count of " + admimcode + ": "
						+ count);

			}
		} catch (Exception e) {
			logger.info(e.getMessage(), e);
			throw e;
		}

	}

	private static Map<String, Collection<String>> parseMeshes(String admincodes)
			throws Exception {

		if (StringUtils.isEmpty(admincodes)) {
			return null;
		}
		Map<String, Collection<String>> adminMeshes = new HashMap<String, Collection<String>>();

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String sql = "SELECT ADMINCODE,MESH FROM CP_MESHLIST WHERE ADMINCODE IN ("
					+ admincodes + ")";
			conn = DBConnector.getInstance().getMetaConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String ac = rs.getString("ADMINCODE");
				String mesh = rs.getString("MESH");
				if (adminMeshes.containsKey(ac)) {
					adminMeshes.get(ac).add(mesh);
				} else {
					Collection<String> meshes = new HashSet<String>();
					meshes.add(mesh);
					adminMeshes.put(ac, meshes);
				}
			}
		} catch (Exception e) {
			if (rs != null)
				rs.close();
			if (pstmt != null)
				pstmt.close();
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			e.printStackTrace();
			throw e;
		}
		return adminMeshes;
	}

	private static void initKcs() {
		charingkcs = new HashSet<String>();
		charingkcs.add("230218");
		charingkcs.add("230227");

	}

	private static void initdeepKcs() {

		deepkcs = new HashSet<String>();

		deepkcs.add("230210");
		deepkcs.add("230215");
		deepkcs.add("110102");
		deepkcs.add("120101");
		deepkcs.add("180400");
		deepkcs.add("180304");
		deepkcs.add("160205");
		deepkcs.add("130501");
		deepkcs.add("150101");
		deepkcs.add("110200");
		deepkcs.add("120102");
		deepkcs.add("130102");
		deepkcs.add("130105");
		deepkcs.add("230108");
		deepkcs.add("170101");
		deepkcs.add("200201");
		deepkcs.add("230103");
		deepkcs.add("160206");
		deepkcs.add("230216");
		deepkcs.add("180210");
		deepkcs.add("200103");
		deepkcs.add("160203");
		deepkcs.add("230214");
		deepkcs.add("190107");
		deepkcs.add("170102");
		deepkcs.add("170100");
		deepkcs.add("190114");
		deepkcs.add("110103");
		deepkcs.add("190403");
		deepkcs.add("180207");
		deepkcs.add("190400");
		deepkcs.add("190402");
		deepkcs.add("180100");
		deepkcs.add("160202");
		deepkcs.add("160208");
		deepkcs.add("230213");
		deepkcs.add("160201");
		deepkcs.add("180308");
		deepkcs.add("160105");
		deepkcs.add("160207");
		deepkcs.add("190100");
		deepkcs.add("200101");
		deepkcs.add("190401");
		deepkcs.add("120201");
		deepkcs.add("160102");
		deepkcs.add("120202");
		deepkcs.add("150103");
		deepkcs.add("190201");
		deepkcs.add("160101");
		deepkcs.add("160103");
		deepkcs.add("190109");
		deepkcs.add("130106");
		deepkcs.add("190111");
		deepkcs.add("180306");
		deepkcs.add("190103");
		deepkcs.add("230100");
		deepkcs.add("180203");
		deepkcs.add("190104");
		deepkcs.add("200104");
		deepkcs.add("110301");
		deepkcs.add("180309");
		deepkcs.add("190108");
		deepkcs.add("130602");
		deepkcs.add("190106");
		deepkcs.add("130402");
		deepkcs.add("190102");
		deepkcs.add("120103");
		deepkcs.add("160106");
		deepkcs.add("230129");
		deepkcs.add("210301");
		deepkcs.add("130700");
		deepkcs.add("230101");
		deepkcs.add("130702");
		deepkcs.add("240100");
		deepkcs.add("220100");
		deepkcs.add("220200");
		deepkcs.add("130408");
		deepkcs.add("230126");
		deepkcs.add("130101");
		deepkcs.add("220300");
		deepkcs.add("150200");
		deepkcs.add("110101");
		deepkcs.add("190101");
		deepkcs.add("210207");
		deepkcs.add("130401");
		deepkcs.add("160100");
		deepkcs.add("130301");
		deepkcs.add("180310");
		deepkcs.add("130705");
		deepkcs.add("130200");
		deepkcs.add("130104");
		deepkcs.add("130103");
		deepkcs.add("110303");
		deepkcs.add("130204");
		deepkcs.add("110302");
		deepkcs.add("130701");
		deepkcs.add("130202");
		deepkcs.add("120104");
		deepkcs.add("130207");
		deepkcs.add("130801");
		deepkcs.add("130206");
		deepkcs.add("110304");
		deepkcs.add("180209");
		deepkcs.add("130410");
		deepkcs.add("130800");
		deepkcs.add("130411");
		deepkcs.add("130304");
		deepkcs.add("130203");
		deepkcs.add("180307");
		deepkcs.add("170105");
		deepkcs.add("180302");
		deepkcs.add("170109");
		deepkcs.add("200105");
		deepkcs.add("170103");
		deepkcs.add("170106");
		deepkcs.add("150104");
		deepkcs.add("130502");
		deepkcs.add("170104");
		deepkcs.add("180102");
		deepkcs.add("230207");
		deepkcs.add("140203");
		deepkcs.add("130303");
		deepkcs.add("190404");
		deepkcs.add("170107");
		deepkcs.add("130703");
		deepkcs.add("180301");
		deepkcs.add("130704");
		deepkcs.add("130201");
		deepkcs.add("130803");
		deepkcs.add("130302");
		deepkcs.add("130205");
		deepkcs.add("130407");
		deepkcs.add("180202");
		deepkcs.add("130603");
		deepkcs.add("130406");
		deepkcs.add("180111");

	}

}
