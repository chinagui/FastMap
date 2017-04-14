/**
 * 
 */
package org.navinfo.dataservice.engine.meta;

import java.io.FileOutputStream;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.meta.rdname.RdNameImportor;
import com.navinfo.dataservice.engine.meta.service.ScBcrossnodeMatchckService;
import com.navinfo.dataservice.engine.meta.service.ScBranchCommcService;
import com.navinfo.dataservice.engine.meta.service.ScBranchSpeccService;
import com.navinfo.dataservice.engine.meta.service.ScModelMatchGService;
import com.navinfo.dataservice.engine.meta.service.ScModelRepdelGService;
import com.navinfo.dataservice.engine.meta.service.ScVectorMatchService;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.BLOB;

/** 
* @ClassName: RdNameSelectorTest 
* @author Zhang Xiaolong
* @date 2016年8月15日 下午5:49:43 
* @Description: TODO
*/
public class RdNameAndPatternImageTest {
	
	@Before
	public void before() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-consumer-datahub-test.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
//	@Test
	public void saveUpdataTest()
	{
//		String parameter = "{'tableName':'scMdelMatchG','data':{'fileId':null,'productLine':'NIDB-G','version':'14夏1','projectNm':'博士','specification':null,'bType':'2D','mType':'arrow','sType':null,'fileName':'032a3121','size':'4671','format':'png','impWorker':'wzs','impDate':'2017-03-21 13:55:00','urlDb':'/multimedia/data/2D/arrow/032a3121.png','urlFile':'D:/14夏企划1/多媒体文件1/PatternImg/2D/arrow/032a3121.png','memo':'dd'}}";
		//String parameter = "{'tableName':'scModelRepdelG','data':{'convBefore':'00','convOut':'2','kind':'3e1'}}";
//		String parameter = "{'tableName':'scVectorMatch','data':{'fileId':null,'productLine':'NIDB-G','version':'14夏1','projectNm':'博士2','specification':null,'type':'2D','panel':'b2p4|b2p1|b2p2|b1p1','fileName':'032a3121','size':'4671','format':'png','impWorker':'wzs','impDate':'2017-03-21 13:55:00','urlDb':'/multimedia/data/2D/arrow/032a3121.png','urlFile':'D:/16夏企划1/多媒体文件1/PatternImg/2D/arrow/032a3121.png','memo':'dd'}}";
//		String parameter = "{'tableName':'scBranchCommc','data':{'branch1':'0','branch2':'2','branch3':'','branch4':'','branch5':'','seriesbranch1':'11','seriesbranch2':'','seriesbranch3':'','seriesbranch4':''}}";
//		String parameter = "{'tableName':'scBranchSpecc','data':{'branch1':'0','branch2':'0','seriesbranch1':'00'}}";
		String parameter = "{'tableName':'scBcrossnodeMatchck','data':{'seq':0,'schematicCode':'22','arrowCode':'22'}}";
		
		
		try{	
			if (StringUtils.isEmpty(parameter)){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}		
			JSONObject parameterJson = JSONObject.fromObject(parameter);			
			if(parameterJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String tableName  = parameterJson.getString("tableName");
			if(tableName==null || StringUtils.isEmpty(tableName)){
				throw new IllegalArgumentException("tableName参数不能为空。");
			}
			JSONObject dataJson = parameterJson.getJSONObject("data");
			if(dataJson==null || dataJson.isEmpty()){
				throw new IllegalArgumentException("data参数不能为空。");
			}
			if(tableName.equals("scMdelMatchG")){
				ScModelMatchGService scModelMatchGService =new ScModelMatchGService();
				scModelMatchGService.saveUpdate(dataJson);
			}else if(tableName.equals("scModelRepdelG")){
				ScModelRepdelGService scModelRepdelGService =new ScModelRepdelGService();
				scModelRepdelGService.saveUpdate(dataJson);
			}else if(tableName.equals("scVectorMatch")){
				ScVectorMatchService scVectorMatchService =new ScVectorMatchService();
				scVectorMatchService.saveUpdate(dataJson);
			}else if(tableName.equals("scBranchCommc")){
				ScBranchCommcService scBranchCommcService =new ScBranchCommcService();
				scBranchCommcService.saveUpdate(dataJson);
			}else if(tableName.equals("scBranchSpecc")){
				ScBranchSpeccService scBranchSpeccService =new ScBranchSpeccService();
				scBranchSpeccService.saveUpdate(dataJson);
			}else if(tableName.equals("scBcrossnodeMatchck")){
				ScBcrossnodeMatchckService scBcrossnodeMatchckService =new ScBcrossnodeMatchckService();
				scBcrossnodeMatchckService.saveUpdate(dataJson);
			}else{
				throw new IllegalArgumentException("不识别的表: "+tableName);
			}
		
		}catch(Exception e){
			throw new IllegalArgumentException("保存失败  "+e.getMessage());
		}
	}
	
	//@Test
	public void deleteTest(){
			//String parameter = "{'tableName':'scMdelMatchG','ids':[20170162142,20170162143]}";
			//String parameter = "{'tableName':'scModelRepdelG','ids':['0','00']}";
			//String parameter = "{'tableName':'scVectorMatch','ids':[20170162142 ,201705002116]}";
		    //String parameter = "{'tableName':'scBranchCommc','ids':[{'branch1':'0','branch2':'0','branch3':'','branch4':'','branch5':'','seriesbranch1':'01','seriesbranch2':'','seriesbranch3':'','seriesbranch4':''},{'branch1':'0','branch2':'1','branch3':'','branch4':'','branch5':'','seriesbranch1':'11','seriesbranch2':'','seriesbranch3':'','seriesbranch4':''}]}";
			//String parameter = "{'tableName':'scBranchSpecc','ids':[{'branch1':'0','branch2':'0','seriesbranch1':'00'},{'branch1':'0','branch2':'1','seriesbranch1':'11'}]}";
			String parameter = "{'tableName':'scBcrossnodeMatchck','ids':[0]}";
		
		try{			
			JSONObject parameterJson = JSONObject.fromObject(parameter);			
			if(parameterJson==null){
				throw new IllegalArgumentException("parameter参数不能为空。");
			}
			String tableName  = parameterJson.getString("tableName");
			if(tableName==null || StringUtils.isEmpty(tableName)){
				throw new IllegalArgumentException("tableName参数不能为空。");
			}
			JSONArray idsJson = parameterJson.getJSONArray("ids");
			if(idsJson==null || idsJson.size() <= 0 ){
				throw new IllegalArgumentException("ids参数不能为空。");
			}
			if(tableName.equals("scMdelMatchG")){
				ScModelMatchGService scModelMatchGService =new ScModelMatchGService();
				scModelMatchGService.deleteByIds(idsJson);
			}else if(tableName.equals("scModelRepdelG")){
				ScModelRepdelGService scModelRepdelGService =new ScModelRepdelGService();
				scModelRepdelGService.deleteByIds(idsJson);
			}else if(tableName.equals("scVectorMatch")){
				ScVectorMatchService scVectorMatchService =new ScVectorMatchService();
				scVectorMatchService.deleteByIds(idsJson);
			}else if(tableName.equals("scBranchCommc")){
				ScBranchCommcService scBranchCommcService =new ScBranchCommcService();
				scBranchCommcService.deleteByIds(idsJson);
			}else if(tableName.equals("scBranchSpecc")){
				ScBranchSpeccService scBranchSpeccService =new ScBranchSpeccService();
				scBranchSpeccService.deleteByIds(idsJson);
			}else if(tableName.equals("scBcrossnodeMatchck")){
				ScBcrossnodeMatchckService scBcrossnodeMatchckService =new ScBcrossnodeMatchckService();
				scBcrossnodeMatchckService.deleteByIds(idsJson);
			}else{
				throw new IllegalArgumentException("不识别的表: "+tableName);
			}
		}catch(Exception e){
			throw new IllegalArgumentException("删除失败 "+e.getMessage());
		}
	}

	
//	@Test
	public void searchTest(){
				String parameter = "{'tableName':'scMdelMatchG','data':{'fileId':null,'productLine':'','version':'','projectNm':'','specification':null,'bType':'','mType':'','sType':null,'fileName':'','size':'','format':'','impWorker':'','impDate':null,'urlDb':'','urlFile':'','memo':''},'sortby':'','pageSize':10,'pageNum':1}";
//				String parameter = "{'tableName':'scModelRepdelG','data':{'convBefore':'83180890','convOut':'83180800','kind':'3d'},'sortby':'-convBefore','pageSize':20,'pageNum':1}";
//				String parameter = "{'tableName':'scVectorMatch','data':{'fileId':null,'productLine':'NIDB-G','version':'14夏1','projectNm':'博士2','specification':null,'type':'2D','panel':'b2p4|b2p1|b2p2|b1p1','fileName':'032a3121','size':'4671','format':'png','impWorker':'wzs','impDate':null,'urlDb':'/multimedia/data/2D/arrow/032a3121.png','urlFile':'D:/16夏企划1/多媒体文件1/PatternImg/2D/arrow/032a3121.png','memo':'dd'},'sortby':'-fileId','pageSize':20,'pageNum':1}";
//				String parameter = "{'tableName':'scBranchCommc','data':{'branch1':'0','branch2':'','branch3':'','branch4':'','branch5':'','seriesbranch1':'11','seriesbranch2':'','seriesbranch3':'','seriesbranch4':''},'sortby':'+branch1','pageSize':20,'pageNum':1}";
//				String parameter = "{'tableName':'scBranchSpecc','data':{'branch1':'0','branch2':'0','seriesbranch1':'00'},'sortby':'-branch1','pageSize':20,'pageNum':1}";
//				String parameter = "{'tableName':'scBcrossnodeMatchck','data':{'seq':0,'schematicCode':'22','arrowCode':'22'},'sortby':'-seq','pageSize':20,'pageNum':1}";
				
		
				try{	
					if (StringUtils.isEmpty(parameter)){
						throw new IllegalArgumentException("parameter参数不能为空。");
					}		
					JSONObject parameterJson = JSONObject.fromObject(parameter);			
					if(parameterJson==null){
						throw new IllegalArgumentException("parameter参数不能为空。");
					}
					String tableName  = parameterJson.getString("tableName");
					if(tableName==null || StringUtils.isEmpty(tableName)){
						throw new IllegalArgumentException("tableName参数不能为空。");
					}
					JSONObject dataJson = parameterJson.getJSONObject("data");
					if(dataJson==null || dataJson.isEmpty()){
						throw new IllegalArgumentException("data参数不能为空。");
					}
					int curPageNum= 1;//默认为第一页
					int pageSize = 20;
					String sortby = "";//排序
					if(parameterJson.containsKey("pageNum") &&  parameterJson.getInt("pageNum") > 0){
						curPageNum = parameterJson.getInt("pageNum");
					}
					if(parameterJson.containsKey("pageSize") &&  parameterJson.getInt("pageSize") > 0){
						pageSize = parameterJson.getInt("pageSize");
					}

					if(parameterJson.containsKey("sortby") &&  parameterJson.getString("sortby") != null){
						sortby = parameterJson.getString("sortby");
					}
					Page data = new Page();
					if(tableName.equals("scMdelMatchG")){
						ScModelMatchGService scModelMatchGService =new ScModelMatchGService();
						data = scModelMatchGService.list(dataJson, curPageNum, pageSize, sortby);
					}else if(tableName.equals("scModelRepdelG")){
						ScModelRepdelGService scModelRepdelGService =new ScModelRepdelGService();
						data = scModelRepdelGService.list(dataJson,curPageNum,pageSize,sortby);
					}else if(tableName.equals("scVectorMatch")){
						ScVectorMatchService scVectorMatchService =new ScVectorMatchService();
						data = scVectorMatchService.list(dataJson,curPageNum,pageSize,sortby);
					}else if(tableName.equals("scBranchCommc")){
						ScBranchCommcService scBranchCommcService =new ScBranchCommcService();
						data = scBranchCommcService.list(dataJson,curPageNum,pageSize,sortby);
					}else if(tableName.equals("scBranchSpecc")){
						ScBranchSpeccService scBranchSpeccService =new ScBranchSpeccService();
						data = scBranchSpeccService.list(dataJson,curPageNum,pageSize,sortby);
					}else if(tableName.equals("scBcrossnodeMatchck")){
						ScBcrossnodeMatchckService scBcrossnodeMatchckService =new ScBcrossnodeMatchckService();
						data = scBcrossnodeMatchckService.list(dataJson,curPageNum,pageSize,sortby);
					}else{
						throw new IllegalArgumentException("不识别的表: "+tableName);
					}
				System.out.println(data.getResult());
				}catch(Exception e){
					throw new IllegalArgumentException("查询失败  "+e.getMessage());
				}
	}
	
	@Test
	public void getImageTest() throws ServiceException{
		
		String parameter = "{'tableName':'scMdelMatchG','id':'201400000165'}";
		JSONObject parameterJson = JSONObject.fromObject(parameter);			
		BLOB imageBlob = null;
		String tableName  = parameterJson.getString("tableName");
		if(tableName==null || StringUtils.isEmpty(tableName)){
			throw new IllegalArgumentException("tableName参数不能为空。");
		}
		String id  = parameterJson.getString("id");
		if(id==null || StringUtils.isEmpty(tableName)){
			throw new IllegalArgumentException("id参数不能为空。");
		}
		if(tableName.equals("scMdelMatchG")){
			ScModelMatchGService scModelMatchGService =new ScModelMatchGService();
			imageBlob =scModelMatchGService.getFileContentById(id);
		}
	
		String filepath = "F:/111.png";
        System.out.println("输出文件路径为:" + filepath);
        try {
	         //处理返回的imageBlob 数据
	    	  InputStream in = imageBlob.getBinaryStream(); // 建立输出流
	    	  int len = (int) imageBlob.length();
	          FileOutputStream file = new FileOutputStream(filepath);
	          byte[] buffer = new byte[len]; // 建立缓冲区
	          while ( (len = in.read(buffer)) != -1) {
	            file.write(buffer, 0, len);
          }
          file.close();
          in.close();
        }catch(Exception e) {
        	
        }
		
	}
}
