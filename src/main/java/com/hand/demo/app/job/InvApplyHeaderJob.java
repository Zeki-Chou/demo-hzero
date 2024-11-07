package com.hand.demo.app.job;

import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;

import java.util.Map;

@JobHandler("EXAM-47355-APPLY-HEADER")
public class InvApplyHeaderJob implements IJobHandler {
    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        return null;
    }
}
