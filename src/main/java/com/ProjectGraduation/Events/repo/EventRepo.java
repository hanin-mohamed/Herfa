package com.ProjectGraduation.Events.repo;

import com.ProjectGraduation.Events.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepo extends JpaRepository<Event,Long> {
}
