package com.navinfo.dataservice.engine.translate.upload;

import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.token.AccessToken;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
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

    private final static String TRANSLATE_PATH = "translate/upload";

    public File upload(HttpServletRequest request) throws Exception{
        File file = null;

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

            String filePath = uploadRoot + File.separator + TRANSLATE_PATH + File.separator;
            file = new File(filePath, uploadItem.getName());
            uploadItem.write(file);
        } catch (Exception e) {
            throw e;
        }
        return file;
    }
}
