package com.navinfo.navicommons.workflow.persistor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.navinfo.navicommons.config.SystemGlobals;

/**
 * @author arnold
 * @version $Id:Exp$
 * @since 2010-8-24
 */
public class FilePersistor extends AbstractPersistor
{
    private static final transient Logger log = Logger.getLogger(FilePersistor.class);
    private static final String PERSISTENCE_DIR_CONFIG = "dms.NaviMapData.workdir";
    private static final String PERSISTENCE_DIR = "flowInstance";
    private static final String SUFIX = ".xml";
    private static final String DEF_SUFIX = "-FlowDef" + SUFIX;

    protected String doLoadFlowInstance(String instancePk) throws PersistorException
    {
        String fullPath = getFlowInstanceDir(instancePk);
        return getFileAsString(fullPath);
    }

    protected void doSaveFlowInstance(String instancePk, String xml) throws PersistorException
    {
        String fullPath = getFlowInstanceDir(instancePk);
        writeStringToFile(fullPath,xml);
    }

    protected String doLoadDefine(String defPk) throws PersistorException
    {
        String fileName = getDefFileName(defPk);
        return getResourceAsString(fileName);
    }

    protected void doSaveDefine(String defPk, String xml) throws PersistorException
    {
        //String fullPath = defPk;
        //writeStringToFile(fullPath,xml); //todo
    }

    private void writeStringToFile(String path,String content) throws PersistorException
    {
        File file = new File(path);
        File parentFile = file.getParentFile();
        if(!parentFile.exists())
            parentFile.mkdirs();
        try
        {
            FileUtils.writeStringToFile(file,content);
        } catch (IOException e)
        {
            log.error("持久化到文件时出错",e);
            throw new PersistorException("持久化到文件时出错",e);
        }
    }

    private String getFileAsString(String path)
    {
        File file = new File(path);
        String content = null;
        try
        {
            content = FileUtils.readFileToString(file);
        } catch (IOException e)
        {
            log.error("读取文件时出错",e);
            throw new PersistorException("读取文件时出错",e);
        }
        return content;
    }

    private String getResourceAsString(String fileName)
    {
        String content = null;
        InputStream is = getClass().getClassLoader().getResourceAsStream(fileName);
        if(is == null)
        {
            log.error("未找到流程定义文件：" + fileName);
            throw new PersistorException("未找到流程定义文件：" + fileName);
        }
        try
        {
            content = IOUtils.toString(is);
        } catch (IOException e)
        {
            log.error("读取流程定义文件时出错：" + fileName,e);
            throw new PersistorException("读取流程定义文件时出错：" + fileName,e);
        } finally
        {
            IOUtils.closeQuietly(is);
        }
        return content;
    }

    private String getFlowInstanceDir(String instancePk)
    {
        return SystemGlobals.getValue(PERSISTENCE_DIR_CONFIG) + File.separator
                + PERSISTENCE_DIR + File.separator + instancePk + SUFIX;
    }

    private String getDefFileName(String defPk)
    {
        return defPk + DEF_SUFIX;
    }
}
