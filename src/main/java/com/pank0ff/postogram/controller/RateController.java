package com.pank0ff.postogram.controller;

import com.pank0ff.postogram.domain.Message;
import com.pank0ff.postogram.domain.Rate;
import com.pank0ff.postogram.domain.User;
import com.pank0ff.postogram.repos.*;
import com.pank0ff.postogram.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Objects;

@Controller
public class RateController {

    private final RateService rateService;

    @Autowired
    private MessageRepo messageRepo;

    @Autowired
    private  RateRepo rateRepo;

    @Autowired
    public RateController(RateService rateService) {
        this.rateService = rateService;
    }


    @PostMapping("/rate/{id}/{username}")
    public String rate(@PathVariable Integer id, @AuthenticationPrincipal User user, @RequestParam int rate){
        Message message = messageRepo.findById(id);
        List<Rate> ratesByUserId = rateRepo.findByUserId(user.getId());
        int counter =0;
        for(Rate rate1 : ratesByUserId){
            if(Objects.equals(rate1.getMessage(), message)){
                counter++;
            }
        }
        if (counter == 0) {
            rateService.createRate(rate, user, message);
        }
            return "redirect:/main";

    }
}
