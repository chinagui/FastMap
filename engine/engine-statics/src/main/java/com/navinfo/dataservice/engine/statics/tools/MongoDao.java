package com.navinfo.dataservice.engine.statics.tools;

import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.navinfo.dataservice.commons.log.LoggerRepos;

public class MongoDao {
	protected Logger log = LoggerRepos.getLogger(this.getClass());
	private MongoDatabase md;

	public MongoDao(String db_name) {
		this.md = MongoManager.getInstance().getMongoInstance().getDatabase(db_name);

	}

	public MongoDatabase getDatabase() {

		return md;

	}
	public void updateOne(String col_name, Bson filter,Bson doc,boolean upsert){
		try {
			UpdateOptions updateOpt = new UpdateOptions();
			updateOpt.upsert(upsert);
			BasicDBObject update = new BasicDBObject("$set",doc);  
			md.getCollection(col_name).updateOne(filter, update,updateOpt);
		} catch (Exception e) {
			log.error("updateOne mongo error", e);
			throw e;
		}
	}
	public void insertOne(String col_name, Document doc) {
		try {
			md.getCollection(col_name).insertOne(doc);
		} catch (Exception e) {
			log.error("insertOne mongo error", e);
			throw e;
		}
	}
	public void insertMany(String col_name, List<Document> docs) {
		try {
			if(docs==null||docs.size()==0){return;}
			md.getCollection(col_name).insertMany(docs);
		} catch (Exception e) {
			log.error("insertMany mongo error", e);
			throw e;
		}
	}

	public void deleteMany(String col_name, Bson filter) {
		try {
			DeleteResult dr =md.getCollection(col_name).deleteMany(filter);
		} catch (Exception e) {
			log.error("deleteMany mongo error", e);
			throw e;
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

	public void createIndex(String col_name, Document keys) {
		try {
			md.getCollection(col_name).createIndex(keys);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public long count(String col_name, Bson filter) {
		try {
			if (filter == null) {
				return md.getCollection(col_name).count();
			} else {
				return md.getCollection(col_name).count(filter);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

}
