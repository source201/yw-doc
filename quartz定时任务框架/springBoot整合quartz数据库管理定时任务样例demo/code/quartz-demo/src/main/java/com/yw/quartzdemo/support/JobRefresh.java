package com.yw.quartzdemo.support;


import com.yw.quartzdemo.dao.JobDefDAO;
import com.yw.quartzdemo.po.JobDefPO;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @description
 * @author yuanwei
 * @date 2021/1/6 11:17
 */
@Component
@Slf4j
public class JobRefresh {

    @Autowired
    private JobDefDAO jobDefDAO;

    @Autowired
    private Scheduler scheduler;

    @SuppressWarnings("unchecked")
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点刷新一次
    @PostConstruct // 应用启动时刷新一次，方便测试，后续可以去掉
    public void refreshTrigger() throws SchedulerException {

        final List<JobDefPO> jobDefs = jobDefDAO.listAll();

        for (final JobDefPO jobDef : jobDefs) {
            final TriggerKey triggerKey = TriggerKey.triggerKey(jobDef.getJobName(), jobDef.getJobGroup());
            CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
            if (null == trigger) {
                // 状态(0：正常，1：禁用)
                if (jobDef.getStatus().intValue() == 1) {
                    continue;
                }
                JobDetail jobDetail = null;
                try {
                    jobDetail = JobBuilder.newJob((Class<? extends Job>) Class.forName(jobDef.getJobName()))
                            .withIdentity(jobDef.getJobName(), jobDef.getJobGroup()).build();
                } catch (final ClassNotFoundException ex) {
                    log.error(ex.getMessage(), ex);
                    throw new RuntimeException("刷新任务异常");
                }
                final CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(jobDef.getCron());
                trigger = TriggerBuilder.newTrigger().withIdentity(jobDef.getJobName(), jobDef.getJobGroup())
                        .withSchedule(scheduleBuilder).build();
                scheduler.scheduleJob(jobDetail, trigger);

            } else {
                if (jobDef.getStatus().intValue() == 1) {
                    final JobKey jobKey = JobKey.jobKey(jobDef.getJobName(), jobDef.getJobGroup());
                    scheduler.deleteJob(jobKey);
                    continue;
                }
                final String selectedCron = jobDef.getCron();
                final String currentCron = trigger.getCronExpression();
                if (!selectedCron.equals(currentCron)) {
                    final CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(selectedCron);
                    trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder)
                            .build();
                    scheduler.rescheduleJob(triggerKey, trigger);
                }
            }
        }

    }
}
