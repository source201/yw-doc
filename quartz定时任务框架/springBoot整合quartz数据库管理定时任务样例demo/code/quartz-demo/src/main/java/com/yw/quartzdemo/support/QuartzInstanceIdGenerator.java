package com.yw.quartzdemo.support;

import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.quartz.spi.InstanceIdGenerator;

import java.net.InetAddress;

/**
 * @description
 * @author yuanwei
 * @date 2021/1/6 13:51
 */
@Slf4j
public class QuartzInstanceIdGenerator implements InstanceIdGenerator {

    @SuppressWarnings("nls")
    @Override
    public String generateInstanceId() throws SchedulerException {

        String quartzInstanceId = "fund-data" + System.currentTimeMillis();
        try {
            final String ipStr = InetAddress.getLocalHost().getHostAddress();
            log.info("ip：" + ipStr);
            quartzInstanceId = "fund-" + ipStr + "-" + System.currentTimeMillis();
        } catch (final Exception e) {
            log.error("构造schedulerInstanceId异常：" + e.getMessage(), e);
        }

        log.info("schedulerInstanceId：" + quartzInstanceId);

        return quartzInstanceId;
    }

}

