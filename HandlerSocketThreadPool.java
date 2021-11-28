package com.operationSystem.four;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xwj
 * @date 2021/11/24 13:14
 *
 * 线程处理类
 *
 * /**
 * 1、当线程数小于核心线程数时，创建线程。
 * 2、当线程数大于等于核心线程数，且任务队列未满时，将任务放入任务队列。
 * 3、当线程数大于等于核心线程数，且任务队列已满
 * 4、若线程数小于最大线程数，创建线程
 * 5、若线程数等于最大线程数，抛出异常，拒绝任务
 *
 */
public class HandlerSocketThreadPool {
    //线程池
    private ExecutorService executor;

    public HandlerSocketThreadPool(int maxPoolSize, int queueSize){
        //queueSize:任务队列容量（阻塞队列）
        this.executor = new ThreadPoolExecutor(
                3, //核心线程数
                maxPoolSize,  //最大线程数
                120L,//线程空闲时间
                //当线程空闲时间达到keepAliveTime时，线程会退出，直到线程数量=corePoolSize
                //如果allowCoreThreadTimeout=true，则会直到线程数量=0
                TimeUnit.SECONDS,//单位时间
                new ArrayBlockingQueue<Runnable>(queueSize) );
    }
    public void execute(Runnable task){
        this.executor.execute(task);
    }
}
