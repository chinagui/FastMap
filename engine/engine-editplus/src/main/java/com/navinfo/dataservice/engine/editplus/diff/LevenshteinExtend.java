package com.navinfo.dataservice.engine.editplus.diff;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;


/**
 * 编辑距离算法，首先由俄国科学家Levenshtein提出的，又叫Levenshtein Distance
 * 主要用来计算从原串（s）转换到目标串(t)所需要的最少的插入，删除和替换的数目， 在NLP中应用比较广泛，同时也常用来计算你对原文所作的改动数
 */
public class LevenshteinExtend {
	private static int compare(String str, String target) {

		int d[][]; // 矩阵
		int n = str.length();
		int m = target.length();
		int i; // 遍历str的
		int j; // 遍历target的
		char ch1; // str的
		char ch2; // target的

		int temp; // 记录相同字符,在某个矩阵位置值的增量,不是0就是1

		if (n == 0) {
			return m;
		}

		if (m == 0) {
			return n;
		}

		d = new int[n + 1][m + 1];
		for (i = 0; i <= n; i++) { // 初始化第一列
			d[i][0] = i;
		}

		for (j = 0; j <= m; j++) { // 初始化第一行
			d[0][j] = j;
		}

		for (i = 1; i <= n; i++) { // 遍历str
			ch1 = str.charAt(i - 1);
			// 去匹配target
			for (j = 1; j <= m; j++) {
				ch2 = target.charAt(j - 1);
				if (ch1 == ch2) {
					temp = 0;
				} else {
					temp = 1;
				}
				// 左边+1,上边+1, 左上角+temp取最小
				d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1]
						+ temp);
			}
		}
		return d[n][m];
	}

	private static int min(int one, int two, int three) {
		return (one = one < two ? one : two) < three ? one : three;

	}

	/**
	 * 
	 * 获取两字符串的相似度
	 * 
	 * 
	 * 
	 * @param str
	 * 
	 * @param target
	 * 
	 * @return
	 */

	public static float getSimilarityRatio(String str, String target) {

		return 1 - (float) compare(str, target)

		/ Math.max(str.length(), target.length());

	}

