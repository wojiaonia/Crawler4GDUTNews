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


    public void addInfo(NewsInfo info) throws SQLException {
        Connection conn = null;
        PreparedStatement ptmt = null;
        ResultSet rs = null;
        try {
            //获取数据库连接
            conn = new DbUtil().getConnection();
            //sql statement
            String sql = "INSERT INTO forcrawl_newsinfo( href , title , department, newstime , readcount)" +
                    "VALUES(" + "?,?,?,?,?)";

            //预编译
            ptmt = conn.prepareStatement(sql);

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
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) { /* ignored */}
            }
            if (ptmt != null) {
                try {
                    ptmt.close();
                } catch (SQLException e) { /* ignored */}
            }
        }
    }

    /**
     * 更新数据
     */
    public void updateInfo(NewsInfo info) throws SQLException {
        Connection conn = null;
        PreparedStatement ptmt = null;
        ResultSet rs = null;
        try {
            //获取数据库连接
            conn = new DbUtil().getConnection();

            //sql  切记 千万别漏了 空格,newsinfo 后加个空格哦
            String sql = "UPDATE forcrawl_newsinfo " +
                    "SET href=?, title=? , department=? , newstime=? , readcount=?" +
                    " WHERE id =?";

            //预编译
            ptmt = conn.prepareStatement(sql);

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
            ptmt.setInt(6, 1);

            //执行
            ptmt.execute();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) { /* ignored */}
            }
            if (ptmt != null) {
                try {
                    ptmt.close();
                } catch (SQLException e) { /* ignored */}
            }

        }

    }

    /**获取数据*/
    public NewsInfo get() throws SQLException{
        Connection conn = null;
        PreparedStatement ptmt = null;
        ResultSet rs = null;
        NewsInfo info = null;
        try {
            //获取连接
            conn = new DbUtil().getConnection();
            //sql, 每行加空格
            String sql = "select * from forcrawl_newsinfo where id=?";

            //预编译SQL，减少sql执行
            ptmt = conn.prepareStatement(sql);

            //传参 其实还是一行数据
            ptmt.setInt(1, 1);
            //执行
            rs = ptmt.executeQuery();
            while (rs.next()) {
                info = new NewsInfo();
                info.setHref(rs.getString("href"));
                info.setTitle(rs.getString("title"));
                info.setDepartment(rs.getString("department"));

                //就是这么刚  强势 cast 咬我?
                info.setTime(LocalDateTime.parse(rs.getString("newstime")));
                info.setCount(rs.getInt("readcount"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) { /* ignored */}
            }
            if (ptmt != null) {
                try {
                    ptmt.close();
                } catch (SQLException e) { /* ignored */}
            }
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) { /* ignored */}
            }
        }
        return info;
    }


    public void delInfo() throws SQLException {
        Connection conn = null;
        PreparedStatement ptmt = null;
        ResultSet rs = null;
        try {
            //获取连接
            conn = new DbUtil().getConnection();
            //sql, 每行加空格
            String sql = "delete from forcrawl_newsinfo where id=?";
            //预编译SQL，减少sql执行
            ptmt = conn.prepareStatement(sql);

            //传参
            ptmt.setInt(1, 1);

            //执行
            ptmt.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) { /* ignored */}
            }
            if (ptmt != null) {
                try {
                    ptmt.close();
                } catch (SQLException e) { /* ignored */}
            }

        }
    }
}
