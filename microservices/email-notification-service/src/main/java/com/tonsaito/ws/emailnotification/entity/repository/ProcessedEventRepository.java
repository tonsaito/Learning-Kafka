package com.tonsaito.ws.emailnotification.entity.repository;

import com.tonsaito.ws.emailnotification.entity.ProcessEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessEventEntity, Long> {

    ProcessEventEntity findByMessageId(String messageId);
}
