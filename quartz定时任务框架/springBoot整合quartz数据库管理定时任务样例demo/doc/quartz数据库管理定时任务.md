# springBoot整合quartz数据库管理定时任务样例demo

### 样例介绍

采用quartz，并使用数据库来动态管理定时任务。

### 步骤

（1）导入依赖

```xml
<!--quartz -->
<dependency>
	<groupId>org.quartz-scheduler</groupId>
	<artifactId>quartz</artifactId>
	<version>2.3.0</version>
</dependency>
<dependency>
	<groupId>org.springframework</groupId>
	<artifactId>spring-context-support</artifactId>
</dependency>
<!--数据库 -->
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<scope>runtime</scope>
</dependency>
<dependency>
	<groupId>com.baomidou</groupId>
	<artifactId>mybatis-plus-boot-starter</artifactId>
	<version>3.0.5</version>
</dependency>

<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>druid-spring-boot-starter</artifactId>
	<version>1.1.10</version>
</dependency>
```

(2)添加配置

数据库配置：

```properties
spring.datasource.druid.url=jdbc:mysql://*.*.*.*(这里使用自己服务器的ip):3306/(自己的数据库)?reWriteBatchedInserts=true
spring.datasource.druid.username=root
spring.datasource.druid.password=123456
spring.datasource.druid.driver-class-name=com.mysql.jdbc.Driver
```

quartz配置：

```properties
#--------------------------------------------------------------
#调度器属性
#--------------------------------------------------------------
# 调度器实例名：可以随意命名
org.quartz.scheduler.instanceName=quartz-demo
# 实例ID: 允许随意命名，但必须保持唯一，集群可以让quartz来帮你命名，设置为AUTO
org.quartz.scheduler.instanceId=AUTO
#只有org.quartz.scheduler.instanceId设置为“AUTO”才使用。
# 默认为“org.quartz.simpl.SimpleInstanceIdGenerator”，它是主机名和时间戳生成实例Id的。
# 其它的IntanceIdGenerator实现包括SystemPropertyInstanceIdGenerator（它从系统属性“org.quartz.scheduler.instanceId”获取实例Id），
# 和HostnameInstanceIdGenerator（它使用本地主机名InetAddress.getLocalHost().getHostName()生成实例Id）。
# 你也实现你自己的InstanceIdGenerator
org.quartz.scheduler.instanceIdGenerator.class=com.yw.quartzdemo.support.QuartzInstanceIdGenerator
#--------------------------------------------------------------
# 线程池属性
#--------------------------------------------------------------
#  org.quartz.threadPool.class这个值是一个实现了 org.quartz.spi.ThreadPool 接口的类的全限名称。
#  Quartz 自带的线程池实现类是 org.quartz.smpl.SimpleThreadPool，
#  它能够满足大多数用户的需求。这个线程池实现具备简单的行为，并经很好的测试过。
#  它在调度器的生命周期中提供固定大小的线程池。
#  你能根据需求创建自己的线程池实现，如果你想要一个随需可伸缩的线程池时也许需要这么做。
#  这个属性没有默认值，你必须为其指定值。
org.quartz.threadPool.class=org.quartz.simpl.SimpleThreadPool
# threadCount 属性控制了多少个工作者线程被创建用来处理 Job。
# 原则上是，要处理的 Job 越多，那么需要的工作者线程也就越多。
# threadCount 的数值至少为 1。
# Quartz 没有限定你设置工作者线程的最大值，但是在多数机器上设置该值超过100的话就会显得相当不实用了，
# 特别是在你的 Job 执行时间较长的情况下。
# 这项没有默认值，所以你必须为这个属性设定一个值
org.quartz.threadPool.threadCount=10
# 优先级：可以是Thread.MIN_PRIORITY (1)和Thread.MAX_PRIORITY (10)之间的任意整数。默认为Thread.NORM_PRIORITY (5).
org.quartz.threadPool.threadPriority=5
# 继承初始化线程的上下文类加载器线程
org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread=true
#--------------------------------------------------------------
#作业存储设置：作业存储部分的设置描述了在调度器实例的生命周期中，Job 和 Trigger 信息是如何被存储的。
# 把调度器信息存储在内存中非常的快也易于配置。
# 当调度器进程一旦被终止，所有的 Job 和 Trigger 的状态就丢失了。
# 要使 Job 存储在内存中需通过设置 org.quartz.jobStrore.class 属性为 org.quartz.simpl.RAMJobStore，
# 在Cron Trigger 和“作业存储和持久化”会用到的不同类型的作业存储实现。
#--------------------------------------------------------------
# trigger被认为失败之前，scheduler能够承受的下一次触发时间（单位毫秒）。默认值为60秒。
org.quartz.jobStore.misfireThreshold=60000
# 用于将调度信息（job、trigger和calendar）存储到关系数据库中。
org.quartz.jobStore.class=org.quartz.impl.jdbcjobstore.JobStoreTX
# 设置当前数据库Driver代理的数据库系统的方言
org.quartz.jobStore.driverDelegateClass=org.quartz.impl.jdbcjobstore.StdJDBCDelegate
#org.quartz.jobStore.driverDelegateClass:org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
# 表前缀
org.quartz.jobStore.tablePrefix=QRTZ_
# JobStore处理失败trigger的最大等待时间。
# 同时处理多个trigger（多于几个）回引发数据表长时间锁定，触发其它的trigger（还没有失败）的性能就会受到限制
org.quartz.jobStore.maxMisfiresToHandleAtATime=10
# 使用集群特性，这个属性必须为true。
# 如果你有多个Quartz实例使用相同的数据库表，这个属性必须为true，否则你会体验一把大破坏。参见集群配置。
org.quartz.jobStore.isClustered=true  
# 设置当前实例check in集群中其它实例的频率。影响检测到故障实例的速度
org.quartz.jobStore.clusterCheckinInterval=20000
# “org.quartz.jobStore.useProperties”配置参数可以被设置为true（默认为false），
# 这样可以指导JDBCJobStore，JobDataMaps中的值都是字符串，因此这样可以以名字-值对存储，
# 而不是存储更加复杂的对象（序列化形式BLOB）。
# 从长远来看，这是很安全的，因为避免了将非字符串类序列化为BLOB的类版本问题
org.quartz.jobStore.useProperties=true
```

