package com.yw.quartzdemo.dao.impl;

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
