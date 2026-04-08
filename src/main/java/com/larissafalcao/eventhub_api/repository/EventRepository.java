package com.larissafalcao.eventhub_api.repository;

import com.larissafalcao.eventhub_api.entity.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT event FROM Event event WHERE event.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
}
