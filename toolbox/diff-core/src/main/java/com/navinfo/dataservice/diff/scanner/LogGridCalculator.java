package com.navinfo.dataservice.diff.scanner;

import com.navinfo.dataservice.datahub.glm.GlmTable;
import com.navinfo.dataservice.diff.exception.DiffException;

/** 
* @ClassName: LogGridCalculator 
* @author Xiao Xiaowen 
* @date 2016年4月13日 下午2:27:21 
* @Description: TODO
*/
public interface LogGridCalculator {
	void calc(GlmTable table,String gdbVersion)
		    throws DiffException;
}
