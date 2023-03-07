package hello.jdbc.exception.basic;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
public class UncheckedTest {

    @Test
    void unchecked_catch_test() {
        Service service = new Service();
        assertThatNoException().isThrownBy(() -> service.callCatch());
    }

    @Test
    void unchecked_throw_test() {
        Service service = new Service();
        assertThatThrownBy(() -> service.callThrow())
                .isInstanceOf(MyUncheckedException.class);
    }

    /**
     * {@link RuntimeException} 을 상속받은 예외는 언체크 예외가 된다.
     */
    static class MyUncheckedException extends RuntimeException {
        public MyUncheckedException(String message) {
            super(message);
        }
    }

    /**
     * Unchecked 예외는
     * 예외를 잡거나, 던지지 않아도 된다.
     * 예외를 잡지 않으면 자동으로 밖으로 던진다.
     */
    static class Service {
        Repository repository = new Repository();

        /**
         * 필요한 경우 예외를 잡아서 처리하면 된다.
         */
        public void callCatch() {
            try {
                repository.call();
            } catch (MyUncheckedException e) {
                //예외 처리 로직
                log.info("예외 처리, message={}", e.getMessage(), e);
            }
        }

        /**
         * 예외를 잡지 않아도 된다. 자연스럽게 상위로 넘어간다.
         * 체크 예외와 다르게 throws 예외를 선언하지 않아도 된다.
         * (throws MyUncheckedException 이 생략된다.)
         */
        public void callThrow() {
            repository.call();
        }
    }

    static class Repository {
        /**
         * : 장점; 의존관계를 참조하지 않아도 된다.(callThrow 가 MyUncheckedException를 몰라도 된다.)
         * : 단점; 개발자가 exception handling 이 필요한 시점을 실수로 누락할 수 있다.
         */
        public void call() {
            throw new MyUncheckedException("ex");
        }
    }
}
