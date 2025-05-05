package com.TPM.project_management_service.repository;

import com.TPM.project_management_service.model.CalendarEvent;
import com.TPM.project_management_service.model.CalendarEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByCalendarId(Long calendarId);
    List<CalendarEvent> findByCalendarIdAndEventType(Long calendarId, CalendarEventType eventType);
    List<CalendarEvent> findByCalendarIdAndStartTimeBetween(Long calendarId, LocalDateTime start, LocalDateTime end);
    List<CalendarEvent> findByTaskId(Long taskId);
}
