package com.navinfo.dataservice.web.fcc.photo.mass.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.navinfo.dataservice.commons.springmvc.BaseController;
import com.navinfo.dataservice.commons.util.ResponseUtils;
import com.navinfo.dataservice.engine.photo.mass.PoiPhotoGetter;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
public class PoiPhotoController extends BaseController{
	private static final Logger logger = Logger.getLogger(PoiPhotoController.class);
	
	 @RequestMapping(value = "/poi/getPhotoRowkey")
	 public ModelAndView getPhotoRowkey(HttpServletRequest request,HttpServletResponse response
	    ) throws ServletException, IOException {
		 
		 String param_pid = request.getParameter("parameter");
		 
		 try {
			 JSONArray poiPhotoRowkey = PoiPhotoGetter.getPoiPhotoRowkey(param_pid); 				
				return new ModelAndView("jsonView", success(poiPhotoRowkey));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return new ModelAndView("jsonView", fail(e.getMessage()));
			}		 
	 }
	 
	 @RequestMapping(value = "/poi/getPhotoRowkeys")
	 public void getPhotoRowkeys(HttpServletRequest request,HttpServletResponse response
	    ) throws ServletException, IOException {
		 
		    response.setContentType("text/javascript;charset=utf-8");
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods",
					"POST, GET, OPTIONS, DELETE,PUT");
			
			String parameter = request.getParameter("parameter");
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			String param_pid = jsonReq.getString("rowkey");

			try {

				JSONArray poiPhotoRowkeys = PoiPhotoGetter.getPoiPhotoRowkey(param_pid); 
				byte[] data = poiPhotoRowkeys.toString().getBytes();
				response.getOutputStream().write(data);
				
			} catch (Exception e) {

				logger.error(e.getMessage(), e);

				response.getWriter().println(
						ResponseUtils.assembleFailResult(e.getMessage()));
			}		 
		 		 	 
	 }
	 
	 
	 @RequestMapping(value = "/poi/getPhotoByRowkey")
	 public void getPhotoByRowkey(HttpServletRequest request,HttpServletResponse response
	    ) throws Exception {
		 		 
			response.setContentType("image/jpeg;charset=GBK");
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Allow-Methods",
					"POST, GET, OPTIONS, DELETE,PUT");
			
			String parameter = request.getParameter("parameter");
			JSONObject jsonReq = JSONObject.fromObject(parameter);
			String rowkey = jsonReq.getString("rowkey");
			String type = jsonReq.getString("type");

			try {

				byte[] photo = PoiPhotoGetter.getPhotoByRowkey(rowkey,type);

				response.getOutputStream().write(photo);

			} catch (Exception e) {

				logger.error(e.getMessage(), e);

				response.getWriter().println(
						ResponseUtils.assembleFailResult(e.getMessage()));
			}		 
	 }
	 
//	 @Test
	 public void testSelectRowkey() throws IOException {
		 String param_pid = "10021";
		 JSONArray poiPhotoRowkey = PoiPhotoGetter.getPoiPhotoRowkey(param_pid); 
		 System.out.println(poiPhotoRowkey.getString(0));
		 System.out.println(poiPhotoRowkey.getString(1));
		 System.out.println(poiPhotoRowkey.getString(2));
		 
		 System.out.println(poiPhotoRowkey);
	}
	 
/*	 @Test
	 public void testSelectPhoto() throws IOException{
		 String rowkey = "wx5j2qnkn152DTIQ4NloiWYbWE9RYyK3";
		 byte[] photo = PoiPhotoGetter.getPhotoByRowkey(rowkey);
		 
		 FileOutputStream fos = null;
		 BufferedOutputStream bos = null;
					
				try{
					fos = new FileOutputStream("D:/test/abc.jpg");
					bos = new BufferedOutputStream(fos);
					bos.write(photo);
					bos.flush();
							
						}catch(Exception e){
							logger.error(e.getMessage(),e);
							throw e;
						}finally{
							try{
								if(fos!=null)fos.close();
							}catch(Exception e2){
								logger.error(e2.getMessage(),e2);
							}
							try{
								if(bos!=null)bos.close();
							}catch(Exception e2){
								logger.error(e2.getMessage(),e2);
							}
						}
	}*/
	 
//	 @Test
	 public void testNio() throws FileNotFoundException {
		 long start = System.currentTimeMillis();	 
		 FileInputStream fis=new FileInputStream(new File("D:/临时文件/Office2010_2010_XiTongZhiJia.rar"));
		 FileChannel readChannel = fis.getChannel();
		 FileOutputStream fos=new FileOutputStream(new File("E:/test/Office888.rar"));
		 FileChannel writeChannel = fos.getChannel();
		 
		 ByteBuffer byteBuffer= ByteBuffer.allocate(2048);
		 int len = 0;
		 while (true) {
			byteBuffer.clear();
			try {
				len=readChannel.read(byteBuffer);
				if (len == -1){
					break;
				}
				byteBuffer.flip();
				writeChannel.write(byteBuffer);
				
			} catch (IOException e) {
				e.printStackTrace();
			} 			
		}
		 
		 try {
				readChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				writeChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			long end = System.currentTimeMillis();
			System.out.println("本次共使用的时间：" + (end - start) + "ms");
					
	}
		 
}
