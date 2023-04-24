package hello.jdbc.repository.ex;

/**
 *  키 중복 예외를 직접 만든다.
 *  RuntimeException을 직접 받아도 되지만
 *  extends MyDbException 한 이유는 -> 디비에서 받은 오류다라고 카테고리 묶을수 있다.
 */
public class MyDuplicateKeyException extends MyDbException{

    public MyDuplicateKeyException() {
    }

    public MyDuplicateKeyException(String message) {
        super(message);
    }

    public MyDuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDuplicateKeyException(Throwable cause) {
        super(cause);
    }
}
