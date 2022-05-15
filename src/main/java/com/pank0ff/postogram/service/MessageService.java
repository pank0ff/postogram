package com.pank0ff.postogram.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.pank0ff.postogram.domain.Comment;
import com.pank0ff.postogram.domain.Message;
import com.pank0ff.postogram.domain.User;
import com.pank0ff.postogram.exception.ApiRequestException;
import com.pank0ff.postogram.repos.CommentRepo;
import com.pank0ff.postogram.repos.MessageRepo;
import com.pank0ff.postogram.repos.UserRepo;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class MessageService {
    private final CommentRepo commentRepo;
    private final RateService rateService;
    private final MessageRepo messageRepo;
    private final UserRepo userRepo;
    private final UserService userService;
    Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dwnzejl4h",
            "api_key", "424976915584458",
            "api_secret", "TlQsPJt2OHBBSJVzwe31u3zFqgY"));

    @Autowired
    public MessageService(CommentRepo commentRepo, RateService rateService, MessageRepo messageRepo, UserRepo userRepo, UserService userService) {
        this.commentRepo = commentRepo;
        this.userService = userService;
        this.userRepo = userRepo;
        this.rateService = rateService;
        this.messageRepo = messageRepo;
    }

    public static String convertMarkdownToHTML(String markdown) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdown);
        HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();
        return htmlRenderer.render(document);
    }

    public List<Message> sortMessages(int choice, String filter, List<Message> messages) {
        ArrayList<Message> messages2 = new ArrayList<>();
        switch (choice) {
            case 1:
                if (filter != null && !filter.isEmpty()) {
                    for (Message message : messages) {
                        if (message.getName().contains(filter)) {
                            messages2.add(message);
                        } else {
                            if (message.getText().contains(filter)) {
                                messages2.add(message);
                            } else {
                                if (message.getTag().contains(filter)) {
                                    messages2.add(message);
                                } else {
                                    if (message.getHashtag().contains(filter)) {
                                        messages2.add(message);
                                    } else {
                                        for (Comment comment : commentRepo.findByMessageId(message.getId())) {
                                            if (comment.getText().contains(filter)) {
                                                messages2.add(message);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    messages2 = (ArrayList<Message>) messages;
                }
                setMessagesAverageRate(messages2);
                Collections.reverse(messages2);

                break;
            case 2:
                if (filter != null && !filter.isEmpty()) {
                    for (Message message : messages) {
                        if (message.getName().contains(filter)) {
                            messages2.add(message);
                        }
                    }
                } else {
                    messages2 = (ArrayList<Message>) messages;
                }
                setMessagesAverageRate(messages2);
                Collections.reverse(messages2);

                break;
            case 3:
                if (filter != null && !filter.isEmpty()) {
                    for (Message message : messages) {
                        for (Comment comment : commentRepo.findByMessageId(message.getId())) {
                            if (comment.getText().contains(filter)) {
                                messages2.add(message);
                                break;
                            }
                        }
                    }
                }
                setMessagesAverageRate(messages2);
                Collections.reverse(messages2);

                break;
            case 4:
                if (filter != null && !filter.isEmpty()) {
                    for (Message message : messages) {
                        if (message.getTag().contains(filter)) {
                            messages2.add(message);
                        }
                    }
                } else {
                    messages2 = (ArrayList<Message>) messages;
                }
                setMessagesAverageRate(messages2);
                Collections.reverse(messages2);

                break;
            case 5:
                if (filter != null && !filter.isEmpty()) {
                    for (Message message : messages) {
                        if (message.getHashtag().contains(filter)) {
                            messages2.add(message);
                        }
                    }
                } else {
                    messages2 = (ArrayList<Message>) messages;
                }
                setMessagesAverageRate(messages2);
                Collections.reverse(messages2);

                break;
            case 6:
                if (filter != null && !filter.isEmpty()) {
                    for (Message message : messages) {
                        if (message.getText().contains(filter)) {
                            messages2.add(message);
                        }
                    }
                } else {
                    messages2 = (ArrayList<Message>) messages;
                }
                Collections.reverse(messages2);
                setMessagesAverageRate(messages2);

                break;
            default:
                messages2 = (ArrayList<Message>) messages;
                Collections.reverse(messages2);
                setMessagesAverageRate(messages2);
        }
        return messages2;
    }

    public void postUpdate(Integer id, String text, String hashtag, String tag, String name, MultipartFile file) throws IOException {
        Message message = messageRepo.findById(id);
        String messageHashtag = message.getHashtag();
        String messageText = message.getText();
        String messageName = message.getName();
        String messageTopic = message.getTag();
        boolean isTextChange = (text != null && !text.equals(messageText)) ||
                (messageText != null && !messageText.equals(text));
        if (isTextChange) {
            String htmlValue = convertMarkdownToHTML(text);
            message.setText(htmlValue);
        }

        boolean isHashtagChange = (hashtag != null && !hashtag.equals(messageHashtag)) ||
                (messageHashtag != null && !messageHashtag.equals(hashtag));
        if (isHashtagChange) {
            message.setHashtag(hashtag);
        }

        boolean isNameChanged = (name != null && !name.equals(messageName)) ||
                (messageName != null && !messageName.equals(name));
        if (isNameChanged) {
            message.setName(name);
        }
        boolean isTopicChanged = (tag != null && !tag.equals(messageTopic)) ||
                (messageTopic != null && !messageTopic.equals(tag));
        if (isTopicChanged) {
            message.setTag(tag);
        }
        /*updateName(name,message.getId());
        updateText(text,message.getId());
        updateTopic(tag,message.getId());
        updateHashtag(hashtag,message.getId());*/

        if (file != null && !Objects.requireNonNull(file.getOriginalFilename()).isEmpty()) {
            File temp = null;
            try {
                temp = File.createTempFile("myTempFile", ".png");
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert temp != null;
            file.transferTo(temp);
            Map uploadResult = cloudinary.uploader().upload(temp, ObjectUtils.emptyMap());
            String resultFilename = (String) uploadResult.get("url");
            message.setFilename(resultFilename);
        }
        messageRepo.save(message);
    }

    public void loadMessages(List<Message> messages, User user) {
        Collections.reverse(messages);
        if (user != null) {
            setMeLiked(messages, userRepo.findByUsername(user.getUsername()));
        }
        setMessagesAverageRate(messages);
        setMessagesLikesCount(messages);
        List<User> users = userRepo.findAll();
        for (User user1 : users) {
            user1.setCountOfLikes(userService.getUserLikesCount(user1));
            user1.setCountOfPosts(userService.getUserCountOfPosts(user1));
        }
    }

    public void getPost(Message message, User user) {
        message.setAverageRate(rateService.calcAverageRate(message));
        message.setLikesCount(message.getLikes().size());
        if (message.getLikes().contains(userRepo.findByUsername(user.getUsername()))) {
            message.setMeLiked(1);
        }
        message.getAuthor().setCountOfLikes(userService.getUserLikesCount(message.getAuthor()));
        message.getAuthor().setCountOfPosts(userService.getUserCountOfPosts(message.getAuthor()));
    }

    public void loadToCloudinary(MultipartFile file, Message message) throws IOException {
        File temp = null;
        try {
            temp = File.createTempFile("myTempFile", ".png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert temp != null;
        file.transferTo(temp);
        Map uploadResult = cloudinary.uploader().upload(temp, ObjectUtils.emptyMap());
        String resultFilename = (String) uploadResult.get("url");
        message.setFilename(resultFilename);
    }

    public void addMessage(Long id, String text, String name, String hashtag, String tag, MultipartFile file) throws IOException {
        User user = userRepo.findById(id).get();
        Message message = new Message(text, tag, name, hashtag, user);
        if (file != null && !Objects.requireNonNull(file.getOriginalFilename()).isEmpty()) {
            loadToCloudinary(file, message);
        }
        String htmlValue = MessageService.convertMarkdownToHTML(message.getText());
        message.setText(htmlValue);
        messageRepo.save(message);
    }

    public Message getMessageById(Integer id) {
        return messageRepo.findById(id);
    }

    public List<Message> getMessagesByHashtag(String hashtag) {
        return messageRepo.findByHashtag(hashtag);
    }

    public List<Message> getMessagesByTopic(String topic) {
        return messageRepo.findByTag(topic);
    }

    public List<Message> getAllMessages() {
        return messageRepo.findAll();
    }

    public void deleteMessage(Integer id) {
        messageRepo.delete(messageRepo.findById(id));
    }

    public void setMessagesLikesCount(List<Message> messages) {
        for (Message message : messages) {
            message.setLikesCount(message.getLikes().size());
        }
    }

    public List<Message> getMessagesByAuthor(User user) {
        return messageRepo.findByAuthor(user);
    }

    public void setMeLiked(List<Message> messages, User user) {
        for (Message message : messages) {
            if (message.getLikes().contains(user)) {
                message.setMeLiked(1);
            }
        }
    }

    public void setMessagesAverageRate(List<Message> messages) {
        for (Message message : messages) {
            message.setAverageRate(rateService.calcAverageRate(message));
        }
    }

    public List<Message> searchTopMessage() {
        List<Message> messages1 = new ArrayList<>();
        for (Message message : getAllMessages()) {
            if (message.getAverageRate() >= 4) {
                messages1.add(message);
            }
        }
        return messages1;
    }

    public List<Message> sortByDate(List<Message> messages, int sortChoice) {
        if (sortChoice == 1) {
            Collections.reverse(messages);
        }
        return messages;
    }

    public void sortMessagesWithExceptionCheck(String filter, int choice, int sortChoice, Model model, User user) {
        try {
            List<Message> messages2 = sortMessages(choice, filter, getMessagesByAuthor(user));
            loadMessages(messages2, user);
            deleteMessageByLockedAccounts(messages2);
            model.addAttribute("messages", sortByDate(messages2, sortChoice));
            user.setUserRate(userService.calcUserRate(user));
        } catch (Exception e) {
            throw new ApiRequestException("Cant sort messages. Check filter, or else fields");
        }
    }

    public void messageLoader(Model model, User user, List<Message> messages) {
        loadMessages(messages, user);
        userService.calcUserRateForAll();
        deleteMessageByLockedAccounts(messages);
        model.addAttribute("user", user);
        model.addAttribute("theme", Objects.equals(user.getTheme(), "LIGHT"));
        model.addAttribute("isAdmin", user.isAdmin());
        model.addAttribute("lang", Objects.equals(user.getLang(), "ENG"));
        model.addAttribute("messages", messages);
    }

    public void deleteMessageByLockedAccounts(List<Message> messages) {
        for (User user : userRepo.findAll()) {
            if (user.getIsLocked() == 1) {
                messages.removeIf(message -> Objects.equals(message.getAuthor().getUsername(), user.getUsername()));
            }
        }
    }

    @Query("UPDATE Message SET name=(:name) WHERE id =(:id)")
    public void updateName(@Param("name") String name, @Param("id") Integer id) {
    }

    @Query("UPDATE Message SET hashtag=(:name) WHERE id =(:id)")
    public void updateHashtag(@Param("name") String name, @Param("id") Integer id) {
    }

    @Query("UPDATE Message SET text=(:name) WHERE id =(:id)")
    public void updateText(@Param("name") String name, @Param("id") Integer id) {
    }

    @Query("UPDATE Message SET tag=(:name) WHERE id =(:id)")
    public void updateTopic(@Param("name") String name, @Param("id") Integer id) {
    }
}
