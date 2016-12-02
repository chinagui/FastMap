package com.navinfo.dataservice.commons.util;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2009-9-21
 */
public class ServiceInvokeUtil
{
    private static final transient Logger log = Logger.getLogger(ServiceInvokeUtil.class);

    public static String invoke(String service_url,Map<String,String> parMap) throws Exception
    {
        PostMethod servicePost = null;
        String json = null;
        try
        {
            servicePost = new PostMethod(service_url);
            servicePost.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            for(String parName : parMap.keySet())
            {
                if(parMap.get(parName) != null)
                {
                    servicePost.addParameter(parName,parMap.get(parName));
                }
            }
            HttpClient client = new HttpClient();
            int status = client.executeMethod(servicePost);
            if (status == HttpStatus.SC_OK)
            {
                json = servicePost.getResponseBodyAsString();
            }
            else
            {
                json = "{success : false,msg:'调用服务失败！'}";
            }
        } catch (IOException e)
        {
            //log.error("调用服务失败",e);
            throw new Exception("调用服务失败，服务为" + service_url,e.getCause());
        } finally
        {
            if(servicePost != null)
            {
                servicePost.releaseConnection();
            }
        }
        return json;
    }
	
	
	 public static String invoke(String service_url,Map<String,String> parMap,int connectTimeOut) throws Exception
    {
        PostMethod servicePost = null;
        String json = null;
        try
        {
            servicePost = new PostMethod(service_url);
            servicePost.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            if(parMap!=null){
                for(String parName : parMap.keySet())
                {
                    if(parMap.get(parName) != null)
                    {
                        servicePost.addParameter(parName,parMap.get(parName));
                    }
                }
            }
            HttpClient client = new HttpClient();
			
            client.getHttpConnectionManager().getParams().setConnectionTimeout(connectTimeOut);  
            int status = client.executeMethod(servicePost);
            if (status == HttpStatus.SC_OK)
            {
                json = servicePost.getResponseBodyAsString();
            }
            else
            {
                json = "{success : false,msg:'调用服务失败！'}";
            }
        } catch (IOException e)
        {
            //log.error("调用服务失败",e);
            throw new Exception("调用服务失败，服务为" + service_url,e.getCause());
        } finally
        {
            if(servicePost != null)
            {
                servicePost.releaseConnection();
            }
        }
        return json;
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public static String upload(String service_url,File file,Map<String,String> parMap) throws Exception
    {
        String json = null;
        PostMethod filePost = null;
        try {
            filePost = new PostMethod(service_url);
//            filePost.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            Part[] parts = new Part[parMap.size() + 1];
            Object[] keyArray = parMap.keySet().toArray();
            for(int i = 0;i < keyArray.length;i++)
            {
                parts[i] = new StringPart((String)keyArray[i],parMap.get(keyArray[i]));
            }
            parts[parts.length - 1] = new FilePart(file.getName(), file);

            filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
            HttpClient client = new HttpClient();
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK)
            {
                json = filePost.getResponseBodyAsString();
            }
            else
            {
                json = "{success : false,msg:'上传失败！'}";
            }
        } catch (IOException e)
        {
            log.error("文件上传失败",e);
            throw new Exception("文件上传失败，服务为" + service_url,e.getCause());
        } finally
        {
            if(filePost != null)
            {
                filePost.releaseConnection();
            }
        }
        return json;
    }
    
    
    public static String invokeByGet(String service_url) throws Exception
    {
        GetMethod serviceGet = null;
        String json = null;
        try
        {
            serviceGet = new GetMethod(service_url);
            serviceGet.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            
            HttpClient client = new HttpClient();
            int status = client.executeMethod(serviceGet);
            if (status == HttpStatus.SC_OK)
            {
                json = serviceGet.getResponseBodyAsString();
            }
            else
            {
                json = "{success : false,msg:'调用服务失败！'}";
            }
        } catch (IOException e)
        {
            //log.error("调用服务失败",e);
            throw new Exception("调用服务失败，服务为" + service_url,e.getCause());
        } finally
        {
            if(serviceGet != null)
            {
                serviceGet.releaseConnection();
            }
        }
        return json;
    }

    public static void main(String []args) throws Exception
    {
        String url = "http://localhost:8088/cpub/SysLoginAction.do?operate=logon&operPage=index";
        Map<String,String> parMap = new HashMap<String, String>();
        parMap.put("orgUserSearch.id","test");
        parMap.put("orgUserSearch.password","test");
        String json = invoke(url,parMap);
        //System.out.println(json);


        String fmeUpload = "http://192.168.8.70:8080/fmedataupload/poi/pretreamentService.fmw";
        File file = new File("G:\\work\\navinfo\\newsvn\\02生产平台\\01扩展POI生产系统\\document\\04需求开发\\样例数据\\workspace.mdb");
        parMap.clear();
        parMap.put("opt_fullpath","true");
        parMap.put("opt_responseformat","json");
        parMap.put("opt_pathlevel","10");
        json = upload(fmeUpload,file,parMap);
        System.out.println(json);
//        Map map = JSONUtils.toMap(json);
//        System.out.println(map);
    }
}
