package com.hand.demo.app.job;

import org.hzero.boot.scheduler.infra.annotation.JobHandler;
import org.hzero.boot.scheduler.infra.enums.ReturnT;
import org.hzero.boot.scheduler.infra.handler.IJobHandler;
import org.hzero.boot.scheduler.infra.tool.SchedulerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@JobHandler("DEMO-47360")
public class DemoJob implements IJobHandler {
    private static final Logger logger = LoggerFactory.getLogger(DemoJob.class);

    @Override
    public ReturnT execute(Map<String, String> map, SchedulerTool tool) {
        logger.info("test job 47360: " + map.get("userId"));
        return ReturnT.SUCCESS;
    }
}
