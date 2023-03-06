package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 repository;

    public void accountTransfer(String fromId, String toId, int transferMoney) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionAttribute());
        try {
            businessLogic(fromId, toId, transferMoney);//비즈니스 로직
            transactionManager.commit(status);//성공시 커밋
        } catch (Exception e) {
            transactionManager.rollback(status);//실패시 롤백
            throw new IllegalStateException(e);
        }
    }

    private void businessLogic(String fromId, String toId, int transferMoney) throws SQLException {
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
