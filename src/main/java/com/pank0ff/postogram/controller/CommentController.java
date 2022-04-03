package com.pank0ff.postogram.controller;

import com.pank0ff.postogram.domain.Message;
import com.pank0ff.postogram.domain.User;
import com.pank0ff.postogram.repos.MessageRepo;
import com.pank0ff.postogram.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class CommentController {
    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }
    @Autowired
    private MessageRepo messageRepo;

    @PostMapping("/post/add/comment/{id}")
    public String create(
            @PathVariable Integer id,
            @RequestParam String text,
            @AuthenticationPrincipal User user
    ){
        Message message = messageRepo.findById(id);
       commentService.create(message,text,user);
       return "redirect:/main";
    }
}
