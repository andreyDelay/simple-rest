package dto.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.Dto;
import model.Account;
import model.Event;
import model.User;
import org.hibernate.LazyInitializationException;

import java.util.List;
import java.util.stream.Collectors;

public class EventDto implements Dto<Event> {

    @Override
    public Event get(Event event) {
        return transferEvent(event);
    }

    @Override
    public List<Event> getAll(List<Event> list) {
        return list.stream()
                    .map(this::transferEvent)
                    .collect(Collectors.toList());

    }

    private Event transferEvent(Event event) {
        if (event == null) {
            return null;
        }
        Event convertedEvent = new Event();
        convertedEvent.setEventDate(event.getEventDate());
        convertedEvent.setId(event.getId());
        convertedEvent.setName(event.getName());

        try {
            User user = new User();
            user.setId(event.getUser().getId());
            user.setName(event.getUser().getName());
            user.setSurname(event.getUser().getSurname());
            convertedEvent.setUser(user);
        } catch (LazyInitializationException e) {
            //TODO
        }
        return convertedEvent;
    }
}
