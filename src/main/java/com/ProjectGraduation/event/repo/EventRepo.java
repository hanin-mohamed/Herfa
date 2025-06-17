package com.ProjectGraduation.event.repo;

import com.ProjectGraduation.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepo extends JpaRepository<Event,Long> {

        @Query("SELECT e FROM Event e JOIN e.interestedUsers u WHERE u.id = :userId")
        List<Event> findEventsByInterestedUserId(@Param("userId") Long userId);


}
