package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * 트랜잭션 - 트랜잭션 템플릿
 * {@link TransactionOperations#execute(TransactionCallback)}
 * {@link TransactionOperations#executeWithoutResult(Consumer)}
 */
@Slf4j
public class MemberServiceV3_2 {
    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 repository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 repository) {
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.repository = repository;
    }

    public void accountTransfer(String fromId, String toId, int transferMoney) {
        txTemplate.executeWithoutResult((status) -> {
            try {
                businessLogic(fromId, toId, transferMoney);//비즈니스 로직
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });
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
