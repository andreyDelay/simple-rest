package dto.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.Dto;
import model.Account;
import model.Event;
import model.File;
import model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserDto implements Dto<User> {

    @Override
    public User get(User user) {
        return transferUser(user);
    }

    @Override
    public List<User> getAll(List<User> list) {
        return list.stream()
                    .map(this::transferUser)
                    .collect(Collectors.toList());
    }

    public List<User> getUserWithEvents(List<User> users) {
        List<User> transferUsers = new ArrayList<>();
        for (User u : users) {
            List<Event> events = u.getEvents();
            User user = transferUser(u);
            user.setEvents(events);
            transferUsers.add(user);
        }
        return transferUsers;
    }

    public List<User> getUserWithFiles(List<User> users) {
        List<User> transferUsers = new ArrayList<>();
        for (User u : users) {
            List<File> files = u.getFiles();
            User user = transferUser(u);
            user.setFiles(files);
            transferUsers.add(user);
        }
        return transferUsers;
    }

    private User transferUser(User user) {
        if (user == null) {
            return null;
        }
        User transferredUser = new User();
        transferredUser.setId(user.getId());
        transferredUser.setName(user.getName());
        transferredUser.setSurname(user.getSurname());
        transferredUser.setRegistrationDate(user.getRegistrationDate());
        transferredUser.setAge(user.getAge());

        Account account = new Account();
        account.setId(user.getAccount().getId());
        account.setAccountName(user.getAccount().getAccountName());
        account.setStatus(user.getAccount().getStatus());

        transferredUser.setAccount(account);

        return transferredUser;
    }


}
