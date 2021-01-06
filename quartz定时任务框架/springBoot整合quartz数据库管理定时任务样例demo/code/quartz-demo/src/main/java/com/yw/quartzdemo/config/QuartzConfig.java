package com.yw.quartzdemo.config;


import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.AdaptableJobFactory;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

@Configuration
@Slf4j
public class QuartzConfig {

    @Autowired
    private AutowireCapableBeanFactory capableBeanFactory;

    @Autowired
    private DataSource dataSource;

    public Properties quartzProperties() throws IOException {

        final PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    @Bean(name = "quartzJobFactory")
    public AdaptableJobFactory quartzJobFactory() {
        return new AdaptableJobFactory() {
            @Override
            protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {

                final Object jobInstance = super.createJobInstance(bundle);
                capableBeanFactory.autowireBean(jobInstance);
                return jobInstance;
            }
        };
    }

    @Bean(name = "schedulerFactoryBean")
    public SchedulerFactoryBean schedulerFactoryBean(@Qualifier("quartzJobFactory") final AdaptableJobFactory quartzJobFactory) {

        final SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        try {
            schedulerFactoryBean.setQuartzProperties(quartzProperties());
            schedulerFactoryBean.setDataSource(dataSource);
            schedulerFactoryBean.setJobFactory(quartzJobFactory);
            schedulerFactoryBean.setStartupDelay(5);
            schedulerFactoryBean.setOverwriteExistingJobs(true);
        } catch (final IOException ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException("定时任务调度实体构建异常");
        }
        return schedulerFactoryBean;
    }

    @Bean(name = "scheduler")
    public Scheduler scheduler(@Qualifier("schedulerFactoryBean") final SchedulerFactoryBean schedulerFactoryBean) {

        return schedulerFactoryBean.getScheduler();
    }
}
