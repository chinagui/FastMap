package com.navinfo.dataservice.scripts.tmp.mongo2Gdb;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.bson.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryOperators;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.mongo.MongoDbFactory;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import org.apache.commons.lang.StringUtils;

public class MongoDataExp {
	Param inParam;
	private static Logger logger = LoggerRepos.getLogger(MongoDataExp.class);

	private static Set<Integer> deepkcs = null;
	private static Set<Integer> charingkcs = null;
	private List<String> collectionNm = Arrays.asList("poi_detail",
			"poi_parking", "poi_rental");

	public static void main(String[] args) throws Exception {

		logger.info("解析获取参数");
		Param inParam = parseArgs(args);
		logger.info(inParam);
		MongoDataExp differ = new MongoDataExp();
		differ.inParam = inParam;
		differ.queryFromMongo();
		logger.info("数据导出完毕");

	}

	private void queryFromMongo() throws Exception {

		logger.info("MongoDB init start");

		MongoClient mongoClient;
		MongoDatabase db;
		MongoCollection<Document> collection;
		Map<String, Collection<String>> adminMeshes = parseMeshes(this.inParam
				.getProps());
		mongoClient = MongoDbFactory.getInstance().getMongoInstance(
				this.inParam.mongodbDbName, this.inParam.getMongoPort());
		db = mongoClient.getDatabase(this.inParam.mongodbDbName);

		logger.info("MongoDB init success");

		int total = 0;

		int count = 0;
		logger.info("MongoDB find data");
		for (String admimcode : adminMeshes.keySet()) {

			total++;

			if (total % 10000 == 0) {
				System.out.println("total:" + total + ",output:" + count);
			}
			PrintWriter pw = null;

			System.out.println("starting exp " + admimcode);

			BasicDBObject condition = new BasicDBObject();
			if (this.inParam.expType == 0) {
				initdeepKcs();
				condition.put("kindCode", new BasicDBObject(QueryOperators.IN,
						deepkcs));
			}
			if (this.inParam.expType == 1) {
				initKcs();
				condition.put("kindCode", new BasicDBObject(QueryOperators.IN,
						charingkcs));
			}
			condition.put("meshid", new BasicDBObject(QueryOperators.IN,
					adminMeshes.get(admimcode)));
			condition.put("lifecycle", new BasicDBObject(QueryOperators.NE, 1));
			if (this.inParam.expType == 0) {
				String fileName = admimcode + "_poi_deep.txt";
				pw = new PrintWriter(fileName);
				for (String col : collectionNm) {
					collection = db.getCollection(col);
					MongoCursor<Document> curDeep = collection.find(condition)
							.iterator();
					while (curDeep.hasNext()) {
						count++;

						pw.write(curDeep.next().toString());
					}
				}
			}
			if (this.inParam.expType == 1) {
				String fileName = admimcode + "_poi_charging.txt";
				pw = new PrintWriter(fileName);
				collection = db.getCollection("poi");
				MongoCursor<Document> cur = collection.find(condition)
						.iterator();
				while (cur.hasNext()) {
					count++;

					pw.write(cur.next().toString());

				}

			}
			pw.flush();
			pw.close();

			System.out.println("Total count of " + admimcode + ": " + total);
			System.out.println("Output count of " + admimcode + ": " + count);

		}

	}

	private static Map<String, Collection<String>> parseMeshes(Properties props)
			throws Exception {

		String admincodes = props.getProperty("admincodes");
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
			conn = DBConnector.getInstance().getManConnection();
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String ac = rs.getString("ADMINCODE");
				String mesh = rs.getString("MESH");
				if (adminMeshes.containsKey(ac)) {
					adminMeshes.get(ac).add(StringUtils.leftPad(mesh, 8, '0'));
				} else {
					Collection<String> meshes = new HashSet<String>();
					meshes.add(StringUtils.leftPad(mesh, 8, '0'));
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

	private static Param parseArgs(String[] args) throws FileNotFoundException,
			IOException {
		Param param = new Param();
		String strArg = args[0];
		String strAdminCode = args[1];
		String[] ArgList = strArg.split(",");
		if (ArgList.length != 3) {
			System.out.println("参数个数错误");
		}
		Properties props = new Properties();
		props.load(new FileInputStream(strAdminCode));

		param.setMongodbHost(ArgList[0]);//
		param.setMongoPort(Integer.parseInt(ArgList[1]));
		param.setExpType(Integer.parseInt(ArgList[2]));
		if (param.getExpType() == 0) {
			param.setMongodbDbName("fm_edit_deepinfo");
		}
		if (param.getExpType() == 1) {
			param.setMongodbDbName("edit_charge");
		}
		param.setProps(props);
		return param;

	}

	private static void initKcs() {
		charingkcs = new HashSet<Integer>();
		charingkcs.add(230218);
		charingkcs.add(230227);

	}

	private static void initdeepKcs() {

		deepkcs = new HashSet<Integer>();
		deepkcs.add(230210);
		deepkcs.add(230213);
		deepkcs.add(230214);
		deepkcs.add(200201);
		deepkcs.add(230215);
		deepkcs.add(230216);
		deepkcs.add(230217);
		// 通用
		deepkcs.add(180308);
		deepkcs.add(180309);
		deepkcs.add(180304);
		deepkcs.add(180400);
		deepkcs.add(160206);
		deepkcs.add(160205);
		deepkcs.add(170101);
		deepkcs.add(170102);
		deepkcs.add(150101);
		deepkcs.add(110103);
		deepkcs.add(110102);
		deepkcs.add(110200);
		deepkcs.add(130501);
		deepkcs.add(110200);
		deepkcs.add(120101);
		deepkcs.add(120102);

	}
}
