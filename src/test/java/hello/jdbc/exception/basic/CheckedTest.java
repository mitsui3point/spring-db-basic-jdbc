package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@Slf4j
public class CheckedTest {

    /**
     * 1. test service.callCatch() repository.call() [예외 발생, 던짐]
     * 2. test service.callCatch() [예외 처리] repository.call()
     * 3. test [정상 흐름] service.callCatch() repository.call()
     */
    @Test
    void checked_catch_test() {
        Service service = new Service();
        assertThatNoException().isThrownBy(() -> service.callCatch());
    }

    /**
     * 1. test service.callThrow() repository.call() [예외 발생, 던짐]
     * 2. test service.callThrow() [예외 던짐] repository.call()
     * 3. test [예외 도착] service.callThrow() repository.call()
     */
    @Test
    void checked_throw_test() {
        Service service = new Service();
        assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyCheckedException.class);
    }

    /**
     * Exception 을 상속받은 예외는 체크 예외가 된다.
     */
    static class MyCheckedException extends Exception {
        public MyCheckedException(String message) {
            super(message);
        }
    }

    /**
     * 체크 예외는
     * 예외를 잡아서 처리하거나,
     * 예외를 밖으로 던지거나 둘 중 하나를 필수로 선택해야 한다.
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 체크 예외를 잡아서 처리하는 코드
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyCheckedException e) {
                //예외 처리 로직
                log.info("예외 처리, message={}", e.getMessage(), e);
            }
        }

        /**
         * 체크 예외를 밖으로 던지는 코드
         * 체크 예외는 예외를 잡지 않고 밖으로 던지려면 throws 예외를 메서드에 필수로 선언해야 한다.
         * @throws MyCheckedException
         */
        public void callThrow() throws MyCheckedException {
            repository.call();
        }
    }

    static class Repository {
        public void call() throws MyCheckedException {
            throw new MyCheckedException("ex");
        }
    }
}
