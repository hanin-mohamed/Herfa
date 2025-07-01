package com.ProjectGraduation.notification.repo;

import com.ProjectGraduation.notification.entity.NotificationMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepo extends JpaRepository<NotificationMessage,Long> {
}
