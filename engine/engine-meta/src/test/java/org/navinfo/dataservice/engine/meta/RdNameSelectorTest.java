/**
 * 
 */
package org.navinfo.dataservice.engine.meta;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.engine.meta.pinyin.PinyinConverter;
import com.navinfo.dataservice.engine.meta.rdname.RdNameImportor;
import com.navinfo.dataservice.engine.meta.rdname.RdNameSelector;
import com.navinfo.dataservice.engine.meta.service.ScPointPoicodeNewService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName: RdNameSelectorTest 
* @author Zhang Xiaolong
* @date 2016年8月15日 下午5:49:43 
* @Description: TODO
*/
public class RdNameSelectorTest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
//	@Test
	public void testGetRdName()
	{
		String parameter = "{'subtaskId':76,'pageNum':1,'pageSize':20,'flag':1,'sortby':'','params':{'name':'','nameGroupid':304000027,'adminId':''}}";//

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			RdNameSelector selector = new RdNameSelector();
			
			int subtaskId = jsonReq.getInt("subtaskId");
			
			ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			
			/*FccApi apiFcc=(FccApi) ApplicationContextUtil.getBean("fccApi");
			
			JSONArray tips = apiFcc.searchDataBySpatial(subtask.getGeometry(),1901,new JSONArray());
			*/
			String tipsStr = "[{'id':'021901C265833BBC274F858C16760D95BF0A53','wkt':'POINT (116.38779 39.98016)','stage':1,'t_operateDate':'20170107153440','t_date':'20170302160111','t_lifecycle':3,'t_command':0,'t_cStatus':1,'t_dStatus':0,'t_inMeth':0,'t_pStatus':0,'t_dInProc':0,'t_mInProc':0,'handler':3662,'t_mStatus':0,'s_sourceCode':2,'s_sourceType':'1901','g_location':{'type':'LineString','coordinates':[[116.3252,39.8014],[116.32619,39.80295]]},'g_guide':{'type':'Point','coordinates':[116.32569,39.80218]},'deep':{'geo':{'coordinates':[116.32569,39.80218],'type':'Point'},'n_array':['左']},'feedback':{'f_array':[]},'s_reliability':100,'tipdiff':{},'s_qTaskId':0,'s_mTaskId':0,'_version_':1560744057518424064,'t':1}]";
			
			
			JSONArray tips = JSONArray.fromObject(tipsStr);
					//new JSONArray();
			/*JSONObject jobj = new JSONObject();
					jobj.put("id", "02190105732d7d9fb74c6e9c66c8269b19d371");
			JSONObject jobj2 = new JSONObject();
					jobj2.put("id", "0219010714071FD07048E191F7803BE65EA02B");
					tips.add(jobj);
					tips.add(jobj2);*/
			
			JSONObject data = selector.searchByName("京", 20, 1);//selector.searchForWeb(jsonReq,tips);
			
			System.out.println(data);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	@Test
	public void testSearchRdName()
	{
		String parameter = "{'subtaskId':76,'pageNum':1,'pageSize':20,'flag':1,'sortby':'-name','params':{'name':'','nameGroupid':'','adminId':''}}";//

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			RdNameSelector selector = new RdNameSelector();
			
			int subtaskId = jsonReq.getInt("subtaskId");
			
			/*ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
			
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);*/
			
			/*FccApi apiFcc=(FccApi) ApplicationContextUtil.getBean("fccApi");
			
			JSONArray tips = apiFcc.searchDataBySpatial(subtask.getGeometry(),1901,new JSONArray());
			*/
			String tipsStr = "[{'id':'021901d59b5e2f89c443a08f5a68690edf60c5','wkt':'POINT (116.38779 39.98016)','stage':1,'t_operateDate':'20170107153440','t_date':'20170302160111','t_lifecycle':3,'t_command':0,'t_cStatus':1,'t_dStatus':0,'t_inMeth':0,'t_pStatus':0,'t_dInProc':0,'t_mInProc':0,'handler':3662,'t_mStatus':0,'s_sourceCode':2,'s_sourceType':'1901','g_location':{'type':'LineString','coordinates':[[116.3252,39.8014],[116.32619,39.80295]]},'g_guide':{'type':'Point','coordinates':[116.32569,39.80218]},'deep':{'geo':{'coordinates':[116.32569,39.80218],'type':'Point'},'n_array':['左']},'feedback':{'f_array':[]},'s_reliability':100,'tipdiff':{},'s_qTaskId':0,'s_mTaskId':0,'_version_':1560744057518424064,'t':1}]";
			
			
			JSONArray tips = JSONArray.fromObject(tipsStr);
					//new JSONArray();
			/*JSONObject jobj = new JSONObject();
					jobj.put("id", "02190105732d7d9fb74c6e9c66c8269b19d371");
			JSONObject jobj2 = new JSONObject();
					jobj2.put("id", "0219010714071FD07048E191F7803BE65EA02B");
					tips.add(jobj);
					tips.add(jobj2);*/
			
			JSONObject data = selector.searchForWeb(jsonReq, tips);//selector.searchForWeb(jsonReq,tips);
			
			System.out.println(data);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	@Test
	public void testGetRdNameAll()
	{
		//{"pageNum":1,"pageSize":20,"sortby":"","flag":0,"params":{"name":"","nameGroupid":"","adminId":"","roadTypes":[]}}
		String parameter = "{'pageNum':1,'pageSize':20,'flag':0,'sortby':'','params':{'name':'定武高速','nameGroupid':'','adminId':214,'roadTypes':[1]}}";//

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			RdNameSelector selector = new RdNameSelector();
			
			int flag = jsonReq.getInt("flag");//1是任务查，0是全库查
            JSONObject data = new JSONObject();
            if(flag > 0){
            	
            }else{
            	data = selector.searchForWeb(jsonReq);
            }
			
			System.out.println(data);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//@Test
	public void testGetRdNameByNameId()
	{

		try {

			RdNameSelector selector = new RdNameSelector();
			
			JSONObject data = selector.searchForWebByNameId("264491");//selector.searchForWeb(jsonReq,tips);
			
			System.out.println(data);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		
		
		String  a = null;
		String  b = "hhh";
		a=b;
		String c = a == null ? "" : a;
        System.out.println("a:"+c);
        
        
	}
	
	@Test
	public void saveRdName(){
		RdNameImportor a = new RdNameImportor();
		JSONObject jsonReq = JSONObject.fromObject("{'data':{'options':{},'geoLiveType':'ROADNAME','pid':null,'nameId':null,'nameGroupid':null,'langCode':'ENG','name':'Ceshiyuyin','type':'*','base':'','prefix':'','infix':'','suffix':'','namePhonetic':'','typePhonetic':'','basePhonetic':'','prefixPhonetic':'','infixPhonetic':'','suffixPhonetic':'','srcFlag':1,'roadType':1,'adminId':214,'codeType':0,'voiceFile':'','srcResume':'\"task\":76','paRegionId':null,'splitFlag':0,'memo':'','routeId':null,'uRecord':null,'uFields':'','city':'','adminName':'全国','rowId':null,'_originalJson':{'nameId':null,'nameGroupid':null,'langCode':'CHI','name':'','type':'','base':'','prefix':'','infix':'','suffix':'','namePhonetic':'','typePhonetic':'','basePhonetic':'','prefixPhonetic':'','infixPhonetic':'','suffixPhonetic':'','srcFlag':0,'roadType':0,'adminId':null,'codeType':0,'voiceFile':'','srcResume':'','paRegionId':null,'splitFlag':0,'memo':'','routeId':0,'uRecord':null,'uFields':'','city':'','adminName':'','rowId':null},'_initHooksCalled':true},'dbId':13,'subtaskId':76}");
				//"{'data':{'options':{},'geoLiveType':'ROADNAME','pid':null,'nameId':null,'nameGroupid':null,'langCode':'CHI','name':'杨柳高速公路','type':'','base':'','prefix':'','infix':'','suffix':'','namePhonetic':'','typePhonetic':'','basePhonetic':'','prefixPhonetic':'','infixPhonetic':'','suffixPhonetic':'','srcFlag':0,'roadType':1,'adminId':214,'codeType':0,'voiceFile':'','srcResume':'','paRegionId':null,'splitFlag':0,'memo':'','routeId':0,'uRecord':null,'uFields':'','city':'','adminName':'全国','rowId':null,'_originalJson':{'nameId':null,'nameGroupid':null,'langCode':'ENG','name':'','type':'','base':'','prefix':'','infix':'','suffix':'','namePhonetic':'','typePhonetic':'','basePhonetic':'','prefixPhonetic':'','infixPhonetic':'','suffixPhonetic':'','srcFlag':0,'roadType':3,'adminId':120000,'codeType':0,'voiceFile':'','srcResume':'','paRegionId':null,'splitFlag':0,'memo':'','routeId':0,'uRecord':null,'uFields':'','city':'','adminName':'','rowId':null},'_initHooksCalled':true},'dbId':243,'subtaskId':76}");
		
		JSONObject data = jsonReq.getJSONObject("data");
		
		int subtaskId = jsonReq.getInt("subtaskId");
		try {
			JSONObject jobj =a.importRdNameFromWeb(data, subtaskId,0);
			System.out.println(jobj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	@Test
	public void searchRdNameFix(){
		RdNameSelector a = new RdNameSelector();
		/*JSONObject jsonReq = JSONObject.fromObject("{'data':{'options':{},'geoLiveType':'ROADNAME','pid':null,'nameId':206000028,'nameGroupid':208000027,'langCode':'CHI','name':'巴拿马高速公路','type':'','base':'','prefix':'','infix':'','suffix':'','namePhonetic':'','typePhonetic':'','basePhonetic':'','prefixPhonetic':'','infixPhonetic':'','suffixPhonetic':'','srcFlag':0,'roadType':3,'adminId':214,'codeType':0,'voiceFile':'','srcResume':'','paRegionId':null,'splitFlag':0,'memo':'','routeId':0,'uRecord':null,'uFields':'','city':'','adminName':'全国','rowId':null,'_originalJson':{'nameId':null,'nameGroupid':null,'langCode':'ENG','name':'','type':'','base':'','prefix':'','infix':'','suffix':'','namePhonetic':'','typePhonetic':'','basePhonetic':'','prefixPhonetic':'','infixPhonetic':'','suffixPhonetic':'','srcFlag':0,'roadType':3,'adminId':120000,'codeType':0,'voiceFile':'','srcResume':'','paRegionId':null,'splitFlag':0,'memo':'','routeId':0,'uRecord':null,'uFields':'','city':'','adminName':'','rowId':null},'_initHooksCalled':true},'dbId':243,'subtaskId':76}");
		
		JSONObject data = jsonReq.getJSONObject("data");
		*/
		try {
	            JSONObject jsonReq = JSONObject.fromObject("{'langCode':''}");

	            String langCode = jsonReq.getString("langCode");
	            if(langCode==null || StringUtils.isEmpty(langCode)){
					throw new IllegalArgumentException("langCode参数不能为空。");
				}
		
			JSONObject jobj =a.searchRdNameFix("CHI");
			System.out.println(jobj);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//@Test
	public void testSearchScPoint()
	{
		String parameter = "{'name':'服务票','code':'210303'}";

		try {
			JSONObject jsonReq = JSONObject.fromObject(parameter);

			ScPointPoicodeNewService selector = new ScPointPoicodeNewService();
			
			JSONArray data = selector.list(jsonReq);
			
			System.out.println(data);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//	@Test
	public void testSearchRdname()
	{

		try {

			RdNameSelector selector = new RdNameSelector();
			
			JSONObject data = selector.searchByName("３３０１", 20, 1);
			
			System.out.println(data);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
