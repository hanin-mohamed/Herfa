package com.ProjectGraduation.events.repo;

import com.ProjectGraduation.events.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepo extends JpaRepository<Event,Long> {
}
