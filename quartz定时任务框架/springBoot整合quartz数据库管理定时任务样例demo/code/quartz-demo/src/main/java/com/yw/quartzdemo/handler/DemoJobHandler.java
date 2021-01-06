package com.yw.quartzdemo.handler;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

/**
 * @description
 * @author yuanwei
 * @date 2021/1/6 16:38
 */
@Slf4j
public class DemoJobHandler implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Date time = new Date();
        log.info("时间：{}，打印",time);
    }
}
