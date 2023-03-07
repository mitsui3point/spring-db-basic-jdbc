package hello.jdbc.exception.basic;

import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 예외에 의존관계가 생기게 되면,
 * JDBC 의 SQLException(JDBC exception) 을 선언하게 되면
 * -> JPA 의 다른 Exception 으로 일일이 코드를 변경하게 된다.
 * -> 개방 폐쇄 원칙 (Open-Closed Principle, OCP; 확장에 대해 열려있고 수정에 대해서는 닫혀있어야 한다) 위배: 다른 코드로 확장하는데 유연하지 못하고 수정이 불가피하게 일어난다.
 * -> throws Exception 이 OCP 는 해결해주지만,
 *    중요한 체크예외가 발생해서 handling 해야 할 때에도,
 *    체크예외를 모두 밖으로 던져버려서 side effect 가 발생하여 좋지않은 해결방법이다.(안티패턴)
 */
public class CheckedAppTest {

    @Test
    void checked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
                .isInstanceOf(Exception.class);
    }

    static class Controller {
        Service service = new Service();

        public void request() throws SQLException, ConnectException {
            service.logic();
        }

    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        public void logic() throws SQLException, ConnectException {
            repository.call();
            networkClient.call();
        }
    }

    static class NetworkClient {
        public void call() throws ConnectException {
            throw new ConnectException("연결 실패");
        }
    }

    static class Repository {
        public void call() throws SQLException {
            throw new SQLException("sql ex");
        }
    }
}
