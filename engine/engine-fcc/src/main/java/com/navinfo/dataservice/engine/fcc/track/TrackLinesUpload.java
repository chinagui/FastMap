package com.navinfo.dataservice.engine.fcc.track;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;

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
        String segmentId = json.getString("segmentId");
        return segmentId;
    }

    @Override
    public Put generatePut(JSONObject json, String rowkey) throws Exception {
        Put put = new Put(rowkey.getBytes());
        put.addColumn("attribute".getBytes(), "a_uuid".getBytes(),
                json.getString("id").getBytes());
        put.addColumn("attribute".getBytes(), "a_startTime".getBytes(),
                json.getString("startTime").getBytes());
        put.addColumn("attribute".getBytes(), "a_endTime".getBytes(),
                Bytes.toBytes(json.getInt("endTime")));
        put.addColumn("attribute".getBytes(), "a_user".getBytes(),
                json.getString("userId").getBytes());
        put.addColumn("attribute".getBytes(), "a_geometry".getBytes(),
                json.getString("geometry").getBytes());
        put.addColumn("attribute".getBytes(), "a_segmentId".getBytes(),
                json.getString("segmentId").getBytes());
        return put;
    }

    @Override
    public int getTrackType() {
        return 0;
    }
}
