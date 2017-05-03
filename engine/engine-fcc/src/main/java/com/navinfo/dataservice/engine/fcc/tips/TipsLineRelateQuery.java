package com.navinfo.dataservice.engine.fcc.tips;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.fcc.SolrController;

public class TipsLineRelateQuery {

	public static final String LINK_SEPARATOR = "|";

	public static String getRelateLine(String s_sourceType, JSONObject deep) {

		String result = "";

		if (TipsLineRelateConstant.simpleF.contains(s_sourceType)) {
			result = getLine1(deep);
		} else if (TipsLineRelateConstant.simpleIn.contains(s_sourceType)) {
			result = getLine2(deep);
		} else if (TipsLineRelateConstant.simpleOut.contains(s_sourceType)) {
			result = getLine3(deep);
		} else if (TipsLineRelateConstant.simpleExp.contains(s_sourceType)) {
			result = getLine4(deep);
		} else if (TipsLineRelateConstant.f_array_Id.contains(s_sourceType)) {
			result = getLine5(deep);
		} else if (TipsLineRelateConstant.f_array_F.contains(s_sourceType)) {
			result = getLine6(deep);
		} else if (TipsLineRelateConstant.complex_1.contains(s_sourceType)) {
			result = getLine7(deep);
		} else if (TipsLineRelateConstant.complex_2.contains(s_sourceType)) {
			result = getLine8(deep);
		} else if (TipsLineRelateConstant.complex_3.contains(s_sourceType)) {
			result = getLine9(deep);
		} else if (TipsLineRelateConstant.complex_4.contains(s_sourceType)) {
			result = getLine10(deep);
		} else if (TipsLineRelateConstant.complex_5.contains(s_sourceType)) {
			result = getLine11(deep);
		} else if (TipsLineRelateConstant.complex_6.contains(s_sourceType)) {
			result = getLine12(deep);
		}

		return result;

	}

	/**
	 * 1.f.id
	 * 
	 * @param deep
	 * @return
	 */
	private static String getLine1(JSONObject deep) {

		String linkStr = "";

		JSONObject f = deep.getJSONObject("f");

		if (StringUtils.isNotEmpty(f.getString("id"))) {
			linkStr =f.getString("id") + LINK_SEPARATOR;
		}
		
		if(StringUtils.isNotEmpty(linkStr)){
			linkStr=LINK_SEPARATOR+linkStr;
		}
		
		return linkStr;

	}

	/**
	 * 2.in.id
	 * 
	 * @param deep
	 * @return
	 */
	private static String getLine2(JSONObject deep) {
		String linkStr = "";

		JSONObject in = deep.getJSONObject("in");

		if (StringUtils.isNotEmpty(in.getString("id"))) {
			linkStr =in.getString("id") + LINK_SEPARATOR;
		}
		
		if(StringUtils.isNotEmpty(linkStr)){
			linkStr=LINK_SEPARATOR+linkStr;
		}

		return linkStr;
	}

	/**
	 * 3.out.id
	 * 
	 * @param deep
	 * @return
	 */
	private static String getLine3(JSONObject deep) {
		String linkStr = "";

		JSONObject out = deep.getJSONObject("out");

		if (StringUtils.isNotEmpty(out.getString("id"))) {
			linkStr =out.getString("id") + LINK_SEPARATOR;
		}
		
		if(StringUtils.isNotEmpty(linkStr)){
			linkStr=LINK_SEPARATOR+linkStr;
		}
		return linkStr;
	}

	/**
	 * 4.exp.id
	 * 
	 * @param deep
	 * @return
	 */
	private static String getLine4(JSONObject deep) {
		String linkStr = "";

		JSONObject exp = deep.getJSONObject("exp");

		if (StringUtils.isNotEmpty(exp.getString("id"))) {
			linkStr =exp.getString("id") + LINK_SEPARATOR;
		}

		if(StringUtils.isNotEmpty(linkStr)){
			linkStr=LINK_SEPARATOR+linkStr;
		}
		return linkStr;
	}

	/**
	 * 5.[f_array].id
	 * 
	 * @param deep
	 * @return
	 */
	private static String getLine5(JSONObject deep) {
		String linkStr = "";

		JSONArray f_array = deep.getJSONArray("f_array");
		for (Object obj : f_array) {

			JSONObject fInfo = JSONObject.fromObject(obj);

			if (StringUtils.isNotEmpty(fInfo.getString("id"))) {
				linkStr +=fInfo.getString("id")
						+ LINK_SEPARATOR;
			}

		}
		
		if(StringUtils.isNotEmpty(linkStr)){
			linkStr=LINK_SEPARATOR+linkStr;
		}

		return linkStr;
	}

