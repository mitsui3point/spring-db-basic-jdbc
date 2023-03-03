package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.constants.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class MemberRepositoryV1Test {
    private MemberRepositoryV1 repository;

    @Test
    void driverManagerDataSourceTest() throws SQLException {
        injectDriverManagerDataSource();
        crud();
    }

    @Test
    void hikariDataSourceTest() throws SQLException, InterruptedException {
        injectHikariDataSource();
        crud();
        Thread.sleep(1000);
    }

    private void injectHikariDataSource() {
        //커넥션 풀링: HikariProxyConnection(Proxy) -> JdbcConnection(Target)
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("testPool");
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        repository = new MemberRepositoryV1(dataSource);
    }

    private void injectDriverManagerDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new MemberRepositoryV1(dataSource);
    }

    private void crud() throws SQLException {
        //create
        Member member = new Member("member", 10000);
        Member savedMember = repository.save(member);
        assertThat(savedMember).isEqualTo(member);

        //read
        Member findMember = repository.findById(member.getMemberId());

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
