package repository.hibernate.impl;

import model.Event;
import model.User;
import org.hibernate.Session;
import repository.hibernate.EventRepository;
import util.SessionUtils;

import javax.persistence.Query;
import java.util.List;
import java.util.stream.Collectors;

public class EventRepositoryImpl implements EventRepository {
    @Override
    public Event save(Event object) {
        Session session = SessionUtils.getSession();
        try {
            session.beginTransaction();
            session.persist(object);
            SessionUtils.commitAndClose(session);
        } catch (Exception e) {
            SessionUtils.commitAndClose(session);
            object = null;
        }
        return object;
    }

    @Override
    public Event update(Event object) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        session.merge(object);
        SessionUtils.commitAndClose(session);
        return object;
    }

    @Override
    public void delete(Long id) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        Event event = session.load(Event.class, id);
        session.delete(event);
        SessionUtils.commitAndClose(session);
    }

    @Override
    public Event find(Long id) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        Event event = session.find(Event.class, id);
        SessionUtils.commitAndClose(session);
        return event;
    }

    @Override
    public List<Event> findAll() {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        List events = session.createQuery("FROM Event ").list();
        SessionUtils.commitAndClose(session);
        return events;
    }

    @Override
    public List<Event> getAllEventsByUserId(Long userId) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        Query query = session.createNativeQuery("SELECT e.event_id, e.event_name, e.event_date, " +
                        "e.user_id, u.name, u.surname " +
                        "FROM events e INNER JOIN users u on e.user_id=? " +
                        "group by e.event_id;",
                "getEventsByUserId");
        query.setParameter(1, userId);
        List<Object[]> resultList = query.getResultList();
        List<Event> files = resultList.stream().map((arr) -> {
            Event e = (Event) arr[0];
            User u = (User) arr[1];
            e.setUser(u);
            return e;
        }).collect(Collectors.toList());
        SessionUtils.commitAndClose(session);
        return files;
    }

    @Override
    public Event getConcreteEventByUserId(Long eventId, Long userId) {
        Session session = SessionUtils.getSession();
        session.beginTransaction();
        Query query = session.createNativeQuery("SELECT e.event_id, e.event_name, e.event_date, " +
                        "e.user_id, u.name, u.surname " +
                        "FROM events e INNER JOIN users u on e.user_id=u.user_id " +
                        "WHERE e.event_id=? and u.user_id=? group by e.event_id;",
                "getEventsByUserId");
        query.setParameter(1, eventId);
        query.setParameter(2, userId);
        List<Object[]> resultList = query.getResultList();
        Event event = resultList.stream().map((arr) -> {
            Event e = (Event) arr[0];
            User u = (User) arr[1];
            e.setUser(u);
            return e;
        }).findFirst().orElse(null);
        SessionUtils.commitAndClose(session);
        return event;
    }
}
