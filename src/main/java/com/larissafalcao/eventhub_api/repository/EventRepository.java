package com.larissafalcao.eventhub_api.repository;

import com.larissafalcao.eventhub_api.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
