package com.navinfo.dataservice.engine.fcc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.spi.AudioFileWriter;

import com.navinfo.dataservice.bizcommons.sys.SysLogConstant;
import com.navinfo.dataservice.bizcommons.sys.SysLogOperator;
import com.navinfo.dataservice.bizcommons.sys.SysLogStats;
import com.navinfo.dataservice.commons.token.AccessToken;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.solr.common.util.ContentStreamBase.FileStream;
import org.junit.Before;
import org.junit.Test;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.commons.util.ZipUtils;
import com.navinfo.dataservice.engine.audio.AudioController;
import com.navinfo.dataservice.engine.audio.AudioImport;
import com.navinfo.dataservice.engine.fcc.patternImage.PatternImageExporter;
import com.navinfo.dataservice.engine.fcc.photo.HBaseController;
import com.navinfo.dataservice.engine.fcc.tips.TipsExporter;
import com.navinfo.dataservice.engine.fcc.tips.TipsUpload;
import com.navinfo.navicommons.geo.computation.CompGridUtil;
import com.navinfo.navicommons.geo.computation.GridUtils;

/** 
 * @ClassName: TipsExportTest.java
 * @author y
 * @date 2016-11-1 上午10:37:59
 * @Description: TODO
 *  
 */
public class TipsExportTest extends InitApplication{
	
	
	@Override
	@Before
	public void init() {
		initContext();
	}


	public TipsExportTest() throws Exception {
	}

	@Test
	public void tesDownload() {
        long userId = 1234;
        String beginDate = DateUtils.getSysDateFormat();
        String uuid = UuidUtils.genUuid();
        String parameter = "{\"condition\":[{\"grid\":45545331,\"date\":\"\"},{\"grid\":45545332,\"date\":\"\"}],\"workType\":1}";

       
        try {

            JSONObject jsonReq = JSONObject.fromObject(parameter);

            String filePath = "E:/fcc/" + uuid + "/";

            File file = new File(filePath);

            if (!file.exists()) {
                file.mkdirs();
            }
            //grid和date的对象数组
            JSONArray condition = jsonReq.getJSONArray("condition");

            TipsExporter op = new TipsExporter();

            Map<String, Set<String>> images = new HashMap<>();

            int expCount = op.export(condition,1, filePath, "tips.txt", images);


            //2.模式图下载： 1406和1401需要导出模式图
            if (images.size() > 0) {
                Set<String> modelPtn = new HashSet<>();
                Set<String> vectorPtn = new HashSet<>();
                for (String key : images.keySet()) {
                    if (key.equals("1401") || key.equals("1406")) {
                        modelPtn.addAll(images.get(key));
                    } else if (key.equals("1402")) {
                        vectorPtn.addAll(images.get(key));
                    }
                }
                if (modelPtn.size() > 0 || vectorPtn.size() > 0) {
                    PatternImageExporter exporter = new PatternImageExporter();
                    exporter.export2SqliteByNames(filePath, modelPtn, vectorPtn);
                }
            }

            String zipFileName = uuid + ".zip";

            String zipFullName = filePath + "/" + zipFileName;

            String day = StringUtils.getCurrentDay();
            //3.打zip包
            ZipUtils.zipFile(filePath, zipFullName);

            String serverUrl = SystemConfigFactory.getSystemConfig().getValue(
                    PropConstant.serverUrl);

            String downloadUrlPath = SystemConfigFactory.getSystemConfig().getValue(
                    PropConstant.downloadUrlPathTips);
            //4.返回的url
            String url = serverUrl + downloadUrlPath + File.separator + day + "/"
                    + zipFileName;

            System.out.println("导出成功:" + filePath);

            JSONObject data = null;
            if (expCount > 0) {
                data = new JSONObject();
                data.put("url", filePath);

                data.put("downloadDate", DateUtils.dateToString(new Date(),
                        DateUtils.DATE_COMPACTED_FORMAT));
            }

            Map<String, Object> result = new HashMap<String, Object>();
            result.put("errcode", 0);
            result.put("errmsg", "success");
            result.put("data", data);

            System.out.println(JSONObject.fromObject(result));
            //Tips下载记录日志 sys库中
            insertExportLog(beginDate, userId, expCount, uuid, "");
        } catch (Exception e) {
            e.printStackTrace();
            String errMsg = "下载tips出错,parameter:" + parameter + "错误信息:" + e.getMessage();
            insertExportLog(beginDate, userId, 0, uuid, errMsg);
        }
        System.exit(1);
    }
    private void insertExportLog(String beginDate, long userId, int expCount, String uuid, String errMsg) {
        try{
            SysLogStats log = new SysLogStats();
            log.setLogType(SysLogConstant.TIPS_DOWNLOAD_TYPE);
            log.setLogDesc(SysLogConstant.TIPS_DOWNLOAD_DESC+",uuid :"+uuid);
            log.setFailureTotal(0);
            log.setSuccessTotal(expCount);
            log.setTotal(expCount);
            log.setBeginTime(beginDate);
            log.setEndTime(DateUtils.getSysDateFormat());
            log.setErrorMsg(errMsg);
            log.setUserId(String.valueOf(userId));
            SysLogOperator.getInstance().insertSysLog(log);
        }catch (Exception e) {

        }
    }


    public static void transFile(byte[] fileBt,String fileName){
        String filePath = "E:\\testE\\exp\\";   //文件路径
        FileOutputStream fstream =null;
        try
        {
        	fstream=new FileOutputStream(filePath+fileName);
            fstream.write(fileBt);
        }
        catch (Exception ex)
        {
            //抛出异常信息
        }
        finally
        {
        	try {
				fstream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
 
    }
	
	
	public static void transAudioFile(byte[] fileBt,String fileName){
        String filePath = "E:\\testE\\exp\\";   //文件路径
        
        OutputStream fstream =null;
        
        try
        {
        	fstream=new FileOutputStream(filePath+fileName);
            fstream.write(fileBt);
        }
        catch (Exception ex)
        {
            //抛出异常信息
        }
        finally
        {
        	try {
				fstream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
 
    }
	public static void main(String[] args) {
		String rowkey="702A3C888FC34A1CAABCE498E188AEE1";
		/*HBaseController  photoCon=new HBaseController();*/
		AudioController  photoCon=new AudioController();
		byte[] photoBytye;
		byte[] photoBytye2;
		try {
			//photoBytye = photoCon.getPhotoByRowkey(rowkey);
			photoBytye = photoCon.getAudioByRowkey(rowkey);
			
			photoBytye2 = FileUtils.readPhotos("E:\\testE\\exp\\").get("702A3C888FC34A1CAABCE498E188AEE1.amr");
			System.out.println(photoBytye.length==photoBytye2.length);
			System.out.println(photoBytye==photoBytye2);
			System.out.println("photoBytye.len(数据库的):"+photoBytye.length);
			System.out.println("photoBytye2.len:"+photoBytye2.length);
			transAudioFile(photoBytye,"test.amr");
			System.out.println("导出成功");
			
			//POINT (116.27175 39.97312)
			String grid_id = CompGridUtil.point2Grids(116.27175, 39.97312)[0];
			
			System.out.println(grid_id);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			
		}
		
		
		
		
	}

}
