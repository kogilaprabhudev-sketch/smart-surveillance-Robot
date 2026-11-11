package com.surveillance.robot.config;

import com.surveillance.robot.model.AppUser;
import com.surveillance.robot.model.Robot;
import com.surveillance.robot.model.Role;
import com.surveillance.robot.repository.AppUserRepository;
import com.surveillance.robot.repository.RobotRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the default robot and the first dashboard users when the database is empty. */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AppUserRepository users;
    private final RobotRepository robots;
    private final PasswordEncoder encoder;
    private final RobotProperties props;
    private final String adminUser;
    private final String adminPass;
    private final String operatorUser;
    private final String operatorPass;

    public DataInitializer(AppUserRepository users, RobotRepository robots, PasswordEncoder encoder,
                           RobotProperties props,
                           @Value("${app.admin.username}") String adminUser,
                           @Value("${app.admin.password}") String adminPass,
                           @Value("${app.operator.username}") String operatorUser,
                           @Value("${app.operator.password}") String operatorPass) {
        this.users = users;
        this.robots = robots;
        this.encoder = encoder;
        this.props = props;
        this.adminUser = adminUser;
        this.adminPass = adminPass;
        this.operatorUser = operatorUser;
        this.operatorPass = operatorPass;
    }

    @Override
    public void run(String... args) {
        createUserIfMissing(adminUser, adminPass, Role.ADMIN);
        createUserIfMissing(operatorUser, operatorPass, Role.OPERATOR);

        if (!robots.existsByRobotCode(props.defaultRobotCode())) {
            robots.save(new Robot(props.defaultRobotCode(), "Smart Public Surveillance Robot"));
            log.info("Created default robot {}", props.defaultRobotCode());
        }
        if ("change-this-robot-key".equals(props.apiKey())) {
            log.warn("ROBOT_API_KEY is still the default value - set your own before deploying!");
        }
    }

    private void createUserIfMissing(String username, String rawPassword, Role role) {
        if (!users.existsByUsername(username)) {
            users.save(new AppUser(username, encoder.encode(rawPassword), role));
            log.warn("Created {} user '{}' - change the default password (see README)", role, username);
        }
    }
}
