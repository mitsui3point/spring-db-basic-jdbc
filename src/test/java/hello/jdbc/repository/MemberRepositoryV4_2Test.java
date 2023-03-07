package hello.jdbc.repository;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;
import java.util.NoSuchElementException;

import static hello.jdbc.constants.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class MemberRepositoryV4_2Test {
    private MemberRepositoryV4_2 repository;

    @Test
    void driverManagerDataSourceCommitTest() {
        DataSource dataSource = getDriverManagerDataSourceConnection();
        transactionManagerCommit(dataSource);
    }

    @Test
    void driverManagerDataSourceRollbackTest() {
        DataSource dataSource = getDriverManagerDataSourceConnection();
        transactionManagerRollback(dataSource);
    }

    @Test
    void hikariDataSourceCommitTest() {
        DataSource dataSource = getHikariDataSource();
        transactionManagerCommit(dataSource);
    }

    @Test
    void hikariDataSourceRollbackTest() {
        DataSource dataSource = getHikariDataSource();
        transactionManagerRollback(dataSource);
    }

    @Test
    void badSqlGrammarExceptionTest() {
        DataSource dataSource = getDriverManagerDataSourceConnection();
        repository = new MemberRepositoryV4_2(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionAttribute());
        //create
        Member member = new Member("member", 10000);
        assertThatThrownBy(() -> repository.badGrammarSqlExecute(member))
                .isInstanceOf(BadSqlGrammarException.class);
        transactionManager.commit(status);
    }

    private void transactionManagerCommit(DataSource dataSource) {
        repository = new MemberRepositoryV4_2(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionAttribute());
        crud();
        transactionManager.commit(status);
        log.info("=========== COMMIT TEST ===========");
    }

    private void transactionManagerRollback(DataSource dataSource)  {
        repository = new MemberRepositoryV4_2(dataSource);
        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionAttribute());
        crud();
        transactionManager.rollback(status);
        log.info("=========== ROLLBACK TEST ===========");
        status = transactionManager.getTransaction(new DefaultTransactionAttribute());
        assertThatThrownBy(() -> repository.findById("member"))
                .isInstanceOf(NoSuchElementException.class);
        transactionManager.commit(status);
    }

    private void crud() {
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

    private DataSource getHikariDataSource() {
        //커넥션 풀링: HikariProxyConnection(Proxy) -> JdbcConnection(Target)
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName("testPool");
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        return dataSource;
    }

    private DataSource getDriverManagerDataSourceConnection() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        return dataSource;
    }
}
