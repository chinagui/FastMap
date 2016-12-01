package com.navinfo.dataservice.engine.dropbox.manger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.dropbox.util.DropboxUtil;

public class DownloadService {
	
	private static class SingletonHolder {
		private static final DownloadService INSTANCE = new DownloadService();
	}

	public static DownloadService getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public JSONObject getBasedata(String projectId) throws Exception {

		String filePath = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.downloadFilePathBasedata);

		String urlPath = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.downloadUrlPathBasedata);

		String dir = filePath + "/" + projectId;

		JSONObject data = DropboxUtil.getLastestInfo(urlPath, dir, projectId);

		ManApi man = (ManApi) ApplicationContextUtil.getBean("manApi");

		String specVersion = man.querySpecVersionByType(2);

		data.put("specVersion", specVersion);

		return data;

	}

	public JSONArray getBasedataList() throws Exception {

		ManApi man = (ManApi) ApplicationContextUtil.getBean("manApi");

		String specVersion = man.querySpecVersionByType(2);

		JSONArray data = DropboxUtil.getGdbList(specVersion);

		return data;

	}

	public JSONObject getNds(String projectId) throws Exception {

		String filePath = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.downloadFilePathNds);

		String urlPath = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.downloadUrlPathNds);

		String dir = filePath + "/" + projectId;

		JSONObject data = DropboxUtil.getLastestInfo(urlPath, dir, projectId);

		return data;

	}

	public JSONArray getNdsList() throws Exception {

		JSONArray data = DropboxUtil.getNdsList();

		return data;

	}

	public JSONObject getPatternimg() throws Exception {

		String filePath = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.downloadFilePathPatternimg);

		String urlPath = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.downloadUrlPathPatternimg);

		JSONObject data = DropboxUtil.getLastestInfo(urlPath, filePath, null);

		ManApi man = (ManApi) ApplicationContextUtil.getBean("manApi");

		String specVersion = man.querySpecVersionByType(3);

		data.put("specVersion", specVersion);

		return data;

	}
	
	/**
	 * @Title: getMetadta
	 * @Description: TODO
	 * @param 
	 * @return
	 * @throws Exception  JSONObject
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月30日 下午8:46:36 
	 */
	public JSONObject getMetadta() throws Exception {

		String filePath = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.downloadFilePathRoot);

		String urlPath = SystemConfigFactory.getSystemConfig().getValue(
				PropConstant.downloadUrlPathRoot);

		JSONObject data = DropboxUtil.getLastestInfo(urlPath, filePath, null);

		ManApi man = (ManApi) ApplicationContextUtil.getBean("manApi");

		String specVersion = man.querySpecVersionByType(3);

		data.put("specVersion", specVersion);

		return data;

	}
	
	public JSONObject getAppVersion(int type,String platform) throws Exception {

		JSONObject data = DropboxUtil.getAppVersion(type,platform);

		return data;

	}
}
