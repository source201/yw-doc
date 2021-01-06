package com.yw.quartzdemo.dao;





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
