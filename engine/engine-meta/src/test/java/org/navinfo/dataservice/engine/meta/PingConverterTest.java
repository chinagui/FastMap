package org.navinfo.dataservice.engine.meta;

import org.junit.Before;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.pinyin.PinyinConverter;
import com.navinfo.dataservice.engine.meta.translates.EnglishConvert;
import net.sf.json.JSONObject;

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
	public void testpyConvert() throws Exception {
		PinyinConverter py = new PinyinConverter();

//		String res = py.pyConvert("上海弄堂", "310000", null);
		String res = py.pyConvert("S271", "310000", null);
		System.out.println("res: "+res);
		
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

		/*String[] res = py.pyVoiceConvert("S271", null, "310000", null);
				//("上海弄堂", "310000", null);

		System.out.println(res);
		
		System.out.println(res[0]);

		System.out.println(res[1]);*/
		
		/*String newWord = py.wordConvert("Ｓ２７0", "310000");
		System.out.println("newWord: "+newWord);*/
		String newWord = "北京 · 医院";
		
		/*String[] res1 = py.pyVoiceConvert(newWord, null, null, null);
		//("上海弄堂", "310000", null);

		System.out.println(res1);
		
		System.out.println(res1[0]);
		
		System.out.println(res1[1]);*/
		
		 String result = py.pyPolyphoneConvert(newWord, null);
         String voiceStr = py.voiceConvert(newWord, null, null, null);

         if (result != null) {
             JSONObject json = new JSONObject();

             json.put("phonetic", result);
             
             json.put("voicefile", voiceStr);
             System.out.println(json);
         }
        
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
	
//	@Test
	public void testpyPolyphoneConvertFq() throws Exception {
		PinyinConverter py = new PinyinConverter();

//		String res = py.pyConvert("上海弄堂", "310000", null);
//		String word = "Ｓ１３３";
		String word = "Ｚ１３３";
		word = py.wordConvert(word, null);
		System.out.println(word);
		
		 String[] result = py.pyVoiceConvert(word, null, null, null);
		

		System.out.println(word);
		//Shang Hai Nong Tang
		//带行政区划号后  Shang Hai Long Tang

	}
//	@Test
	public void testpyPolyphoneConvert2() throws Exception {
		PinyinConverter py = new PinyinConverter();
		String word = "永丰路";
		String voiceStr = py.voiceConvert(word, null, null, null);
		System.out.println("1: "+voiceStr);
		voiceStr = voiceStr.replace("gaosugonglu", "");
		System.out.println("2: "+voiceStr);
		voiceStr = voiceStr.endsWith("'")?voiceStr.substring(0, voiceStr.length() - 1):voiceStr;
		System.out.println("3: "+voiceStr);
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
