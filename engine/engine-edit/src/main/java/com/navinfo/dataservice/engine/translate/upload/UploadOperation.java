package com.navinfo.dataservice.engine.translate.upload;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.commons.util.FileUtils;
import com.navinfo.dataservice.dao.tranlsate.selector.TranslateOperator;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;

/**
 * @Title: ListOperation
 * @Package: com.navinfo.dataservice.engine.translate.upload
 * @Description: ${TODO}
 * @Author: Crayeres
 * @Date: 10/13/2017
 * @Version: V1.0
 */
public class UploadOperation {

    private final static String TRANSLATE_UPLOAD = "translate";

    private final static Logger logger = LoggerRepos.getLogger(UploadOperation.class);

    public void upload(HttpServletRequest request) throws Exception{
        Connection conn = null;

        JSONObject json = new JSONObject();

        AccessToken tokenObj = (AccessToken) request.getAttribute("token");
        long userId = tokenObj.getUserId();

        DiskFileItemFactory factory = new DiskFileItemFactory();

        ServletFileUpload upload = new ServletFileUpload(factory);

        try {
            List<FileItem> items = upload.parseRequest(request);
            Iterator<FileItem> it = items.iterator();

            FileItem uploadItem = null;

            while (it.hasNext()) {
                FileItem item = it.next();
                if (item.getName() != null && !item.getName().equals("")) {
                    uploadItem = item;
                } else {
                    throw new Exception("上传的文件格式有问题！");
                }
            }
            String uploadRoot = SystemConfigFactory.getSystemConfig().getValue(PropConstant.uploadPath);

            String filePath = uploadRoot + File.separator + UploadOperation.TRANSLATE_UPLOAD + File.separator;

            logger.info(String.format("fileName:%s", uploadItem.getName()));

            StringBuilder fileName = new StringBuilder();
            fileName.append(uploadItem.getName().substring(0, uploadItem.getName().lastIndexOf('.')));
            fileName.append("_").append(System.currentTimeMillis());
            fileName.append(uploadItem.getName().substring(uploadItem.getName().lastIndexOf('.')));

            File file = new File(filePath, fileName.toString());
            File parentFile = file.getParentFile();

            logger.info(String.format("filePath:%s", parentFile.getPath()));
            if (!parentFile.exists()) {
                org.apache.commons.io.FileUtils.forceMkdir(file);
                logger.info(String.format("filePath create!"));
            }

            logger.info(String.format("file:%s", file.getPath()));
            if (!file.exists()) {
                file.createNewFile();
                logger.info(String.format("file create!"));
            }
            uploadItem.write(file);

            json.put("fileName", uploadItem.getName());
            json.put("fileSize", file.length());
            json.put("userId", userId);
            json.put("downloadPath", filePath);
            json.put("downloadFileName", fileName.toString());

            JobApi api = (JobApi) ApplicationContextUtil.getBean("jobApi");
            JSONObject dataJson = new JSONObject();
            dataJson.put("filePath", uploadRoot + fileName.toString());
            //long jobId = api.createJob("translateJob", json, userId, 0, "英文翻译工具");
            //json.put("jobId", jobId);
            json.put("jobId", 14554);

            logger.info(String.format("save params:[%s]", json.toString()));

            conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
            TranslateOperator operator = new TranslateOperator(conn);
            operator.save(json);

            conn.commit();
        } catch (Exception e) {
            DbUtils.rollback(conn);
            throw e;
        } finally {
            DbUtils.closeQuietly(conn);
        }
    }

    public static void main(String[] args) {
        System.out.println("rules.xml".lastIndexOf('.'));
    }
}
