package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class MemberRepositoryV1Test {

    MemberRepositoryV1 repositoryV1;

    @BeforeEach
    void beforeEach() {
        // 기본 DriverManager 을 통한 - 항상 새로운 커넥션을 획득
//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        // 커넥션 풀링 - 커넥션을 미리 만들어 놓고 재사용
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        repositoryV1 = new MemberRepositoryV1(dataSource);
    }

    @Test
    void crud() throws SQLException {
        // save
        Member member = new Member("memberV100", 10000);
        repositoryV1.save(member);

        // findById
        Member findMember = repositoryV1.findById(member.getMemberId());
        log.info("findMember = {}", findMember);
        log.info("member == findMember = {}", member == findMember);
        log.info("member equals findMember = {}", member.equals(findMember));
        assertThat(findMember).isEqualTo(member);

        // update: money: 10000 -> 20000
        repositoryV1.update(member.getMemberId(), 20000);
        Member updateMember = repositoryV1.findById(member.getMemberId());
        assertThat(updateMember.getMoney()).isEqualTo(20000);

        // delete
        repositoryV1.delete(member.getMemberId());
        assertThatThrownBy(() -> repositoryV1.findById(member.getMemberId()))
                .isInstanceOf(NoSuchElementException.class);
//        Member deletedMember = repositoryV0.findById(member.getMemberId());

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}