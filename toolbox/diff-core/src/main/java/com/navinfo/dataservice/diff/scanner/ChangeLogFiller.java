package com.navinfo.dataservice.diff.scanner;

import com.navinfo.dataservice.datahub.glm.GlmTable;
import com.navinfo.dataservice.diff.exception.DiffException;

/** 
* @ClassName: ChangeLogFiller 
* @author Xiao Xiaowen 
* @date 2016年4月13日 下午2:21:02 
* @Description: TODO
*/
public interface ChangeLogFiller {
	void fill(GlmTable table,String leftTableFullName,String rightTableFullName)
		    throws DiffException;
}
