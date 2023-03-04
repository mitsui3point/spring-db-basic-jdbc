package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

import static hello.jdbc.constants.ConnectionConst.*;

@RequiredArgsConstructor
public class MemberServiceV1 {

    private final MemberRepositoryV1 repository;

    public void accountTransfer(String fromMemberId, String toMemberId, int transferMoney) throws SQLException {

        Member fromMember = repository.findById(fromMemberId);
        Member toMember = repository.findById(toMemberId);
        repository.update(fromMemberId, fromMember.getMoney() - transferMoney);
        validation(toMember);
        repository.update(toMemberId, toMember.getMoney() + transferMoney);
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalArgumentException("이체중 예외 발생");
        }
    }
}
