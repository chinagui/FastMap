package com.navinfo.dataservice.engine.fcc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;

import org.junit.Test;

import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.engine.fcc.tips.TipsSelector;
import com.navinfo.navicommons.geo.GeoUtils;
import com.navinfo.navicommons.geo.computation.MeshUtils;

public class TipsSelectorTest {

	TipsSelector selector = new TipsSelector();

	//根据网格、类型、作业状态获取tips的snapshot列表（rowkey，点位，类型）
	//@Test
	public void testGetSnapshot() {
		JSONArray grid = JSONArray
				.fromObject("[59567101,59567102,59567103,59567104,59567201,60560301,60560302,60560303,60560304]");
		System.out.println(grid.toString());
		JSONArray stage = new JSONArray();
		stage.add(1);
		int type = 1101;
		int projectId = 11;
		try {
			System.out.println(selector.getSnapshot(grid, stage, type,
					projectId));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
    //根据瓦片扩圈获取Tips数据
	//@Test
	public void testSearchDataByTileWithGap() {
		JSONArray types = new JSONArray();
		types.add(1301);
		types.add(1205);
		types.add(1401);
		types.add(1110);
		types.add(1515);
		types.add(1105);
		types.add(1806);
		types.add(1901);
		try {
			System.out.println(selector.searchDataByTileWithGap(107944, 49615, 17,
					20, types,"m"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//根据网格获取tips统计
	//	@Test
		public void testGetStats() {
			JSONArray grid = JSONArray
					.fromObject("[59567101,59567102,59567103,59567104,59567201,60560301,60560302,60560303,60560304]");
			JSONArray stage = new JSONArray();
			try {
				System.out.println(selector.getStats(grid, stage));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		//根据rowkey获取单个tips的详细信息
		@Test
		public void testSearchDataByRowkey() {
			try {
				System.out.println("sorl by rowkey:");
				System.out.println(selector.searchDataByRowkey("021806d2379145037f471ebda56b88a659999"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		//根据wkt范围获取tips的snapshot列表
	//	@Test
		public void testSearchDataBySpatial() {
			try {
				JSONArray ja =
						selector.searchDataBySpatial("POLYGON ((113.70469 26.62879, 119.70818 26.62879, 119.70818 29.62948, 113.70469 29.62948, 113.70469 26.62879))");

				System.out.println(ja.size());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		//@Test
		public void testGeoUtis() {
			try {
				//1.坐标转换为图幅
				
				System.out.println(MeshUtils.point2Meshes(117.04654,40.27268).length);
				System.out.println(MeshUtils.point2Meshes(117.04654,40.27268)[0]);
				//2.全角转半角
				System.out.println("---２３４５----");
				System.out.println(ExcelReader.f2h("---２３４５----"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		/**
		 * 已经测试成功的取出正则表达式匹配的字符串         
	         * @author erikas
	         * @throws Exception
		 */
		//@org.junit.Test
		public void testReg() throws Exception {
		//  ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅩⅪⅫ⑴⑵⑶
			
			//String str = "[ X , I , V , IIII , IV , VI , X , XII , C , CI , IX , IV ]";
			//String str = " I xa II  as III as  IV  as  V  as X  as C as CII as CXXI ";
			String str="ⅠasⅡasⅢ asⅥasVassVIasⅫ";
			str=str.replace("Ⅰ", "  I  ");
			str=str.replace("Ⅱ", "  II  ");
			str=str.replace("Ⅲ", "  III  ");
			str=str.replace("Ⅵ", "  IV  ");
			
			System.out.println(str);
			System.out.println(str.replace("Ⅰ", "I"));
			String result="";
			//匹配罗马数字的正则,但是由于每一个都可能是0个 空字符串也会被匹配出来 需要后期在程序里再处理
			String regex = "(-| +|^)M{0,9}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})( +|$)"; 
			Pattern p = Pattern.compile(regex);
			Matcher matcher = p.matcher(str);
			List<String> list = new ArrayList<String>();
			while (matcher.find()) {
				//matcher.find()返回true表示匹配到了结果 但是执行了之后如果后面没有再匹配成功的 会马上变成false
				String srcStr = matcher.group();
				//String value=r2a(srcStr);
				if(!" ".equals(srcStr)){
					System.out.println(srcStr+":"+r2a(srcStr)+"----");
					str=str.replace(srcStr,r2a(srcStr));
					System.out.println("替换后："+str);
				}
				//把取出的结果 放入list
				list.add(srcStr);
			}
			//System.out.println(str);
			System.out.println(list);
		}
		
		//罗马数字转阿拉伯数字：
	    // 从前往后遍历罗马数字，如果某个数比前一个数小，则把该数加入到结果中；
	    // 反之，则在结果中两次减去前一个数并加上当前这个数；
	    // I、V、X、   L、   C、     D、     M
	    // 1．5、10、50、100、500、1000
	    private static String r2a(String in){
	        int graph[] = new int[400];
	        graph['I'] = 1;
	        graph['V']=5;
	        graph['X']=10;
	        graph['L']=50;
	        graph['C']=100;
	        graph['D']=500;
	        graph['M']=1000;
	        char[] num = in.toCharArray();
	        // 遍历这个数，用sum来总计和
	        int sum = graph[num[0]];
	        for(int i=0; i<num.length-1; i++){
	            // 如果，i比i+1大的话，直接相加
	            if(graph[num[i]] >= graph[num[i+1]]){
	                sum += graph[num[i+1]];
	            }
	            // 如果i比i+1小的话，则将总和sum减去i这个地方数的两倍，同时加上i+1
	            // 就相当于后边的数比左边的数大，则用右边的数减左边的数
	            else{
	                sum = sum + graph[num[i+1]] - 2*graph[num[i]];
	            }
	        }
	        return String.valueOf(sum);
	    }
	    



}
