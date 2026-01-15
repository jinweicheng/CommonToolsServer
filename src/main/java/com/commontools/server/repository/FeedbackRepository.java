package com.commontools.server.repository;

import com.commontools.server.model.Feedback;
import com.commontools.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByUser(User user);
    List<Feedback> findByStatus(Feedback.FeedbackStatus status);
    List<Feedback> findByUserAndStatus(User user, Feedback.FeedbackStatus status);
}
