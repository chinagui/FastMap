package com.navinfo.dataservice.FosEngine.test;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class Test4 {

	public static class MyMapper extends Mapper<LongWritable, Text, Text, IntWritable>{
		
		private IntWritable iw = new IntWritable(1);

		@Override
		protected void map(LongWritable key, Text value, Context context)
				throws IOException, InterruptedException {
			
			context.write(value, iw);
		}
		
		
	}
	
	public static class MyReducer extends Reducer<Text,IntWritable,Text,IntWritable>{

		private IntWritable sumInt = new IntWritable();
		
		@Override
		protected void reduce(Text arg0, Iterable<IntWritable> arg1,
				Context arg2)
				throws IOException, InterruptedException {

			int sum = 0;
			
			for(IntWritable iw: arg1){
				
				sum += iw.get();
			}
			
			sumInt.set(sum);
			
			arg2.write(arg0, sumInt);
			
		}
		
		
		
	}
	
	public static void main(String[] args) throws Exception {
	
		Configuration conf = new Configuration();
		
		Job job = Job.getInstance(conf, "com1");
		
		job.setJarByClass(Test4.class);
		
		job.setMapperClass(MyMapper.class);
		
		job.setCombinerClass(MyReducer.class);
		
		job.setReducerClass(MyReducer.class);
		
		FileInputFormat.addInputPath(job, new Path(args[0]));
		
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		job.setNumReduceTasks(10);
		
		job.setMapOutputKeyClass(Text.class);
		
		job.setMapOutputValueClass(IntWritable.class);
		
		job.setOutputKeyClass(Text.class);
		
		job.setOutputValueClass(IntWritable.class);
		
		job.waitForCompletion(true);
	}

}
