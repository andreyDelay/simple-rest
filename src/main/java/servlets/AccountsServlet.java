package servlets;

import model.Account;
import model.User;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import service.impl.AccountServiceImpl;
import util.IdentifierName;
import util.ServletEvents;
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
import java.util.Arrays;
import java.util.List;

@WebServlet(urlPatterns = "/accounts/*")
public class AccountsServlet extends HttpServlet {

    private AccountServiceImpl accountServiceImpl;
    private List resultList;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (ServletUtils.isIdSpecified(req, IdentifierName.ACCOUNT_ID)) {
            resultList.add(accountServiceImpl.get(ServletUtils.getSpecifiedID(req, IdentifierName.ACCOUNT_ID)));
        } else {
            resultList.addAll(accountServiceImpl.getAll());
        }
        ServletUtils.printContentInResponse(resp, resultList, accountServiceImpl);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletEvents event;
        User createdUser = ServletUtils.createAndSaveNewUser(req);
        if (createdUser != null) {
            event = ServletEvents.ACCOUNT_CREATED;
        } else {
            event = ServletEvents.ACCOUNT_USER_CREATION_ERROR;
        }

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/html");
        pw.println(ServletUtils.getResponseContent(event.toString()));
        pw.flush();
        pw.close();

        if (event.equals(ServletEvents.ACCOUNT_CREATED)) {
            postEvent(createdUser.getAccount(), req, event);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletEvents event;
        Account updatedAccount = null;

        String accountId = req.getParameter(IdentifierName.ACCOUNT_ID.getKeyName());
        if (ServletUtils.isIdentifierValid(accountId)) {
            Account account = accountServiceImpl.get(Long.parseLong(accountId));
            updatedAccount = accountServiceImpl.put(updateAccount(account, req));
            event = updatedAccount == null ? ServletEvents.ACCOUNT_UPDATE_ERROR : ServletEvents.ACCOUNT_UPDATED;
        } else {
            event = ServletEvents.ID_ERROR;
        }

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/html");
        pw.println(ServletUtils.getResponseContent(event.toString()));
        pw.flush();
        pw.close();

        if (event.equals(ServletEvents.ACCOUNT_UPDATED)) {
            postEvent(updatedAccount, req, event);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletEvents event;
        String errorText = "";
        String accountId = req.getParameter(IdentifierName.ACCOUNT_ID.getKeyName());
        if (ServletUtils.isIdentifierValid(accountId)) {
            Long id = Long.parseLong(accountId);
            try {
                accountServiceImpl.delete(id);
                event = ServletEvents.ACCOUNT_DELETED;
            } catch (Exception e) {
                event = ServletEvents.ACCOUNT_DELETE_ERROR;
                errorText = e.getMessage();
            }
        } else {
            event = ServletEvents.ID_ERROR;
        }

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/html");
        pw.println(ServletUtils.getResponseContent(event.toString() + errorText));
        pw.flush();
        pw.close();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        accountServiceImpl = new AccountServiceImpl();
        resultList = new ArrayList();
        super.init(config);
    }

    private Account updateAccount(Account account, HttpServletRequest request) {
        String newLogin = request.getParameter("login");
        String newStatus = request.getParameter("status");
        if (newStatus != null) {
            establishNewStatus(newStatus, account);
        }
        if (newLogin != null) {
            account.setAccountName(newLogin);
        }
        return account;
    }

    private void establishNewStatus(String newStatus, Account account) {
        boolean isNewStatusValid =
                Arrays.stream(Account.AccountStatus.values())
                        .map(Account.AccountStatus::getStatusValue)
                        .anyMatch(status -> status.equals(newStatus));
        if (isNewStatusValid) {
            account.setStatus(Account.AccountStatus.valueOf(newStatus));
        }
    }

    private void postEvent(Account account, HttpServletRequest request,
                           ServletEvents event) throws IOException {
        if (account != null) {
            String eventText = event.toString();
            HttpPost post = ServletUtils.createPost(request, eventText, account.getUser().getId());
            if (post != null) {
                CloseableHttpClient httpClient = HttpClients.createDefault();
                httpClient.execute(post);
            }
        }
    }

}
