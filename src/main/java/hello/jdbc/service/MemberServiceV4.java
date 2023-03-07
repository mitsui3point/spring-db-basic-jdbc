package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

/**
 * 예외 누수 문제 해결
 * SQLException 제거
 *
 * MemberRepository interface 에 의존
 */
@RequiredArgsConstructor
public class MemberServiceV4 {
    private final MemberRepository repository;

    @Transactional
    public void accountTransfer(String fromId, String toId, int transferMoney) {
        businessLogic(fromId, toId, transferMoney);//비즈니스 로직
    }

    private void businessLogic(String fromId, String toId, int transferMoney) {
        Member fromMember = repository.findById(fromId);
        Member toMember = repository.findById(toId);
        repository.update(fromId, fromMember.getMoney() - transferMoney);
        validation(toMember);
        repository.update(toId, toMember.getMoney() + transferMoney);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
