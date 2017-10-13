package com.navinfo.dataservice.dao.photo.mass;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.hbase.async.GetRequest;
import org.hbase.async.KeyValue;

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.util.ByteUtils;
import com.navinfo.dataservice.dao.photo.HBaseConnector;

import net.sf.json.JSONArray;


public class HbaseTipsController {
	private static final Logger logger = Logger.getLogger(HbaseTipsController.class);
	
	//根据rowkey获得FCC库中的照片
	public byte[] getFccPhotoByRowkey(String rowkey) throws Exception {

		List<KeyValue> list = getByRowkey("photo", rowkey, "data", "origin");

		for (KeyValue kv : list) {
			return kv.value();
		}

		return new byte[0];
	}
	
	//根据旧的rowkey（url_db）查询获得photo的新rowkey
	public String getNewRowkey4Photo(String oldRowkey) throws IOException {
		
		 Connection hbaseConn = HBaseConnector.getInstance().getConnection();	 
		 Table htab = null;
			try{
				htab = hbaseConn.getTable(TableName.valueOf(Bytes.toBytes(HBaseConstant.massPhotosTipsIdxTab)));
				Get get = new Get(Bytes.toBytes(oldRowkey));
				Result result = htab.get(get);					 				 
				String newRowkey = Bytes.toString(result.getValue(Bytes.toBytes("idx"), Bytes.toBytes("photos_tips_rk")));				 				 
				return newRowkey;
			}finally{
				if (htab!=null){
					htab.close();
				}
/*				if (hbaseConn!=null){
					hbaseConn.close();
				}*/
			}			
	}
	
	
	//根据rowkey获得照片
	public byte[] getPhotoByRowkey(String rowkey) throws Exception {
		byte[] photo = null;
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		 
		 Table htab = null;
			try{
				htab = hbaseConn.getTable(TableName.valueOf(Bytes.toBytes(HBaseConstant.massPhotosTipsTab)));
				
				Get get = new Get(Bytes.toBytes(rowkey));
				get.addColumn(Bytes.toBytes("origin"), Bytes.toBytes("o_photo"));
				Result result = htab.get(get);
				
				 for (Cell cell : result.rawCells()) {
						photo = CellUtil.cloneValue(cell);
					}
				 				 
			}finally{
				if (htab!=null){
					htab.close();
				}
			}	

		return photo;
	}
	
	//根据Geohash过滤查询到photoRowkey
	public JSONArray getPhotoRowkeyByGeohash(String geohash) throws IOException {
		JSONArray array = new JSONArray();
		 Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		 
		 Table htab = null;
			try{
				htab = hbaseConn.getTable(TableName.valueOf(Bytes.toBytes(HBaseConstant.massPhotosTipsTab)));
				
				Scan scan = new Scan();
				 Filter filter = new RowFilter(CompareOp.EQUAL,
						 new BinaryPrefixComparator(geohash.getBytes()));
				 
				 scan.setFilter(filter);
				 ResultScanner scanner = htab.getScanner(scan);
				 
				 for (Result rs : scanner) {
					 byte[] rowkey=rs.getRow();
					 array.add(Bytes.toString(rowkey));
				}
				 				 
			}finally{
				if (htab!=null){
					htab.close();
				}
			}	
			System.out.println(array.size());
		return array;
		
	}
	
	public ArrayList<KeyValue> getByRowkey(String tableName, String rowkey,
			String family, String... qualifiers) throws Exception {

		final GetRequest get = new GetRequest(tableName, rowkey);

		if (family != null) {
			get.family(family);
		}

		if (qualifiers.length > 0) {
			get.qualifiers(ByteUtils.toBytes(qualifiers));
		}

		ArrayList<KeyValue> list = HBaseConnector.getInstance().getClient()
				.get(get).joinUninterruptibly();

		return list;
	}
	
//	@Test
	public void testGeoHash() throws IOException{
		JSONArray array= getPhotoRowkeyByGeohash("w");
		System.out.println(array);
		for (Object object : array) {
			System.out.println(object);
		}
	}
	
	public void testGetNewRowkey() throws IOException {
		String string = getNewRowkey4Photo("/DCS/PHOTO/DSCN0005.9241e72e-1122-4625-b3ed-fdf37b410af8.jpg");
		
	}
	
//	@Test
	public void testFcc() throws Exception {
		String rowkey = "00002f083eec4f0a8bec52407f38de4e";
		HbaseTipsController hbaseTipsController = new HbaseTipsController();
		byte[] photo = hbaseTipsController.getFccPhotoByRowkey(rowkey);
		
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("D:/test/2017/123.jpg"));
		bos.write(photo);
		bos.flush();
		bos.close();
	}
	
//	@Test
	public void testSelect() throws Exception{
		String rowkey = "7zzzzzzzz1721Rvlq8tlxQNOewFNfut5";
		getPhotoByRowkey(rowkey);
	}
	
}
