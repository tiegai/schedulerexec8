package com.nike.ncp.scheduler.common.biz.model;

import java.io.Serializable;

public class LogResult implements Serializable {
    private static final long serialVersionUID = 42L;

    public LogResult() {
    }

    public LogResult(int fromLineNum, int toLineNum, String logContent, boolean end) {
        this.fromLineNum = fromLineNum;
        this.toLineNum = toLineNum;
        this.logContent = logContent;
        this.end = end;
    }

    private int fromLineNum;
    private int toLineNum;
    private String logContent;
    private boolean end;

    public int getFromLineNum() {
        return fromLineNum;
    }

    public void setFromLineNum(int fromLineNum) {
        this.fromLineNum = fromLineNum;
    }

    public int getToLineNum() {
        return toLineNum;
    }

    public void setToLineNum(int toLineNum) {
        this.toLineNum = toLineNum;
    }

    public String getLogContent() {
        return logContent;
    }

    public void setLogContent(String logContent) {
        this.logContent = logContent;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }
}