//	public static double getSimilarityRatioModify(Source s, FusePoi fp) {
//
//		Poi p = fp.getAttributes();
//		if (p == null || s == null)
//			return 0;
//		double r1 = 0, r2 = 0, r3 = 0, r4 = 0, r5 = 0;
//		double x1, y1, x2, y2;
//		double d;
//		double tempc = 0;
//
//		// 判断source源与对应的poi是否为同一种类别
//		String sourceKindCode = s.getMatchs().getMatchs_kindcode();
//		String poiKindCode = fp.getAttributes().getKindCode();
//
//		if (sourceKindCode == null || "".equals(sourceKindCode)
//				|| poiKindCode == null || "".equals(poiKindCode)) {
//			if (!PoiPoitfixUtil.checkName(s.getMatchs().getMatchs_name(),
//					p.getName())) {
//				return 0.0;
//			}
//		} else if (!sourceKindCode.contains(poiKindCode)) {
//			return 0.0;
//		}
//
//		// 计算距离的相似度。（对应算法：欧式距离相似度）
//		if ((!s.getMatchs().getMatchs_latitude().equals(""))
//				&& (!s.getMatchs().getMatchs_longitude().equals("")))
//			try {
//				x1 = Double.valueOf(s.getMatchs().getMatchs_latitude());
//				y1 = Double.valueOf(s.getMatchs().getMatchs_longitude());
//				x2 = Double.valueOf(p.getLocation().getLatitude());
//				y2 = Double.valueOf(p.getLocation().getLongitude());
//				d = LngLat.distanceByLngLat(y1, x1, y2, x2);
//				if (s.getMatchs().getIsGeocoding().equals("0"))
//					if (d > 0.015)
//						return 0;
//				// 停车场时增大距离的权重
//				if (sourceKindCode != null
//						&& !"".equals(sourceKindCode)
//						&& SourceConstant.SOGOPARK_TargetKindCode
//								.contains(sourceKindCode)) {
//					r1 = 90 / (90 + d);
//				} else {
//					r1 = 1 / (1 + d);
//
//				}
//
//				if (r1 != 0)
//					tempc += 0.2;
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//		// 计算名称、地址、电话、邮编的相似度。（对应算法：编辑距离公式）
//		if ((s.getMatchs().getMatchs_name() != null)
//				&& (!s.getMatchs().getMatchs_name().equals(""))
//				&& (p.getName() != null) && (!p.getName().equals(""))) {
//			// 当数据源的名称为停用词时，返回0
//
//			boolean stopFlag = (StopwordsConstant.getPostfixMap()).get(s
//					.getMatchs().getMatchs_name()) == null ? false
//					: (Boolean) (StopwordsConstant.getPostfixMap()).get(s
//							.getMatchs().getMatchs_name());
//			if (stopFlag) {
//				System.out.println("55555555555555555555555:     "
//						+ sourceKindCode + "   poiKindCode: " + poiKindCode);
//				r2 = 0;
//			} else {
//				// System.out.println("666666666666666666:     "+sourceKindCode+"   poiKindCode: "+poiKindCode+"  s.getMatchs().getMatchs_name()  "+s.getMatchs().getMatchs_name());
//				r2 = getSimilarityRatio(s.getMatchs().getMatchs_name(),
//						p.getName());
//			}
//			if (r2 != 0)
//				tempc += 0.3;
//		}
//
//		if ((s.getMatchs().getMatchs_address() != null)
//				&& (!s.getMatchs().getMatchs_address().equals(""))
//				&& (p.getAddress() != null) && (!p.getAddress().equals(""))) {
//			r3 = getSimilarityRatio(s.getMatchs().getMatchs_address(),
//					p.getAddress());
//			if (r3 != 0)
//				tempc += 0.27;
//		}
//
//		if ((s.getMatchs().getMatchs_postcode() != null)
//				&& (!s.getMatchs().getMatchs_postcode().equals(""))
//				&& (p.getPostCode() != null) && (!p.getPostCode().equals(""))) {
//			r4 = getSimilarityRatio(s.getMatchs().getMatchs_postcode(),
//					p.getPostCode());
//			if (r4 != 0)
//				tempc += 0.03;
//		}
//
//		// 不同的电话之间为独立事件
//		if ((s.getMatchs().getMatchs_telephone() != null)
//				&& (!s.getMatchs().getMatchs_telephone().equals(""))
//				&& (p.getContacts() != null)) {
//			String sourcePhone[] = (s.getMatchs().getMatchs_telephone())
//					.split(";");
//			List<Contact> contacts = JSONArray.toList(
//					JSONArray.fromObject(p.getContacts()), new Contact(),
//					new JsonConfig());
//			if (sourcePhone != null && contacts != null) {
//				double r5_temp = 1;
//				for (int i = 0; i < sourcePhone.length; i++) {
//					double temp = 0;
//					for (int j = 0; j < contacts.size(); j++) {
//						Contact c = contacts.get(j);
//						String number = contacts.get(j).getNumber();
//						if (number != null && !"".equals(number))
//							number = number.replace("+86", "").replaceAll(
//									"\\D", "");
//						String str = sourcePhone[i];
//						if (str != null && !"".equals(str))
//							str = str.replace("+86", "").replaceAll("\\D", "");
//
//						if (str != null && number != null && (!str.equals(""))
//								&& (!number.equals("")))
//
//							if (str.equals(number))
//								return 1;
//						double t = 0;
//						if (t > temp)
//							temp = t;
//					}
//					r5_temp *= (1 - temp);
//				}
//				r5 = 1 - r5_temp;
//				if (r5 != 0)
//					tempc += 0.2;
//			}
//
//		}
//
//		try {
//			List<Source> sources = JSONArray.toList(
//					JSONArray.fromObject(fp.getSources()), new Source(),
//					new JsonConfig());
//			if (sources.size() > 0) {
//				for (Source source : sources) {
//					if (("1").equals(source.getCpAcception())
//							&& (source.getOriginalContent().getString("uuid"))
//									.equals(s.getOriginalContent().getString(
//											"uuid"))) {
//						return 1.0;
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//		double similarity1 = 1 - (1 - r1) * (1 - r2)
//				* (1 - 0.9 * r3 - 0.1 * r4) * (1 - r5);
//
//		double similarity2 = 0.0;
//		if (tempc < 0.3) {
//			similarity2 = (0.2 * r1 + 0.3 * r2 + 0.27 * r3 + 0.03 * r4 + 0.2 * r5);
//		} else {
//			similarity2 = (0.2 * r1 + 0.3 * r2 + 0.27 * r3 + 0.03 * r4 + 0.2 * r5)
//					/ tempc;
//		}
//
//		return (similarity1 + similarity2) / 2.0;
//	}
//
//	/**
//	 * 代理店单挑下挂
//	 * 
//	 * @param s
//	 * @param p
//	 * @param similarityMap
//	 * @return
//	 */
//	public static double getSimilarityRatioModify(Source s, Poi p,
//			Map<String, Double> similarityMap) {
//		if (p == null || s == null)
//			return 0;
//		double r1 = 0, r2 = 0, r3 = 0, r4 = 0, r5 = 0;
//		double x1, y1, x2, y2;
//		double d;
//		double tempc = 0;
//
//		double r1_weight = similarityMap.get("r1").doubleValue();
//		double r2_weight = similarityMap.get("r2").doubleValue();
//		double r3_weight = similarityMap.get("r3").doubleValue();
//		double r4_weight = similarityMap.get("r4").doubleValue();
//		double r5_weight = similarityMap.get("r5").doubleValue();
//		double minWeight = similarityMap.get("minWeight").doubleValue();
//		// System.out.println("权重信息：    r1_weight: "+r1_weight+" r2_weight: "+r2_weight+" r3_weight: "+r3_weight+" r4_weight: "+r4_weight+" r5_weight: "+r5_weight);
//
//		// 判断source源与对应的poi是否为同一种类别
//		String sourceKindCode = s.getMatchs().getMatchs_kindcode();
//		String poiKindCode = p.getKindCode();
//
//		if (sourceKindCode == null || "".equals(sourceKindCode)
//				|| poiKindCode == null || "".equals(poiKindCode)) {
//			if (!PoiPoitfixUtil.checkName(s.getMatchs().getMatchs_name(),
//					p.getName())) {
//				return 0.0;
//			}
//		} else if (!sourceKindCode.contains(poiKindCode)) {
//			return 0.0;
//		}
//
//		// 计算距离的相似度。（对应算法：欧式距离相似度）
//		if ((!s.getMatchs().getMatchs_latitude().equals(""))
//				&& (!s.getMatchs().getMatchs_longitude().equals("")))
//			try {
//				x1 = Double.valueOf(s.getMatchs().getMatchs_latitude());
//				y1 = Double.valueOf(s.getMatchs().getMatchs_longitude());
//				x2 = Double.valueOf(p.getLocation().getLatitude());
//				y2 = Double.valueOf(p.getLocation().getLongitude());
//				d = LngLat.distanceByLngLat(y1, x1, y2, x2);
//				if (s.getMatchs().getIsGeocoding().equals("0") && d > 0.015)
//					return 0;
//				// 停车场时增大距离的权重
//				if (sourceKindCode != null
//						&& !"".equals(sourceKindCode)
//						&& SourceConstant.SOGOPARK_TargetKindCode
//								.contains(sourceKindCode)) {
//					r1 = 90 / (90 + d);
//				} else {
//					r1 = 1 / (1 + d);
//				}
//				if (r1 != 0)
//					tempc += r1_weight;
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//		// 计算名称、地址、电话、邮编的相似度。（对应算法：编辑距离公式）
//		if ((s.getMatchs().getMatchs_name() != null)
//				&& (!s.getMatchs().getMatchs_name().equals(""))
//				&& (p.getName() != null) && (!p.getName().equals(""))) {
//			// 当数据源的名称为停用词时，返回0
//
//			boolean stopFlag = (StopwordsConstant.getPostfixMap()).get(s
//					.getMatchs().getMatchs_name()) == null ? false
//					: (Boolean) (StopwordsConstant.getPostfixMap()).get(s
//							.getMatchs().getMatchs_name());
//			if (stopFlag) {
//				r2 = 0;
//			} else {
//				r2 = getSimilarityRatio(s.getMatchs().getMatchs_name(),
//						p.getName());
//			}
//			if (r2 != 0)
//				tempc += r2_weight;
//		}
//
//		if ((s.getMatchs().getMatchs_address() != null)
//				&& (!s.getMatchs().getMatchs_address().equals(""))
//				&& (p.getAddress() != null) && (!p.getAddress().equals(""))) {
//			r3 = getSimilarityRatio(s.getMatchs().getMatchs_address(),
//					p.getAddress());
//
//			if (p.getName() != null && !"".equals(p.getName())) {
//				int length2 = p.getName().length();
//				int length = s.getMatchs().getMatchs_address().length();
//				if (length / length2 <= 1 || length2 / length <= 1) {
//					float r3Temp = getSimilarityRatio(s.getMatchs()
//							.getMatchs_address(), p.getName());
//					if (r3Temp > r3) {
//						r3 = r3Temp;
//					}
//				}
//			}
//
//			if (r3 != 0)
//				tempc += r3_weight;
//		}
//
//		if ((s.getMatchs().getMatchs_postcode() != null)
//				&& (!s.getMatchs().getMatchs_postcode().equals(""))
//				&& (p.getPostCode() != null) && (!p.getPostCode().equals(""))) {
//			r4 = getSimilarityRatio(s.getMatchs().getMatchs_postcode(),
//					p.getPostCode());
//			if (r4 != 0)
//				tempc += r4_weight;
//		}
//
//		// 不同的电话之间为独立事件
//		if ((s.getMatchs().getMatchs_telephone() != null)
//				&& (!s.getMatchs().getMatchs_telephone().equals(""))
//				&& (p.getContacts() != null)) {
//
//			String sourcePhone[] = (s.getMatchs().getMatchs_telephone())
//					.split(";");
//
//			List<Contact> contacts = JSONArray.toList(
//					JSONArray.fromObject(p.getContacts()), new Contact(),
//					new JsonConfig());
//
//			if (!TelephoneUtil.isEmp(contacts)
//					&& !TelephoneUtil.isEmp(sourcePhone)) {
//
//				for (int i = 0; i < sourcePhone.length; i++)
//					for (int j = 0; j < contacts.size(); j++) {
//						String number = contacts.get(j).getNumber();
//						if (number != null && !"".equals(number))
//							number = number.replace("+86", "").replaceAll(
//									"\\D", "");
//						String str = sourcePhone[i];
//						if (str != null && !"".equals(str))
//							str = str.replace("+86", "").replaceAll("\\D", "");
//						if (str != null && number != null && (!str.equals(""))
//								&& (!number.equals(""))) {
//							if (str.equals(number))
//								return 1;
//						}
//
//					}
//				tempc += r5_weight;
//			}
//
//		}
//
//		double similarity1 = 1 - (1 - r1) * (1 - r2)
//				* (1 - 0.9 * r3 - 0.1 * r4) * (1 - r5);
//		double similarity2 = 0.0;
//		if (tempc < minWeight) {
//			similarity2 = (0.2 * r1 + 0.3 * r2 + 0.27 * r3 + 0.03 * r4 + 0.2 * r5);
//		} else {
//			similarity2 = (0.2 * r1 + 0.3 * r2 + 0.27 * r3 + 0.03 * r4 + 0.2 * r5)
//					/ tempc;
//		}
//
//		return (similarity1 + similarity2) / 2.0;
//	}
//
//	/**
//	 * @param s
//	 * @param fp
//	 * @param similarityMap
//	 * @return
//	 */
//	public static double getSimilarityRatioModify(FastSource s, BusinessPoi fp,
//			Map<String, Double> similarityMap) {
//
//		if (fp == null || s == null)
//			return 0;
//		double r1 = 0, r2 = 0, r3 = 0, r4 = 0, r5 = 0;
//		double x1, y1, x2, y2;
//		double d;
//		double tempc = 0;
//
//		double r1_weight = similarityMap.get("r1").doubleValue();
//		double r2_weight = similarityMap.get("r2").doubleValue();
//		double r3_weight = similarityMap.get("r3").doubleValue();
//		double r4_weight = similarityMap.get("r4").doubleValue();
//		double r5_weight = similarityMap.get("r5").doubleValue();
//		double minWeight = similarityMap.get("minWeight").doubleValue();
//		// System.out.println("权重信息：    r1_weight: "+r1_weight+" r2_weight: "+r2_weight+" r3_weight: "+r3_weight+" r4_weight: "+r4_weight+" r5_weight: "+r5_weight);
//
//		// 判断fastsource源与对应的businesspoi是否为同一种类别
//		String sourceKindCode = s.getKindCode();
//		String poiKindCode = fp.getKindCode();
//
//		if (sourceKindCode == null || "".equals(sourceKindCode)
//				|| poiKindCode == null || "".equals(poiKindCode)) {
//			if (!PoiPoitfixUtil.checkName(s.getName(), fp.getName())) {
//				return 0.0;
//			}
//		} else if (!sourceKindCode.contains(poiKindCode)) {
//			return 0.0;
//		}
//
//		// 计算距离的相似度。（对应算法：欧式距离相似度）
//		if ((!s.getY().equals("")) && (!s.getX().equals("")))
//			try {
//				x1 = Double.valueOf(s.getY());
//				y1 = Double.valueOf(s.getX());
//				x2 = Double.valueOf(fp.getY());
//				y2 = Double.valueOf(fp.getX());
//				d = LngLat.distanceByLngLat(y1, x1, y2, x2);
//				// 停车场时增大距离的权重
//				if (sourceKindCode != null
//						&& !"".equals(sourceKindCode)
//						&& SourceConstant.SOGOPARK_TargetKindCode
//								.contains(sourceKindCode)) {
//					r1 = 90 / (90 + d);
//				} else {
//					r1 = 1 / (1 + d);
//				}
//				if (r1 != 0)
//					tempc += r1_weight;
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
//		// 计算名称、地址、电话、邮编的相似度。（对应算法：编辑距离公式）
//		if ((s.getName() != null) && (fp.getName() != null)
//				&& (!s.getName().equals("")) && (!fp.getName().equals(""))) {
//			// 当数据源的名称为停用词时，返回0
//
//			boolean stopFlag = (StopwordsConstant.getPostfixMap()).get(s
//					.getName()) == null ? false : (Boolean) (StopwordsConstant
//					.getPostfixMap()).get(s.getName());
//			if (stopFlag) {
//				r2 = 0;
//			} else {
//				r2 = getSimilarityRatio(s.getName(), fp.getName());
//			}
//			if (r2 != 0)
//				tempc += r2_weight;
//		}
//
//		if ((s.getAddr() != null) && (fp.getAddr() != null)
//				&& (!s.getAddr().equals("")) && (!fp.getAddr().equals(""))) {
//			r3 = getSimilarityRatio(s.getAddr(), fp.getAddr());
//
//			if (fp.getName() != null && !"".equals(fp.getName())) {
//				int length2 = fp.getName().length();
//				int length = s.getAddr().length();
//				if (length / length2 <= 1 || length2 / length <= 1) {
//					float r3Temp = getSimilarityRatio(s.getAddr(), fp.getName());
//					if (r3Temp > r3) {
//						r3 = r3Temp;
//					}
//				}
//			}
//
//			if (r3 != 0)
//				tempc += r3_weight;
//		}
//
//		if ((s.getPostCode() != null) && (fp.getPostCode() != null)
//				&& (!s.getPostCode().equals(""))
//				&& (!fp.getPostCode().equals(""))) {
//			r4 = getSimilarityRatio(s.getPostCode(), fp.getPostCode());
//			if (r4 != 0)
//				tempc += r4_weight;
//		}
//
//		// 不同的电话之间为独立事件
//		if ((s.getTel() != null) && (fp.getTel() != null)
//				&& (!s.getTel().equals("")) && (!fp.getTel().equals(""))) {
//			String sourcePhone[] = (s.getTel()).split(";");
//			String poiPhone[] = (fp.getTel()).split(";");
//			if (sourcePhone.length > 0 && poiPhone.length > 0) {
//				double r5_temp = 1;
//				for (int i = 0; i < sourcePhone.length; i++) {
//					int temp = 0;
//					for (int j = 0; j < poiPhone.length; j++) {
//						String number = poiPhone[j];
//						if (number != null && !"".equals(number))
//							number = number.replace("+86", "").replaceAll(
//									"\\D", "");
//						String str = sourcePhone[i];
//						if (str != null && !"".equals(str))
//							str = str.replace("+86", "").replaceAll("\\D", "");
//						double t = 0;
//						if (str != null && number != null && (!str.equals(""))
//								&& (!number.equals(""))) {
//							if (str.equals(number)){
//								r5 = 1;
//								temp = 1;
//								break;
//							}
//							else
//								r5 = 0;
//						} else {
//							r5_weight = 0;
//						}
//
//					}
//					if(temp == 1){
//						break;
//					}
//				}
//				//r5 = 0;
//				if (r5 != 0)
//					tempc += r5_weight;
//			}
//
//		}
//
//		double similarity1 = 1 - (1 - r1) * (1 - r2)
//				* (1 - r3_weight * r3 - r4_weight * r4) * (1 - r5);
//		double similarity2 = 0.0;
//		if (tempc < minWeight) {
//			similarity2 = (r1_weight * r1 + r2_weight * r2 + r3_weight * r3
//					+ r4_weight * r4 + r5_weight * r5);
//		} else {
//			similarity2 = (r1_weight * r1 + r2_weight * r2 + r3_weight * r3
//					+ r4_weight * r4 + r5_weight * r5)
//					/ tempc;
//		}
//
//		return (similarity1 + similarity2) / 2.0;
//	}
//
//	public static void main(String[] args) {
//		// String str = "厦门市集美区后溪门北站汽车物流中心内 ";
//		// String target = "厦门市集美区后溪镇厦门北站汽车物流中心内";
//		// System.out.println("similarityRatio="
//		// + LevenshteinExtend.getSimilarityRatio(str, target));
//		FastSource s = new FastSource();
//		s.setKindCode("140101");
//		s.setName("淄博金润");
//		s.setAddr("山东省淄博市临淄区临淄大道与清田路交汇处西500米路南");
//		s.setTel("0533-7685577;");
//		s.setPostCode("255400");
//		s.setX(118.22189);
//		s.setY(36.8213);
//
//		BusinessPoi fp = new BusinessPoi();
//		fp.setKindCode("140101");
//		fp.setName("淄博金润");
//		fp.setAddr("山东省淄博市临淄区临淄大道与清田路交汇处西500米路南");
//		fp.setTel("0533-7685577;");
//		fp.setPostCode("255400");
//		fp.setX(118.22189);
//		fp.setY(36.8213);
//
//	}

}