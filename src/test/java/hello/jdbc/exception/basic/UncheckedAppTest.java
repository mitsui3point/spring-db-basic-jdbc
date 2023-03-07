package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 런타임 예외 - 대부분 복구 불가능한 예외
 * 시스템에서 발생한 예외는 대부분 복구 불가능 예외이다.
 * 런타임 예외를 사용하면 서비스나 컨트롤러가 이런 복구 불가능한 예외를 신경쓰지 않아도 된다.
 * 물론 이렇게 복구 불가능한 예외는 일관성 있게 공통으로 처리해야 한다.
 *
 * 런타임 예외 - 의존 관계에 대한 문제
 * 런타임 예외는 해당 객체가 처리할 수 없는 예외는 무시하면 된다. 따라서 체크 예외 처럼 예외를 강제로
 * 의존하지 않아도 된다.
 */
@Slf4j
public class UncheckedAppTest {

    @Test
    void unchecked() {
        Controller controller = new Controller();
        assertThatThrownBy(() -> controller.request())
                .isInstanceOf(RuntimeSQLException.class);
    }

    @Test
    void printEx() {
        Controller controller = new Controller();
        try {
            controller.request();
        } catch (Exception e) {
            //e.printStackTrace();//System.err 사용, 현업에서 지양해야 함
            log.info("ex", e);
        }
    }

    static class Controller {
        Service service = new Service();

        public void request() {
            service.logic();
        }

    }

    static class Service {
        Repository repository = new Repository();
        NetworkClient networkClient = new NetworkClient();

        /**
         * service 에서 SQLException, ConnectException 의존관계가 사라짐
         * NetworkClient 는 단순히 기존 체크 예외를 RuntimeConnectException 이라는 런타임 예외가
         * 발생하도록 코드를 바꾸었다.
         */
        public void logic() {
            repository.call();
            networkClient.call();
        }
    }

    /**
     * 예외 전환
     * NetworkClient 는 단순히 기존 체크 예외를 RuntimeConnectException 이라는 런타임 예외가 발생하도록 코드를 바꾸었다.
     */

    static class NetworkClient {
        public void call() {
            try {
                connectNetwork();
            } catch (ConnectException e) {
                throw new RuntimeConnectException(e);
            }
        }
        public void connectNetwork() throws ConnectException {
            throw new ConnectException();
        }
    }

    /**
     * 예외 전환
     * 리포지토리에서 체크 예외인 SQLException 이 발생하면 런타임 예외인 RuntimeSQLException 으로 전환해서 예외를 던진다.
     * 참고로 이때 기존 예외를 포함해주어야 예외 출력시 스택 트레이스에서 기존 예외도 함께 확인할 수 있다.
     * 예외 포함에 대한 부분은 조금 뒤에 더 자세히 설명한다.
     */
    static class Repository {
        public void call() {
            try {
                runSQL();
            } catch (SQLException e) {
                //throw new RuntimeSQLException();//전환 이전 exception 미포함시 stackTrace를 추적할 수 없다.
                throw new RuntimeSQLException(e);//e: Caused by: java.sql.SQLException: sql ex
            }
        }
        public void runSQL() throws SQLException {
            throw new SQLException("sql ex");
        }
    }


    static class RuntimeConnectException extends RuntimeException {
        public RuntimeConnectException(Throwable cause) {
            super(cause);
        }
    }

    static class RuntimeSQLException extends RuntimeException {
        public RuntimeSQLException() {
        }

        public RuntimeSQLException(Throwable cause) {
            super(cause);
        }
    }
}
