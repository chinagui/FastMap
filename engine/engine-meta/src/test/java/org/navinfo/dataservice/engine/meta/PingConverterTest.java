/**
 * 
 */
package org.navinfo.dataservice.engine.meta;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.pinyin.PinyinConverter;

/**
 * @ClassName: PingConverterTest
 * @author Zhang Xiaolong
 * @date 2016年9月22日 上午9:32:47
 * @Description: TODO
 */
public class PingConverterTest {
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	@Test
	public void testQueryPingYin() throws Exception {
		PinyinConverter py = new PinyinConverter();

		String[] res = py.convert("京承高速收费站1");

		System.out.println(res);

		System.out.println(res[0]);

		System.out.println(res[1]);

	}
}
