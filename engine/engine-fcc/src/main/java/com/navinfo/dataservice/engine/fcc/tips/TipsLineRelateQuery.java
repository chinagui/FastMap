package com.navinfo.dataservice.engine.fcc.tips;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.dao.fcc.SolrController;

public class TipsLineRelateQuery {

	public static final String LINK_SEPARATOR = "|";

	public static Map<String, String> getRelateLine(String s_sourceType,
			JSONObject deep) {

		Map<String, String> result = new HashMap<String, String>();

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
		}else{
			
			result.put("relate_nodes", "");

			result.put("relate_links", "");
		}

		return result;

	}

	/**
	 * 1.f.id
	 * 
	 * @param deep
	 * @return
	 */
	private static Map<String, String> getLine1(JSONObject deep) {

		Map<String, String> relateMap = new HashMap<String, String>();

		String linkStr = "";

		String nodeStr = "";

		JSONObject f = deep.getJSONObject("f");

		if (f != null && f.containsKey("id")) {

			if (StringUtils.isNotEmpty(f.getString("id"))) {

				// 3 道路NODE；
				if (f.getInt("type") == 3) {

					nodeStr = f.getString("id") + LINK_SEPARATOR;
				} else {
					linkStr = f.getString("id") + LINK_SEPARATOR;
				}
			}
		}

		if (StringUtils.isNotEmpty(linkStr)) {
			linkStr = LINK_SEPARATOR + linkStr;
		}

		if (StringUtils.isNotEmpty(nodeStr)) {
			nodeStr = LINK_SEPARATOR + nodeStr;
		}

		relateMap.put("relate_nodes", nodeStr);

		relateMap.put("relate_links", linkStr);

		return relateMap;

	}

	/**
	 * 2.in.id
	 * 
	 * @param deep
	 * @return
	 */
	private static Map<String, String> getLine2(JSONObject deep) {

		Map<String, String> relateMap = new HashMap<String, String>();

		String linkStr = "";

		String nodeStr = "";

		JSONObject in = deep.getJSONObject("in");

		if (in != null && in.containsKey("id")) {

			// 3 道路NODE；
			if (in.getInt("type") == 3) {

				nodeStr = in.getString("id") + LINK_SEPARATOR;
			} else {
				linkStr = in.getString("id") + LINK_SEPARATOR;
			}

		}

		if (StringUtils.isNotEmpty(linkStr)) {
			linkStr = LINK_SEPARATOR + linkStr;
		}

		if (StringUtils.isNotEmpty(nodeStr)) {
			nodeStr = LINK_SEPARATOR + nodeStr;
		}

		relateMap.put("relate_nodes", nodeStr);

		relateMap.put("relate_links", linkStr);

		return relateMap;
	}

	/**
	 * 3.out.id
	 * 
	 * @param deep
	 * @return
	 */
	private static Map<String, String> getLine3(JSONObject deep) {

		Map<String, String> relateMap = new HashMap<String, String>();

		String linkStr = "";

		String nodeStr = "";

		JSONObject out = deep.getJSONObject("out");

		if (out != null && out.containsKey("id")) {

			// 3 道路NODE；
			if (out.getInt("type") == 3) {

				nodeStr = out.getString("id") + LINK_SEPARATOR;
			} else {
				linkStr = out.getString("id") + LINK_SEPARATOR;
			}
		}

		if (StringUtils.isNotEmpty(linkStr)) {
			linkStr = LINK_SEPARATOR + linkStr;
		}
		if (StringUtils.isNotEmpty(nodeStr)) {
			nodeStr = LINK_SEPARATOR + nodeStr;
		}

		relateMap.put("relate_nodes", nodeStr);

		relateMap.put("relate_links", linkStr);

		return relateMap;
	}

	/**
	 * 4.exp.id
	 * 
	 * @param deep
	 * @return
	 */
	private static Map<String, String> getLine4(JSONObject deep) {

		Map<String, String> relateMap = new HashMap<String, String>();

		String linkStr = "";

		String nodeStr = "";

		JSONObject exp = deep.getJSONObject("exp");

		if (exp != null && exp.containsKey("id")) {

			if (StringUtils.isNotEmpty(exp.getString("id"))) {

				// 3 道路NODE；
				if (exp.getInt("type") == 3) {

					nodeStr = exp.getString("id") + LINK_SEPARATOR;
				} else {
					linkStr = exp.getString("id") + LINK_SEPARATOR;
				}
			}

		}

		if (StringUtils.isNotEmpty(linkStr)) {
			linkStr = LINK_SEPARATOR + linkStr;
		}
		if (StringUtils.isNotEmpty(nodeStr)) {
			nodeStr = LINK_SEPARATOR + nodeStr;
		}

		relateMap.put("relate_nodes", nodeStr);

		relateMap.put("relate_links", linkStr);

		return relateMap;
	}

	/**
	 * 5.[f_array].id
	 * 
	 * @param deep
	 * @return
	 */
	private static Map<String, String> getLine5(JSONObject deep) {

		Map<String, String> relateMap = new HashMap<String, String>();

		String linkStr = "";

		String nodeStr = "";

		JSONArray f_array = deep.getJSONArray("f_array");

		if (f_array != null) {

			for (Object obj : f_array) {

				JSONObject fInfo = JSONObject.fromObject(obj);

				if (fInfo != null && fInfo.containsKey("id")) {

					if (StringUtils.isNotEmpty(fInfo.getString("id"))) {

						// 3 道路NODE；
						if (fInfo.getInt("type") == 3) {

							nodeStr += fInfo.getString("id") + LINK_SEPARATOR;
						} else {
							linkStr += fInfo.getString("id") + LINK_SEPARATOR;
						}
					}
				}

			}
		}

		if (StringUtils.isNotEmpty(linkStr)) {
			linkStr = LINK_SEPARATOR + linkStr;
		}

		if (StringUtils.isNotEmpty(nodeStr)) {
			nodeStr = LINK_SEPARATOR + nodeStr;
		}

		relateMap.put("relate_nodes", nodeStr);

		relateMap.put("relate_links", linkStr);

		return relateMap;
	}

	/**
	 * 6.[f_array].f.id (f唯一是对象)
	 * 
	 * @param deep
	 * @return
	 */
	private static Map<String, String> getLine6(JSONObject deep) {

		Map<String, String> relateMap = new HashMap<String, String>();

		String linkStr = "";

		String nodeStr = "";

		JSONArray f_array = deep.getJSONArray("f_array");

		if (f_array != null) {

			for (Object obj : f_array) {

				JSONObject fInfo = JSONObject.fromObject(obj);

				JSONObject f = fInfo.getJSONObject("f");

				if (f != null && f.containsKey("id")) {

					if (StringUtils.isNotEmpty(f.getString("id"))) {
						
						// 3 道路NODE；
						if (f.getInt("type") == 3) {

							nodeStr += f.getString("id") + LINK_SEPARATOR;
						} else {
							linkStr += f.getString("id") + LINK_SEPARATOR;
						}
					}
				}

			}
		}

		if (StringUtils.isNotEmpty(linkStr)) {
			linkStr = LINK_SEPARATOR + linkStr;
		}

		if (StringUtils.isNotEmpty(nodeStr)) {
			nodeStr = LINK_SEPARATOR + nodeStr;
		}

		relateMap.put("relate_nodes", nodeStr);

		relateMap.put("relate_links", linkStr);

		return relateMap;
	}

	/**
	 * 7.复杂关系（in.id+[o_array].out.id）
	 * 
	 * @param deep
	 * @return
	 */
	private static Map<String, String> getLine7(JSONObject deep) {

		Map<String, String> relateMap = new HashMap<String, String>();

		String linkStr = "";

		String nodeStr = "";

		// in.id
		JSONObject in = deep.getJSONObject("in");

		if (in != null && in.containsKey("id")) {

			if (StringUtils.isNotEmpty(in.getString("id"))) {
				
				// 3 道路NODE；
				if (in.getInt("type") == 3) {

					nodeStr += in.getString("id") + LINK_SEPARATOR;
				} else {
					linkStr += in.getString("id") + LINK_SEPARATOR;
				}
			}
		}

		// [o_array].out.id
		JSONArray o_array = deep.getJSONArray("o_array");

		if (o_array != null) {

			for (Object obj : o_array) {

				JSONObject oInfo = JSONObject.fromObject(obj);

				if (oInfo != null && oInfo.containsKey("out")) {

					JSONObject out = oInfo.getJSONObject("out");

					if (out != null && out.containsKey("id")) {

						if (StringUtils.isNotEmpty(out.getString("id"))) {
							
							
							// 3 道路NODE；
							if (out.getInt("type") == 3) {

								nodeStr += out.getString("id") + LINK_SEPARATOR;
							} else {
								linkStr += out.getString("id") + LINK_SEPARATOR;
							}
						}
					}

				}

			}

		}

		if (StringUtils.isNotEmpty(linkStr)) {
			linkStr = LINK_SEPARATOR + linkStr;
		}

		if (StringUtils.isNotEmpty(nodeStr)) {
			nodeStr = LINK_SEPARATOR + nodeStr;
		}

		relateMap.put("relate_nodes", nodeStr);

		relateMap.put("relate_links", linkStr);

		return relateMap;
	}

	/**
	 * 8.复杂关系-车信（in.id+[o_array].[d_array].out.id(out是对象)）
	 * 
	 * @param deep
	 * @return
	 */
	private static Map<String, String> getLine8(JSONObject deep) {

		Map<String, String> relateMap = new HashMap<String, String>();

		String linkStr = "";

		String nodeStr = "";

		// in.id
		JSONObject in = deep.getJSONObject("in");

		if (in != null && in.containsKey("id")) {

			if (StringUtils.isNotEmpty(in.getString("id"))) {
				
				// 3 道路NODE；
				if (in.getInt("type") == 3) {

					nodeStr += in.getString("id") + LINK_SEPARATOR;
				} else {
					linkStr += in.getString("id") + LINK_SEPARATOR;
				}
			}

		}

		// [o_array].out.id
		JSONArray o_array = deep.getJSONArray("o_array");

		if (o_array != null) {

			for (Object obj : o_array) {

				JSONObject oInfo = JSONObject.fromObject(obj);

				if (oInfo != null && oInfo.containsKey("d_array")) {

					JSONArray d_array = oInfo.getJSONArray("d_array");

					if (d_array != null) {

						for (Object object : d_array) {

							JSONObject dInfo = JSONObject.fromObject(object);

							if (dInfo != null && dInfo.containsKey("out")) {

								JSONObject out = dInfo.getJSONObject("out");

								if (out != null && out.containsKey("id")) {

									if (StringUtils.isNotEmpty(out
											.getString("id"))) {

										// 3 道路NODE；
										if (out.getInt("type") == 3) {

											nodeStr += out.getString("id") + LINK_SEPARATOR;
										} else {
											linkStr += out.getString("id") + LINK_SEPARATOR;
										}

									}
								}

							}
						}
					}

				}

			}

		}

		if (StringUtils.isNotEmpty(linkStr)) {
			linkStr = LINK_SEPARATOR + linkStr;
		}
		if (StringUtils.isNotEmpty(nodeStr)) {
			nodeStr = LINK_SEPARATOR + nodeStr;
		}

		relateMap.put("relate_nodes", nodeStr);

		relateMap.put("relate_links", linkStr);

		return relateMap;
	}

	/**
	 * 9.复杂关系-公交车道（[ln].[o_array].id+[f_array].id)
	 * 
	 * @param deep
	 * @return
	 */
	private static Map<String, String> getLine9(JSONObject deep) {

		Map<String, String> relateMap = new HashMap<String, String>();

		String linkStr = "";

		String nodeStr = "";

		// [ln].[o_array].id
		JSONArray ln = deep.getJSONArray("ln");

		if (ln != null) {

			for (Object obj : ln) {

				JSONObject lnInfo = JSONObject.fromObject(obj);

				if (lnInfo != null && lnInfo.containsKey("o_array")) {

					JSONArray o_array = lnInfo.getJSONArray("o_array");

					if (o_array != null) {
						for (Object object : o_array) {

							JSONObject oInfo = JSONObject.fromObject(object);

							if (oInfo != null && oInfo.containsKey("id")) {
								if (StringUtils.isNotEmpty(oInfo
										.getString("id"))) {

									// 3 道路NODE；
									if (oInfo.getInt("type") == 3) {

										nodeStr += oInfo.getString("id") + LINK_SEPARATOR;
									} else {
										linkStr += oInfo.getString("id") + LINK_SEPARATOR;
									}
								}
							}

						}
					}

				}

			}
		}
		// [f_array].id
		JSONArray f_array = deep.getJSONArray("f_array");

		if (f_array != null) {

			for (Object obj : f_array) {

				JSONObject oInfo = JSONObject.fromObject(obj);

				if (oInfo != null && oInfo.containsKey("id")) {

					if (StringUtils.isNotEmpty(oInfo.getString("id"))) {
						
						// 3 道路NODE；
						if (oInfo.getInt("type") == 3) {

							nodeStr += oInfo.getString("id") + LINK_SEPARATOR;
						} else {
							linkStr += oInfo.getString("id") + LINK_SEPARATOR;
						}

					}
				}

			}
		}

		if (StringUtils.isNotEmpty(linkStr)) {
			linkStr = LINK_SEPARATOR + linkStr;
		}
		if (StringUtils.isNotEmpty(nodeStr)) {
			nodeStr = LINK_SEPARATOR + nodeStr;
		}

		relateMap.put("relate_nodes", nodeStr);

		relateMap.put("relate_links", linkStr);

		return relateMap;
	}

	/**
	 * 10.复杂关系-可变导向车道（f.id+[ln].[o_array].out.id)
	 * 
	 * @param deep
	 * @return
	 */
	private static Map<String, String> getLine10(JSONObject deep) {

		Map<String, String> relateMap = new HashMap<String, String>();

		String linkStr = "";

		String nodeStr = "";

		// f.id

		JSONObject f = deep.getJSONObject("f");

		if (f != null && f.containsKey("id")) {

			if (StringUtils.isNotEmpty(f.getString("id"))) {
				
				// 3 道路NODE；
				if (f.getInt("type") == 3) {

					nodeStr += f.getString("id") + LINK_SEPARATOR;
				} else {
					linkStr += f.getString("id") + LINK_SEPARATOR;
				}
			}

		}

		// [ln].[o_array].out.id
		JSONArray ln = deep.getJSONArray("ln");

		if (ln != null) {

			for (Object obj : ln) {

				JSONObject lnInfo = JSONObject.fromObject(obj);

				if (lnInfo != null && lnInfo.containsKey("o_array")) {

					JSONArray o_array = lnInfo.getJSONArray("o_array");

					if (o_array != null) {

						for (Object object : o_array) {

							JSONObject oInfo = JSONObject.fromObject(object);

							if (oInfo != null && oInfo.containsKey("out")) {

								JSONObject out = oInfo.getJSONObject("out");

								if (out != null && out.containsKey("id")) {

									if (StringUtils.isNotEmpty(out
											.getString("id"))) {
										
										// 3 道路NODE；
										if (out.getInt("type") == 3) {

											nodeStr += out.getString("id") + LINK_SEPARATOR;
										} else {
											linkStr +=out.getString("id") + LINK_SEPARATOR;
										}
									}
								}

							}

						}
					}

				}

			}

		}

		if (StringUtils.isNotEmpty(linkStr)) {
			linkStr = LINK_SEPARATOR + linkStr;
		}
		if (StringUtils.isNotEmpty(nodeStr)) {
			nodeStr = LINK_SEPARATOR + nodeStr;
		}

		relateMap.put("relate_nodes", nodeStr);

		relateMap.put("relate_links", linkStr);

		return relateMap;
	}

	/**
	 * 11.复杂关系（in.id+out.id)
	 * 
	 * @param deep
	 * @return
	 */
	private static Map<String, String> getLine11(JSONObject deep) {

		Map<String, String> relateMap = new HashMap<String, String>();

		String linkStr = "";

		String nodeStr = "";

		// in.id
		JSONObject in = deep.getJSONObject("in");

		if (in != null && in.containsKey("id")) {

			if (StringUtils.isNotEmpty(in.getString("id"))) {
				
				// 3 道路NODE；
				if (in.getInt("type") == 3) {

					nodeStr += in.getString("id") + LINK_SEPARATOR;
				} else {
					linkStr +=in.getString("id") + LINK_SEPARATOR;
				}
			}
		}

		// out.id
		JSONObject out = deep.getJSONObject("out");

		if (out != null && out.containsKey("id")) {

			if (out.containsKey("id")
					&& StringUtils.isNotEmpty(out.getString("id"))) {
				// 3 道路NODE；
				if (out.getInt("type") == 3) {

					nodeStr += out.getString("id") + LINK_SEPARATOR;
				} else {
					linkStr +=out.getString("id") + LINK_SEPARATOR;
				}
			}

		}

		if (StringUtils.isNotEmpty(linkStr)) {
			linkStr = LINK_SEPARATOR + linkStr;
		}
		if (StringUtils.isNotEmpty(nodeStr)) {
			nodeStr = LINK_SEPARATOR + nodeStr;
		}

		relateMap.put("relate_nodes", nodeStr);

		relateMap.put("relate_links", linkStr);

		return relateMap;
	}

	/**
	 * 12.复杂关系in.id+[o_array].[out].id 【out是数组】
	 * 
	 * @param deep
	 * @return
	 */
	private static Map<String, String> getLine12(JSONObject deep) {

		Map<String, String> relateMap = new HashMap<String, String>();

		String linkStr = "";

		String nodeStr = "";

		// in.id
		JSONObject in = deep.getJSONObject("in");

		if (in != null && in.containsKey("id")) {

			if (StringUtils.isNotEmpty(in.getString("id"))) {
				
				
				// 3 道路NODE；
				if (in.getInt("type") == 3) {

					nodeStr += in.getString("id") + LINK_SEPARATOR;
				} else {
					linkStr +=in.getString("id") + LINK_SEPARATOR;
				}
			}
		}

		// [o_array].[out].id 【out是数组】

		JSONArray o_array = deep.getJSONArray("o_array");
		if (o_array != null) {
			for (Object object : o_array) {

				JSONObject oInfo = JSONObject.fromObject(object);

				if (oInfo != null && oInfo.containsKey("out")) {

					JSONArray out = oInfo.getJSONArray("out"); // 是数组哦

					if (out != null) {

						for (Object object2 : out) {

							JSONObject outInfo = JSONObject.fromObject(object2);

							if (outInfo != null && outInfo.containsKey("id")) {

								if (StringUtils.isNotEmpty(outInfo
										.getString("id"))) {

									
									// 3 道路NODE；
									if (outInfo.getInt("type") == 3) {

										nodeStr += outInfo.getString("id") + LINK_SEPARATOR;
									} else {
										linkStr +=outInfo.getString("id") + LINK_SEPARATOR;
									}
								}
							}

						}
					}

				}

			}

		}

		if (StringUtils.isNotEmpty(linkStr)) {
			linkStr = LINK_SEPARATOR + linkStr;
		}
		if (StringUtils.isNotEmpty(nodeStr)) {
			nodeStr = LINK_SEPARATOR + nodeStr;
		}

		relateMap.put("relate_nodes", nodeStr);

		relateMap.put("relate_links", linkStr);

		return relateMap;
	}


}
