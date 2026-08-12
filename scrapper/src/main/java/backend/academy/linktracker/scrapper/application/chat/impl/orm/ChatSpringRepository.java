package backend.academy.linktracker.scrapper.application.chat.impl.orm;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatSpringRepository extends CrudRepository<Chat, Long> {}
