package webdriver;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import dataBase.NewsInfo;
import dataBase.NewsInfoDAO;
import regex.RegexTools;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class HtmlUnitDriver {
    //params
    private static WebClient webClient;
    private String urlLogin;
    private String urlTarget;
    private String username;
    private String password;
    private String resultAll;
    //最新发布的新闻的访问地址
    private String latestUrl;
    //正则 pattern ,  第一个用于找出最新的地址,  剩余三个用于找出 specific 新闻里的关键信息
    private String patternStr;
    private String titlePattern;
    private String departmentPattern ;
    private String timePattern ;
    private String countPattern;

    //整个新闻页面的 sourceCode
    private String latestResult;
    //四个需要爬出出来的信息
    private String title;
    private String department;
    private String timeStr;
    private LocalDateTime newstime;
    private String readCount;
    //newsinfo object
    NewsInfo newsInfo;




    //留着以后用 现在只爬监考网
    /*
    HtmlUnitDriver(String urlLogin , String urlTarget){

        newsInfo = new NewsInfo();
        this.urlLogin = urlLogin;
        this.urlTarget = urlTarget;
        //default login params
        this.username = "gdutnews";
        this.password = "newsgdut";
        //---
        patternStr = "<div.id=\"ContentPlaceHolder1_ListView1_ItemPlaceHolderContainer\">.+?<p>.+?<a.href=\"(.+?)\"";
        titlePattern = "<center>\\s*<span.style=.*?>\\s*(.+?)\\s*</span>";
        departmentPattern = "\\[所属部门:(.+?)\\]";
        timePattern = "\\[发布日期:(.+?)\\]";
        countPattern = "\\[阅读次数:(\\d+)\\]";
    }
*/
    public HtmlUnitDriver() {

        newsInfo = new NewsInfo();
        this.urlLogin = "http://news.gdut.edu.cn/UserLogin.aspx";
        this.urlTarget = "http://news.gdut.edu.cn/ArticleList.aspx?keyword=%E7%9B%91%E8%80%83&category=5&department=2147483647&start=&end=";
        //default login params
        this.username = "gdutnews";
        this.password = "newsgdut";
        //---
        patternStr = "<div.id=\"ContentPlaceHolder1_ListView1_ItemPlaceHolderContainer\">.+?<p>.+?<a.href=\"(.+?)\"";
        /**
         * 这次使用 \s 也就是 空字符匹配 来去除 title 两头的各种看上去的空格= =.
         * 当然有心思还可以使用  dom4j  或者 jsoup 来解 xml
         *
         * */
        titlePattern = "<center>\\s*<span.style=.*?>\\s*(.+?)\\s*</span>";
        departmentPattern = "\\[所属部门:(.+?)\\]";
        timePattern = "\\[发布日期:(.+?)\\]";
        countPattern = "\\[阅读次数:(\\d+)\\]";
    }


    private  WebClient getClient(){
        //一个 driver
        webClient = new WebClient();
        return webClient;
    }
    /*
    public static  void main(String args[]) throws IOException {
        NewsInfo test = new HtmlUnitDriver().getLatestInfo();

        System.out.println(test);
    }
    */
    public NewsInfo getLatestInfo() throws IOException, InterruptedException, SQLException {
        WebClient webClient = new HtmlUnitDriver().getClient();
        /**
         * 经过测试  关闭 css 和 关闭 javascript 的加载可以减少错误
         * 另外 我访问的网页全都是 静态网页
         * 静态推荐两个都关闭
         * */
        //关闭 css
        webClient.getOptions().setCssEnabled(false);
        //关闭 js 支持
        webClient.getOptions().setJavaScriptEnabled(false);
        //由于如果 response 的 status code 不是 200 如302跳转等  就会抛出failing status code 的错误 这里关闭这个错误的抛出 忽略
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        //读取网页内容

        HtmlPage pageLogin = webClient.getPage(urlTarget);
        /**
         * 这里我用了一个很猥琐的方法 判断是否已取得 cookie
         * 就是判断页面的文本长度 = =.
         * */
        int loginPageLength = pageLogin.asXml().length();
        //5889 为登录页面的 文本长度
        if (loginPageLength == 5889) {
            //发现是登录页面 无 cookie 执行登录

            HtmlForm form = pageLogin.getForms().get(0);

            //获取表单上的各个元素
            HtmlTextInput user = form.getInputByName("ctl00$ContentPlaceHolder1$userEmail");
            HtmlPasswordInput pwd = (HtmlPasswordInput) form.getInputByName("ctl00$ContentPlaceHolder1$userPassWord");
            HtmlSubmitInput button = form.getInputByName("ctl00$ContentPlaceHolder1$Button1");

            System.out.println("cookies were out of date , relogin!!!");
            //set the input form values
            user.setValueAttribute("gdutnews");
            pwd.setValueAttribute("newsgdut");
            HtmlPage pageDefault = button.click();
        }//至此  登录完毕

        //get the content
        HtmlPage pageTarget = webClient.getPage(urlTarget);

        //获取 target 网页的内容
        resultAll = pageTarget.asXml();
        //regex 出最新发布的新闻的地址 , 记得加 gdut 前缀 ,同时去掉 那个 . 通过 substring
        latestUrl = "http://news.gdut.edu.cn" + new RegexTools().doRegex(resultAll, patternStr).get(0).substring(1);

        /**
         * 这是2017 11-10 晚上改的
         * 原因是想着如何减少cpu资源损耗 优化一下
         * 出发点是 如果 得到的这个 latestUrl 和上一次一样  我为何还要访问那个 latest 的页面
         * 另外那个页面会计算点击率的呢 = =.
         * 所以这里加一个判断
         * 优化优化
         * */
        newsInfo = new NewsInfoDAO().get();
        String lastHref = newsInfo.getHref();
        if (lastHref != latestUrl) {//if equals skip the following pocedure

            //跳转到地址
            /**
             * 该页面所需要爬出下来的内容为:
             * [标题]
             * [所属部门:教务处] -> department
             * [发布日期:2017/11/1 10:23:59] -> time
             * [阅读次数:1359] -> readCount
             * */
            HtmlPage lastestPage = webClient.getPage(latestUrl);
            //获取最新发布的信息网页内容
            latestResult = lastestPage.asXml();

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
