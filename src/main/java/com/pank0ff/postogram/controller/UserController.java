package com.pank0ff.postogram.controller;

import com.pank0ff.postogram.domain.Message;
import com.pank0ff.postogram.domain.Role;
import com.pank0ff.postogram.domain.User;
import com.pank0ff.postogram.exception.ApiRequestException;
import com.pank0ff.postogram.service.MessageService;
import com.pank0ff.postogram.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Validated
@Controller
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final MessageService messageService;

    @Autowired
    public UserController(UserService userService, MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public String userList(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("theme", Objects.equals(user.getTheme(), "LIGHT"));
        model.addAttribute("lang", Objects.equals(user.getLang(), "ENG"));
        model.addAttribute("users", userService.getAllUsers());
        return "userList";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/{user}")
    public String userEditForm(@PathVariable User user, @AuthenticationPrincipal User user1, Model model) {
        model.addAttribute("theme", Objects.equals(user1.getTheme(), "LIGHT"));
        model.addAttribute("lang", Objects.equals(user1.getLang(), "ENG"));
        model.addAttribute("user", user);
        model.addAttribute("roles", Role.values());

        return "userEdit";
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public String userSave(
            @Valid @RequestParam String username,
            @RequestParam Map<String, String> form,
            @RequestParam("userId") User user
    ) {
        try {
            userService.userSave(username, form, user);
        } catch (Exception e) {
            throw new ApiRequestException("Cant save user. Check fields");
        }
        return "redirect:/user";
    }

    @GetMapping("/profile")
    public String getProfile(Model model, @AuthenticationPrincipal User user1) {
        User user = userService.getUserByUsername(user1.getUsername());
        List<Message> messages = messageService.getMessagesByAuthor(user);
        messageService.setMessagesLikesCount(messages);
        messageService.setMeLiked(messages, user);
        user.setCountOfLikes(userService.getUserLikesCount(user));
        user.setCountOfPosts(userService.getUserCountOfPosts(user));
        user.setUserRate(userService.calcUserRate(user));
        messageService.setMessagesAverageRate(messages);
        Collections.reverse(messages);
        model.addAttribute("countOfSubscribers", user.getSubscribers().size());
        model.addAttribute("countOfSubscriptions", user.getSubscriptions().size());
        model.addAttribute("theme", Objects.equals(user.getTheme(), "LIGHT"));
        model.addAttribute("lang", Objects.equals(user.getLang(), "ENG"));
        model.addAttribute("user", user);
        model.addAttribute("aboutMyself", user.getAboutMyself());
        model.addAttribute("messages", messages);

        return "profile";
    }

    @GetMapping("/profile/{username}/settings")
    public String settings(
            Model model, @Valid @PathVariable String username, @AuthenticationPrincipal User userCurrent
    ) {
        User user = userService.getUserByUsername(username);
        model.addAttribute("id", user.getId());
        model.addAttribute("theme", Objects.equals(userCurrent.getTheme(), "LIGHT"));
        model.addAttribute("lang", Objects.equals(userCurrent.getLang(), "ENG"));
        model.addAttribute("userChoice", user.getLang());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("email", user.getEmail());
        model.addAttribute("aboutMyself", user.getAboutMyself());
        model.addAttribute("linkFacebook", user.getLinkFacebook());
        model.addAttribute("linkGoogle", user.getLinkGoogle());
        model.addAttribute("linkYoutube", user.getLinkYoutube());
        model.addAttribute("linkDribble", user.getLinkDribble());
        model.addAttribute("linkLinkedIn", user.getLinkLinkedIn());
        return "settings";
    }

    @PostMapping("/profile/{id}/settings")
    public String updateProfile(
            @PathVariable Long id,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String aboutMyself,
            @RequestParam String userChoice,
            @RequestParam String theme,
            @RequestParam String linkFacebook,
            @RequestParam String linkGoogle,
            @RequestParam String linkYoutube,
            @RequestParam String linkDribble,
            @RequestParam String linkLinkedIn,
            @RequestParam("file") MultipartFile file

    ) {
        try {
            userService.updateProfile(userService.getUserById(id), password, email, aboutMyself, userChoice, theme, linkFacebook, linkGoogle, linkYoutube, linkDribble, linkLinkedIn, file);
        } catch (Exception e) {
            throw new ApiRequestException("Cant update user. Check fields");
        }
        return "redirect:/user/profile";
    }

    @GetMapping("/profile/{id}/{username}")
    public String userProfile(
            Model model, @Valid @PathVariable String username, @AuthenticationPrincipal User currentUser) {
        User user = userService.getUserByUsername(username);
        List<Message> messages = messageService.getMessagesByAuthor(user);
        messageService.setMeLiked(messages, currentUser);
        messageService.setMessagesLikesCount(messages);
        user.setCountOfLikes(userService.getUserLikesCount(user));
        user.setCountOfPosts(userService.getUserCountOfPosts(user));
        user.setUserRate(userService.calcUserRate(user));
        messageService.setMessagesAverageRate(messages);
        Collections.reverse(messages);
        model.addAttribute("isCurrentUser", Objects.equals(user.getUsername(), currentUser.getUsername()));
        model.addAttribute("subscribersCount", user.getSubscribers().size());
        model.addAttribute("subscriptionsCount", user.getSubscriptions().size());
        model.addAttribute("theme", Objects.equals(currentUser.getTheme(), "LIGHT"));
        model.addAttribute("isSubscriber", userService.isSubscriber(user, currentUser));
        model.addAttribute("lang", Objects.equals(currentUser.getLang(), "ENG"));
        model.addAttribute("admin", currentUser.isAdmin());
        model.addAttribute("user", user);
        model.addAttribute("messages", messages);
        return "userProfile";
    }

    @PostMapping("/profile/{id}/settings/delete")
    public String deleteUser(
            @Valid @PathVariable Long id,
            @AuthenticationPrincipal User user1
    ) {
        userService.deleteUser(userService.getUserById(id));
        if (user1.isAdmin()) {
            return "redirect:/user";
        } else {
            return "redirect:/login";
        }
    }

    @GetMapping("/subscribe/{user}")
    public String subscribe(@PathVariable User user, @AuthenticationPrincipal User currentUser) {
        userService.subscribe(currentUser, user);
        return "redirect:/user/profile/" + user.getId() + '/' + user.getUsername();
    }

    @GetMapping("/unsubscribe/{user}")
    public String unsubscribe(@PathVariable User user, @AuthenticationPrincipal User currentUser) {
        userService.unsubscribe(currentUser, user);
        return "redirect:/user/profile/" + user.getId() + '/' + user.getUsername();
    }

    @GetMapping("/like/{messageId}")
    public String like(@PathVariable Integer messageId, @AuthenticationPrincipal User user) {
        Message message = messageService.getMessageById(messageId);
        userService.like(user, message);
        return "redirect:/post/" + message.getId();
    }

    @GetMapping("/unlike/{messageId}")
    public String unlike(@PathVariable Integer messageId, @AuthenticationPrincipal User user) {
        Message message = messageService.getMessageById(messageId);
        userService.unlike(user, message);
        return "redirect:/post/" + message.getId();
    }

    @GetMapping("/{type}/{user}/list")
    public String userListOf(Model model,
                             @PathVariable User user,
                             @PathVariable String type) {
        model.addAttribute("userChannel", user);
        model.addAttribute("type", type);
        if ("subscriptions".equals(type)) {
            model.addAttribute("users", user.getSubscriptions());
        } else {
            model.addAttribute("users", user.getSubscribers());
        }

        return "subscriptions";
    }

    @GetMapping("/lock/{id}")
    public String lockAccount(@PathVariable Long id) {
        User currentUser = userService.getUserById(id);
        userService.lockAccount(currentUser);
        return "redirect:/user/profile";
    }

    @GetMapping("/unlock/{id}")
    public String unlockAccount(@PathVariable Long id) {
        User currentUser = userService.getUserById(id);
        userService.unlockAccount(currentUser);
        return "redirect:/user/profile";
    }
}
