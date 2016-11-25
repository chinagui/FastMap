package com.navinfo.dataservice.cop.waistcoat.job;

import com.navicop.navinfo.check.dms.NaviCopCheckEntry;
import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import org.apache.commons.lang.StringUtils;
import java.util.Random;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

/**
 * @ClassName: BatchCoreJob
 * @author Zhang Runze
 * @date 2016年6月21日 上午11:56:42
 * @Description: TODO: 批处理核心Job实现
 *
 */
public class CheckCoreJob extends AbstractJob {

    public CheckCoreJob(JobInfo jobInfo) {
        super(jobInfo);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void execute() throws JobException {
        CheckCoreJobRequest req = (CheckCoreJobRequest) request;
        CheckCoreParams checkParams = analyzeCheckParams(req);
        int timeOut = req.getTimeOut();
        String checkRuleIds = StringUtils.join(req.getRuleIds(), ",");
        String checkResult = "";
        NaviCopCheckEntry checkMgr = new NaviCopCheckEntry();
        try {
//        	System.out.println("元数据库:" + checkParams.getCheckUserName()+"  "+ checkParams.getCheckPasswd()+" "+checkParams.getCheckHost());
//        	System.out.println("kdb: "+  checkParams.getKdbUserName()+"  "+checkParams.getKdbPasswd()+"  "+ checkParams.getKdbHost());
            String taskName = getRandomString(32);
          //  System.out.println("TaskName:" + taskName);
            checkResult = checkMgr.prepareCheck(taskName, checkParams.getCheckUserName(), checkParams.getCheckPasswd(),
                    checkParams.getCheckHost(), checkParams.getCheckPort(), checkParams.getCheckSid(), checkParams.getKdbUserName(),
                    checkParams.getKdbPasswd(), checkParams.getKdbHost(), checkParams.getKdbPort(), checkParams.getKdbSid(), checkRuleIds, timeOut, null);
                    
            //System.out.println(checkResult);

            if (checkResult.indexOf("<code>0</code>") > 0) {
                    checkResult = "环境检测失败";
            }else{
            	checkResult = "环境检测成功";
            }
            response("环境监测步骤完成", null);

            if (checkResult.equals("环境检测成功")) {
                String checkExecuteResult = checkMgr.execCheck();
                if(checkExecuteResult.indexOf("<code>1</code>")>0) {
                    checkExecuteResult = "检查执行成功";
                }
                response("检查执行步骤完成", null);
                if (!checkExecuteResult.equals("检查执行成功")) {
                        throw new JobException(checkExecuteResult);
                }
            } else {
                throw new JobException(checkResult);
            }

        } catch (Exception e) {
                throw new JobException(e.getMessage(), e);
        }
    }

	@Override
	public Exception getException() {
		return super.getException();
	}

	/**
	 * 通过传入seq中的DBId号
	 * @param req
	 * @return
     */
	private CheckCoreParams analyzeCheckParams(CheckCoreJobRequest req) {
        CheckCoreParams checkParams = new CheckCoreParams();
        DatahubApi datahub = (DatahubApi) ApplicationContextUtil.getBean("datahubApi");

        try {
            //解析检查库参数
            DbInfo checkDBInfo = datahub.getDbById(req.getExecuteDBId());
            checkParams.setCheckUserName(checkDBInfo.getDbUserName());
            checkParams.setCheckPasswd(checkDBInfo.getDbUserPasswd());
            checkParams.setCheckHost(checkDBInfo.getDbServer().getIp());
            checkParams.setCheckPort(Integer.toString(checkDBInfo.getDbServer().getPort()));
            checkParams.setCheckSid(checkDBInfo.getDbServer().getServiceName());

            //解析元数据库参数
            DbInfo kdbDBInfo = datahub.getDbById(req.getKdbDBId());
            checkParams.setKdbUserName(kdbDBInfo.getDbUserName());
            checkParams.setKdbPasswd(kdbDBInfo.getDbUserPasswd());
            checkParams.setKdbHost(kdbDBInfo.getDbServer().getIp());
            checkParams.setKdbPort(Integer.toString(kdbDBInfo.getDbServer().getPort()));
            checkParams.setKdbSid(kdbDBInfo.getDbServer().getServiceName());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return checkParams;
    }

    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";//含有字符和数字的字符串
        Random random = new Random();//随机类初始化
        StringBuffer sb = new StringBuffer();//StringBuffer类生成，为了拼接字符串

        for (int i = 0; i < length; ++i) {
            int number = random.nextInt(62);// [0,62)

            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

	class CheckCoreParams {

        private String checkUserName;
        private String checkPasswd;
        private String checkHost;
        private String checkPort;
        private String checkSid;

        private String kdbUserName;
        private String kdbPasswd;
        private String kdbHost;
        private String kdbPort;
        private String kdbSid;

        public String getCheckUserName() {
			return checkUserName;
		}

        public void setCheckPasswd(String checkUserName) {
			this.checkUserName = checkUserName;
		}

        public String getCheckPasswd() {
			return checkPasswd;
		}

        public void setCheckUserName(String checkPasswd) {
            this.checkPasswd = checkPasswd;
        }

        public String getCheckHost() {
			return checkHost;
		}

        public void setCheckHost(String checkHost) {
			this.checkHost = checkHost;
		}

        public String getCheckPort() {
			return checkPort;
		}

        public void setCheckPort(String checkPort) {
			this.checkPort = checkPort;
		}

        public String getCheckSid() {
			return checkSid;
		}

        public void setCheckSid(String checkSid) {
			this.checkSid = checkSid;
		}

        public String getKdbUserName() {
			return kdbUserName;
		}

        public void setKdbUserName(String kdbUserName) {
			this.kdbUserName = kdbUserName;
		}

        public String getKdbPasswd() {
			return kdbPasswd;
		}

        public void setKdbPasswd(String kdbPasswd) {
			this.kdbPasswd = kdbPasswd;
		}

        public String getKdbHost() {
			return kdbHost;
		}

        public void setKdbHost(String kdbHost) {
			this.kdbHost = kdbHost;
		}

        public String getKdbPort() {
			return kdbPort;
		}

        public void setKdbPort(String kdbPort) {
			this.kdbPort = kdbPort;
		}

        public String getKdbSid() {
			return kdbSid;
		}

        public void setKdbSid(String kdbSid) {
			this.kdbSid = kdbSid;
		}

	}

}
