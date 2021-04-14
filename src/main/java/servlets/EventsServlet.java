package servlets;

import model.Event;
import model.User;
import service.impl.EventServiceImpl;
import service.impl.UserServiceImpl;
import util.IdentifierName;
import util.ServletUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@WebServlet(urlPatterns = "/events/*")
public class EventsServlet extends HttpServlet {

    private EventServiceImpl eventServiceImpl;
    private List resultList;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resultList = new ArrayList();
        if (ServletUtils.isIdSpecified(req, IdentifierName.USER_ID)) {
            getResultForSpecifiedParent(req);
        } else if (req.getAttribute("parent") != null) {
            UserServiceImpl userService = new UserServiceImpl();
            if (ServletUtils.isIdSpecified(req, IdentifierName.EVENT_ID)) {
                Long eventId = ServletUtils.getSpecifiedID(req, IdentifierName.EVENT_ID);
                resultList.addAll(userService.getUsersWithConcreteEvent(eventId));
            } else {
                resultList.addAll(userService.getUsersWithEvents());
            }
            ServletUtils.printContentInResponse(resp, resultList, userService);
            return;
        } else {
            resultList.addAll(eventServiceImpl.getAll());
        }
        ServletUtils.printContentInResponse(resp, resultList, eventServiceImpl);
    }

    private void getResultForSpecifiedParent(HttpServletRequest request) {
        Long userId = ServletUtils.getSpecifiedID(request, IdentifierName.USER_ID);
        if (ServletUtils.isIdSpecified(request, IdentifierName.EVENT_ID)) {
            Long eventId = ServletUtils.getSpecifiedID(request, IdentifierName.EVENT_ID);
            resultList.add(eventServiceImpl.getConcreteEventByUserId(eventId, userId));
        } else {
            resultList.addAll(eventServiceImpl.getAllEventsByUserId(userId));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String eventText = req.getHeader("event");
        String userId = req.getParameter(IdentifierName.USER_ID.getKeyName());
        Event createdEvent = null;
        String body;

        if (eventText != null && ServletUtils.isIdentifierValid(userId)) {
            Event event = new Event();
            event.setName(eventText);
            event.setEventDate(new Date(System.currentTimeMillis()));
            User user = new User();
            user.setId(Long.parseLong(userId));
            event.setUser(user);
            createdEvent = eventServiceImpl.post(event);
        }

        if (createdEvent != null) {
            body = "Event saved successfully!";
        } else {
            body = "Error during saving the event!";
        }

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/html");
        pw.println(ServletUtils.getResponseContent(body));
        pw.flush();
        pw.close();
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String eventId = req.getParameter(IdentifierName.EVENT_ID.getKeyName());
        String userId = req.getParameter(IdentifierName.USER_ID.getKeyName());
        Event updatedEvent;
        String body;

        if (ServletUtils.isChildAndUserIdValid(eventId, userId)) {
            Event oldValue = eventServiceImpl.get(Long.parseLong(eventId));
            updatedEvent = updateEvent(req, oldValue, userId);
            if (updatedEvent != null) {
                body = "event was successfully updated!";
            } else {
                body = "Error, event's parameters didn't change!";
            }
        } else {
            body = "Error! Event id and user id must be specified!";
        }

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/html");
        pw.println(ServletUtils.getResponseContent(body));
        pw.flush();
        pw.close();
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String body;
        String eventId = req.getParameter(IdentifierName.EVENT_ID.getKeyName());
        if (ServletUtils.isIdentifierValid(eventId)) {
            try {
                eventServiceImpl.delete(Long.parseLong(eventId));
                body = "Event was successfully deleted!";
            } catch (Exception e) {
                body = "Error! Cause: " + e.getMessage();
            }
        } else {
            body = "Error! Event id must be specified!";
        }

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/html");
        pw.println(ServletUtils.getResponseContent(body));
        pw.flush();
        pw.close();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        eventServiceImpl = new EventServiceImpl();
        super.init(config);
    }

    private Event updateEvent(HttpServletRequest request, Event event, String userId) {
        if (event != null) {
            String newEventName = request.getParameter("event_name");
            if (newEventName != null) {
                event.setName(newEventName);
                event.setEventDate(new Date(System.currentTimeMillis()));
            }
            User user = new UserServiceImpl().get(Long.parseLong(userId));
            event.setUser(user);
            return eventServiceImpl.put(event);
        }
        return null;
    }

}
