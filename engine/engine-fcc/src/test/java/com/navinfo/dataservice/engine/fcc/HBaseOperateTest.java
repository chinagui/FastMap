package com.navinfo.dataservice.engine.fcc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.dataservice.dao.fcc.HBaseController;

/**
 * @ClassName: HBaseOperateTest.java
 * @author y
 * @date 2016-6-29下午3:43:09
 * @Description: TODO
 * 
 */
public class HBaseOperateTest {

	 static String tableName=HBaseConstant.tipTab;

	//static String tableName = HBaseConstant.trackLineTab;

	public static Configuration configuration;
	static {
		configuration = HBaseConfiguration.create();
		configuration.set("hbase.zookeeper.property.clientPort", "2181");
		configuration.set("hbase.zookeeper.quorum", "192.168.1.100");
		configuration.set("hbase.master", "192.168.1.100:600000");
	}

	/*
	 * 查询所有数据
	 * 
	 * @param tableName
	 */
	public static void QueryAll() throws IOException {
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		Table htab = hbaseConn.getTable(TableName.valueOf(tableName));
		try {
			ResultScanner rs = htab.getScanner(new Scan());
			for (Result r : rs) {
				System.out.println("获得到rowkey:" + new String(r.getRow()));
				for (KeyValue keyValue : r.raw()) {
					System.out.println("列：" + new String(keyValue.getFamily())
							+ "====值:" + new String(keyValue.getValue()));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		htab.close();
	}

	/*
	 * 单条件查询,根据rowkey查询唯一一条记录
	 * 
	 * @param tableName
	 */
	public static void QueryByCondition1(String rowkey) throws IOException {
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		Table htab = hbaseConn.getTable(TableName.valueOf(tableName));
		try {
			Get scan = new Get(rowkey.getBytes());// 根据rowkey查询
			System.out.println("开始查询啊");
			Result result = htab.get(scan);
		/*	if (result != null && result.getRow() != null) {
				System.out.println("获得到rowkey:" + new String(result.getRow()));
				for (KeyValue keyValue : result.raw()) {
					System.out.println("列：" + new String(keyValue.getFamily())
							+ "====值:" + new String(keyValue.getValue()));
				}
			}*/
			
			  List<Cell> ceList =   result.listCells();  
              Map<String,Object> map = new HashMap<String,Object>();  
              Map<String,Map<String,Object>> returnMap = new HashMap<String,Map<String,Object>>();  
              String  row = "";  
              if(ceList!=null&&ceList.size()>0){  
                    for(Cell cell:ceList){  
                     row =Bytes.toString( cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());  
                     String value =Bytes.toString( cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());  
                     String family =  Bytes.toString(cell.getFamilyArray(),cell.getFamilyOffset(),cell.getFamilyLength());  
                     String quali = Bytes.toString( cell.getQualifierArray(),cell.getQualifierOffset(),cell.getQualifierLength());  
                     map.put(family+"_"+quali, value);  
                     
                     System.out.println(family+"_"+quali+":"+value);
                    }  
                    map.put("row",row );  
                } 

		} catch (IOException e) {
			e.printStackTrace();
		}
		htab.close();
	}

	/*
	 * 单条件按查询，查询多条记录
	 * 
	 * @param tableName
	 */
	public static void QueryByCondition2() {

		try {
			Connection hbaseConn = HBaseConnector.getInstance().getConnection();
			Table htab = hbaseConn.getTable(TableName.valueOf(tableName));
			System.out.println("查询啊~~");
		/*	Filter filter = new SingleColumnValueFilter(
					Bytes.toBytes("rowkey"), null, CompareOp.EQUAL,
					Bytes.toBytes("777777777777"));// 当列column1的值为aaa时进行查询
*/			//Scan s = new Scan();
			
			Scan scan = new Scan();
			scan.addColumn(Bytes.toBytes("attribute"),Bytes.toBytes("a_uuid"));
			//scan.addFamily(Bytes.toBytes("attribute"));
			SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes("attribute"), Bytes.toBytes("a_uuid"),CompareOp.EQUAL, Bytes.toBytes("Line2"));
			filter.setFilterIfMissing(true);
			scan.setFilter(filter);
			
			//scan 'tracklines_sprint5',{COLUMN => 'attribute', FILTER =>"(SingleColumnValueFilter('attribute','attribute:a_uuid',=,'binary:uuid1'))"}

			//s.setFilter(filter);
			ResultScanner rs = htab.getScanner(scan);
			for (Result r : rs) {
				System.out.println("获得到rowkey:" + new String(r.getRow()));
				for (KeyValue keyValue : r.raw()) {
					System.out.println("列：" + new String(keyValue.getFamily())
							+ "====值:" + new String(keyValue.getValue()));
				}

			}
			
			htab.close();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}

	}
	
	
	
	/*
	 * 单条件按查询，查询多条记录
	 * 
	 * @param tableName
	 */
	public static void QueryByCondition4() {

		try {
			Connection hbaseConn = HBaseConnector.getInstance().getConnection();
			Table htab = hbaseConn.getTable(TableName.valueOf(tableName));
			System.out.println("查询啊~~");
			
			Scan scan = new Scan();
			scan.addColumn(Bytes.toBytes("data"),Bytes.toBytes("track"));
			//scan.addFamily(Bytes.toBytes("attribute"));
			
			RegexStringComparator comp = new RegexStringComparator(".],\"t_cStatus\"."); // or (\W|^)test(\W|$) if you want complete words only  
			
			//SubstringComparator comp = new SubstringComparator("test"); 

			SingleColumnValueFilter filter = new SingleColumnValueFilter(Bytes.toBytes("data"), Bytes.toBytes("track"),CompareOp.NOT_EQUAL, comp);
			//查询t_lifecycle=1
			//RegexStringComparator comp2 = new RegexStringComparator(".\"t_lifecycle\":1,."); 
			
			//RegexStringComparator comp2 = new RegexStringComparator(".\"t_lifecycle\":0,."); 
			
			//SingleColumnValueFilter filter2 = new SingleColumnValueFilter(Bytes.toBytes("data"), Bytes.toBytes("track"),CompareOp.EQUAL, comp2);
			
			filter.setFilterIfMissing(true);
			//filter2.setFilterIfMissing(true);
			//scan.setFilter(filter2);
			scan.setFilter(filter);
			
			//scan 'tracklines_sprint5',{COLUMN => 'attribute', FILTER =>"(SingleColumnValueFilter('attribute','attribute:a_uuid',=,'binary:uuid1'))"}

			//s.setFilter(filter);
			ResultScanner rs = htab.getScanner(scan);
			int count=0;
					
			for (Result r : rs) {
				System.out.println("获得到rowkey:" + new String(r.getRow()));
				for (KeyValue keyValue : r.raw()) {
					System.out.println("列：" + new String(keyValue.getFamily())
							+ "====值:" + new String(keyValue.getValue()));
				}
				
				System.out.println("获得到rowkey:" + new String(r.getRow()));
				count++;

				//update(new String(r.getRow()));
			}
			System.out.println("总条数："+count);
			
			htab.close();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}

	}

	/*
	 * 组合条件查询
	 * 
	 * @param tableName
	 */
	public static void QueryByCondition3() {

		try {
			Connection hbaseConn = HBaseConnector.getInstance().getConnection();
			Table htab = hbaseConn.getTable(TableName.valueOf(tableName));

			List<Filter> filters = new ArrayList<Filter>();

			Filter filter1 = new SingleColumnValueFilter(
					Bytes.toBytes("column1"), null, CompareOp.EQUAL,
					Bytes.toBytes("aaa"));
			filters.add(filter1);

			Filter filter2 = new SingleColumnValueFilter(
					Bytes.toBytes("column2"), null, CompareOp.EQUAL,
					Bytes.toBytes("bbb"));
			filters.add(filter2);

			Filter filter3 = new SingleColumnValueFilter(
					Bytes.toBytes("column3"), null, CompareOp.EQUAL,
					Bytes.toBytes("ccc"));
			filters.add(filter3);

			FilterList filterList1 = new FilterList(filters);

			Scan scan = new Scan();
			scan.setFilter(filterList1);
			ResultScanner rs = htab.getScanner(scan);
			for (Result r : rs) {
				System.out.println("获得到rowkey:" + new String(r.getRow()));
				for (KeyValue keyValue : r.raw()) {
					System.out.println("列：" + new String(keyValue.getFamily())
							+ "====值:" + new String(keyValue.getValue()));
				}
			}
			rs.close();
			htab.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void selectByFilter() throws IOException {
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		Table htab = hbaseConn.getTable(TableName.valueOf(tableName));
		System.out.println("开始查询");
		// Filter多条件查询，条件：查询 course列族中art列值为97 ，且 course列族中math列值为100的行
		List<String> arr = new ArrayList<String>();
		//   arr.add("course,art,97");  
		//   arr.add("course,math,100");  
		//arr.add("attribute,a_uuid,1234test");
		 arr.add("attribute,attribute:a_uuid,uuid1");
		// HBaseBasic03.selectByFilter("scores",arr);
		FilterList filterList = new FilterList();
		Scan s1 = new Scan();
		for (String v : arr) { // 各个条件之间是“与”的关系
			String[] s = v.split(",");
			filterList.addFilter(new SingleColumnValueFilter(Bytes
					.toBytes(s[0]), Bytes.toBytes(s[1]), CompareOp.EQUAL, Bytes
					.toBytes(s[2])));
			// 添加下面这一行后，则只返回指定的cell，同一行中的其他cell不返回
			//s1.addFamily(Bytes.toBytes(s[0]));
		}
		s1.addColumn(Bytes.toBytes("attribute"), Bytes.toBytes("a_uuid"));
		s1.setFilter(filterList);
		ResultScanner ResultScannerFilterList = htab.getScanner(s1);
		for (Result rr = ResultScannerFilterList.next(); rr != null; rr = ResultScannerFilterList
				.next()) {
			for (Cell kv : rr.rawCells()) {
				System.out.println("row : " + new String(kv.getRow()));
				System.out.println("column : " + new String(kv.getFamily()));
				System.out.println("value : " + new String(kv.getValue()));
				
				System.out.println("family:"+new String(kv.getFamily() 
			             + "\n=====value:"+new String(kv.getValue() )
			             + "\n=====qualifer:"+new String(kv.getQualifier() ))) ;

			}
		}
		
		htab.close();
		
		System.out.println("查询完毕");
	}

	/**
	 * 创建表
	 * 
	 * @param tableName
	 */
	public static void createTable(String tableName) {
		System.out.println("start create table ......");
		try {
			HBaseAdmin hBaseAdmin = new HBaseAdmin(configuration);
			if (hBaseAdmin.tableExists(tableName)) {// 如果存在要创建的表，那么先删除，再创建
				hBaseAdmin.disableTable(tableName);
				hBaseAdmin.deleteTable(tableName);
				System.out.println(tableName + " is exist,detele....");
			}
			HTableDescriptor tableDescriptor = new HTableDescriptor(tableName);
			tableDescriptor.addFamily(new HColumnDescriptor("column1"));
			tableDescriptor.addFamily(new HColumnDescriptor("column2"));
			tableDescriptor.addFamily(new HColumnDescriptor("column3"));
			hBaseAdmin.createTable(tableDescriptor);
		} catch (MasterNotRunningException e) {
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("end create table ......");
	}

	/**
	 * 插入数据
	 * 
	 * @param tableName
	 * @throws IOException
	 */
	public static void insertData() throws IOException {
		System.out.println("start insert data ......");
		System.out.println(tableName);
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		Table htab = hbaseConn.getTable(TableName.valueOf(tableName));
		String rowkey = "rowkey004";
		Put put = new Put(rowkey.getBytes());// 一个PUT代表一行数据，再NEW一个PUT表示第二行数据,每行一个唯一的ROWKEY，此处rowkey为put构造方法中传入的值
		JSONObject json = new JSONObject();
		json.put("a_uuid", "uuid4");
		json.put("a_startTime", "20160628");
		json.put("a_endTime", "20160629");
		json.put("a_user", 234);
		json.put("a_geometry", " { type: \"Point\", coordinates: [ 40, 5 ] } ");
		json.put("a_segmentId", 234);

		put.addColumn("attribute".getBytes(),"a_uuid".getBytes(), "uuid4".getBytes());
		put.addColumn("attribute".getBytes(),"a_startTime".getBytes(), "20160628".getBytes());
		put.addColumn("attribute".getBytes(),"a_endTime".getBytes(), "20160629".getBytes());
		put.addColumn("attribute".getBytes(),"a_user".getBytes(), "234".getBytes());
		put.addColumn("attribute".getBytes(),"a_geometry".getBytes(), "{ type: \"Point\", coordinates: [ 40, 5 ] } ".getBytes());
		put.addColumn("attribute".getBytes(),"a_segmentId".getBytes(), "234".getBytes());
		// 本行数据的第一列
	/*	put.addColumn("attribute", a_uuid, value);
		("attribute".getBytes(), "attribute".getBytes(), json
				.toString().getBytes());// 本行数据的第一列
*/		// put.add("column2".getBytes(), null, "bbb".getBytes());// 本行数据的第三列
		/*
		 * put.addColumn("data".getBytes(), "source".getBytes(), jsonSource
		 * .toString().getBytes());
		 */
		try {
			htab.put(put);
			htab.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("end insert data ......");
	}

	/**
	 * 删除一张表
	 * 
	 * @param tableName
	 */
	public static void dropTable(String tableName) {
		try {
			HBaseAdmin admin = new HBaseAdmin(configuration);
			admin.disableTable(tableName);
			admin.deleteTable(tableName);
		} catch (MasterNotRunningException e) {
			e.printStackTrace();
		} catch (ZooKeeperConnectionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 根据 rowkey删除一条记录
	 * 
	 * @param tablename
	 * @param rowkey
	 */
	public static void deleteRow(String rowkey) {
		try {
			Connection hbaseConn = HBaseConnector.getInstance().getConnection();
			Table htab = hbaseConn.getTable(TableName.valueOf(tableName));
			List list = new ArrayList();
			Delete d1 = new Delete(rowkey.getBytes());
			list.add(d1);

			htab.delete(list);
			System.out.println("删除行成功!");
			htab.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 组合条件删除
	 * 
	 * @param tablename
	 * @param rowkey
	 */
	public static void deleteByCondition(String tablename, String rowkey) {
		// 目前还没有发现有效的API能够实现 根据非rowkey的条件删除 这个功能能，还有清空表全部数据的API操作

	}

	public static void main(String[] args) throws Exception {
		HBaseOperateTest test = new HBaseOperateTest();
		String rowkey = "021606597d72114ef24985bc35dade8993e6b1";
		// test.QueryAll();
		 test.QueryByCondition1(rowkey);
		// test.QueryByCondition4();
		
	//	test.insertData();
		//test.selectByFilter();
		// test.QueryByCondition1(rowkey);
	//	test.QueryByCondition2();

		// test.QueryByCondition3();
	/*	
		HBaseController  coll=new HBaseController();
		 ArrayList<ArrayList<org.hbase.async.KeyValue>> result=null;
		 result=coll.scan(tableName, "", "", "attribute", "a_uuid");
		for (ArrayList<org.hbase.async.KeyValue> value: result) {
			for (org.hbase.async.KeyValue  kv : value) {
				System.out.println(new String(kv.value()));
			}

		}*/
	}
	
	
	
	/**
	 * 修改tips(增加三个字段)
	 * 
	 * @param rowkey
	 * @param mdFlag 
	 * @param content
	 * @return
	 * @throws Exception
	 */
	public static boolean update(String rowkey)
			throws Exception {

		Connection hbaseConn = HBaseConnector.getInstance().getConnection();

		Table htab = hbaseConn.getTable(TableName.valueOf(HBaseConstant.tipTab));

		Get get = new Get(rowkey.getBytes());

		get.addColumn("data".getBytes(), "track".getBytes());


		Result result = htab.get(get);

		if (result.isEmpty()) {
			return false;
		}

		Put put = new Put(rowkey.getBytes());

		JSONObject track = JSONObject.fromObject(new String(result.getValue(
				"data".getBytes(), "track".getBytes())));

		int lifecycle = track.getInt("t_lifecycle");

		if (0 == lifecycle) {
			track.put("t_lifecycle", 2);
		}

		JSONArray trackInfo = track.getJSONArray("t_trackInfo");
		
		
		track.put("t_cStatus", 1);
		
		track.put("t_dStatus", 0);
		
		track.put("t_mStatus", 0);
		
		String date = StringUtils.getCurrentTime();

		track.put("t_trackInfo", trackInfo);

		track.put("t_date", date);

		put.addColumn("data".getBytes(), "track".getBytes(), track.toString()
				.getBytes());

		htab.put(put);

		return true;
	}

}
