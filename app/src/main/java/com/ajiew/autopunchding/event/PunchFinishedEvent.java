package com.ajiew.autopunchding.event;

/**
 * author: aaron.chen
 * created on: 2018/9/5 08:45
 * description: 完成打卡事件
 */
public class PunchFinishedEvent {

    private PunchType punchType;

    private String time;

    public PunchFinishedEvent(PunchType punchType, String time) {
        this.punchType = punchType;
        this.time = time;
    }

    public PunchType getPunchType() {
        return punchType;
    }

    public void setPunchType(PunchType punchType) {
        this.punchType = punchType;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
