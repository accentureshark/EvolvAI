package org.shark.evolvai.chathistory.adapter.out;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatHistoryJpaRepository extends JpaRepository<ChatHistoryEntity, Long> {
    List<ChatHistoryEntity> findByConversationIdOrderByTimestampAsc(String conversationId);
}