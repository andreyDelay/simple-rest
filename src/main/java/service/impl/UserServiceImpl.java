package service.impl;

import dto.impl.UserDto;
import model.User;
import repository.hibernate.impl.UserRepositoryImpl;
import service.Service;
import service.UserService;

import java.util.List;

public class UserServiceImpl implements Service<User>, UserService {

    private final UserRepositoryImpl repository = new UserRepositoryImpl();
    private final UserDto userDto = new UserDto();

    @Override
    public User post(User entity) {
        return repository.save(entity);
    }

    @Override
    public User put(User entity) {
        return repository.update(entity);
    }

    @Override
    public void delete(Long id) {
        repository.delete(id);
    }

    @Override
    public User get(Long id) {
        User user = repository.find(id);
        return userDto.get(user);
    }

    @Override
    public List<User> getAll() {
        List<User> users = repository.findAll();
        return userDto.getAll(users);
    }

    @Override
    public String getJson(List<User> list) {
        return userDto.getJson(list);
    }

    @Override
    public List<User> getUsersWithEvents() {
        List<User> usersWithEvents = repository.getUsersWithEvents();
        return userDto.getUserWithEvents(usersWithEvents);
    }

    @Override
    public List<User> getUsersWithFiles() {
        List<User> usersWithFiles = repository.getUsersWithFiles();
        return userDto.getUserWithFiles(usersWithFiles);
    }

    @Override
    public List<User> getUsersWithConcreteEvent(Long eventId) {
        List<User> usersWithConcreteEvent = repository.getUsersWithConcreteEvent(eventId);
        return userDto.getUserWithEvents(usersWithConcreteEvent);
    }

    @Override
    public List<User> getUsersWithConcreteFile(Long filetId) {
        List<User> usersWithConcreteFile = repository.getUsersWithConcreteFile(filetId);
        return userDto.getUserWithFiles(usersWithConcreteFile);
    }
}
