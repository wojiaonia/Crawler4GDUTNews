package dataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * DAO层 主要负责增删改查
 *
 * */
public class NewsInfoDAO {
    //just for test
    /**
    public static void main(String args[]) {
        NewsInfo info = new NewsInfo();

        try {
            info = new NewsInfoDAO().get();
            System.out.println(info);
            info.setHref("1");
            info.setDepartment("1");
            info.setTime(LocalDateTime.now());
            info.setTitle("1");
            info.setCount(1);
            new NewsInfoDAO().updateInfo(info);
            info = new NewsInfoDAO().get();
            System.out.println(info);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    */
    //增
    public void addInfo(NewsInfo info) throws SQLException {
        //获取数据库连接
        Connection conn = DbUtil.getConnection();
        //sql statement
        String sql = "INSERT INTO forcrawl_newsinfo( href , title , department, newstime , readcount)" +
                "VALUES(" + "?,?,?,?,?)";

        //预编译
        PreparedStatement ptmt = conn.prepareStatement(sql);

        //传参数
        ptmt.setString(1, info.getHref());
        ptmt.setString(2, info.getTitle());
        ptmt.setString(3, info.getDepartment());
        //java8 开始可以采用 setObject 来 set LocalDateTime 等对象
        //然后 java 再转换成 sql 支持的 时间格式
        ptmt.setString(4, info.getTime().toString());
        ptmt.setInt(5, info.getCount());

        //执行
        ptmt.execute();
    }

    //更新数据
    public void updateInfo(NewsInfo info) throws SQLException {
        //获取数据库连接
        Connection conn = DbUtil.getConnection();
        //sql  切记 千万别漏了 空格

        String sql = "UPDATE forcrawl_newsinfo " +  //newsinfo 后加个空格哦
                "SET href=?, title=? , department=? , newstime=? , readcount=?" +
                " WHERE id =?";

        //预编译
        PreparedStatement ptmt = conn.prepareStatement(sql);

        //执行
        //传参数
        ptmt.setString(1, info.getHref());
        ptmt.setString(2, info.getTitle());
        ptmt.setString(3, info.getDepartment());
        //java8 开始可以采用 setObject 来 set LocalDateTime 等对象
        //然后 java 再转换成 sql 支持的 时间格式
        ptmt.setString(4, info.getTime().toString());
        ptmt.setInt(5, info.getCount());
        //其实就一行数据= =.
        ptmt.setInt(6 , 1);

        //执行
        ptmt.execute();

    }

    //获取数据
    public NewsInfo get() throws SQLException{
        NewsInfo info = null;
        //获取连接
        Connection conn = DbUtil.getConnection();
        //sql, 每行加空格
        String sql = "select * from forcrawl_newsinfo where id=?";

        //预编译SQL，减少sql执行
        PreparedStatement ptmt = conn.prepareStatement(sql);

        //传参 其实还是一行数据
        ptmt.setInt(1, 1);
        //执行
        ResultSet rs = ptmt.executeQuery();
        while(rs.next()){
            info = new NewsInfo();
            info.setHref(rs.getString("href"));
            info.setTitle(rs.getString("title"));
            info.setDepartment(rs.getString("department"));
            //就是这么刚  强势 cast 咬我?
            //info.setTime(LocalDateTime.parse(rs.getString("newstime")));
            info.setTime(LocalDateTime.parse(rs.getString("newstime")));
            info.setCount(rs.getInt("readcount"));
        }
        return info;
    }


    public void delInfo() throws SQLException {
        //获取连接
        Connection conn = DbUtil.getConnection();
        //sql, 每行加空格
        String sql = "delete from forcrawl_newsinfo where id=?";
        //预编译SQL，减少sql执行
        PreparedStatement ptmt = conn.prepareStatement(sql);

        //传参
        ptmt.setInt(1, 1);

        //执行
        ptmt.execute();
    }
}
