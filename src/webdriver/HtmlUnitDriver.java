package webdriver;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import dataBase.NewsInfo;
import dataBase.NewsInfoDAO;
import regex.RegexTools;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Driver class
 *
 * @author wojiaotommy
 * @date 2017/11/14
 */

public class HtmlUnitDriver {

    private String urlTarget;
    /**
     * timeout
     */
    private static long WAIT_PAGE_REFRESH = 2000L;
    /**
     * 最新发布的新闻的访问地址
     */
    private String latestUrl;

    private String titlePattern;
    private String departmentPattern ;
    private String timePattern ;
    private String countPattern;
    /**
     * 正则 pattern ,  第一个用于找出最新的地址,  剩余三个用于找出 specific 新闻里的关键信息
     */
    private String patternStr;
    private String department;
    private String timeStr;
    private LocalDateTime newstime;
    private String readCount;
    /**
     * 四个需要爬出出来的信息
     */
    private String title;
    /**newsinfo object*/
    private NewsInfo newsInfo;
    /**single client*/
    private WebClient webClient;


    public HtmlUnitDriver() {

        {
            //constructor
            webClient = new WebClient();

            //经过测试  关闭 css 和 关闭 javascript 的加载可以减少错误
            //另外 我访问的网页全都是 静态网页
            //静态推荐两个都关闭
            //关闭 css
            webClient.getOptions().setCssEnabled(false);
            //关闭 js 支持
            webClient.getOptions().setJavaScriptEnabled(false);
            //由于如果 response 的 status code 不是 200 如302跳转等  就会抛出failing status code 的错误 这里关闭这个错误的抛出 忽略
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            //允许重定向
            webClient.getOptions().setRedirectEnabled(true);
            //cookie support
            webClient.getCookieManager().setCookiesEnabled(true);
            //设置 timeout 时间 避免因为 网络突然缓慢导出 的 java.net.SocketTimeoutException: Read timed out under tomcat
            webClient.getOptions().setTimeout(120000);

            //由于长时间重复执行后  会报 “FailingHttpStatusCodeException: Too much redirect for”  的错误
            //原因已查实 是因为 htmlunit 会默认 把response cache 起来(相当于历史记录 可以实现回退和跳转),过多的 cache 会占据资源空间 因此报错误提醒
            //解决办法 . 把 cache 功能关闭
            webClient.getCache().setMaxSize(0);

        }


        newsInfo = new NewsInfo();
        /*params*/
        String urlLogin = "http://news.gdut.edu.cn/UserLogin.aspx";
        this.urlTarget = "http://news.gdut.edu.cn/ArticleList.aspx?keyword=%E7%9B%91%E8%80%83&category=5&department=2147483647&start=&end=";
        //default login params
        String username = "gdutnews";
        String password = "newsgdut";
        //---
        patternStr = "<div.id=\"ContentPlaceHolder1_ListView1_ItemPlaceHolderContainer\">.+?<p>.+?<a.href=\"(.+?)\"";
        /*
         * 这次使用 \s 也就是 空字符匹配 来去除 title 两头的各种看上去的空格= =.
         * 当然有心思还可以使用  dom4j  或者 jsoup 来解 xml
         *
         * */
        titlePattern = "<center>\\s*<span.style=.*?>\\s*(.+?)\\s*</span>";
        departmentPattern = "\\[所属部门:(.+?)\\]";
        timePattern = "\\[发布日期:(.+?)\\]";
        countPattern = "\\[阅读次数:(\\d+)\\]";
    }


    /**
     * get title
     **/
    private String getTitle(String content){
        String pStr = "<title>.+?(广东工业大学新闻通知网).+?</title>";

        return new RegexTools().doRegex(content, pStr).get(0);
    }


