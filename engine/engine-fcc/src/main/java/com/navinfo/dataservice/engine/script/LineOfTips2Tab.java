package com.navinfo.dataservice.engine.script;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.hadoop.hbase.TableName;
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
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.constant.HBaseConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.JtsGeometryFactory;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.fcc.HBaseConnector;
import com.navinfo.navicommons.database.sql.OracleConnectionManager;
import com.navinfo.navicommons.geo.computation.MeshUtils;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 测线Tips转Tab
 * @author SGQ
 *
 */
public class LineOfTips2Tab {
	private static final Logger logger = Logger.getLogger(LineOfTips2Tab.class);
	static ClassPathXmlApplicationContext context =null;
	static {
		//初始化 
		initContext();
	}
	//初始化	
		private static void initContext() {
			
			context = new ClassPathXmlApplicationContext(
					new String[] {"dubbo-app-scripts.xml","dubbo-scripts.xml"});
						
			context.start();
			new ApplicationContextUtil().setApplicationContext(context);
			
		}	
	
	public static void main(String[] args) throws Exception {
		
		int tipsCount = 0;
		
		 if (args.length == 0) {
	            System.out.println("请输入配置文件路径！");
	            System.exit(0); 
	        }
        Properties props = new Properties();
        
        try {
			props.load(new FileInputStream(args[0]));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	    
        // 母库信息
        String metadata_db_ip = props.getProperty("metadata_db_ip");
        String metadata_db_sid = props.getProperty("metadata_db_sid");
        String metadata_db_username = props.getProperty("metadata_db_username");
        String metadata_db_password = props.getProperty("metadata_db_password");
        
        
        // 需要转换的Tips范围，根据province查询获得meshid,province可以是多个
        String provinces = new String(props.getProperty("province").getBytes("ISO8859-1"), "UTF-8"); 
        
        //测试是否获取到province
        //System.err.println("输入的范围参数为" + provinces);
              	       
        if (StringUtils.isEmpty(metadata_db_ip)) {
        	logger.error("没有输入metadata_db_ip参数，请输入");
            return;
        }
        if (StringUtils.isEmpty(metadata_db_sid)) {
        	logger.error("没有输入metadata_db_service_name参数，请输入");
            return;
        }
        if (StringUtils.isEmpty(metadata_db_username)) {
        	logger.error("没有输入metadata_db_username参数，请输入");
            return;
        }
        if (StringUtils.isEmpty(metadata_db_password)) {
        	logger.error("没有输入metadata_db_password参数，请输入");
            return;
        }
        
        
        
        String filePath = "tab";
		File file = new File(filePath);
    	if (!file.exists()) {
    		file.mkdirs();
		}
    	filePath += "/tipsOfLine.mif";
		FileWriter fw = new FileWriter(filePath, true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("Version   650" + "\r\n");
        bw.write("Charset \"WindowsSimpChinese\"" + "\r\n");
        bw.write("Delimiter \",\"" + "\r\n");
        bw.write("CoordSys Earth Projection 1, 104" + "\r\n");
        bw.write("Columns 9" + "\r\n");
        bw.write("  ID Char(254)" + "\r\n");
        bw.write("  GEO Char(254)" + "\r\n");
        bw.write("  SRC Char(254)" + "\r\n");
        bw.write("  LN Char(254)" + "\r\n");
        bw.write("  KIND Char(254)" + "\r\n");
        bw.write("  LEN Char(254)" + "\r\n");
        bw.write("  SHP Char(254)" + "\r\n");
        bw.write("  CONS Char(254)" + "\r\n");
        bw.write("  TIME Char(254)" + "\r\n");
        bw.write("Data" + "\r\n" + "\r\n");
        
        bw.close();
        fw.close();
        
        Table hbaseTipsTable = getHbaseTipsTable();
        
        if (StringUtils.isEmpty(provinces)) {
        	logger.debug("没有输入province参数，即将提取所有省份符合要求的测线tips数据");
        	long startTime = System.currentTimeMillis();
        	List<String> rowkeyArray = getTipsRowkey();
//        	logger.debug("过滤查询到的测线tips条数：" + rowkeyArray.size());
        	for (String rowkey : rowkeyArray) {
        		Map<String, String> tipsInfo = getTipsInfo(hbaseTipsTable,rowkey);
    			if(tipsInfo.isEmpty()){
    				continue;
    			}
    			writeToMid(tipsInfo);
    			writeToMif(tipsInfo);
    			tipsCount++;
			}
//        	logger.debug("本次参加转tab的tips共有：" + tipsCount + "条");
        	long endTime = System.currentTimeMillis();
        	logger.debug("本次任务已经完成，总共用时：" + (endTime-startTime) + "毫秒");
    		
    		 try {
					if(hbaseTipsTable!=null) hbaseTipsTable.close();
				} finally {
					hbaseTipsTable.close();
				}   
    		
        }else{
        	
        	 List<String> provinceList = new ArrayList<>(Arrays.asList(provinces
        	           .split(",")));
        	 
        	 Connection oracleConn = getConnection(metadata_db_ip,metadata_db_sid,metadata_db_username,metadata_db_password);
        	 
        	 Connection tipsIdxConn = getTipsIdxConnection();
        	 
        	 long startTime = System.currentTimeMillis();
        	 Set<String> tipsRowkey = new HashSet<>();
        	// 根据输入的province逐个提取对应的信息。
		        for (String province : provinceList) {
		    		String wkt = LineOfTips2Tab.getMeshNums(oracleConn,province);
		    		Set<String> rowkeyArray = getTipsRowkey(tipsIdxConn,wkt);
		    		
//		    		logger.debug("本次从tips索引库查询到" + province + "符合条件的测线tips条数为：" + rowkeyArray.size() + "条");
		    		
		    		for (String rowkey : rowkeyArray) {
		    			tipsRowkey.add(rowkey);		    			
		    		}		    								       											
				}
		        
		        for (String rowkey : tipsRowkey){
		        	Map<String, String> tipsInfo = getTipsInfo(hbaseTipsTable,rowkey);
	    			if(tipsInfo.isEmpty()){
	    				continue;
	    			}
	    			
	    			writeToMid(tipsInfo);
	    			writeToMif(tipsInfo);
	    			tipsCount++;
		        }
		        
//		        logger.debug("本次参加转tab的tips共有：" + tipsCount + "条");			
	    		long endTime = System.currentTimeMillis();
	    		logger.debug("本次任务已经完成，总共用时：" + (endTime-startTime) + "毫秒");
		        
		        try {
					if(oracleConn!=null) oracleConn.close();
					if(tipsIdxConn!=null) tipsIdxConn.close();
					if(hbaseTipsTable!=null) hbaseTipsTable.close();
				} finally {
					oracleConn.close();
					tipsIdxConn.close();
					hbaseTipsTable.close();
				}     
        	
        }
        
        	        	              
        System.exit(0);
		
	}
	
	//1.根据输入的数据库信息获取连接（sc_partition_meshlist）
	public static Connection getConnection(String ip,String sid,String username,String password) throws Exception {
		
		long startOracle = System.currentTimeMillis();
		Connection oracleConn  = OracleConnectionManager.getConnection(ip, sid, username, password);
		long endOracle = System.currentTimeMillis();
		logger.debug("获取配置数据库连接用时：" + (endOracle - startOracle) + "毫秒");	
		logger.debug("=============================DBINFO==========================");
		logger.debug("url:jdbc:oracle:thin:@" + ip + ":1521/" + sid);
		logger.debug("username:" + username);
		logger.debug("password:" + password);
		return oracleConn;
	}
	
	//2.获取tips索引库的数据库连接
	public static Connection getTipsIdxConnection() throws Exception {
		
		long startOracle = System.currentTimeMillis();
		Connection tipsIdxConn = DBConnector.getInstance().getTipsIdxConnection();
		long endOracle = System.currentTimeMillis();
		logger.debug("获取tips索引库连接用时：" + (endOracle - startOracle) + "毫秒");			
		return tipsIdxConn;
	}
	
	//3.获取hbase库的数据库连接
	public static Table getHbaseTipsTable() throws Exception {
		
		long startOracle = System.currentTimeMillis();
		org.apache.hadoop.hbase.client.Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		long endOracle = System.currentTimeMillis();
		logger.debug("获取hbase数据库连接用时：" + (endOracle - startOracle) + "毫秒");
		
		Table htab = hbaseConn.getTable(TableName.valueOf(Bytes.toBytes(HBaseConstant.tipTab)));
				
		return htab;
	}
	
	//4.根据输入的province参数，获得要转换的Tips所在的图幅号,直接将图幅号转为wkt
	public static String getMeshNums(Connection conn,String province) throws Exception {
		String sql = "SELECT MESH FROM SC_PARTITION_MESHLIST WHERE PROVINCE = ?";	
		
		PreparedStatement pps = null;
		if(StringUtils.isEmpty(province)){
			sql = "SELECT MESH FROM SC_PARTITION_MESHLIST";
			pps = conn.prepareStatement(sql);
		}else{
			pps = conn.prepareStatement(sql);
			pps.setString(1, province);
		}
		
		ResultSet result = pps.executeQuery();
		
		Set<String> meshList = new HashSet<>();
		
		//判断查询结果是否为空,不为空则执行；为空则提示，并退出程序。
		if (result.next()){
			//此处判断时已将指针下移了一位，必须先将该值取出放入集合中，否则下面while (result.next()) 执行时，缺少第一行记录
			meshList.add(result.getString(1));		
			result.setFetchSize(10000);
			 while (result.next()) {
				 meshList.add(result.getString(1));			 
			}
//			 logger.debug("获取到的图幅号个数为：" + meshList.size());
			
		}else{
			logger.error("输入的province参数不正确，请重新输入。错误参数值为：" + province);
			System.exit(0);
		}
				
		Geometry meshes2Jts = MeshUtils.meshes2Jts(meshList);
		String wkt =JtsGeometryFactory.writeWKT(meshes2Jts);
		
		try {
			if(pps!=null) pps.close();
		} finally {
			pps.close();
		}
		
		return wkt;
	}
	
	//5.根据1中获得的wkt到tips索引库查询对应的测线（s_sourcetype=2001）tips的rowkey
	public static Set<String> getTipsRowkey(Connection tipsIdxConn,String wkt) throws Exception{       
		String isWkt = " sdo_filter(wkt,sdo_geometry(:1,8307)) = 'TRUE' ";
		String sql = "SELECT ID FROM TIPS_INDEX WHERE " + isWkt + " AND S_SOURCETYPE = '2001'";
		PreparedStatement pps = tipsIdxConn.prepareStatement(sql);
		pps.setClob(1, ConnectionUtil.createClob(tipsIdxConn, wkt));
		ResultSet result = pps.executeQuery();
		result.setFetchSize(5000);
		Set<String> rowkeyArray = new HashSet<>();
		while(result.next()){
			rowkeyArray.add(result.getString(1));
		}
		
		try {
			if(pps!=null) pps.close();
		} finally {
			pps.close();
		}
		
		return rowkeyArray;
	}
	//6.根据2中查询到的rowkey到hbase查询，判断 deep中src值，如果为0或2，则获取Tips的信息（geometry的g_location和deep），否则不获取    
	public static Map<String, String> getTipsInfo(Table htab,String rowkey) throws Exception{
			try{
				
				Get get = new Get(Bytes.toBytes(rowkey));
				Result result = htab.get(get);
				
				String data = Bytes.toString(result.getValue(Bytes.toBytes("data"), Bytes.toBytes("deep")));
				JSONObject dataObject = JSONObject.fromObject(data);
				if(!dataObject.containsKey("src")){
					return new HashMap<>();
				}
				int src = dataObject.getInt("src");
				if (!(0==src || 2==src)){
					return new HashMap<>();
				}else{
				
					String geometry = Bytes.toString(result.getValue(Bytes.toBytes("data"), Bytes.toBytes("geometry")));
					if(StringUtils.isEmpty(data) || StringUtils.isEmpty(geometry)){
						return new HashMap<>();
					}
					Map<String, String> tipsInfo = new HashMap<>();
					tipsInfo.put("data", data);
					tipsInfo.put("geometry", geometry);	
					
					return tipsInfo;
				}
								
				 				 
			}finally{
				/*if (htab!=null){
					htab.close();
				}*/
			}	
						
	}
	
	//7.将查询到的tips的deep信息写入到.mid文件
	public static void writeToMid(Map<String, String> tipsInfo) throws Exception {	
		JSONObject dataObject = JSONObject.fromObject(tipsInfo.get("data"));
		String id = dataObject.getString("id");
		
		JSONObject geoObject = dataObject.getJSONObject("geo");
		JSONArray geoArray = geoObject.getJSONArray("coordinates");
		String geo = geoArray.getString(0) + "," + geoArray.getString(1);
		
		int src = dataObject.getInt("src");
		int ln = dataObject.getInt("ln");
		int kind = dataObject.getInt("kind");
		Double len = dataObject.getDouble("len");
		int shp = dataObject.getInt("shp");
		int cons = dataObject.getInt("cons");
		String time = dataObject.getString("time");		
		String dataForMid = "\"" + id + "\",\"" + geo + "\",\"" + src + "\",\"" + ln + "\",\"" + kind + "\",\"" + len + "\",\"" + shp + "\",\"" + cons + "\",\"" + time + "\"";					
		
		String filePath = "tab";
		File file = new File(filePath);
    	if (!file.exists()) {
    		file.mkdirs();
		}
    	filePath += "/tipsOfLine.mid";
		FileWriter fw = new FileWriter(filePath, true);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(dataForMid);
        bw.write("\r\n");
        bw.close();
        fw.close();

	}
	
	//8.将查询到的tips的g_location信息写入到.mif文件
	public static void writeToMif(Map<String, String> tipsInfo) throws Exception{
		JSONObject geometryObject = JSONObject.fromObject(tipsInfo.get("geometry"));
		JSONObject glocationObject = geometryObject.getJSONObject("g_location");
		JSONArray glocationArray = glocationObject.getJSONArray("coordinates");
		
		
		String filePath = "tab";
		File file = new File(filePath);
    	if (!file.exists()) {
    		file.mkdirs();
		}
    	filePath += "/tipsOfLine.mif";
		FileWriter fw = new FileWriter(filePath, true);
        BufferedWriter bw = new BufferedWriter(fw);
        if (2 == glocationArray.size()){
        	bw.write("Line" + " ");
        	for (int i = 0;i<glocationArray.size();i++) {
    			JSONArray array = JSONArray.fromObject(glocationArray.get(i));
    			String x = array.get(0).toString();
    			String y = array.get(1).toString();
    			String glocation = x + " " + y;		
    	        bw.write(glocation);
    	        bw.write(" ");	        
    		}
        	bw.write("\r\n");
        	
        }else{
        	bw.write("Pline" + " " + glocationArray.size());
            bw.write("\r\n");
            
    		for (int i = 0;i<glocationArray.size();i++) {
    			JSONArray array = JSONArray.fromObject(glocationArray.get(i));
    			String x = array.get(0).toString();
    			String y = array.get(1).toString();
    			String glocation = x + " " + y;		
    	        bw.write(glocation);
    	        bw.write("\r\n");	        
    		}
        }
        
		bw.close();
        fw.close();
		
		
	}
	
	//9.如果是全库提取测线tips，则直接通过rowkey过滤查询到测线tips的rowkey
	public static List<String> getTipsRowkey() throws IOException {
		List<String> array = new ArrayList<>();
		long startHbase = System.currentTimeMillis();
		org.apache.hadoop.hbase.client.Connection hbaseConn = HBaseConnector.getInstance().getConnection();
		long endHbase = System.currentTimeMillis();
		logger.debug("获取hbase连接用时：" + (endHbase - startHbase) + "毫秒");
		 Table htab = null;
			try{
				htab = hbaseConn.getTable(TableName.valueOf(Bytes.toBytes(HBaseConstant.tipTab)));
				
				//过滤出新增的测线tips的Rowkey
				 Scan scan1 = new Scan();
				 Filter filter1 = new RowFilter(CompareOp.EQUAL,
						 new BinaryPrefixComparator("022001".getBytes()));				 
				 scan1.setFilter(filter1);
				 ResultScanner scanner1 = htab.getScanner(scan1);				 
				 for (Result rs : scanner1) {
					 byte[] rowkey=rs.getRow();
					 array.add(Bytes.toString(rowkey));
				}
				 
				//过滤出同步的测线tips的Rowkey
				 Scan scan2 = new Scan();
				 Filter filter2 = new RowFilter(CompareOp.EQUAL,
						 new BinaryPrefixComparator("112001".getBytes()));				 
				 scan2.setFilter(filter2);
				 ResultScanner scanner2 = htab.getScanner(scan2);				 
				 for (Result rs : scanner2) {
					 byte[] rowkey=rs.getRow();
					 array.add(Bytes.toString(rowkey));
				}				
				 				 
			}finally{
				if (htab!=null){
					htab.close();
				}
				
				/*if (hbaseConn!=null){
					hbaseConn.close();
				}*/
			}	
			System.out.println(array.size());
		return array;		
	}
}