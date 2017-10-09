package com.navinfo.dataservice.engine.fcc.track;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.api.es.model.TrackPoint;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.engine.fcc.tips.TipsUtils;
import com.navinfo.nirobot.common.utils.GeometryConvertor;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: TrackLinesUpload.java
 * @author y
 * @date 2016-6-29下午2:03:48
 * @Description: 轨迹上传，入hbase库
 * 
 */
public class TrackLinesUpload extends TrackUpload{
    public static final int TRACK_POINT_SRC = 1;

    @Override
    public String getSourceRowkey(JSONObject json) {
        String a_prjName=json.getString("prjName");
        String a_weekSeconds=json.getString("weekSeconds");
        return TRACK_POINT_SRC + a_prjName + a_weekSeconds;
    }

    @Override
    public Put generatePut(JSONObject json, String rowkey, List trackIdxList) throws Exception {
        Put put = this.getPut(json, rowkey);
        TrackPoint point = this.getTrackIdx(json, rowkey);
        trackIdxList.add(point);
        return put;
    }

    private Put getPut(JSONObject json, String rowkey){
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
                Bytes.toBytes(json.getInteger("userId")));
        put.addColumn("attribute".getBytes(), "a_deviceNum".getBytes(),
                json.getString("deviceNum").getBytes());
        put.addColumn("attribute".getBytes(), "a_hdop".getBytes(),
                Bytes.toBytes(json.getDouble("hdop")));
        put.addColumn("attribute".getBytes(), "a_height".getBytes(),
                Bytes.toBytes(json.getDouble("altitude")));
        put.addColumn("attribute".getBytes(), "a_posType".getBytes(),
                Bytes.toBytes(json.getInteger("posType")));
        put.addColumn("attribute".getBytes(), "a_satNum".getBytes(),
                Bytes.toBytes(json.getInteger("satNum")));
        put.addColumn("attribute".getBytes(), "a_mediaFlag".getBytes(),
                Bytes.toBytes(json.getInteger("mediaFlag")));
        put.addColumn("attribute".getBytes(), "a_linkId".getBytes(),
                Bytes.toBytes(json.getInteger("linkId")));
        put.addColumn("attribute".getBytes(), "a_prjName".getBytes(),
                json.getString("prjName").getBytes());
        put.addColumn("attribute".getBytes(), "a_geometry".getBytes(),
                json.getString("geometry").getBytes());
        //20170927新增
        put.addColumn("attribute".getBytes(), "a_src".getBytes(),
                "1".getBytes());
        put.addColumn("attribute".getBytes(), "a_plateNum".getBytes(),
                json.getString("plateNum").getBytes());
        return put;
    }

    private TrackPoint getTrackIdx(JSONObject json, String rowkey) throws Exception {
        TrackPoint point = new TrackPoint();
        point.setId(rowkey);
        String wkt = json.getString("geometry");
        point.setA_geometry(GeoTranslator.jts2Geojson(GeoTranslator.wkt2Geometry(wkt)));
        point.setA_linkId(json.getInteger("linkId"));
        point.setA_user(json.getInteger("userId"));
        point.setA_recordTime(json.getString("recordTime").substring(0,14));
        return point;
    }

    @Override
    public int getTrackType() {
        return 0;
    }
}