（3）添加定时任务表的PO,mapper,dao

po:

```java
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("JOB_DEF")
@Builder
public class JobDefPO {
    /**
     * 任务名称-类全名
     */
    private String jobName;

    /**
     * 任务分组
     */
    private String jobGroup;

    /**
     * 任务执行表达式
     */
    private String cron;

    /**
     * 任务状态
     */
    private Integer status;

}
```

mapper:

```java
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yw.quartzdemo.po.JobDefPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface JobDefMapper extends BaseMapper<JobDefPO> {

}
```

dao

```java
import com.yw.quartzdemo.po.JobDefPO;

import java.util.List;

/**
 * 任务定义接口
 */
public interface JobDefDAO {

    /***
     * @Description 查询所有任务
     * @author yuanwei
     * @param 
     * @return java.util.List<com.yw.quartzdemo.po.JobDefPO>
     * @time 2021/1/6 16:35
     */
    List<JobDefPO> listAll();
}
```

dao.impl:

```java
import com.baomidou.mybatisplus.core.conditions.Condition;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yw.quartzdemo.dao.JobDefDAO;
import com.yw.quartzdemo.mapper.JobDefMapper;
import com.yw.quartzdemo.po.JobDefPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 任务定义实现
 */
@Service
@Slf4j
public class JobDefDAOImpl extends ServiceImpl<JobDefMapper, JobDefPO> implements JobDefDAO {

    
    @Override
    public List<JobDefPO> listAll() {
        List<JobDefPO> result = null;
        final QueryWrapper<JobDefPO> wrapper = Condition.create();
        try {
            result = this.list(wrapper);
        } catch (final Exception e) {
            log.error("查询定时任务定义异常：{}，{}", e.getMessage(), e);
            throw new RuntimeException("查询定时任务定义失败");
        }
        return result;
    }
}
```



（4）添加配置：

config包下

quartzConfig.java 

```java

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
```

supper包下：

JobRefresh.java 用于设置定时刷新频率。

```java
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

```

(5)添加定时任务

```java
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

```

（6）在数据库中添加（5）中任务的触发时间

其中status中0为开启，1为关闭。

```mysql
INSERT INTO `fund`.`JOB_DEF`(`JOB_NAME`, `JOB_GROUP`, `CRON`, `STATUS`) VALUES ('com.yw.quartzdemo.handler.DemoJobHandler', 'demo', '* * * * * ?', 0);
```



