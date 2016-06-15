package com.navinfo.dataservice.engine.statics.tools;

import java.util.List;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoDatabase;

public class MongoDao {
	private MongoDatabase md;

	public MongoDao(String db_name) {
		this.md = MongoManager.getInstance().getMongoInstance().getDatabase(db_name);

	}

	public MongoDatabase getDatabase() {
		
		return md;

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

}
