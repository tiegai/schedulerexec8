package com.nike.ncp.scheduler.common.biz;

import com.nike.ncp.scheduler.common.biz.model.IdleBeatParam;
import com.nike.ncp.scheduler.common.biz.model.KillParam;
import com.nike.ncp.scheduler.common.biz.model.ReturnT;
import com.nike.ncp.scheduler.common.biz.model.TriggerParam;
import com.nike.ncp.scheduler.common.biz.model.LogResult;
import com.nike.ncp.scheduler.common.biz.model.LogParam;

public interface ExecutorBiz {

    /**
     * beat
     * @return
     */
    ReturnT<String> beat();

    /**
     * idle beat
     *
     * @param idleBeatParam
     * @return
     */
    ReturnT<String> idleBeat(IdleBeatParam idleBeatParam);

    /**
     * run
     * @param triggerParam
     * @return
     */
    ReturnT<String> run(TriggerParam triggerParam);

    /**
     * kill
     * @param killParam
     * @return
     */
    ReturnT<String> kill(KillParam killParam);

    /**
     * log
     * @param logParam
     * @return
     */
    ReturnT<LogResult> log(LogParam logParam);

}
