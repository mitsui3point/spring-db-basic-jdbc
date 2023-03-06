package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.constants.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - 트랜잭션 템플릿
 */
@Slf4j
public class MemberServiceV3_2Test {

    public static final String MEMBER_A = "from";
    public static final String MEMBER_B = "to";
    public static final String MEMBER_EX = "ex";

    private MemberServiceV3_2 memberService;
    private MemberRepositoryV3 memberRepository;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        memberRepository = new MemberRepositoryV3(dataSource);
        memberService = new MemberServiceV3_2(transactionManager, memberRepository);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransferTest() throws SQLException {
        //given
        Member memberA = memberRepository.save(new Member(MEMBER_A, 10000));
        Member memberB = memberRepository.save(new Member(MEMBER_B, 10000));
        int transferMoney = 2000;

        //when
        log.info("SERVICE START");
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), transferMoney);
        log.info("SERVICE END");
        Member actualAMember = memberRepository.findById(memberA.getMemberId());
        Member actualBMember = memberRepository.findById(memberB.getMemberId());

        //then
        assertThat(actualAMember.getMoney()).isEqualTo(8000);
        assertThat(actualBMember.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferFailTest() throws SQLException {
        //given
        Member memberA = memberRepository.save(new Member(MEMBER_A, 10000));
        Member memberEx = memberRepository.save(new Member(MEMBER_EX, 10000));
        int transferMoney = 2000;

        //when
        log.info("SERVICE START");
        assertThatThrownBy(() -> {
            //then
            memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), transferMoney);
        }).isInstanceOf(IllegalStateException.class);
        log.info("SERVICE END");
        Member actualAMember = memberRepository.findById(memberA.getMemberId());
        Member actualExMember = memberRepository.findById(memberEx.getMemberId());

        //then
        assertThat(actualAMember.getMoney()).isEqualTo(10000);
        assertThat(actualExMember.getMoney()).isEqualTo(10000);
    }

    @AfterEach
    void tearDown() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }
}
