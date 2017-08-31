package com.navinfo.dataservice.engine.fcc.track;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.commons.util.DateUtils;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.Date;
import java.util.List;

import java.util.List;

/**
 * @ClassName: AdasTrackPointUpload.java
 * @author y
 * @date 2017-4-9 下午4:06:07
 * @Description: adas轨迹点上传
 *
 */
public class AdasTrackPointUpload extends TrackUpload{

    @Override
    public String getSourceRowkey(JSONObject json) {
//        String a_prjName=json.getString("prjName");
//        String a_weekSeconds=json.getString("weekSeconds");
        String timestamp = DateUtils.dateToString(new Date(), "yyyyMMdd");
        String uuid = json.getString("id");
        return timestamp + uuid;
    }

    @Override
    public Put generatePut(JSONObject json, String rowkey, List trackIdxList) throws Exception {
        Put put = new Put(rowkey.getBytes());
        put.addColumn("attribute".getBytes(), "a_id".getBytes(),
                json.getString("id").getBytes());
        put.addColumn("attribute".getBytes(), "a_week".getBytes(),
                Bytes.toBytes(json.getInteger("week")));
        put.addColumn("attribute".getBytes(), "a_weekSeconds".getBytes(),
                Bytes.toBytes(json.getDouble("weekSeconds")));
        put.addColumn("attribute".getBytes(), "a_height".getBytes(),
                Bytes.toBytes(json.getDouble("height")));
        put.addColumn("attribute".getBytes(), "a_nV".getBytes(),
                Bytes.toBytes(json.getDouble("nV")));
        put.addColumn("attribute".getBytes(), "a_eV".getBytes(),
                Bytes.toBytes(json.getDouble("eV")));
        put.addColumn("attribute".getBytes(), "a_uV".getBytes(),
                Bytes.toBytes(json.getDouble("uV")));
        put.addColumn("attribute".getBytes(), "a_roll".getBytes(),
                Bytes.toBytes(json.getDouble("roll")));
        put.addColumn("attribute".getBytes(), "a_pitch".getBytes(),
                Bytes.toBytes(json.getDouble("pitch")));
        put.addColumn("attribute".getBytes(), "a_azimuth".getBytes(),
                Bytes.toBytes(json.getDouble("azimuth")));
        put.addColumn("attribute".getBytes(), "a_status".getBytes(),
                Bytes.toBytes(json.getInteger("status")));
//        20170513 取消linkId
//        put.addColumn("attribute".getBytes(), "a_linkId".getBytes(),
//                Bytes.toBytes(json.getInt("linkId")));
        put.addColumn("attribute".getBytes(), "a_satNum".getBytes(),
                Bytes.toBytes(json.getInteger("satNum")));
        put.addColumn("attribute".getBytes(), "a_prjName".getBytes(),
                json.getString("prjName").getBytes());
        put.addColumn("attribute".getBytes(), "a_user".getBytes(),
                Bytes.toBytes(json.getInteger("userId")));
        put.addColumn("attribute".getBytes(), "a_processed".getBytes(),
                Bytes.toBytes(0));
        put.addColumn("attribute".getBytes(), "a_geometry".getBytes(),
                json.getString("geometry").getBytes());
        return put;
    }

    @Override
    public int getTrackType() {
        return 1;
    }
}
