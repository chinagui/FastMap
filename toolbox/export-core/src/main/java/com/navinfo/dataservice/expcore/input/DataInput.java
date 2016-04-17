package com.navinfo.dataservice.expcore.input;

import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.expcore.exception.ExportInputException;

/** 
 * @ClassName: DataInput 
 * @author Xiao Xiaowen 
 * @date 2015-11-2 上午11:42:03 
 * @Description: TODO
 *  
 */
public interface DataInput {
	public void initSource()throws ExportException;
	public void releaseSource();
	public void loadScripts()throws ExportInputException,Exception;
	public void serializeParameters()throws ExportException;
	public void input()throws ExportInputException,Exception;
}
