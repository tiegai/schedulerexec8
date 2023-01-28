package com.nike.ncp.scheduler.common.log;

import com.nike.ncp.scheduler.common.biz.model.LogResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * store trigger log in each log-file
 */
public final class XxlJobFileAppender {
    private XxlJobFileAppender() {

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(XxlJobFileAppender.class);

    /**
     * log base path
     * <p>
     * strut like:
     * ---/
     * ---/gluesource/
     * ---/gluesource/10_1514171108000.js
     * ---/gluesource/10_1514171108000.js
     * ---/2017-12-25/
     * ---/2017-12-25/639.log
     * ---/2017-12-25/821.log
     */
    private static String logBasePath = "/data/applogs/xxl-job/jobhandler";
    private static String glueSrcPath = logBasePath.concat("/gluesource");

    public static void initLogPath(String logPath) {
        // init
        if (logPath != null && logPath.trim().length() > 0) {
            logBasePath = logPath;
        }
        // mk base dir
        File logPathDir = new File(logBasePath);
        if (!logPathDir.exists()) {
            try {
                logPathDir.mkdirs();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        logBasePath = logPathDir.getPath();

        // mk glue dir
        File glueBaseDir = new File(logPathDir, "gluesource");
        if (!glueBaseDir.exists()) {
            try {
                glueBaseDir.mkdirs();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        glueSrcPath = glueBaseDir.getPath();
    }

    public static String getLogPath() {
        return logBasePath;
    }

    public static String getGlueSrcPath() {
        return glueSrcPath;
    }

    /**
     * log filename, like "logPath/yyyy-MM-dd/9999.log"
     *
     * @param triggerDate
     * @param logId
     * @return
     */
    @SuppressWarnings("all")
    public static String makeLogFileName(Date triggerDate, long logId) {

        // filePath/yyyy-MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");    // avoid concurrent problem, can not be static
        File logFilePath = new File(getLogPath(), sdf.format(triggerDate));
        if (!logFilePath.exists()) {
            try {
                logFilePath.mkdir();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        // filePath/yyyy-MM-dd/9999.log
        String logFileName = logFilePath.getPath().concat(File.separator).concat(String.valueOf(logId)).concat(".log");
        return logFileName;
    }

    /**
     * append log
     *
     * @param logFileName
     * @param appendLog
     */
    @SuppressWarnings("all")
    public static void appendLog(String logFileName, String appendLog) {

        // log file
        if (logFileName == null || logFileName.trim().length() == 0) {
            return;
        }
        File logFile = new File(logFileName);

        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                return;
            }
        }

        // log
        if (appendLog == null) {
            appendLog = "";
        }
        appendLog += "\r\n";
        // append file content
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(logFile, true);
            fos.write(appendLog.getBytes("utf-8"));
            fos.flush();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * support read log-file
     *
     * @param logFileName
     * @return log content
     */
    @SuppressWarnings("all")
    public static LogResult readLog(String logFileName, int fromLineNum) {

        // valid log file
        if (logFileName == null || logFileName.trim().length() == 0) {
            return new LogResult(fromLineNum, 0, "readLog fail, logFile not found", true);
        }
        File logFile = new File(logFileName);

        if (!logFile.exists()) {
            return new LogResult(fromLineNum, 0, "readLog fail, logFile not exists", true);
        }

        // read file
        StringBuffer logContentBuffer = new StringBuffer();
        int toLineNum = 0;
        LineNumberReader reader = null;
        try {
            //reader = new LineNumberReader(new FileReader(logFile));
            reader = new LineNumberReader(new InputStreamReader(new FileInputStream(logFile), "utf-8"));
            String line = null;

            while ((line = reader.readLine()) != null) {
                toLineNum = reader.getLineNumber();        // [from, to], start as 1
                if (toLineNum >= fromLineNum) {
                    logContentBuffer.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }

        // result
        LogResult logResult = new LogResult(fromLineNum, toLineNum, logContentBuffer.toString(), false);
        return logResult;
    }

    /**
     * read log data
     *
     * @param logFile
     * @return log line content
     */
    @SuppressWarnings("all")
    public static String readLines(File logFile) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(logFile), "utf-8"));
            //if (reader != null) {
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
            //}
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
        return null;
    }

}
