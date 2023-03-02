package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class MemberRepositoryV0Test {
    private MemberRepositoryV0 repository = new MemberRepositoryV0();

    @Test
    void crud() throws SQLException {
        //create
        Member member = new Member("member", 10000);
        Member savedMember = repository.save(member);
        assertThat(savedMember).isEqualTo(member);

        //read
        Member findMember = repository.findById(member.getMemberId());
        log.info("findMember = {}", findMember);
        log.info("findMember == member : {}", findMember == member);

        assertThat(findMember != member).isTrue();
        assertThat(findMember).isEqualTo(member);
        assertThatThrownBy(() -> {
            repository.findById("memberrrr");
        }).isInstanceOf(NoSuchElementException.class);

        //update
        repository.update(member.getMemberId(), 20000);
        Member updatedMember = repository.findById(member.getMemberId());
        assertThat(updatedMember).extracting("memberId").isEqualTo("member");
        assertThat(updatedMember).extracting("money").isEqualTo(20000);

        //delete
        repository.delete(member.getMemberId());
        assertThatThrownBy(() -> {
            repository.findById(member.getMemberId());
        }).isInstanceOf(NoSuchElementException.class);
    }
}
