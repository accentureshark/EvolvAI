

package org.shark.evolvai.chathistory.adapter.out;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMemoryJpaRepository extends JpaRepository<ChatMemoryEntity, String> {
}
