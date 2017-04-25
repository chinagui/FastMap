package com.navinfo.dataservice.control.service;

import java.util.ArrayList;
import java.util.List;

/** 
 * @ClassName: UploadResult
 * @author xiaoxiaowen4127
 * @date 2017年4月24日
 * @Description: UploadResult.java
 */
public class UploadResult {

	private int success=0;
	
	private List<UploadErrorLog> fail=new ArrayList<UploadErrorLog>();
}
