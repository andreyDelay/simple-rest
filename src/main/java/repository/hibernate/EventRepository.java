package repository.hibernate;

import model.Event;

import java.util.List;

public interface EventRepository extends Repository<Event, Long> {

    List<Event> getAllEventsByUserId(Long userId);

    Event getConcreteEventByUserId(Long fileId, Long userId);
}
