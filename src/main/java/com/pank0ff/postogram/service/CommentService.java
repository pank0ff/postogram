package com.pank0ff.postogram.service;

import com.pank0ff.postogram.domain.Comment;
import com.pank0ff.postogram.domain.Message;
import com.pank0ff.postogram.domain.User;
import com.pank0ff.postogram.repos.CommentRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private final CommentRepo commentRepo;

    @Autowired
    public CommentService(CommentRepo commentRepo){
        this.commentRepo = commentRepo;
    }

    public void create(Message message, String text, User user) {
        Comment comment = new Comment();
        comment.setMessage(message);
        comment.setText(text);
        comment.setAuthor(user);
        commentRepo.save(comment);
    }
}
