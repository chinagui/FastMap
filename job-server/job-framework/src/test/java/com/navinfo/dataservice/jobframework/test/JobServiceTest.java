package com.navinfo.dataservice.jobframework.test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.bizcommons.glm.Glm;
import com.navinfo.dataservice.bizcommons.glm.GlmCache;
import com.navinfo.dataservice.bizcommons.glm.GlmTable;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.service.JobService;

/** 
* @ClassName: JobServiceTest 
* @author Xiao Xiaowen 
* @date 2016年6月12日 下午2:19:01 
* @Description: TODO
*  
*/
public class JobServiceTest {
	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] {"dubbo-consumer-man-test.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	@Test
	public void hello_001(){
		try{
			System.out.println(JobService.getInstance().hello());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Test
	public void hello_002(){
		try{
			List<Integer> list = new ArrayList<Integer>();
			list.add(8);
			list.add(9);
			list.add(24);
			for (int i : list) {
				Connection conn = DBConnector.getInstance().getConnectionById(i);
				System.out.println(conn + "----------------test1");

			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	@Test
	public void hello_003(){
		Glm glm = GlmCache.getInstance().getGlm("250+");
		Map<String,GlmTable> tables = glm.getEditTables();
		
		for(String name:tables.keySet()){
			System.out.println("INSERT INTO GLM_TABLE VALUES(GLM_TABLE_SEQ.NEXTVAL,'"+name+"',NULL,0,0,'240+');");
//			GlmTable table = tables.get(name);
//			List<GlmColumn> cols = table.getColumns();
//			for(GlmColumn col:cols){
//				System.out.println("--"+col.getName()+":"+col.getDataType()+":"+col.isPk());
//			}
		}
		System.out.println("Over.");
	}
}
