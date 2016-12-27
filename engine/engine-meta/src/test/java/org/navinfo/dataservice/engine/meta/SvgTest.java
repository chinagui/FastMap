/**
 * 
 */
package org.navinfo.dataservice.engine.meta;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.patternimage.PatternImageExporter;
import com.navinfo.dataservice.engine.meta.patternimage.PatternImageImporter;

/** 
* @ClassName: SvgTest 
* @author Zhang Xiaolong
* @date 2016年12月23日 上午9:31:06 
* @Description: TODO
*/
public class SvgTest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	//@Test
	public void testUpdateSvgData() throws Exception
	{
		String sql = "update SC_VECTOR_MATCH set file_content = ? where file_name = 'S0CLL15OC91B'";
		//String sql = "update SC_VECTOR_MATCH set memo = ? where file_name = 'S0CYZ139NE28'";

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;
		FileInputStream in = null;
		try {

			conn = DBConnector.getInstance().getMetaConnection();

			pstmt = conn.prepareStatement(sql);
			File f = new File("f:/S0CLL15OC91B.svg");
			 in = new FileInputStream(f);
			int length = in.available();
			pstmt.setBinaryStream(1, in);
			//pstmt.setString(1, "测试");
			pstmt.executeUpdate();
			
			conn.commit();

		} catch (Exception e) {

			e.printStackTrace();

		} finally {
			in.close();
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
	}
	
	@Test
	public void testUpdateSvgExp() throws Exception
	{
		String path = "f:/";
		PatternImageExporter patternImageExporter=new PatternImageExporter();
		patternImageExporter.export2Sqlite(path);
	}
	//@Test
	public void testUpdateSvgImp() throws Exception
	{
		/*String path = "f:/PatternImg";
		//PatternImageImporter patternImageImp = new PatternImageImporter();
		Connection conn = null;
		PreparedStatement pstmt =null;
		PreparedStatement pstmtSvg=null;
		int counter = 0;
		String sql = "update SC_MODEL_MATCH_G set format=:1,file_content=:2 where file_name=:3";
		String sqlSvg = "update SC_VECTOR_MATCH set format=:1,file_content=:2 where file_name=:3";

		conn = DBConnector.getInstance().getMetaConnection();
		conn.setAutoCommit(false);

		pstmt = conn.prepareStatement(sql);
		pstmtSvg = conn.prepareStatement(sqlSvg);
		PatternImageImporter patternImageImporter = new PatternImageImporter(pstmt,pstmtSvg);
		patternImageImporter.readDataImg(path);

		patternImageImporter.readDataSvg(path);
		
		conn.commit();

		conn.close();
		
		System.out.println("Done. Total:"+counter);*/
	}
	
	
	
	
	public static void main(String[] args) throws IOException {
		File file = new File("f:/S0CLL15OC91B.svg");
		ImageInputStream is = null;
		try {
			is = ImageIO.createImageInputStream(file);
			String[] splits = file.getName().split("\\.");
			 if(null == is)  
	            {  
				 System.out.println("xxxx:  "+splits[0]);
	            }  
			 String format = splits[splits.length - 1];
			 System.out.println("format : "+format);
			 System.out.println(splits[1]+"  yy:  "+splits[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			//outSteam.close();  
		    is.close(); 
		}  
	}
}
