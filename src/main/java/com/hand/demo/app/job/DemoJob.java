package com.hand.demo.app.job;

import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@JobHandler("DEMO_JOB_47361")
public class DemoJob implements IJobHandler {
    private static final Logger LOG = LoggerFactory.getLogger(DemoJob.class);

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        LOG.info("Test Job 47361: " + map.get("userId"));
        LOG.info("Email :" + map.get("email"));

        return ReturnT.SUCCESS;
    }

}
