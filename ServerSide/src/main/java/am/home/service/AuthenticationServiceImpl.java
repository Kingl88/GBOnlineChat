package am.home.service;

import am.home.service.interfaces.AuthenticationService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AuthenticationServiceImpl implements AuthenticationService {
    private final static Logger LOGGER = LogManager.getLogger(AuthenticationServiceImpl.class.getName());
    private Statement statement;


    public AuthenticationServiceImpl(Statement statement) {
        this.statement = statement;
    }

    @Override
    public void start() {
        LOGGER.info("Authentication service start");
    }

    @Override
    public void stop() {

        LOGGER.info("Authentication service stop");
    }

    @Override
    public String getNickNameByLoginAndPassword(String login, String password) {
        ResultSet resultSet;
        UserEntity userEntity = null;
        try {
            resultSet = statement.executeQuery("SELECT * FROM users WHERE login = " +
                    "'" + login + "' AND password = '" +
                    password + "';");
            userEntity = new UserEntity(resultSet.getString("login"), resultSet.getString("password"), resultSet.getString("nickName"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (userEntity != null) {
            return userEntity.nickName;
        } else {
            return null;
        }
    }

    private class UserEntity {
        private String login;
        private String password;
        private String nickName;

        public UserEntity(String login, String password, String nickName) {
            this.login = login;
            this.password = password;
            this.nickName = nickName;
        }
    }

}
