package com.navinfo.dataservice.dao.photo.mass;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.dao.photo.HBaseConnector;

import net.sf.json.JSONArray;

public class HBasePoiController {
	private static final Logger logger = Logger.getLogger(HBasePoiController.class);
		
	//获得pid对应的照片rowkey
	public JSONArray getRowkeys(String pid) throws IOException {
		JSONArray array = new JSONArray();
		 Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		 
		 Table htab = null;
			try{
				htab = hbaseConn.getTable(TableName.valueOf(Bytes.toBytes(HBaseConstant.massPhotosPoiIdxTab)));
				
				Scan scan = new Scan();
				 Filter filter = new RowFilter(CompareOp.EQUAL,
						 new BinaryPrefixComparator(pid.getBytes()));
				 
				 scan.setFilter(filter);
				 scan.addColumn(Bytes.toBytes("idx"), Bytes.toBytes("poi_photo_rk"));
				 ResultScanner scanner = htab.getScanner(scan);
				 
				 for (Result rs : scanner) {
					 byte[] rowkey=rs.getValue(Bytes.toBytes("idx"), Bytes.toBytes("poi_photo_rk"));
					 array.add(Bytes.toString(rowkey));
/*					for (Cell cell : rs.rawCells()) {
						String rowkey = Bytes.toString(CellUtil.cloneValue(cell));
						array.add(rowkey);						
					}*/
				}
				 				 
			}finally{
				if (htab!=null){
					htab.close();
				}
			}	
			System.out.println(array.size());
		return array;
	}
	
	
	
	//获得照片
	public byte[] getPhotoByRowkey(String rowkey) throws Exception {
		byte[] photo = null;
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		 
		 Table htab = null;
			try{
				htab = hbaseConn.getTable(TableName.valueOf(Bytes.toBytes(HBaseConstant.massPhotosPoiTab)));
				
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
	
	//获得照片的文件名和后缀名	
	public String getPhotoNameByRowkey(String rowkey) throws IOException {
		Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		 Table htab = null;
		 String photoName = null;
		 String photoSuffix = null;
			try{
				htab = hbaseConn.getTable(TableName.valueOf(Bytes.toBytes(HBaseConstant.massPhotosPoiTab)));
				
				Get get1 = new Get(Bytes.toBytes(rowkey));
				get1.addColumn(Bytes.toBytes("attribute"), Bytes.toBytes("p_name"));
				Result result1 = htab.get(get1);				
				 for (Cell cell : result1.rawCells()) {
					 photoName = Bytes.toString(CellUtil.cloneValue(cell));
					}
				
				 Get get2 = new Get(Bytes.toBytes(rowkey));
				 get2.addColumn(Bytes.toBytes("attribute"), Bytes.toBytes("p_format"));
				Result result2 = htab.get(get2);				
				 for (Cell cell : result2.rawCells()) {
					 photoSuffix = Bytes.toString(CellUtil.cloneValue(cell));
					}
				 
				 				 
			}finally{
				if (htab!=null){
					htab.close();
				}
			}			
		
		return photoName + "." + photoSuffix;
	}
	
//	@Test
	public void testSelectPhoto() throws Exception{
		HBasePoiController hBasePoiController = new HBasePoiController();
		byte[] photoInputStream = hBasePoiController.getPhotoByRowkey("wx4u18g1j152ySHsiZJv4LXULkBQKW22");
		
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
				
			try{
				String photoNameAndSuffix = getPhotoNameByRowkey("wx4u18g1j152ySHsiZJv4LXULkBQKW22");		
				fos = new FileOutputStream("D:/test/" + photoNameAndSuffix);
				bos = new BufferedOutputStream(fos);
				bos.write(photoInputStream);
				bos.flush();
						
					}catch(Exception e){
						logger.error(e.getMessage(),e);
						throw e;
					}finally{
						try{
							if(fos!=null)fos.close();
						}catch(Exception e2){
							logger.error(e2.getMessage(),e2);
						}
						try{
							if(bos!=null)bos.close();
						}catch(Exception e2){
							logger.error(e2.getMessage(),e2);
						}
					}
							
	}
	
}
