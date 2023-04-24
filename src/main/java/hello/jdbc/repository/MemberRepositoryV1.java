package hello.jdbc.repository;

import hello.jdbc.connection.DBConnectionUtil;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 *  JDBC - DataSource 사용, JdbcUtils 사용
 */
@Slf4j
public class MemberRepositoryV1 {

    private final DataSource dataSource;
    // DataSource 를 사용하기 위해 의존관계 주입을 받는다.
    public MemberRepositoryV1(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection con = null;
        // PreparedStatement 는 Statement 와 다르게 파라미터를 바인딩 할 수 있다.
        // Statement 를 상속받아서 만들어졌다.
        // sql injection 을 막을 수 있다.
        PreparedStatement pstmt = null; // 이걸 가지고 db에 쿼리를 날린다.

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);// sql 문을 db 에 전달해서 실행할 수 있는 객체를 만들어준다.
            pstmt.setString(1, member.getMemberId()); // 파라미터 바인딩
            pstmt.setInt(2, member.getMoney()); // 파라미터 바인딩
            pstmt.executeUpdate(); // db 에 쿼리를 날린다. // 숫자가 리턴되는데 몇개의 row 가 영향을 받았는지 리턴해준다.
            return member;
        } catch (SQLException e) {
            log.error("db error = {}", e);
            throw e;
        } finally { // finally 블록은 try 블록에서 예외가 발생하든 안하든 무조건 실행된다. // 항상 호출되는게 보장되어야 할때 쓴다.
            // 안닫으면 외부 리소스를 계속 차지하고 있기 때문에 닫아줘야 한다.
            close(con, pstmt, null);
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null; // Select Query 를 결과를 담고있는 통이라고 생각하면 된다.

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();// select 문은 executeQuery() 를 사용한다.

            if(rs.next()) { // 내부에 커서가 있고 한번은 next() 를 호출해야 실제 데이터를 가져올 수 있다.
                // 처음에는 rs 가 아무것도 가르키지 않는다.
                // next 를 호출해서 데이터가 있어 없어 확인 호출이 필요하다.
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            } else {
                throw new NoSuchElementException("member not found memberId = " + memberId);
            }

        } catch (SQLException e) {
            log.error("db error = {}", e);
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money = ? where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);// sql 문을 db 에 전달해서 실행할 수 있는 객체를 만들어준다.
            pstmt.setInt(1, money); // 파라미터 바인딩
            pstmt.setString(2, memberId); // 파라미터 바인딩
            pstmt.executeUpdate();// db 에 쿼리를 날린다. // 숫자가 리턴되는데 몇개의 row 가 영향을 받았는지 리턴해준다.
        } catch (SQLException e) {
            log.error("db error = {}", e);
            throw e;
        } finally { // finally 블록은 try 블록에서 예외가 발생하든 안하든 무조건 실행된다. // 항상 호출되는게 보장되어야 할때 쓴다.
            // 안닫으면 외부 리소스를 계속 차지하고 있기 때문에 닫아줘야 한다.
            close(con, pstmt, null);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);// sql 문을 db 에 전달해서 실행할 수 있는 객체를 만들어준다.
            pstmt.setString(1, memberId); // 파라미터 바인딩
            int resultSize = pstmt.executeUpdate();// db 에 쿼리를 날린다. // 숫자가 리턴되는데 몇개의 row 가 영향을 받았는지 리턴해준다.
            log.info("resultSize = {}", resultSize);
        } catch (SQLException e) {
            log.error("db error = {}", e);
            throw e;
        } finally { // finally 블록은 try 블록에서 예외가 발생하든 안하든 무조건 실행된다. // 항상 호출되는게 보장되어야 할때 쓴다.
            // 안닫으면 외부 리소스를 계속 차지하고 있기 때문에 닫아줘야 한다.
            close(con, pstmt, null);
        }

    }

    // Statement 는 sql 를 그대로 넣는것
    private void close(Connection con, Statement stmt, ResultSet rs) {

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);
/*
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error = {}", e);
            }
        }

        if (stmt != null) {
            try {
                stmt.close(); // SQLException 이 발생해도 catch 로 잡아주기 때문에 connection 을 닫아줄 수 있다.
            } catch (SQLException e) {
                log.info("error = {}", e);
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error = {}", e);
            }
        }
 */
    }

    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("get connection = {} class = {}", con, con.getClass());
        return con;
    }

}
