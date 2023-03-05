package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.constants.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - 커넥션 파라미터 전달 방식 동기화
 */
public class MemberServiceV2Test {

    public static final String MEMBER_A = "from";
    public static final String MEMBER_B = "to";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV1 testDataRepository;
    private MemberServiceV2 memberService;
    private MemberRepositoryV2 memberRepository;
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        testDataRepository = new MemberRepositoryV1(dataSource);

        memberRepository = new MemberRepositoryV2();
        memberService = new MemberServiceV2(dataSource, memberRepository);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransferTest() throws SQLException {
        //given
        Member memberA = testDataRepository.save(new Member(MEMBER_A, 10000));
        Member memberB = testDataRepository.save(new Member(MEMBER_B, 10000));
        int transferMoney = 2000;

        //when
        memberService.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), transferMoney);
        Member actualAMember = testDataRepository.findById(memberA.getMemberId());
        Member actualBMember = testDataRepository.findById(memberB.getMemberId());

        //then
        assertThat(actualAMember.getMoney()).isEqualTo(8000);
        assertThat(actualBMember.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferFailTest() throws SQLException {
        //given
        Member memberA = testDataRepository.save(new Member(MEMBER_A, 10000));
        Member memberEx = testDataRepository.save(new Member(MEMBER_EX, 10000));
        int transferMoney = 2000;

        //when
        assertThatThrownBy(() -> {
            //then
            memberService.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), transferMoney);
        }).isInstanceOf(IllegalStateException.class);
        Member actualAMember = testDataRepository.findById(memberA.getMemberId());
        Member actualExMember = testDataRepository.findById(memberEx.getMemberId());

        //then
        assertThat(actualAMember.getMoney()).isEqualTo(10000);
        assertThat(actualExMember.getMoney()).isEqualTo(10000);
    }

    @AfterEach
    void tearDown() throws SQLException {
        testDataRepository.delete(MEMBER_A);
        testDataRepository.delete(MEMBER_B);
        testDataRepository.delete(MEMBER_EX);
    }
}