	/**
	 * 6.[f_array].f.id (f唯一是对象)
	 * 
	 * @param deep
	 * @return
	 */
	private static String getLine6(JSONObject deep) {
		String linkStr = "";

		JSONArray f_array = deep.getJSONArray("f_array");
		for (Object obj : f_array) {

			JSONObject fInfo = JSONObject.fromObject(obj);

			JSONObject f = fInfo.getJSONObject("f");

			if (StringUtils.isNotEmpty(f.getString("id"))) {
				linkStr +=f.getString("id") + LINK_SEPARATOR;
			}
		}
		
		if(StringUtils.isNotEmpty(linkStr)){
			linkStr=LINK_SEPARATOR+linkStr;
		}

		return linkStr;
	}

	/**
	 * 7.复杂关系（in.id+[o_array].out.id）
	 * 
	 * @param deep
	 * @return
	 */
	private static String getLine7(JSONObject deep) {
		String linkStr = "";

		// in.id
		JSONObject in = deep.getJSONObject("in");

		if (StringUtils.isNotEmpty(in.getString("id"))) {
			linkStr +=in.getString("id") + LINK_SEPARATOR;
		}

		// [o_array].out.id
		JSONArray o_array = deep.getJSONArray("o_array");
		for (Object obj : o_array) {

			JSONObject oInfo = JSONObject.fromObject(obj);

			JSONObject out = oInfo.getJSONObject("out");

			if (StringUtils.isNotEmpty(out.getString("id"))) {
				linkStr +=out.getString("id")
						+ LINK_SEPARATOR;
			}
		}

		if(StringUtils.isNotEmpty(linkStr)){
			linkStr=LINK_SEPARATOR+linkStr;
		}
		
		return linkStr;
	}

	/**
	 * 8.复杂关系-车信（in.id+[o_array].[d_array].out.id(out是对象)）
	 * 
	 * @param deep
	 * @return
	 */
	private static String getLine8(JSONObject deep) {
		String linkStr = "";

		// in.id
		JSONObject in = deep.getJSONObject("in");

		if (StringUtils.isNotEmpty(in.getString("id"))) {
			linkStr +=in.getString("id") + LINK_SEPARATOR;
		}

		// [o_array].out.id
		JSONArray o_array = deep.getJSONArray("o_array");
		for (Object obj : o_array) {

			JSONObject oInfo = JSONObject.fromObject(obj);

			JSONArray d_array = oInfo.getJSONArray("d_array");

			for (Object object : d_array) {
				
				JSONObject dInfo = JSONObject.fromObject(object);
				
				JSONObject out = dInfo.getJSONObject("out");
				
				if (StringUtils.isNotEmpty(out.getString("id"))) {
					
					linkStr +=out.getString("id")
							+ LINK_SEPARATOR;
				}
			}

		}
		
		if(StringUtils.isNotEmpty(linkStr)){
			linkStr=LINK_SEPARATOR+linkStr;
		}
		return linkStr;
	}
	
	
	/**
	 * 9.复杂关系-公交车道（[ln].[o_array].id+[f_array].id)
	 * 
	 * @param deep
	 * @return
	 */
	private static String getLine9(JSONObject deep) {
		String linkStr = "";

		// [ln].[o_array].id
		JSONArray ln = deep.getJSONArray("ln");
		
		for (Object obj : ln) {

			JSONObject lnInfo = JSONObject.fromObject(obj);
			
			JSONArray o_array = lnInfo.getJSONArray("o_array");
			
			for (Object object : o_array) {
				

				JSONObject oInfo = JSONObject.fromObject(object);

				if (StringUtils.isNotEmpty(oInfo.getString("id"))) {
					
					linkStr +=oInfo.getString("id")
							+ LINK_SEPARATOR;
				}
			}

		}
		// [f_array].id
		JSONArray f_array = deep.getJSONArray("f_array");
		for (Object obj : f_array) {

			JSONObject oInfo = JSONObject.fromObject(obj);

			if (StringUtils.isNotEmpty(oInfo.getString("id"))) {
				
				linkStr +=oInfo.getString("id")
						+ LINK_SEPARATOR;
			}

		}
		
		if(StringUtils.isNotEmpty(linkStr)){
			linkStr=LINK_SEPARATOR+linkStr;
		}
		return linkStr;
	}
	
	
	/**
	 * 10.复杂关系-可变导向车道（f.id+[ln].[o_array].out.id)
	 * 
	 * @param deep
	 * @return
	 */
	private static String getLine10(JSONObject deep) {
		String linkStr = "";
		
		//f.id
		
		JSONObject f = deep.getJSONObject("f");

		if (StringUtils.isNotEmpty(f.getString("id"))) {
			linkStr =f.getString("id") + LINK_SEPARATOR;
		}

		// [ln].[o_array].out.id
		JSONArray ln = deep.getJSONArray("ln");
		
		for (Object obj : ln) {

			JSONObject lnInfo = JSONObject.fromObject(obj);
			
			JSONArray o_array = lnInfo.getJSONArray("o_array");
			
			for (Object object : o_array) {
				

				JSONObject oInfo = JSONObject.fromObject(object);
				
				JSONObject out=oInfo.getJSONObject("out");

				if (StringUtils.isNotEmpty(out.getString("id"))) {
					
					linkStr +=out.getString("id")
							+ LINK_SEPARATOR;
				}
			}

		}
		
		if(StringUtils.isNotEmpty(linkStr)){
			linkStr=LINK_SEPARATOR+linkStr;
		}
		return linkStr;
	}
	
	
	
