package com.navinfo.dataservice.engine.edit.edit.display;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableMapper;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class TileStat {
	
	public static class MapStat extends TableMapper<Text,IntWritable>{

		Text textKey = new Text();
		
		IntWritable intValue = new IntWritable();
		
		@Override
		protected void map(ImmutableBytesWritable key, Result value,
				Context context) throws IOException, InterruptedException {

			textKey.set(key.get());
			
			intValue.set(value.getValue("index".getBytes(), null).length);
			
			context.write(textKey, intValue);
			
		}
		
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		Configuration conf = new Configuration();
		
		conf.set("hbase.zookeeper.quorum", "hadoop-01");
		
		Job job = Job.getInstance(conf, "statistics tile size");
		
		job.setJarByClass(TileStat.class);
		
		job.setNumReduceTasks(0);
		
		Scan scan = new Scan();
		
		scan.setCacheBlocks(false);
		
		scan.setBatch(5000);
		
		TableMapReduceUtil.initTableMapperJob("link_tile",scan, MapStat.class, Text.class, IntWritable.class, job);
	
		FileOutputFormat.setOutputPath(job, new Path(args[0]));
		
		job.waitForCompletion(true);
	}

}
