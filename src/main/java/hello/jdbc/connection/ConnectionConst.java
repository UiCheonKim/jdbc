package hello.jdbc.connection;

public abstract class ConnectionConst {
    // 상수를 모아놔서 객체를 생성해서 수정하면 안되게 하기 위해 abstract class 로 생성
    // abstract class 는 객체를 생성할 수 없다.
    public static final String URL = "jdbc:h2:tcp://localhost/~/test";
    public static final String USERNAME = "sa";
    public static final String PASSWORD = "";
}
