package com.yw.quartzdemo.po;

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