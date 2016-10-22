package com.navinfo.dataservice.engine.statics.tools;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

public class MongoDao {
	private MongoDatabase md;

	public MongoDao(String db_name) {
		this.md = MongoManager.getInstance().getMongoInstance().getDatabase(db_name);

	}

	public MongoDatabase getDatabase() {

		return md;

	}
	public void insertOne(String col_name, Document doc) {
		try {
			md.getCollection(col_name).insertOne(doc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void insertMany(String col_name, List<Document> docs) {
		try {
			md.getCollection(col_name).insertMany(docs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteMany(String col_name, Bson filter) {
		try {
			md.getCollection(col_name).deleteMany(filter);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public FindIterable<Document> find(String col_name, Bson filter) {
		try {
			if (filter == null) {
				return md.getCollection(col_name).find();
			} else {
				return md.getCollection(col_name).find(filter);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
