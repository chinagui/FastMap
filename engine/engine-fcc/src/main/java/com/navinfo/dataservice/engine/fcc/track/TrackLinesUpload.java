package com.navinfo.dataservice.engine.fcc.track;

import net.sf.json.JSONObject;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * @ClassName: TrackLinesUpload.java
 * @author y
 * @date 2016-6-29下午2:03:48
 * @Description: 轨迹上传，入hbase库
 * 
 */
public class TrackLinesUpload extends TrackUpload{
    @Override
    public String getSourceRowkey(JSONObject json) {
        String a_prjName=json.getString("prjName");
        String a_weekSeconds=json.getString("weekSeconds");
        return a_prjName + a_weekSeconds;
    }

    @Override
    public Put generatePut(JSONObject json, String rowkey) throws Exception {
        Put put = new Put(rowkey.getBytes());
        put.addColumn("attribute".getBytes(), "a_id".getBytes(),
                json.getString("id").getBytes());
        put.addColumn("attribute".getBytes(), "a_weekSeconds".getBytes(),
                Bytes.toBytes(json.getDouble("weekSeconds")));
        put.addColumn("attribute".getBytes(), "a_direction".getBytes(),
                Bytes.toBytes(json.getDouble("direction")));
        put.addColumn("attribute".getBytes(), "a_speed".getBytes(),
                Bytes.toBytes(json.getDouble("speed")));
        put.addColumn("attribute".getBytes(), "a_recordTime".getBytes(),
                json.getString("recordTime").getBytes());
        put.addColumn("attribute".getBytes(), "a_user".getBytes(),
                Bytes.toBytes(json.getInt("userId")));
        put.addColumn("attribute".getBytes(), "a_deviceNum".getBytes(),
                json.getString("deviceNum").getBytes());
        put.addColumn("attribute".getBytes(), "a_hdop".getBytes(),
                Bytes.toBytes(json.getDouble("hdop")));
        put.addColumn("attribute".getBytes(), "a_height".getBytes(),
                Bytes.toBytes(json.getDouble("altitude")));
        put.addColumn("attribute".getBytes(), "a_posType".getBytes(),
                Bytes.toBytes(json.getInt("posType")));
        put.addColumn("attribute".getBytes(), "a_satNum".getBytes(),
                Bytes.toBytes(json.getInt("satNum")));
        put.addColumn("attribute".getBytes(), "a_mediaFlag".getBytes(),
                Bytes.toBytes(json.getInt("mediaFlag")));
        put.addColumn("attribute".getBytes(), "a_linkId".getBytes(),
                Bytes.toBytes(json.getInt("linkId")));
        put.addColumn("attribute".getBytes(), "a_prjName".getBytes(),
                json.getString("prjName").getBytes());
        put.addColumn("attribute".getBytes(), "a_geometry".getBytes(),
                json.getString("geometry").getBytes());
        return put;
    }

    @Override
    public int getTrackType() {
        return 0;
    }
}
