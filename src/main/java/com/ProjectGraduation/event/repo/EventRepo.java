package com.ProjectGraduation.event.repo;

import com.ProjectGraduation.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepo extends JpaRepository<Event,Long> {
}
