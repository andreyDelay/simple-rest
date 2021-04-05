package servlets;

import model.Event;
import model.User;
import service.impl.EventServiceImpl;
import service.impl.UserServiceImpl;
import util.RequestIdentifierName;
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
        if (ServletUtils.isIdSpecified(req, RequestIdentifierName.USER_ID)) {
            getResultForSpecifiedParent(req);
        } else if (req.getAttribute("parent") != null) {
            UserServiceImpl userService = new UserServiceImpl();
            if (ServletUtils.isIdSpecified(req, RequestIdentifierName.EVENT_ID)) {
                Long eventId = ServletUtils.getSpecifiedID(req, RequestIdentifierName.EVENT_ID);
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
        Long userId = ServletUtils.getSpecifiedID(request, RequestIdentifierName.USER_ID);
        if (ServletUtils.isIdSpecified(request, RequestIdentifierName.EVENT_ID)) {
            Long eventId = ServletUtils.getSpecifiedID(request, RequestIdentifierName.EVENT_ID);
            resultList.add(eventServiceImpl.getConcreteEventByUserId(eventId, userId));
        } else {
            resultList.addAll(eventServiceImpl.getAllEventsByUserId(userId));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String eventName = req.getParameter("event_name");
        String userId = req.getParameter(RequestIdentifierName.USER_ID.getKeyName());
        Event createdEvent = null;
        resp.setContentType("text/html");
        String body;
        PrintWriter pw = resp.getWriter();
        if (eventName != null) {
            Event event = new Event();
            event.setName(eventName);
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
        pw.println("<!DOCTYPE html>");
        pw.println("<html>\n" + "<head><title>Persistence report</title></head>" +
                "<body>" + body + "</body>");
        pw.flush();
        pw.close();
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String eventId = req.getParameter(RequestIdentifierName.EVENT_ID.getKeyName());
        String userId = req.getParameter(RequestIdentifierName.USER_ID.getKeyName());
        Event updatedEvent;
        resp.setContentType("text/html");
        String body = "";
        PrintWriter pw = resp.getWriter();

        if (eventId != null && userId != null) {
            Event oldValue = eventServiceImpl.get(Long.parseLong(eventId));
            String newEventName = req.getParameter("event_name");
            if (newEventName != null) {
                oldValue.setName(newEventName);
                oldValue.setEventDate(new Date(System.currentTimeMillis()));
            }
            User user = new UserServiceImpl().get(Long.parseLong(userId));
            oldValue.setUser(user);
            updatedEvent = eventServiceImpl.put(oldValue);
            if (updatedEvent != null) body = "event was successfully updated!";
        } else {
            body = "Error! Event id and user id must be specified!";
        }

        pw.println("<!DOCTYPE html>");
        pw.println("<html>\n" + "<head><title>Updating report</title></head>" +
                "<body>" + body + "</body>");
        pw.flush();
        pw.close();
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        String body;
        PrintWriter pw = resp.getWriter();
        String eventId = req.getParameter(RequestIdentifierName.EVENT_ID.getKeyName());
        Long id;
        if (eventId != null) {
            id = Long.parseLong(eventId);
            try {
                eventServiceImpl.delete(id);
                body = "Event was successfully deleted!";
            } catch (Exception e) {
                body = "Error! Cause: " + e.getMessage();
            }
        } else {
            body = "Error! Event id must be specified!";
        }
        pw.println("<!DOCTYPE html>");
        pw.println("<html>\n" + "<head><title>Deleting report</title></head>" +
                "<body>" + body + "</body>");
        pw.flush();
        pw.close();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        eventServiceImpl = new EventServiceImpl();
        resultList = new ArrayList<>();
        super.init(config);
    }

}
