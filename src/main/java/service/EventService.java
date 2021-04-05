package service;

import model.Event;

import java.util.List;

public interface EventService extends Service<Event> {
    List<Event> getAllEventsByUserId(Long userId);

    Event getConcreteEventByUserId(Long entityId, Long userId);
}
