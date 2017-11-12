package main;
import dataBase.NewsInfoDAO;
import mailTools.MailTools;
import dataBase.NewsInfo;
import webdriver.HtmlUnitDriver;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

public class Task4Crawl implements Runnable {
    //param
    private LocalDateTime lastTime;
    private LocalDateTime latestTime;

    //首先访问登录页面 登陆后 跳转到目标页面 在访问最新发布的新闻的页面

    String urlLogin = "http://news.gdut.edu.cn/UserLogin.aspx";
    String urlTarget = "http://news.gdut.edu.cn/ArticleList.aspx?keyword=%E7%9B%91%E8%80%83&category=5&department=2147483647&start=&end=";

    @Override
    public void run() {
        NewsInfo lastInfo = new NewsInfo();
        NewsInfo latestInfo = new NewsInfo();

        //上班时间 和 下班时间 还有当前时间
        LocalTime startTime = LocalTime.of(9, 0, 0);
        LocalTime endTime = LocalTime.of(18, 0, 0);
        LocalTime nowTime = LocalTime.now();
        System.out.println("It's " + nowTime + " now");

        //若处于上班时间内 执行爬取 其余时间 待机
        if (nowTime.isAfter(startTime) && nowTime.isBefore(endTime)) {

                try {
                System.out.println("working time ! crawling...");
                //读取 数据库中上一次的对象信息
                lastInfo = new NewsInfoDAO().get();
                lastTime = lastInfo.getTime();

                //System.out.println(lastInfo);

                //获取资讯对象
                latestInfo = new HtmlUnitDriver().getLatestInfo();
                latestTime = latestInfo.getTime();

                //System.out.println(latestInfo);

                //这个记得做  要把心爬的 录进去 作为下一次 用
                new NewsInfoDAO().updateInfo(latestInfo);

            } catch (Exception e) {
                e.printStackTrace();
            }

            /**
             * 若发现有上次爬去未爬取得信息 , 邮件提醒
             *
             * */
            if (latestTime.isAfter(lastTime)) {
                //邮件提醒! 最新的 不是 lastInfo 哦哈
                try {
                    System.out.println("Find new release!");
                    new MailTools().mailAlert(latestInfo);
                    System.out.println("...Mail complete.Crawled by tommy at " + LocalDateTime.now());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //测试用
                System.out.println(" Not latest news . Crawled by tommy at " + LocalDateTime.now());

            }
        }// end if
        else {
            System.out.println("it s not working time , just waiting......");
        }

    }
}
