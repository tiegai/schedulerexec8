package com.nike.ncp.scheduler.common.biz.client;


import com.nike.ncp.scheduler.common.biz.model.IdleBeatParam;
import com.nike.ncp.scheduler.common.biz.model.KillParam;
import com.nike.ncp.scheduler.common.biz.model.ReturnT;
import com.nike.ncp.scheduler.common.biz.model.TriggerParam;
import com.nike.ncp.scheduler.common.biz.model.LogResult;
import com.nike.ncp.scheduler.common.biz.model.LogParam;
import com.nike.ncp.scheduler.common.util.XxlJobRemotingUtil;
import com.nike.ncp.scheduler.common.biz.ExecutorBiz;


public class ExecutorBizClient implements ExecutorBiz {

    public ExecutorBizClient() {
    }

    public ExecutorBizClient(String addressUrl, String accessToken) {
        this.addressUrl = addressUrl;
        this.accessToken = accessToken;

        // valid
        if (!this.addressUrl.endsWith("/")) {
            this.addressUrl = this.addressUrl + "/";
        }
    }

    private transient String addressUrl;
    private transient String accessToken;
    private transient int timeout = 3;


    @Override
    public ReturnT<String> beat() {
        return XxlJobRemotingUtil.postBody(addressUrl + "beat", accessToken, timeout, "", String.class);
    }

    @Override
    public ReturnT<String> idleBeat(IdleBeatParam idleBeatParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + "idleBeat", accessToken, timeout, idleBeatParam, String.class);
    }

    @Override
    public ReturnT<String> run(TriggerParam triggerParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + "run", accessToken, timeout, triggerParam, String.class);
    }

    @Override
    public ReturnT<String> kill(KillParam killParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + "kill", accessToken, timeout, killParam, String.class);
    }

    @Override
    public ReturnT<LogResult> log(LogParam logParam) {
        return XxlJobRemotingUtil.postBody(addressUrl + "log", accessToken, timeout, logParam, LogResult.class);
    }

}
