package main;


import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Crawler4j {
    public static void main(String args[]) {
        //创建线程池
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        //每6分钟执行一次 一小时 10 次
        Task4Crawl task = new Task4Crawl();
        //时间定为 6 分钟 一次  使用  AtFixedRate
        executor.scheduleAtFixedRate(task , 0 , 6 , TimeUnit.MINUTES);
    }

}
