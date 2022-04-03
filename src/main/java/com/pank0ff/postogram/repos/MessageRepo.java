package com.pank0ff.postogram.repos;

import com.pank0ff.postogram.domain.Message;
import com.pank0ff.postogram.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepo extends JpaRepository<Message, Long> {

    List<Message> findAll();

    List<Message> findByTag(String tag);

    List<Message> findByName(String filter);

    Message findById(Integer id);

    List<Message> findByAuthor(User user);

    List<Message> findByHashtag(String hashtag);
}