    public NewsInfo getLatestInfo(CookieManager cm) throws IOException, InterruptedException, SQLException {


        //设置 webclient 的 timeout 时间( milliseconds)
        /*
         * Note: The timeout is used twice. The first is for making the socket connection, the second is for data retrieval.
         *If the time is critical you must allow for twice the time specified here.
         *
         */
        //由于是周期任务 ,而 CookieManager 是 在 main 方法里面传过来的
        //所以 还能保存到上一次获取的 cookies 若未过期 set 上去就可以直接访问 target 页面啦
        //无需在浪费资源访问 , 登录页面
        webClient.setCookieManager(cm);


        //尝试访问 目标页面 ,如果还没登录 无 cookie 服务器会重定向到 login page
        HtmlPage pageTemp = webClient.getPage(urlTarget);

        //等待网页响应啦 等价于 setimeout()
        Thread.sleep(WAIT_PAGE_REFRESH);

        /*
         * 这里我要说一个很狗血的东西
         * 就是 广工所有新闻网里面的网页的 title 基本都是一样的
         * 只不过 是 login 页面 是 "广东工业大学新闻通知网" 而其他页面为 "广东工业大学 新闻通知网"
         * 没错就是多了个空格 所以长度相差 1 , nice
         * 之前用了 String 的 contain() 也可以的 , 实现方式大同小异
         *
         * */
        boolean isNotLogin = pageTemp.getTitleText().toCharArray().length == "广东工业大学新闻通知网".toCharArray().length;


        //若未登录 则登录
        if (isNotLogin) {
            //发现是登录页面 无 cookie 执行登录

            //清空上次的 cookie
            cm.clearCookies();

            HtmlForm form = pageTemp.getForms().get(0);

            //获取表单上的各个元素
            HtmlTextInput user = form.getInputByName("ctl00$ContentPlaceHolder1$userEmail");
            HtmlPasswordInput pwd = form.getInputByName("ctl00$ContentPlaceHolder1$userPassWord");
            //参考 文档 理解清楚什么是 select  什么是 checkboxinput 什么是 option
            //获取 单选框 设置为 true
            HtmlCheckBoxInput checkbox = form.getInputByName("ctl00$ContentPlaceHolder1$CheckBox1");
            //设置为下次自动登录
            checkbox.setDefaultChecked(true);
            //click to request the form
            HtmlSubmitInput button = form.getInputByName("ctl00$ContentPlaceHolder1$Button1");

            //log
            System.out.println("cookies were out of date , relogin!!!");

            //set the input form values
            user.setValueAttribute("gdutnews");
            pwd.setValueAttribute("newsgdut");

            HtmlPage defaultPage = button.click();

            Thread.sleep(WAIT_PAGE_REFRESH);

            //log
            System.out.println("login finish");


            //set cookies with the existing cookies
            //我不知道可不可以这样理解 就是 cookiemanager 是记录管理传输的 cookie 的, 若要使用
            //需要先 set 进去, 这样实现了 cookie 与 request 独立
            webClient.setCookieManager(cm);


        }//至此  登录完毕

        //get the content
        HtmlPage pageTarget = webClient.getPage(urlTarget);
        System.out.println("sucessfully get the target page content! now regex out latest url ");
        //获取 target 网页的内容
        String resultAll = pageTarget.asXml();
        //regex 出最新发布的新闻的地址 , 记得加 gdut 前缀 ,同时去掉 那个 . 通过 substring
        latestUrl = "http://news.gdut.edu.cn" + new RegexTools().doRegex(resultAll, patternStr).get(0).substring(1);

        /*
         * 这是2017 11-10 晚上改的
         * 原因是想着如何减少cpu资源损耗 优化一下
         * 出发点是 如果 得到的这个 latestUrl 和上一次一样  我为何还要访问那个 latest 的页面
         * 另外那个页面会计算点击率的呢 = =.
         * 所以这里加一个判断
         * 优化优化
         *
         * */
        newsInfo = new NewsInfoDAO().get();
        String lastHref = newsInfo.getHref();

        if (!(lastHref.equals(latestUrl))) {
            //if equals skip the following pocedure

            //跳转到地址
            /*
             * 该页面所需要爬出下来的内容为:
             * [标题]
             * [所属部门:教务处] -> department
             * [发布日期:2017/11/1 10:23:59] -> time
             * [阅读次数:1359] -> readCount
             * */
            HtmlPage lastestPage = webClient.getPage(latestUrl);
            //获取最新发布的信息网页内容
            /*整个新闻页面的 sourceCode*/
            String latestResult = lastestPage.asXml();

            //获取完毕 关闭 client
            webClient.close();

            //从页面中爬出四个元素 string 其实只有 get(0) 哈哈
            title = new RegexTools().doRegex(latestResult, titlePattern).get(0);
            department = new RegexTools().doRegex(latestResult, departmentPattern).get(0);
            //
            timeStr = new RegexTools().doRegex(latestResult, timePattern).get(0);
            newstime = new RegexTools().doRegex(timeStr);
            //
            readCount = new RegexTools().doRegex(latestResult, countPattern).get(0);

            //create a NewsInfo object to store these information
            newsInfo.setTitle(title);
            newsInfo.setDepartment(department);
            newsInfo.setHref(latestUrl);
            newsInfo.setTime(newstime);
            newsInfo.setCount(Integer.parseInt(readCount));
            //保存为一个对象中  其实就是最新发布的新闻的关键信息
            return newsInfo;
        }

        //return last info
        return newsInfo;
    }


}
