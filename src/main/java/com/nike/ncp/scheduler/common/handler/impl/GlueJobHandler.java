package com.nike.ncp.scheduler.common.handler.impl;

import com.nike.ncp.scheduler.common.context.XxlJobHelper;
import com.nike.ncp.scheduler.common.handler.IJobHandler;

/**
 * glue job handler
 */
public class GlueJobHandler extends IJobHandler {
    private long glueUpdatetime;
    private IJobHandler jobHandler;

    public GlueJobHandler(IJobHandler jobHandler, long glueUpdatetime) {
        this.jobHandler = jobHandler;
        this.glueUpdatetime = glueUpdatetime;
    }

    public long getGlueUpdatetime() {
        return glueUpdatetime;
    }

    @Override
    public void execute() throws Exception {
        XxlJobHelper.log("----------- glue.version:" + glueUpdatetime + " -----------");
        jobHandler.execute();
    }

    @Override
    public void init() throws Exception {
        this.jobHandler.init();
    }

    @Override
    public void destroy() throws Exception {
        this.jobHandler.destroy();
    }
}
