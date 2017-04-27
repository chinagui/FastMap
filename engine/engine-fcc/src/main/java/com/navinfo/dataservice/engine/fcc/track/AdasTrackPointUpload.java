package com.navinfo.dataservice.engine.fcc.track;

import net.sf.json.JSONObject;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

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
        String a_prjName=json.getString("prjName");
        String a_weekSeconds=json.getString("weekSeconds");
        return a_prjName + a_weekSeconds;
    }

    @Override
    public Put generatePut(JSONObject json, String rowkey) throws Exception {
        Put put = new Put(rowkey.getBytes());
        put.addColumn("attribute".getBytes(), "a_id".getBytes(),
                json.getString("id").getBytes());
        put.addColumn("attribute".getBytes(), "a_week".getBytes(),
                Bytes.toBytes(json.getInt("week")));
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
                Bytes.toBytes(json.getInt("status")));
        put.addColumn("attribute".getBytes(), "a_linkId".getBytes(),
                Bytes.toBytes(json.getInt("linkId")));
        put.addColumn("attribute".getBytes(), "a_satNum".getBytes(),
                Bytes.toBytes(json.getInt("satNum")));
        put.addColumn("attribute".getBytes(), "a_prjName".getBytes(),
                json.getString("prjName").getBytes());
        put.addColumn("attribute".getBytes(), "a_user".getBytes(),
                Bytes.toBytes(json.getInt("userId")));
        put.addColumn("attribute".getBytes(), "a_geometry".getBytes(),
                json.getString("geometry").getBytes());
        return put;
    }

    @Override
    public int getTrackType() {
        return 1;
    }
}
