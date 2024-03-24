package com.sky.task;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class MyTask {

    /**
     * 定时任务业务逻辑相关代码
     */


    public  void executeTask(){
        log.info("我是定时任务  " + new Date());
    }
}
