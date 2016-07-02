package org.navinfo.dataservice.engine.meta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.rdname.RdNameImportor;

/** 
 * @ClassName: RdNameImportTest.java
 * @author y
 * @date 2016-7-2下午3:07:28
 * @Description: TODO
 *  
 */
public class RdNameImportTest {
	
	
	@Test
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer.xml"});
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
		RdNameImportor importor = new RdNameImportor();
		try {
			/*importor.importName("A45", 116.49266, 40.20926, "test_imp1");
			importor.importName("测试高速公路", 116.49266, 40.20926, "test_imp1");
			importor.importName("测试高架路", 116.49266, 40.20926, "test_imp1");
			importor.importName("测试高架桥", 116.49266, 40.20926, "test_imp1");
			importor.importName("测试快速路", 116.49266, 40.20926, "test_imp1");
			
			importor.importName("N", 116.49266, 40.20926, "test_imp1");
			importor.importName("n", 116.49266, 40.20926, "test_imp1");
			importor.importName("NO", 116.49266, 40.20926, "test_imp1");
			importor.importName("no", 116.49266, 40.20926, "test_imp1");
			importor.importName("No", 116.49266, 40.20926, "test_imp1");
			importor.importName("无道路名", 116.49266, 40.20926, "test_imp1");
			importor.importName("无", 116.49266, 40.20926, "test_imp1");
			
			importor.importName("Ｎ", 116.49266, 40.20926, "test_imp1");
			importor.importName("ＮＯ", 116.49266, 40.20926, "test_imp1");
			
			importor.importName("测试1#路", 116.49266, 40.20926, "test_imp1");
			importor.importName("测试2＃路", 116.49266, 40.20926, "test_imp1");*/
			
			importor.importName("测试罗马 V 路", 116.49266, 40.20926, "test_imp1");
			importor.importName("测试123c路", 116.49266, 40.20926, "test_imp1");
			
			System.out.println("测试完成");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
