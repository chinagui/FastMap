package com.navinfo.navicommons.download;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;

/**
 * @ClassName: DownloadUtils
 * @author Xiao Xiaowen
 * @date 2016年11月17日 上午12:06:53
 * @Description: TODO
 */
public class DownloadUtils {
	public static Logger log = LoggerRepos.getLogger(DownloadUtils.class);
	public static void download(String remoteFilePath,String localFilePath)throws Exception{
		URL urlfile = null;
		HttpURLConnection httpUrl = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		File f = new File(localFilePath);
		try{
			urlfile = new URL(remoteFilePath);
			httpUrl = (HttpURLConnection)urlfile.openConnection();
			httpUrl.connect();
			bis = new BufferedInputStream(httpUrl.getInputStream());
			bos = new BufferedOutputStream(new FileOutputStream(f));
			int len = 2048;
			byte[] b = new byte[len];
			while ((len = bis.read(b)) != -1){
				bos.write(b, 0, len);
			}
			bos.flush();
			bis.close();
			httpUrl.disconnect();
		}catch(Exception e){
			log.error(e.getMessage(),e);
			throw e;
		}finally{
			try{
				if(bis!=null)bis.close();
			}catch(Exception e1){
				log.error(e1.getMessage(),e1);
			}
			try{
				if(bos!=null)bos.close();
			}catch(Exception e2){
				log.error(e2.getMessage(),e2);
			}
		}
	}
	public static void main(String[] args) {
		try{		
			download("http://luyan.navinfo.com/resources/download/poi/20161027/eb3f6771150f4b4696515fca6db24a4c/poi.txt","my_poi.txt");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
