package repository.hibernate;

import model.User;

import java.util.List;

public interface UserRepository extends Repository<User, Long> {
    List<User> getUsersWithEvents();

    List<User> getUsersWithFiles();

    List<User> getUsersWithConcreteEvent(Long eventId);

    List<User> getUsersWithConcreteFile(Long filetId);
}
