package com.navinfo.dataservice.expcore;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.navinfo.dataservice.expcore.config.ExportConfig;
import com.navinfo.dataservice.expcore.exception.ExportException;
import com.navinfo.dataservice.expcore.input.OracleInput;
import com.navinfo.dataservice.expcore.output.DataOutput;
import com.navinfo.dataservice.expcore.output.Oracle2OracleDataOutput;
import com.navinfo.dataservice.commons.thread.ThreadLocalContext;

/** 
 * @ClassName: Exporter2OracleByScripts 
 * @author Xiao Xiaowen 
 * @date 2015-11-6 下午7:00:26 
 * @Description: TODO
 *  
 */
public class Exporter2OracleByScripts extends ExporterByScripts {
	
	public Exporter2OracleByScripts(ExportConfig expConfig){
		super(expConfig);
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.expcore.FlexibleExporter#validateExportConfig(com.navinfo.dataservice.expcore.ExporterResult)
	 */
	@Override
	public boolean validateExportConfig(ExporterResult result)
			throws ExportException {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.expcore.FlexibleExporter#initDataInput(com.navinfo.dataservice.expcore.ExporterResult)
	 */
	@Override
	public OracleInput initDataInput(ExporterResult result)
			throws ExportException {
		OracleInput input = new OracleInput(expConfig);
		return input;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.expcore.FlexibleExporter#initDataOutput(com.navinfo.dataservice.expcore.ExporterResult)
	 */
	@Override
	public DataOutput initDataOutput(ExporterResult result)
			throws ExportException {
		if(ExportConfig.MODE_DELETE.equals(expConfig.getExportMode())){
			//删除数据不需要output
			return null;
		}
		ThreadLocalContext ctx = new ThreadLocalContext(log);
		Oracle2OracleDataOutput output = new Oracle2OracleDataOutput(expConfig,result,ctx);
		return output;
	}
	
	public static void main(String[] args){
		InputStream is = null;
		try{
			is = Exporter2OracleByScripts.class.getResourceAsStream("/com/navinfo/dataservice/expcore/resources/export-config-template.xml");
			String configStr = IOUtils.toString(is);
			ExportConfig expConfig = new ExportConfig();
//			expConfig.setGdbVersion("240");
//			expConfig.setExportMode(ExportConfig.MODE_COPY);
//			expConfig.setSourceIp("192.168.4.103");
//			expConfig.setSourcePort(1521);
//			expConfig.setSourceUserName("acg_schema_zq_1");
//			expConfig.setSourcePassword("acg_schema_zq_1");
//			expConfig.setSourceServiceName("orcl11g");
//			expConfig.setCondition(ExportConfig.CONDITION_BY_MESH);
//			Set<String> conditionParams = new HashSet<String>();
//			conditionParams.add("605621");
//			conditionParams.add("605624");
//			expConfig.setConditionParams(conditionParams);
//			expConfig.setFeature(ExportConfig.FEATURE_ALL);
//			expConfig.setDestroyTarget(true);
//			expConfig.setNewTarget(true);
//			expConfig.setTargetIp("192.168.3.42");
//			expConfig.setTargetPort(1521);
//			expConfig.setTargetServiceName("orcl");
//			expConfig.setTargetSysName("app_user");
//			expConfig.setTargetSysPassword("n@vidms");
			
			Exporter2OracleByScripts export = new Exporter2OracleByScripts(expConfig);
			ExporterResult result = export.execute();
			System.out.println(result.getNewTargetDbId());
		}catch(Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}finally{
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
	}

}
