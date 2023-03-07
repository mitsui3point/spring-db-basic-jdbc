package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import hello.jdbc.repository.MemberRepositoryV3;
import hello.jdbc.repository.MemberRepositoryV4_1;
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

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 예외 누수 문제 해결
 * SQLException 제거
 *
 * MemberRepository interface 에 의존
 */
@Slf4j
@SpringBootTest
public class MemberServiceV4Test {

    public static final String MEMBER_A = "from";
    public static final String MEMBER_B = "to";
    public static final String MEMBER_EX = "ex";

    @Autowired
    private MemberServiceV4 memberService;

    @Autowired
    private MemberRepository memberRepository;

    @TestConfiguration
    static class TestConfig {
        private final DataSource dataSource;

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Bean
        public MemberRepository memberRepository() {
            return new MemberRepositoryV4_1(dataSource);
        }

        @Bean
        public MemberServiceV4 memberService() {
            return new MemberServiceV4(memberRepository());
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
    void accountTransferTest() {
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
    void accountTransferFailTest() {
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
    void tearDown() {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }
}
