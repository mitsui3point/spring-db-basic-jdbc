package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.constants.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MemberServiceV1Test {

    public static final String MEMBER_A = "from";
    public static final String MEMBER_B = "to";
    public static final String MEMBER_EX = "ex";
    private MemberServiceV1 memberService;
    private MemberRepositoryV1 memberRepository;

    @BeforeEach
    void setUp() throws SQLException {
        memberRepository = new MemberRepositoryV1(new DriverManagerDataSource(URL, USERNAME, PASSWORD));
        memberService = new MemberServiceV1(memberRepository);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransferTest() throws SQLException {
        //given
        Member memberA = memberRepository.save(new Member(MEMBER_A, 10000));
        Member memberB = memberRepository.save(new Member(MEMBER_B, 10000));
        int transferMoney = 2000;

        //when
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), transferMoney);
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
        assertThatThrownBy(() -> {
            //then
            memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), transferMoney);
        }).isInstanceOf(IllegalArgumentException.class);
        Member actualAMember = memberRepository.findById(memberA.getMemberId());
        Member actualExMember = memberRepository.findById(memberEx.getMemberId());

        //then
        assertThat(actualAMember.getMoney()).isEqualTo(8000);
        assertThat(actualExMember.getMoney()).isEqualTo(10000);
    }

    @AfterEach
    void tearDown() throws SQLException {
        memberRepository.delete(MEMBER_A);
        memberRepository.delete(MEMBER_B);
        memberRepository.delete(MEMBER_EX);
    }
}
