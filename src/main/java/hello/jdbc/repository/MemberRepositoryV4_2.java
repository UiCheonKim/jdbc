package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 *  예외 누수 문제 해결
 *  체크 예외를 런타임 예외로 변경
 *  MemberRepository 인터페이스 사용
 *  throws SQLException 제거
 *  JDBC - 트랜잭션 매니저
 *  DataSourceUtils.getConnection() - Connection 을 가져온다.
 *  DataSourceUtils.releaseConnection() - Connection 을 반환한다.
 *  SQLExceptionTranslator 추가
 */
@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository{

    private final DataSource dataSource;
    // DataSource 를 사용하기 위해 의존관계 주입을 받는다.
    private final SQLExceptionTranslator exTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    @Override
    public Member save(Member member) {
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
            throw exTranslator.translate("save", sql, e);
//            throw new MyDbException(e);
//            log.error("db error = {}", e);
//            throw e;
        } finally { // finally 블록은 try 블록에서 예외가 발생하든 안하든 무조건 실행된다. // 항상 호출되는게 보장되어야 할때 쓴다.
            // 안닫으면 외부 리소스를 계속 차지하고 있기 때문에 닫아줘야 한다.
            close(con, pstmt, null);
        }
    }

    @Override
    public Member findById(String memberId) {
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
            throw exTranslator.translate("findById", sql, e);
//            throw new MyDbException(e);
//            log.error("db error = {}", e);
//            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money = ? where member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);// sql 문을 db 에 전달해서 실행할 수 있는 객체를 만들어준다.
            pstmt.setInt(1, money); // 파라미터 바인딩
            pstmt.setString(2, memberId); // 파라미터 바인딩
            int resultSize = pstmt.executeUpdate();// db 에 쿼리를 날린다. // 숫자가 리턴되는데 몇개의 row 가 영향을 받았는지 리턴해준다.
            log.info("resultSize = {}", resultSize);
        } catch (SQLException e) {
            throw exTranslator.translate("update", sql, e);
//            throw new MyDbException(e);
//            log.error("db error = {}", e);
//            throw e;
        } finally { // finally 블록은 try 블록에서 예외가 발생하든 안하든 무조건 실행된다. // 항상 호출되는게 보장되어야 할때 쓴다.
            // 안닫으면 외부 리소스를 계속 차지하고 있기 때문에 닫아줘야 한다.
            close(con, pstmt, null);
        }
    }

    @Override
    public void delete(String memberId) {
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
            throw exTranslator.translate("delete", sql, e);
//            throw new MyDbException(e);
//            log.error("db error = {}", e);
//            throw e;
        } finally { // finally 블록은 try 블록에서 예외가 발생하든 안하든 무조건 실행된다. // 항상 호출되는게 보장되어야 할때 쓴다.
            // 안닫으면 외부 리소스를 계속 차지하고 있기 때문에 닫아줘야 한다.
            close(con, pstmt, null);
        }

    }

    // Statement 는 sql 를 그대로 넣는것
    private void close(Connection con, Statement stmt, ResultSet rs) {

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils 를 사용해야 한다.
        DataSourceUtils.releaseConnection(con, dataSource);
//        JdbcUtils.closeConnection(con);
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
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils 를 사용해야 한다.
        Connection con = DataSourceUtils.getConnection(dataSource);
        // 코드만 직접 dataSource 에서 꺼내는 게 아니라 DataSourceUtils 에서 꺼내는 것
        log.info("get connection = {} class = {}", con, con.getClass());
        return con;
    }

}
