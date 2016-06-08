package com.navinfo.dataservice.diff.dataaccess;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.glm.GlmTable;


/**
 * @author arnold
 * @version $Id:Exp$
 * @since 12-3-9 上午9:55
 */
public class DblinkDataAccess implements DataAccess
{
    private static final transient Logger log = Logger.getLogger(DblinkDataAccess.class);

    public DblinkDataAccess(){
    }


    @Override
    public String accessTable(GlmTable table) 
    {
    	return "";
    }
}
