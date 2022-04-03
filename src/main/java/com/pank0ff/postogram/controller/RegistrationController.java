package com.pank0ff.postogram.controller;

import com.pank0ff.postogram.domain.Role;
import com.pank0ff.postogram.domain.User;
import com.pank0ff.postogram.repos.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

@Controller
public class RegistrationController {
    @Autowired
    private UserRepo userRepo;

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    public String addUser(@RequestParam String choice, @RequestParam String theme, User user, Map<String, Object> model) {
        User userFromDb = userRepo.findByUsername(user.getUsername());

        if (userFromDb != null) {
            model.put("message", "User exists!");
            return "registration";
        }
        Date date = new Date();
        user.setDateOfRegistration(date.toString().substring(4));
        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        user.setTheme(theme);
        user.setChoice(choice);
        userRepo.save(user);

        return "redirect:/login";
    }
}
