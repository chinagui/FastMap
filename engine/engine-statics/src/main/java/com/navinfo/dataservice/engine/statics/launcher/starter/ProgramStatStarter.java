package com.navinfo.dataservice.engine.statics.launcher.starter;

import com.navinfo.dataservice.engine.statics.launcher.StatJobStarter;

/**
 * ProgramStatStarter
 * Created by songhe on 2017/9/5
 */
public class ProgramStatStarter extends StatJobStarter {

    @Override
    public String jobType() {
        return "programStat";
    }
}
