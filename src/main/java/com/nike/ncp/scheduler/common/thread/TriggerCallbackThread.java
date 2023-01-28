package com.nike.ncp.scheduler.common.thread;

import com.nike.ncp.scheduler.common.biz.AdminBiz;
import com.nike.ncp.scheduler.common.biz.model.HandleCallbackParam;
import com.nike.ncp.scheduler.common.biz.model.ReturnT;
import com.nike.ncp.scheduler.common.context.XxlJobContext;
import com.nike.ncp.scheduler.common.context.XxlJobHelper;
import com.nike.ncp.scheduler.common.enums.RegistryConfig;
import com.nike.ncp.scheduler.common.executor.XxlJobExecutor;
import com.nike.ncp.scheduler.common.log.XxlJobFileAppender;
import com.nike.ncp.scheduler.common.util.FileUtil;
import com.nike.ncp.scheduler.common.util.JdkSerializeTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class TriggerCallbackThread {
    private static final Logger LOGGER = LoggerFactory.getLogger(TriggerCallbackThread.class);

    private static TriggerCallbackThread instance = new TriggerCallbackThread();

    public static TriggerCallbackThread getInstance() {
        return instance;
    }

    /**
     * job results callback queue
     */
    private transient LinkedBlockingQueue<HandleCallbackParam> callBackQueue = new LinkedBlockingQueue<HandleCallbackParam>();

    public static void pushCallBack(HandleCallbackParam callback) {
        getInstance().callBackQueue.add(callback);
        LOGGER.debug(">>>>>>>>>>> xxl-job, push callback request, logId:{}", callback.getLogId());
    }

    /**
     * callback thread
     */
    private transient Thread triggerCallbackThreadExe;
    private transient Thread triggerRetryCallbackThread;
    private transient volatile boolean toStopFlag = false;

    @SuppressWarnings("all")
    public void start() {

        // valid
        if (XxlJobExecutor.getAdminBizList() == null) {
            LOGGER.warn(">>>>>>>>>>> xxl-job, executor callback config fail, adminAddresses is null.");
            return;
        }

        // callback
        triggerCallbackThreadExe = new Thread(new Runnable() {

            @Override
            public void run() {

                // normal callback
                while (!toStopFlag) {
                    try {
                        HandleCallbackParam callback = getInstance().callBackQueue.take();
                        if (callback != null) {

                            // callback list param
                            List<HandleCallbackParam> callbackParamList = new ArrayList<HandleCallbackParam>();
                            getInstance().callBackQueue.drainTo(callbackParamList);
                            callbackParamList.add(callback);

                            // callback, will retry if error
                            if (callbackParamList != null && callbackParamList.size() > 0) {
                                doCallback(callbackParamList);
                            }
                        }
                    } catch (Exception e) {
                        if (!toStopFlag) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }

                // last callback
                try {
                    List<HandleCallbackParam> callbackParamList = new ArrayList<HandleCallbackParam>();
                    getInstance().callBackQueue.drainTo(callbackParamList);
                    if (callbackParamList != null && callbackParamList.size() > 0) {
                        doCallback(callbackParamList);
                    }
                } catch (Exception e) {
                    if (!toStopFlag) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                LOGGER.info(">>>>>>>>>>> xxl-job, executor callback thread destroy.");

            }
        });
        triggerCallbackThreadExe.setDaemon(true);
        triggerCallbackThreadExe.setName("xxl-job, executor TriggerCallbackThread");
        triggerCallbackThreadExe.start();


        // retry
        triggerRetryCallbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!toStopFlag) {
                    try {
                        retryFailCallbackFile();
                    } catch (Exception e) {
                        if (!toStopFlag) {
                            LOGGER.error(e.getMessage(), e);
                        }

                    }
                    try {
                        TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                    } catch (InterruptedException e) {
                        if (!toStopFlag) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
                LOGGER.info(">>>>>>>>>>> xxl-job, executor retry callback thread destroy.");
            }
        });
        triggerRetryCallbackThread.setDaemon(true);
        triggerRetryCallbackThread.start();

    }

    public void toStop() {
        toStopFlag = true;
        // stop callback, interrupt and wait
        if (triggerCallbackThreadExe != null) {    // support empty admin address
            triggerCallbackThreadExe.interrupt();
            try {
                triggerCallbackThreadExe.join();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        // stop retry, interrupt and wait
        if (triggerRetryCallbackThread != null) {
            triggerRetryCallbackThread.interrupt();
            try {
                triggerRetryCallbackThread.join();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

    }

    /**
     * do callback, will retry if error
     *
     * @param callbackParamList
     */
    @SuppressWarnings("all")
    private void doCallback(List<HandleCallbackParam> callbackParamList) {
        boolean callbackRet = false;
        // callback, will retry if error
        for (AdminBiz adminBiz : XxlJobExecutor.getAdminBizList()) {
            try {
                ReturnT<String> callbackResult = adminBiz.callback(callbackParamList);
                if (callbackResult != null && ReturnT.SUCCESS_CODE == callbackResult.getCode()) {
                    callbackLog(callbackParamList, "<br>----------- xxl-job job callback finish.");
                    callbackRet = true;
                    break;
                } else {
                    callbackLog(callbackParamList, "<br>----------- xxl-job job callback fail, callbackResult:" + callbackResult);
                }
            } catch (Exception e) {
                callbackLog(callbackParamList, "<br>----------- xxl-job job callback error, errorMsg:" + e.getMessage());
            }
        }
        if (!callbackRet) {
            appendFailCallbackFile(callbackParamList);
        }
    }

    /**
     * callback log
     */
    private void callbackLog(List<HandleCallbackParam> callbackParamList, String logContent) {
        for (HandleCallbackParam callbackParam : callbackParamList) {
            String logFileName = XxlJobFileAppender.makeLogFileName(new Date(callbackParam.getLogDateTim()), callbackParam.getLogId());
            XxlJobContext.setXxlJobContext(new XxlJobContext(
                    -1,
                    null,
                    logFileName,
                    -1,
                    -1));
            XxlJobHelper.log(logContent);
        }
    }


    // ---------------------- fail-callback file ----------------------

    private static String failCallbackFilePath = XxlJobFileAppender.getLogPath().concat(File.separator).concat("callBacklog").concat(File.separator);
    private static String failCallbackFileName = failCallbackFilePath.concat("xxl-job-callback-{x}").concat(".log");

    private void appendFailCallbackFile(List<HandleCallbackParam> callbackParamList) {
        // valid
        if (callbackParamList == null || callbackParamList.size() == 0) {
            return;
        }

        // append file
        byte[] callbackParamListBytes = JdkSerializeTool.serialize(callbackParamList);

        File callbackLogFile = new File(failCallbackFileName.replace("{x}", String.valueOf(System.currentTimeMillis())));
        if (callbackLogFile.exists()) {
            for (int i = 0; i < 100; i++) {
                callbackLogFile = new File(failCallbackFileName.replace("{x}", String.valueOf(System.currentTimeMillis()).concat("-").concat(String.valueOf(i))));
                if (!callbackLogFile.exists()) {
                    break;
                }
            }
        }
        FileUtil.writeFileContent(callbackLogFile, callbackParamListBytes);
    }

    private void retryFailCallbackFile() {

        // valid
        File callbackLogPath = new File(failCallbackFilePath);
        if (!callbackLogPath.exists()) {
            return;
        }
        if (callbackLogPath.isFile()) {
            try {
                callbackLogPath.delete();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        String[] callbackLogPathList = callbackLogPath.list();
        if (!(callbackLogPath.isDirectory() && callbackLogPathList != null && callbackLogPathList.length > 0)) {
            return;
        }

        // load and clear file, retry
        File[] callbackLogPathFile = callbackLogPath.listFiles();
        if (callbackLogPathFile != null && callbackLogPathFile.length > 0) {
            for (File callBackLogFile : callbackLogPathFile) {
                byte[] callbackParamListBytes = FileUtil.readFileContent(callBackLogFile);

                // avoid empty file
                if (callbackParamListBytes == null || callbackParamListBytes.length < 1) {
                    try {
                        callBackLogFile.delete();
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    continue;
                }

                List<HandleCallbackParam> callbackParamList = (List<HandleCallbackParam>) JdkSerializeTool.deserialize(callbackParamListBytes, List.class);
                try {
                    callBackLogFile.delete();
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                doCallback(callbackParamList);
            }
        }
    }

}
