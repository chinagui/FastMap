package com.navinfo.dataservice.engine.statics.launcher.starter;

import com.navinfo.dataservice.engine.statics.launcher.StatJobStarter;

/**
 * NoTaskStatStarter
 * Created by songhe on 2017/8/31
 */
public class NoTaskStatStarter extends StatJobStarter {

    @Override
    public String jobType() {
        return "noTaskJob";
    }
}
