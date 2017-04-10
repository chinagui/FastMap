package com.navinfo.dataservice.scripts.tmp;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.scripts.JobScriptsInterface;
import com.navinfo.dataservice.scripts.tmp.service.CollectConvertMain;
import com.navinfo.dataservice.scripts.tmp.service.CollectConvertUtils;

public class CollectConvert {
	private static Logger log = LogManager.getLogger(CollectConvert.class);
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		//incremental.zip解压后的路径，例如：data/incremental
		String path = String.valueOf(args[0]);
		int dbId=0;
		if(args.length>1){
			dbId=Integer.valueOf(args[1]);
		}
		if (path == null) {
			System.exit(0);
		} else {
			//初始化context
			JobScriptsInterface.initContext();
			CollectConvert.convertMain(dbId,path);
		}
		System.exit(0);
	}
	/**
	 * 输入：incremental.zip解压后的路径，例如：data/incremental
	 * 输出：一体化对应规格
	 * 原则：
	 * 1.	模块：输入的zip包路径排序list
	 * 2.	遍历路径
	 * 3.	调用照片拷贝模块
	 * 4.	调用读取模块，获取IncrementalData_3565_10047_20161114203047/Datum_Point.json中数据
	 * 5.	获取到的数据进行遍历，调用转换模块：json转换模块
	 * 6.	转换后的数据调用写入模块，将转换结果写入到对应的poi.txt文件中
	 *      a.获取序列号模块
	 *      b.路径拼接:路径（sys库sys_config表dropbox.upload.path值）+/+序列号+/+poi.txt
	 * 7.	序列号统一输出到outConvert.txt中，可调用写入模块
	 * @param dbId 可选参数，脚本外面可能会传入
	 *  @param path
	 */

	public static void convertMain(int dbId, String path)throws Exception {
		log.info("Start convert");
		log.info("按照时间排序后的路径list");
		List<String> listPath = CollectConvertUtils.listPath(path);
		log.info("遍历路径");
		List<Integer> convertSeqList=new ArrayList<Integer>();
		List<String> errorList=new ArrayList<String>();
		for(String outPath:listPath){
			log.info("开始转换路径："+outPath);	
			int seq=CollectConvertUtils.getUploadSeq();
			convertSeqList.add(seq);
			String inPath=SystemConfigFactory.getSystemConfig().getValue(PropConstant.uploadPath)+"/"
		+seq+"/";
			log.info("转入路径："+inPath);
			log.info("路径"+outPath+"照片拷贝");
			CollectConvertUtils.copyPhoto(outPath, inPath);
			log.info("路径"+outPath+"数据读取");
			List<JSONObject> oldListJson = CollectConvertUtils.readJsonObjects(outPath+"Datum_Point.json");
			List<JSONObject> newListJson=new ArrayList<JSONObject>();
			log.info("路径"+outPath+"数据转换");
			for(JSONObject oldPoi:oldListJson){
				try{
					JSONObject newPoi = CollectConvertMain.convertMain(dbId,inPath,oldPoi);
					newListJson.add(newPoi);
				}catch (Exception e) {
					log.error("转换错误", e);
					errorList.add("from "+outPath+",to "+inPath+",message:"+e.getMessage());
					errorList.add("from "+outPath+",to "+inPath+",data:"+oldPoi);
				}
			}
			log.info("路径"+outPath+"数据写入");
			CollectConvertUtils.writeJSONObject2TxtFile(inPath+"poi.txt", newListJson);
		}
		log.info("本次转换路径汇总");
		CollectConvertUtils.writeInteger2TxtFile(path+"outConvert.txt", convertSeqList);
		log.info("end convert");
	}
}
