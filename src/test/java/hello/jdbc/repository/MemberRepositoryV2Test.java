package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import static hello.jdbc.constants.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class MemberRepositoryV2Test {
    private MemberRepositoryV2 repository;

    @Test
    void driverManagerDataSourceTest() throws SQLException {
        repository = new MemberRepositoryV2();
        DataSource dataSource = getDriverManagerDataSourceConnection();
        Connection con = dataSource.getConnection();
        con.setAutoCommit(false);//transaction start
        crud(con);
        con.setAutoCommit(true);//transaction end
    }

    @Test
    void hikariDataSourceTest() throws SQLException, InterruptedException {
        repository = new MemberRepositoryV2();
        DataSource dataSource = getHikariDataSource();
        Connection con = dataSource.getConnection();
        con.setAutoCommit(false);//transaction start
        crud(con);
        con.setAutoCommit(true);//transaction end
    }

    private void crud(Connection con) throws SQLException {
        //create
        Member member = new Member("member", 10000);
        Member savedMember = repository.save(con, member);
        assertThat(savedMember).isEqualTo(member);

        //read
        Member findMember = repository.findById(con, member.getMemberId());

        assertThat(findMember != member).isTrue();
        assertThat(findMember).isEqualTo(member);
        assertThatThrownBy(() -> {
            repository.findById(con, "memberrrr");
        }).isInstanceOf(NoSuchElementException.class);

        //update
        repository.update(con, member.getMemberId(), 20000);
        Member updatedMember = repository.findById(con, member.getMemberId());
        assertThat(updatedMember).extracting("memberId").isEqualTo("member");
        assertThat(updatedMember).extracting("money").isEqualTo(20000);

        //delete
        repository.delete(con, member.getMemberId());
        assertThatThrownBy(() -> {
            repository.findById(con, member.getMemberId());
        }).isInstanceOf(NoSuchElementException.class);
    }

    private DataSource getHikariDataSource() throws SQLException {
        //커넥션 풀링: HikariProxyConnection(Proxy) -> JdbcConnection(Target)
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("testPool");
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        return dataSource;
    }

    private DataSource getDriverManagerDataSourceConnection() throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        return dataSource;
    }
}
