package com.navinfo.dataservice.scripts;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.UuidUtils;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import net.sf.json.JSONObject;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class DayLogCheck {

    public static void execute(JSONObject request) throws Exception {

        int bakDbId = request.getInt("bakDbId");

        int regionDbId = request.getInt("regionDbId");

        int type = 0;

        if (request.containsKey("type")) {
            type = request.getInt("type");
        }

        if (type == 0) {
            logImport(bakDbId, regionDbId);
        }

        dbCheck(bakDbId, regionDbId);
    }

    private static void logImport(int bakDbId, int regionDbId) throws Exception {
        JSONObject dayLogCheckReq = new JSONObject();
        dayLogCheckReq.put("logDbId", regionDbId);
        dayLogCheckReq.put("targetDbId", bakDbId);
        JobInfo jobInfo = new JobInfo(0, UuidUtils.genUuid());
        jobInfo.setType("dayLogCheckImport");
        jobInfo.setRequest(dayLogCheckReq);
        jobInfo.setTaskId(0);
        AbstractJob job = JobCreateStrategy.createAsMethod(jobInfo);
        job.run();

        if (job.getJobInfo().getStatus() != 3) {
            String msg = (job.getException() == null) ? "未知错误。" : "错误：" + job.getException().getMessage();
            throw new Exception("调用刷履历job内部发生" + msg);
        }
    }

    private static void dbCheck(int bakDbId, int regionDbId) throws Exception {
        JSONObject dayLogCheckReq = new JSONObject();
        dayLogCheckReq.put("bakDbId", bakDbId);
        dayLogCheckReq.put("regionDbId", regionDbId);
        JobInfo jobInfo = new JobInfo(0, UuidUtils.genUuid());
        jobInfo.setType("dayLogCheck");
        jobInfo.setRequest(dayLogCheckReq);
        jobInfo.setTaskId(0);
        AbstractJob job = JobCreateStrategy.createAsMethod(jobInfo);
        job.run();

        if (job.getJobInfo().getStatus() != 3) {
            String msg = (job.getException() == null) ? "未知错误。" : "错误：" + job.getException().getMessage();
            throw new Exception("调用履历验证job内部发生" + msg);
        }
    }


    public static void main(String[] args) throws Exception {
        initContext();

        System.out.println("args.length:" + args.length);
        if (args == null || args.length != 2) {
            System.out.println("ERROR:need args:");
            return;
        }

        String str1=args[0];
        String str2=args[1];

        System.out.println(str1);
        System.out.println(str2);

        int regionDbId=Integer.parseInt(args[0]);

        int bakDbId=Integer.parseInt(args[1]);

        JSONObject request = new JSONObject();
        request.put("regionDbId", regionDbId);
        request.put("bakDbId", bakDbId);
        request.put("type", 0);

        execute(request);
        System.out.println("Over.");
        System.exit(0);
    }

    public static void initContext() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                new String[]{"dubbo-app-scripts.xml", "dubbo-scripts.xml"});
        context.start();
        new ApplicationContextUtil().setApplicationContext(context);
    }
}
