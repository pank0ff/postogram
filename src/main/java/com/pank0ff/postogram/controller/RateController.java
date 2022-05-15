package com.pank0ff.postogram.controller;

import com.pank0ff.postogram.domain.Message;
import com.pank0ff.postogram.domain.User;
import com.pank0ff.postogram.service.MessageService;
import com.pank0ff.postogram.service.RateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

@Validated
@Controller
public class RateController {

    private final RateService rateService;
    private final MessageService messageService;

    @Autowired
    public RateController(RateService rateService, MessageService messageService) {
        this.messageService = messageService;
        this.rateService = rateService;
    }


    @PostMapping("/rate/{id}")
    public String rate(@PathVariable Integer id, @AuthenticationPrincipal User user, @Valid @RequestParam int rate) {
        Message message = messageService.getMessageById(id);
        int counter = rateService.getRateCounter(user, message);
        if (counter == 0) {
            rateService.createRate(rate, user, message);
        }
        return "redirect:/main";

    }
}
