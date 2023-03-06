package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.constants.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - DataSource, transactionManager 자동 등록
 */
@Slf4j
@SpringBootTest
public class MemberServiceV3_4Test {

    public static final String MEMBER_A = "from";
    public static final String MEMBER_B = "to";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberServiceV3_3 memberService;

    @Autowired
    private MemberRepositoryV3 memberRepository;

    @TestConfiguration
    static class TestConfig {
        private final DataSource dataSource;

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        public MemberRepositoryV3 memberRepository() {
            return new MemberRepositoryV3(dataSource);
        }

        @Bean
        public MemberServiceV3_3 memberService() {
            return new MemberServiceV3_3(memberRepository());
        }
    }

    @Test
    void aopCheck() {
        log.info("memberService.getClass={}", memberService.getClass());
        log.info("memberRepository.getClass={}", memberRepository.getClass());
        Assertions.assertThat(AopUtils.isAopProxy(memberService)).isTrue();
        Assertions.assertThat(AopUtils.isAopProxy(memberRepository)).isFalse();
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
