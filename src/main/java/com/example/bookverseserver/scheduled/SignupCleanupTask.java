package com.example.bookverseserver.scheduled;

import com.example.bookverseserver.repository.SignupRequestRepository;
import com.example.bookverseserver.entity.User.SignupRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class SignupCleanupTask {
    private static final Logger log = LoggerFactory.getLogger(SignupCleanupTask.class);
    private final SignupRequestRepository signupRequestRepository;

    public SignupCleanupTask(SignupRequestRepository signupRequestRepository) {
        this.signupRequestRepository = signupRequestRepository;
    }

    @Scheduled(fixedDelayString = "${app.signup.cleanup-millis:3600000}")
    @Transactional
    public void cleanupExpired() {
        int deleted = signupRequestRepository.deleteExpired(Instant.now());
        if (deleted > 0) {
            log.info("Deleted {} expired signup requests", deleted);
        }
    }
}
