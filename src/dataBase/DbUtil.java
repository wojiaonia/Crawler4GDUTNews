package dataBase;

import java.sql.*;

public class DbUtil {
    //预先创建 forCrawl dataBase
    public static final String URL = "jdbc:mysql://localhost:3306/forcrawl";
    public static final String USER = "root";
    public static final String PASSWORD = "Wobu4pjq,";
    //public static final String PASSWORD = "12345678";
    private static Connection conn = null;
    static{
        try {
            //1.加载驱动程序
            Class.forName("com.mysql.jdbc.Driver");
            //2. 获得数据库连接
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //静态封装 connection
    public static Connection getConnection(){
        return conn;
    }
}
//初始化 sql
//insert into forcrawl_newsinfo (href,title,department,newstime,readcount) values ('1','1','1','2017-11-09T16:00:52','1');
//create table forcrawl_newsinfo(id int auto_increment,href varchar(255),title varchar(255),department varchar(255),newstime varchar(255),readcount int ,PRIMARY KEY(id)) default charset utf8;