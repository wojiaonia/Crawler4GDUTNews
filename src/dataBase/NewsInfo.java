package dataBase;


import java.time.LocalDateTime;

/**
 * 该页面所需要爬出下来的内容为:
 * [地址]
 * [标题]
 * [所属部门:教务处] -> department
 * [发布日期:2017/11/1 10:23:59] -> time
 * [阅读次数:1359] -> readCount
 * */
public class NewsInfo {
    private String href;
    private String title;
    private String department;
    private LocalDateTime newstime;
    private int readCount;
    //java bean
    public void setHref(String href){
        this.href = href;
    }
    public String getHref(){
        return href;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public String getTitle(){
        return title;
    }
    public void setDepartment(String department){
        this.department = department;
    }
    public String getDepartment(){
        return department;
    }
    public void setTime(LocalDateTime time){
        this.newstime = time;
    }
    public LocalDateTime getTime(){
        return newstime;
    }
    public void setCount(int readCount){
        this.readCount = readCount;
    }
    public int getCount(){ return readCount;
    }

    /**
     * 这个只是测试用，以后可以修改 一共有六个变量，现在只输出三个我最关注的
     */
    @Override
    public String toString(){
        return " time: " + newstime + ",title: " + title +",address: " + href + " ,dep: " + department + " ,count: " + readCount + "" + "\n";

    }
}
