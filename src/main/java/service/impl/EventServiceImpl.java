package service.impl;

import dto.impl.EventDto;
import model.Event;
import repository.hibernate.impl.EventRepositoryImpl;
import service.EventService;

import java.util.List;

public class EventServiceImpl implements EventService {

    private final EventRepositoryImpl repository = new EventRepositoryImpl();
    private final EventDto eventDto = new EventDto();

    @Override
    public Event post(Event entity) {
        return repository.save(entity);
    }

    @Override
    public Event put(Event entity) {
        return repository.update(entity);
    }

    @Override
    public void delete(Long id) {
        repository.delete(id);
    }

    @Override
    public Event get(Long id) {
        Event event = repository.find(id);
        return eventDto.get(event);
    }

    @Override
    public List<Event> getAll() {
        List<Event> events = repository.findAll();
        return eventDto.getAll(events);
    }

    @Override
    public String getJson(List<Event> list) {
        return eventDto.getJson(list);
    }

    @Override
    public List<Event> getAllEventsByUserId(Long userId) {
        List<Event> events = repository.getAllEventsByUserId(userId);
        return eventDto.getAll(events);
    }

    @Override
    public Event getConcreteEventByUserId(Long eventId, Long userId) {
        Event event = repository.getConcreteEventByUserId(eventId, userId);
        return eventDto.get(event);
    }
}
