package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

/**
 *  예외 누수 문제 해결
 *  SQLException 제거
 *
 *  MemberRepository 인터페이스에 의존
 *  트랜잭션 - @Transaction AOP
 */
@Slf4j
public class MemberServiceV4 {

    private final MemberRepository memberRepository;

    public MemberServiceV4(MemberRepository  memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    // 이 메서드 호출 될 때 Transactional 걸고 시작하겠다.
    // 이 메서드 호출 이 끝날 때 성공하면 commit, 실패하면(런 타입에러) rollback
    // 클래스에 붙여도 된다.
    public void accountTransfer(String fromId, String toId, int money) {
        bizLogic(fromId, toId, money);
    }

    private void bizLogic(String fromId, String toId, int money) {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생 ");
        }
    }
}
