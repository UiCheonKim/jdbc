package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 *  트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

//    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager; // 트랜잭션 매니저
    private final MemberRepositoryV3 memberRepositoryV3;

    public void accountTransfer(String fromId, String toId,int money) throws SQLException {

        // 트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        // new DefaultTransactionDefinition() : 트랜잭션 옵션을 설정
        // status 를 커밋, 롤백 할 때 넣어줘야 함

        try{
            // 비즈니스 로직
            // 시작
            bizLogic(fromId, toId, money);
            // 커밋, 롤백
            transactionManager.commit(status); // 성공시 커밋
        }catch (Exception e){
            transactionManager.rollback(status); // 실패시 롤백
            throw new IllegalStateException(e);
        } // finally 는 없어도 됨, 커밋이나 롤백 될 때 트랜잭션 매니저가 알아서 해줌
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepositoryV3.findById(fromId);
        Member toMember = memberRepositoryV3.findById(toId);

        memberRepositoryV3.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepositoryV3.update(toId, toMember.getMoney() + money);
    }

    private void release(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true); // setAutoCommit(false)로 남아있을 수 있으므로 다시 true 로 변경 // 커넥션 풀 고려
                con.close(); // 풀에 반납
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생 ");
        }
    }
}
