package com.navinfo.dataservice.commons.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

/** 
* @ClassName: JSONObjectDiffUtils 
* @author Xiao Xiaowen 
* @date 2017年10月24日 上午11:00:02 
* @Description: TODO
*/
public class JSONObjectDiffUtils {
	
	public static final String STRING_SPLIT_BY_VERTICAL_LINE="stringSplitByVerticalLine";
	/**
	 * 不能传入null
	 * 支持传入Integer,Long,Float,Double,Boolean,String,JSONNull,JSONObject,JSONArray
	 * JSONNull和空字符串"",空对象{}，空数组[],认为相同
	 * JSONNull和基本类型的属性不认为相同，理论上也不应该出现此情形
	 * JSONObject中rowId属性为可选，JSONArray中如果属性值为JSONObject,那必须包含rowId属性
	 * JSONObject中先以row_id属性作为主键匹配，如果不包含row_id则按对象的属性再差分
	 * JSONArray中如果存在JSONObject对象，对象必须有row_id,否则无法差分，差分时以row_id属性作为匹配主键，row_id不存在或者为空的对象
	 * JSONArray中还能包含Integer,Long,Float,Double,Boolean,String,JSONObject,不能直接再包含数组，类似[[],[]]否则无法差分，
	 * @param tar
	 * @param ref
	 * @return tar和ref是否相同，true相同，false不同
	 */
	@SuppressWarnings("unchecked")
	public static boolean diff(Object tar,Object ref)throws Exception{
		if(tar==null||ref==null){
			throw new Exception("tar or ref cann't be null");
		}
		//开始差分
		if(tar instanceof JSONNull){
			if(ref instanceof JSONNull){
				return false;
			}
			if(ref.equals("")){
				return false;
			}
			if((ref instanceof JSONObject)&&(((JSONObject)ref).keySet().size()==0)){
				return false;
			}
			if((ref instanceof JSONArray)&&((JSONArray)ref).size()==0){
				return false;
			}
			return true;
		}else if(tar instanceof Integer){
			int tarValue = ((Integer) tar).intValue();
			int refValue = ((Integer) ref).intValue();
			if(tarValue==refValue){
				return false;
			}else{
				return true;
			}
		}else if(tar instanceof Long){
			long tarValue = ((Long) tar).longValue();
			long refValue = ((Long) ref).longValue();
			if(tarValue==refValue){
				return false;
			}else{
				return true;
			}
		}else if(tar instanceof Float){
			float tarValue = ((Float) tar).floatValue();
			float refValue = ((Float) ref).floatValue();
			if(tarValue==refValue){
				return false;
			}else{
				return true;
			}
		}else if(tar instanceof Double){
			double tarValue = ((Double) tar).doubleValue();
			double refValue = ((Double) ref).doubleValue();
			if(tarValue==refValue){
				return false;
			}else{
				return true;
			}
		}else if(tar instanceof Boolean){
			boolean tarValue = ((Boolean) tar).booleanValue();
			boolean refValue = ((Boolean) ref).booleanValue();
			if(tarValue==refValue){
				return false;
			}else{
				return true;
			}
		}else if(tar instanceof String){
			String tarValue = (String)tar;
			if(ref instanceof JSONNull){
				if(tarValue.equals("")){
					return false;
				}
				return true;
			}else{
				String refValue = (String) ref;
				if(tarValue.indexOf("|")>-1||refValue.indexOf("|")>-1){
					List<String> tSubs = Arrays.asList(tarValue.split("\\|"));
					List<String> rSubs = Arrays.asList(refValue.split("\\|"));
					if(tSubs.containsAll(rSubs)&&rSubs.containsAll(tSubs)){
						return false;
					}else{
						return true;
					}
				}else{
					return !tar.equals(refValue);
				}
			}
		}else if(tar instanceof JSONObject){
			JSONObject tarJo = (JSONObject) tar;
			if(ref instanceof JSONNull){
				if(tarJo.keySet().size()==0){
					return false;
				}else{
					return true;
				}
			}else{
				JSONObject refJo = (JSONObject) ref;
				if(tarJo.containsKey("rowId")){//如果存在rowId字段，那么目标对象和参考对象都应该包含rowId字段
					if(diff(tarJo.get("rowId"),refJo.get("rowId"))){
						return true;
					}
				}
				//其他属性
				Iterator<String> it = tarJo.keys();
				while(it.hasNext()){
					String k = it.next();
					if(diff(tarJo.get(k),refJo.get(k))){
						return true;
					}
				}
				return false;
			}
		}else if(tar instanceof JSONArray){
			JSONArray tarJa = (JSONArray) tar;
			if(ref instanceof JSONNull){
				if(tarJa.size()==0){
					return false;
				}else{
					return true;
				}
			}else{
				JSONArray refJa = (JSONArray) ref;
				if(tarJa.size()==0){
					if(refJa.size()==0){
						return false;
					}else{
						return true;
					}
					
				}else{//tarJa中一定有值
					//先确定数组中的对象类型
					Object tarJaObj = tarJa.get(0);
					if(tarJaObj instanceof JSONObject){
						List<String> trids = new ArrayList<String>();
						for(Object to:tarJa){
							JSONObject tjo = (JSONObject)to;
							if(!tjo.containsKey("rowId")){
								throw new Exception("a jsonobject in jsonarray should contain rowId key");
							}
							trids.add(tjo.getString("rowId"));
						}
						List<String> rrids = new ArrayList<String>();
						for(Object ro:refJa){
							JSONObject rjo = (JSONObject)ro;
							if(!rjo.containsKey("rowId")){
								throw new Exception("a jsonobject in jsonarray should contain rowId key");
							}
							rrids.add(rjo.getString("rowId"));
						}
						if(trids.containsAll(rrids)&&rrids.containsAll(trids)){
							for(Object to:tarJa){
								JSONObject tjo = (JSONObject)to;
								String trid = tjo.getString("rowId");
								for(Object ro:refJa){
									JSONObject rjo = (JSONObject)ro;
									String rrid = rjo.getString("rowId");
									if(trid.equals(rrid)){
										if(diff(tjo,rjo)){
											return true;
										}
									}
								}
							}
							return false;
						}else{
							return true;
						}
					}else if(tarJaObj instanceof JSONArray){
						throw new Exception("diff operation doesn't support a jsonarray contains another jsonarray");
					}else{//则是其他基础类型，使用数组差分
						if(tarJa.containsAll(refJa)&&refJa.containsAll(tarJa)){
							return false;
						}
						return true;
					}
				}
			}
		}else{
			throw new Exception("unsupported attr type");
		}
	}
	/**
	 * 
	 * @param target
	 * @param refer
	 * @param attrFilter:传入一级属性需要过滤不差分的字段
	 * @return 一级属性中有变化的属性名集合
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static Collection<String> diffFirstLevel(JSONObject target,JSONObject refer,Collection<String> attrFilter)throws Exception{
		if(target==null||refer==null){
			return null;
		}
		Collection<String> changedAttr = new HashSet<String>();
		//只差分根属性
		Iterator<String> it = target.keys(); 
		while(it.hasNext()){
			String k = it.next();
			if(attrFilter!=null&&attrFilter.contains(k)){
				continue;
			}
			Object tv = target.get(k);
			Object rv = refer.get(k);//此处refer中不能不包含此key，不能为null
			if(diff(tv,rv)){
				changedAttr.add(k);
			}
		}
		
		return changedAttr;
	}
	
	public static void main(String[] args)throws Exception {
		String jso1 = "{'fid':'00365520171019134255','name':'加油站','pid':0,'meshid':0,'kindCode':'230215',"
				+ "'guide':{'latitude':39.73235,'linkPid':212986,'longitude':116.4115},'address':'东磁村','postCode':'258863',"
				+ "'level':'B1','open24H':2,'parentFid':'','relateChildren':[],'contacts':[{'linkman':'','number':'010-25668566',"
				+ "'priority':1,'type':1,'rowId':'60CAF952E3DD4244A1D340E3EFC2FE95'},{'linkman':'','number':'18966586696',"
				+ "'priority':2,'type':2,'rowId':'41633FDA055040A6B02B7EDFFD32CF71'}],'foodtypes':{'avgCost':0,'creditCards':'1|2',"
				+ "'foodtype':'3002|2002','openHour':'07:00-20:00','parking':3,'rowId':'F0A6D52CF2DE41A0AFA967A6511946A5'},"
				+ "'parkings':null,'hotel':null,'sportsVenues':'','chargingStation':null,'chargingPole':[{'acdc':0,"
				+ "'availableState':'4','count':1,'current':'','factoryNum':'','floor':2,'groupId':1,'locationType':0,"
				+ "'manufacturer':'','mode':0,'openType':'1','parkingNum':'','payment':'4','plotNum':'','plugNum':1,'plugType':'9',"
				+ "'power':'','prices':'','productNum':'无型号','voltage':'','rowId':'28AA7E07EA44417AA5EB23A1F7696112'}],"
				+ "'gasStation':{'egType':'E92|E97','fuelType':'0|1|2|6','mgType':'M10|M50','oilType':'90',"
				+ "'openHour':'０６：００－２０：００','payment':'2','service':'2|5','servicePro':'',"
				+ "'rowId':'CD32ADC73A0C40D19FD0B87EBA5CEB08'},'indoor':{'floor':'','type':0},"
				+ "'attachments':[{'content':'B26384E0D3E74EB596AD10F794F6B0F1.jpg','extContent':null,"
				+ "'id':'B26384E0D3E74EB596AD10F794F6B0F1','tag':3,'type':1},{'content':'F70E5EB76245498C9FF55CE6B0F10878.jpg',"
				+ "'extContent':null,'id':'F70E5EB76245498C9FF55CE6B0F10878','tag':2,'type':1},"
				+ "{'content':'1B9527CCE45D480BA35F41F3E78F1394.jpg','extContent':null,'id':'1B9527CCE45D480BA35F41F3E78F1394',"
				+ "'tag':3,'type':1},{'content':'CA84F08B39A84AA4BB57E87D50D9F272.jpg',"
				+ "'extContent':null,'id':'CA84F08B39A84AA4BB57E87D50D9F272','tag':3,'type':1}],'chain':'2007','rawFields':'2|4|5',"
				+ "'t_lifecycle':3,'geometry':'POINT (116.41131 39.73234)','vipFlag':'','t_operateDate':'20171019134410',"
				+ "'truck':2,'sameFid':'','orgInfo':null,'sourceName':'Android'}";
		
		String jsoRef = "{'fid':'00365520171019134255','name':'加油站','pid':0,'meshid':0,'kindCode':'230215',"
				+ "'guide':{'latitude':39.73235,'linkPid':212986,'longitude':116.4115},'address':'东磁村','postCode':'258863',"
				+ "'level':'B1','open24H':2,'parentFid':'','relateChildren':[],'contacts':[{'linkman':'','number':'010-25668566',"
				+ "'priority':1,'type':1,'rowId':'60CAF952E3DD4244A1D340E3EFC2FE95'},{'linkman':'','number':'18966586696',"
				+ "'priority':2,'type':2,'rowId':'41633FDA055040A6B02B7EDFFD32CF71'}],'foodtypes':{'avgCost':0,'creditCards':'1|2',"
				+ "'foodtype':'3002|2002','openHour':'07:00-20:00','parking':3,'rowId':'F0A6D52CF2DE41A0AFA967A6511946A5'},"
				+ "'parkings':null,'hotel':null,'sportsVenues':'','chargingStation':null,'chargingPole':[{'acdc':0,"
				+ "'availableState':'4','count':1,'current':'','factoryNum':'','floor':1,'groupId':1,'locationType':0,"
				+ "'manufacturer':'','mode':0,'openType':'1','parkingNum':'','payment':'4','plotNum':'','plugNum':1,'plugType':'9',"
				+ "'power':'','prices':'','productNum':'无型号','voltage':'','rowId':'28AA7E07EA44417AA5EB23A1F7696112'}],"
				+ "'gasStation':{'egType':'E92|E97','fuelType':'1|0|2|6','mgType':'M10|M50','oilType':'90',"
				+ "'openHour':'０６：００－２０：００','payment':'2','service':'2|5','servicePro':'',"
				+ "'rowId':'CD32ADC73A0C40D19FD0B87EBA5CEB08'},'indoor':{'floor':'1','type':0},"
				+ "'attachments':[{'content':'B26384E0D3E74EB596AD10F794F6B0F1.jpg','extContent':null,"
				+ "'id':'B26384E0D3E74EB596AD10F794F6B0F1','tag':3,'type':1},{'content':'F70E5EB76245498C9FF55CE6B0F10878.jpg',"
				+ "'extContent':null,'id':'F70E5EB76245498C9FF55CE6B0F10878','tag':2,'type':1},"
				+ "{'content':'1B9527CCE45D480BA35F41F3E78F1394.jpg','extContent':null,'id':'1B9527CCE45D480BA35F41F3E78F1394',"
				+ "'tag':3,'type':1},{'content':'CA84F08B39A84AA4BB57E87D50D9F272.jpg',"
				+ "'extContent':null,'id':'CA84F08B39A84AA4BB57E87D50D9F272','tag':3,'type':1}],'chain':'2007','rawFields':'2|4|5',"
				+ "'t_lifecycle':2,'geometry':'POINT (116.41131 39.73234)','vipFlag':'','t_operateDate':'20171019134410',"
				+ "'truck':2,'sameFid':'','orgInfo':null,'sourceName':'Android'}";
		
		JSONObject tar = JSONObject.fromObject(jso1);
		JSONObject ref = JSONObject.fromObject(jsoRef);
		long t1 = System.currentTimeMillis();
		List<String> poiFilterFields = new ArrayList<String>();
		poiFilterFields.add("pid");
		poiFilterFields.add("fid");
		poiFilterFields.add("rawFields");
		poiFilterFields.add("t_lifecycle");
		poiFilterFields.add("sourceName");
		poiFilterFields.add("parentFid");
		poiFilterFields.add("sameFid");
		poiFilterFields.add("orgInfo");
		poiFilterFields.add("attachments");
		poiFilterFields.add("contacts");
		poiFilterFields.add("childFid");
		poiFilterFields.add("indoor");
		Collection<String> changedAttr = diffFirstLevel(tar,ref,poiFilterFields);
		System.out.println("time:"+(System.currentTimeMillis()-t1));
		System.out.println("changed attr:"+StringUtils.join(changedAttr,","));
	}
}
