package util;

import model.Account;
import model.User;
import org.apache.http.client.methods.HttpPost;
import service.Service;
import service.impl.UserServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class ServletUtils {

    private static final String urlParametersName = "identifiers";

    public static String getUrlParametersName() {
        return urlParametersName;
    }

    public static boolean isIdSpecified(HttpServletRequest request, IdentifierName endPoint) {
        return getMapWithIdentifiers(request).containsKey(endPoint.getKeyName());
    }

    public static Long getSpecifiedID(HttpServletRequest request, IdentifierName endPoint) {
        return getMapWithIdentifiers(request).get(endPoint.getKeyName());

    }

    private static Map<String, Long> getMapWithIdentifiers(HttpServletRequest request) {
        Map<String, Long> attributes = (Map<String, Long>) request.getAttribute(urlParametersName);
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        return attributes;
    }

    public static String getSubRequestUrl(HttpServletRequest request, IdentifierName endPoint) {
        String url;
        if (isIdSpecified(request, endPoint)) {
            Long specifiedID = getSpecifiedID(request, endPoint);
            url = request.getPathInfo().replaceAll("/" + specifiedID,"");
        } else {
            url = request.getPathInfo();
        }
        return url;
    }

    public static boolean isUrlMatchToAnySubRequest(String url) {
        return url != null && (url.contains("files") || url.contains("events"));
    }

    public static void printContentInResponse(HttpServletResponse response,
                                              List<?> list,
                                              Service service) throws IOException {
        String content;
        PrintWriter pw = response.getWriter();
        content = service.getJson(list);
        if (content.equals("[null]")) {
            content = "<h6>No any data was found for requested parameters!</h6>";
            response.setContentType("text/html");
            pw.println("<!DOCTYPE html>");
            pw.println("<html>\n" + "<head><title>Operation result cannot be present as JSON!</title></head>" +
                    "<body>" + content + "</body>");
        } else {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            pw.print(content);
        }
        response.setCharacterEncoding("UTF-8");
        pw.flush();
        pw.close();
    }

    public static boolean isParametersCorrect(List<String> parameters) {
        for (String param : parameters) {
            if (param == null) return false;
        }
        return true;
    }


    public static User createAndSaveNewUser(HttpServletRequest request) {
        User createdUser = null;

        String login = request.getParameter("login");
        String userName = request.getParameter("username");
        String userSurName = request.getParameter("surname");
        String age = request.getParameter("age");
        List<String> parameters = new ArrayList<>();
        parameters.add(login);
        parameters.add(userName);
        parameters.add(userSurName);
        parameters.add(age);

        if (ServletUtils.isParametersCorrect(parameters)) {
            UserServiceImpl userService = new UserServiceImpl();
            User user = new User();
            user.setName(userName);
            user.setSurname(userSurName);
            user.setAge(Integer.parseInt(age));
            user.setRegistrationDate(new Date(System.currentTimeMillis()));

            Account account = new Account();
            account.setAccountName(login);
            account.setStatus(Account.AccountStatus.ACTIVE);
            account.setUser(user);

            user.setAccount(account);
            createdUser = userService.post(user);
        }

        return createdUser;
    }

    public static HttpPost createPost(HttpServletRequest request, String event, Long userId) {
        HttpPost post = null;
        try {
            String server ="http://" + request.getServerName();
            int port = request.getServerPort();
            post = new HttpPost(new URI(server + ":" + port + "/events?user_id=" + userId));
            post.setHeader("event", event);
        } catch (URISyntaxException e) {
            //TODO write to a log that event should be posted, but something went wrong
        }
        return post;
    }

    public static String getResponseContent(String body) {
        StringBuilder builder = new StringBuilder("<!DOCTYPE html>");
        builder.append("<html>\n" +
                        "<head>" +
                            "<title>Operation report</title>" +
                        "</head>" +
                "<body>" + body + "</body>");
        return builder.toString();
    }

    public static boolean isIdentifierValid(String userId) {
        return isChildAndUserIdValid("1", userId);
    }

    public static boolean isChildAndUserIdValid(String childId, String userId) {
        boolean result = (childId != null && userId != null);
        if (result) {
            try {
                Long.parseLong(childId);
                Long.parseLong(userId);
            } catch (IllegalArgumentException e) {
                result = false;
                //TODO how to inform in response that id(s) not correct
            }
        }
        return result;
    }

}
