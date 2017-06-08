/**
 * 
 */
package org.navinfo.dataservice.engine.meta;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.pinyin.PinyinConverter;
import com.navinfo.dataservice.engine.meta.translates.EnglishConvert;

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

//	@Test
	public void testQueryPingYin() throws Exception {
		PinyinConverter py = new PinyinConverter();

		String[] res = py.convert("京承高速收费站1");

		System.out.println(res);

		System.out.println(res[0]);

		System.out.println(res[1]);

	}
	
//	@Test
	public void testConvertPingYin() throws Exception {
		PinyinConverter py = new PinyinConverter();

		for (int i=0;i<=10;i++){
			String res = py.convertHz("１号楼");
			System.out.println(res);
		}

//		System.out.println(res);

	}
	
	@Test
	public void testpyConvert() throws Exception {
		PinyinConverter py = new PinyinConverter();

//		String res = py.pyConvert("上海弄堂", "310000", null);
		String res = py.pyConvert("S271", null, null);
		

		System.out.println(res);
		//Shang Hai Nong Tang
		//带行政区划号后  Shang Hai Long Tang

	}
	
//	@Test
	public void testvoiceConvert() throws Exception {
		PinyinConverter py = new PinyinConverter();

		String res = py.voiceConvert("S271", null, null, null);
				//("上海弄堂", "310000", null);

		System.out.println(res);
		//Shang Hai Nong Tang
		//带行政区划号后  Shang Hai Long Tang

	}
	
//	@Test
	public void testpyvConvert() throws Exception {
		PinyinConverter py = new PinyinConverter();

		String[] res = py.pyVoiceConvert("S271", null, null, null);
				//("上海弄堂", "310000", null);

		System.out.println(res);
		
		System.out.println(res[0]);

		System.out.println(res[1]);

	}
	
//	@Test
	public void testEngConvert() throws Exception {
		PinyinConverter py = new PinyinConverter();

		String res = py.engConvert("停车场", null);
				//("上海弄堂", "310000", null);
		System.out.println(res);
	}
	
//	@Test
	public void testEngConvert2() throws Exception {
		EnglishConvert py = new EnglishConvert();

		String res = py.convert("停车场");
				//("上海弄堂", "310000", null);
		System.out.println(res);
	}
	
	
//	@Test
	public void testpyPolyphoneConvert() throws Exception {
		PinyinConverter py = new PinyinConverter();

//		String res = py.pyConvert("上海弄堂", "310000", null);
		String res = py.pyPolyphoneConvert("思维差心°", null);

		System.out.println(res);
		//Shang Hai Nong Tang
		//带行政区划号后  Shang Hai Long Tang

	}
	
	public static void main(String[] args) {
		/*String targets = "[IX_POI,500000008];[IX_POI,510000006];[IX_POI,509000015];[IX_POI,504000007]";
		System.out.println(targets);
		String b =targets.replaceAll("[\\[\\]]","").replaceAll("IX_POI,", "").replaceAll(";", ",");  
		//.replaceAll("]", "");
		System.out.println(targets);
		System.out.println(b);
//		String[] pidsArr = pids.split(",");
*/	
		/*String str = "上海弄堂";
		char[] chars = str.toCharArray();
		System.out.println("chars: "+chars.toString());*/
		String str = "shdhfe";
		str = str.substring(0, 1).toUpperCase() + str.substring(1);
		
		System.out.println(str);
		
		
	}
	
}