	/**
	 * 11.复杂关系（in.id+out.id)
	 * 
	 * @param deep
	 * @return
	 */
	private static String getLine11(JSONObject deep) {
		
		String linkStr = "";
		
		//in.id
		JSONObject in = deep.getJSONObject("in");

		if (StringUtils.isNotEmpty(in.getString("id"))) {
			linkStr =in.getString("id") + LINK_SEPARATOR;
		}
		//out.id
		JSONObject out = deep.getJSONObject("out");

		if (StringUtils.isNotEmpty(out.getString("id"))) {
			linkStr =out.getString("id") + LINK_SEPARATOR;
		}
		
		if(StringUtils.isNotEmpty(linkStr)){
			linkStr=LINK_SEPARATOR+linkStr;
		}
		return linkStr;
	}
	
	/**
	 * 12.复杂关系in.id+[o_array].[out].id 【out是数组】
	 * 
	 * @param deep
	 * @return
	 */
	private static String getLine12(JSONObject deep) {

		String linkStr = "";

		// in.id
		JSONObject in = deep.getJSONObject("in");

		if (StringUtils.isNotEmpty(in.getString("id"))) {
			linkStr =in.getString("id") + LINK_SEPARATOR;
		}

		// [o_array].[out].id 【out是数组】

		JSONArray o_array = deep.getJSONArray("o_array");

		for (Object object : o_array) {

			JSONObject oInfo = JSONObject.fromObject(object);

			JSONArray out = oInfo.getJSONArray("out"); //是数组哦
			
			for (Object object2 : out) {
				
				JSONObject outInfo = JSONObject.fromObject(object2);
				
				if (StringUtils.isNotEmpty(outInfo.getString("id"))) {

					linkStr +=outInfo.getString("id")
							+ LINK_SEPARATOR;
				}
			}
		}

		
		if(StringUtils.isNotEmpty(linkStr)){
			linkStr=LINK_SEPARATOR+linkStr;
		}
		return linkStr;
	}
	
	//测试link id获取
	public static void main(String[] args) {
		
		SolrController solr = new SolrController();
		
		try{
		JSONObject  json=solr.getById("0212013727709be42a43c79763dde3286891e7"); // 1. f.id 1201   |19361676|
		
		json=solr.getById("11110315691"); // 2. in.id 1103   |13677571|

		json=solr.getById("111106264"); // 3. out.id  1106   |246625|
		
		json=solr.getById("111703469213"); // 4.复杂关系-in out (in.id+[o_array].out.id) 1703   |578700||601561||49101284|
		
		json=solr.getById("1113016921617"); // 5.复杂关系-车信（in.id+[o_array].[d_array].out.id(out是对象)） 1301  |20152891||7729087||7753658|
		
		json=solr.getById("021310aedee6fac94f4b89bc73a1f151088470"); // 6.复杂关系-公交车道（[ln].[o_array].id+[f_array].id)  1310  |88026245||19613249|
		
		json=solr.getById("02131124edfa0a1f674511879a4ac289287c83"); // 7.in.id+out.id  1112  |1297433||1360730|
		
		json=solr.getById("02160496eb93ca12d4471fb9a9f5a34924dbc7"); // 8. [f_array].id 1604  |667570|
		
		json=solr.getById("021302B2BE0D2A81CE49F2A9C577BC46F4795B"); // 9. 	in.id+[o_array].[out].id 【out是数组】  1302  |17230727|

		json=solr.getById("123"); // 10. exp.id  1307   【没数据】

		json=solr.getById("0211028C17499F2BA14CD4AD301065EC5F3EFE"); // 10. [f_array].f.id  (f唯一是对象) 1102   数据中[f_array]为空
		
		String s_sourceType=json.getString("s_sourceType");
		
		JSONObject deep=JSONObject.fromObject(json.get("deep"));
		
		String result=TipsLineRelateQuery.getRelateLine(s_sourceType, deep);
		
		System.out.println(result);
		
		}catch (Exception e) {
			
			e.printStackTrace();
		}
		
		
		
	}
	
	
	
	

}
