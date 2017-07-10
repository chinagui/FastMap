package com.navinfo.dataservice.scripts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;

import net.sf.json.JSONObject;

public class HbasePoiInfo {
	private static Logger log = LoggerRepos.getLogger(HbasePoiInfo.class);
	
    private Map<String,List<PoiInfo>> poiCollectionByMesh = new HashMap<>();
    
    public Map<String,List<PoiInfo>> getPoiCollectionByMesh(){
    	return this.poiCollectionByMesh;
    }
	
	public void getHBaseDataInfo() throws Exception {
		Connection hbaseConn = null;
		Table htab = null;

		// TODO:取到的poi数据<读取hadoop库，遍历，限制阈值，分页遍历10万条数据截取一次，用完之后，内存数据清空>
		try {
			hbaseConn = HBaseConnector.getInstance().getConnection();
			htab = hbaseConn.getTable(TableName.valueOf("poi"));

			ResultScanner rs = htab.getScanner(new Scan());
			for (Result r : rs) {
				log.info("获得到rowkey:" + new String(r.getRow()));

				JSONObject poiObj = JSONObject
						.fromObject(new String(r.getValue("attribute".getBytes(), "history".getBytes())));
				
				PoiInfo poiInfo = resolveResultInfo(poiObj);
				String meshId = poiInfo.getMeshId();
				log.info("获取poi信息：" + poiInfo);

				if (poiCollectionByMesh.containsKey(meshId)) {
					poiCollectionByMesh.get(meshId).add(poiInfo);
				} else {
					List<PoiInfo> poiCollection = new ArrayList<>();
					poiCollection.add(poiInfo);
					poiCollectionByMesh.put(meshId, poiCollection);
				}
			}//for
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			htab.close();
			hbaseConn.close();
		}
	}
	
	/**
	 * hadoop一条数据转换成PoiInfo对象
	 * @param poiObj
	 * @return
	 */
	private PoiInfo resolveResultInfo(JSONObject poiObj){
		PoiInfo poiInfo = new PoiInfo();

		JSONObject attribute = poiObj.getJSONObject("attribute");
		JSONObject history = poiObj.getJSONObject("history");
		JSONObject verifyFlags = history.getJSONObject("verifyFlags");
		JSONObject sourceFlags = history.getJSONObject("sourceFlags");
		
		poiInfo.setPid(attribute.getInt("pid"));
		poiInfo.setFieldVerification(attribute.getInt("fieldVerification"));
		poiInfo.setVerifyRecord(GetVerRecord(verifyFlags.getString("record")));
		poiInfo.setSourceRecord(GetSrcRecord(sourceFlags.getString("record")));
		poiInfo.setMeshId(attribute.getString("meshid"));
		
		return poiInfo;
	}
	
	private int GetVerRecord(String verifyRecord) {
		int verRecord = 0;
		switch (verifyRecord) {
		case "010000020001":
			verRecord = 1;
			break;
		case "010000060001":
			verRecord = 2;
			break;
		case "010000040001":
		case "010000050001":
			verRecord = 3;
			break;
		case "010000010001":
			verRecord = 4;
			break;
		case "010000030001":
			verRecord = 5;
			break;
		default:
			break;
		}
		return verRecord;
	}
	
	private int GetSrcRecord(String sourceRecord){
		if(sourceRecord==null||sourceRecord.isEmpty()){
			return 0;
		}
		
		int srcRecord = 1;
		switch (sourceRecord) {
		case "001000010000":
			srcRecord = 1;
			break;
		case "001000020000":
		case "001000030000":
		case "001000030001":
		case "001000030003":
		case "001000030004":
			srcRecord = 3;
			break;
		case "0010001300000":
			srcRecord = 2;
			break;
		case "001000040000":
			srcRecord = 4;
			break;
		case "001000140000":
			srcRecord = 5;
			break;
		default:
			break;
		}
		return srcRecord;			
	}
	
	public void clearCollection(){
		poiCollectionByMesh.clear();
	}
}
