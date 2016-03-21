package com.navinfo.dataservice.engine.edit.edit.display;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;

import com.navinfo.dataservice.commons.mercator.MercatorProjection;

public class PhotoTileBuilder {

	public static class Mapper extends
			TableMapper<ImmutableBytesWritable, ImmutableBytesWritable> {

		@Override
		public void map(ImmutableBytesWritable row, Result values,
				Context context) throws IOException {
			
			String attr = new String(values.getValue(
					"data".getBytes(), "attribute".getBytes()));
			
			
			
			JSONObject jsonAttr = null;
			try {
				jsonAttr = JSONObject.fromObject(attr);
			} catch (Exception e1) {
				System.out.println(attr);
				
				throw new IOException(e1);
			}

			double longitude = jsonAttr.getDouble("a_longitude");

			double latitude = jsonAttr.getDouble("a_latitude");

			for (byte zoom = 7; zoom <= 16; zoom++) {
				
				long tileX = MercatorProjection.longitudeToTileX(longitude,
						zoom);

				long tileY = MercatorProjection.latitudeToTileY(latitude, zoom);

				double px = MercatorProjection.longitudeToPixelX(longitude,
						zoom);

				double py = MercatorProjection.latitudeToPixelY(latitude, zoom);

				double tilePx = MercatorProjection.tileXToPixelX(tileX);

				double tilePy = MercatorProjection.tileYToPixelY(tileY);

				int x = (int) (px - tilePx);

				int y = (int) (py - tilePy);

				String tile = String.format("%02d", zoom)
						+ String.format("%08d", tileX)
						+ String.format("%07d", tileY);

				String photo = String.format("%03d", x)
						+ String.format("%03d", y);

				try {
					context.write(new ImmutableBytesWritable(tile.getBytes()),
							new ImmutableBytesWritable(photo.getBytes()));
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new IOException(e);
				}
			}

		}
	}

	public static class Reducer
			extends
			TableReducer<ImmutableBytesWritable, ImmutableBytesWritable, ImmutableBytesWritable> {

		@Override
		public void reduce(ImmutableBytesWritable key,
				Iterable<ImmutableBytesWritable> values, Context context)
				throws IOException, InterruptedException {
			
			Map<String,Integer> map = new HashMap<String,Integer>();
			
			for(int i=0;i<256;i++){
				for(int j=0;j<256;j++){
					map.put(String.format("%03d", i) + String.format("%03d", j), 0);
				}
			}
			
			Put put = new Put(key.get());

			for (ImmutableBytesWritable val : values) {

				String value = new String(val.get());
				
				map.put(value, map.get(value)+1);

			}
			
			String rowkey = new String(key.get());
			
			byte zoom = Byte.parseByte(rowkey.substring(0, 2));
			
			int tileX = Integer.parseInt(rowkey.substring(2, 10));
			
			int tileY = Integer.parseInt(rowkey.substring(10));
			
			Iterator<String> it = map.keySet().iterator();
			
			JSONArray ja = new JSONArray();
			
			while(it.hasNext()){
				String tmpKey = it.next();
				
				if (map.get(tmpKey) > 0){
					
					JSONObject json = new JSONObject();
					
					json.put("o", MercatorProjection.tileXToLongitude(tileX, zoom));
					
					json.put("a", MercatorProjection.tileYToLatitude(tileY, zoom));
					
					json.put("t", map.get(tmpKey));
					
					ja.add(json);
				}
			}
			
			
			put.addColumn("data".getBytes(), "photo".getBytes(), ja.toString().getBytes());
			
			context.write(key, put);
		}

	}

	public static void main(String[] args) throws Exception {

		Configuration conf = HBaseConfiguration.create();
		
		conf.set("hbase.zookeeper.quorum", args[0]);

		createTable(conf);
		
		conf.set("mapred.child.java.opts", "-Xmx1024m");

		Job job = Job.getInstance(conf, "photo tile builder");
		
		job.setPartitionerClass(HashPartitioner.class);

		job.setNumReduceTasks(10);

		job.setJarByClass(PhotoTileBuilder.class);

		Scan scan = new Scan();
		
		scan.addColumn("data".getBytes(), "attribute".getBytes());
		
		TableMapReduceUtil
				.initTableMapperJob("photo", scan, Mapper.class,
						ImmutableBytesWritable.class,
						ImmutableBytesWritable.class, job);
		TableMapReduceUtil.initTableReducerJob("photoTile", Reducer.class, job);
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

	private static void createTable(Configuration conf) throws Exception {
		Connection conn = ConnectionFactory.createConnection(conf);

		Admin admin = conn.getAdmin();

		TableName tableName = TableName.valueOf("photoTile");

		if (!admin.tableExists(tableName)) {
			HTableDescriptor htd = new HTableDescriptor(tableName);

			HColumnDescriptor hcd = new HColumnDescriptor("data");

			htd.addFamily(hcd);

			admin.createTable(htd);
		}
	}

}
