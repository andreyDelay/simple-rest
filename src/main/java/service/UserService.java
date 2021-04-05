package service;

import model.User;

import java.util.List;

public interface UserService extends Service<User> {
    List<User> getUsersWithEvents();

    List<User> getUsersWithFiles();

    List<User> getUsersWithConcreteEvent(Long eventId);

    List<User> getUsersWithConcreteFile(Long filetId);
}
