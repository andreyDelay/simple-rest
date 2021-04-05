package filters;

import filters.urlconstants.URLPatterns;
import util.RequestIdentifierName;
import util.ServletUtils;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AbstractFilter {

    private boolean errorOccurred = false;

    protected boolean isErrorOccurred() {
        return errorOccurred;
    }

    protected String getHttpMethod(ServletRequest servletRequest) {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        return request.getMethod();
    }

    protected void forwardUrlIfMatchPattern(ServletRequest servletRequest,
                                            ServletResponse servletResponse) throws IOException,
                                                                                            ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (isUrlMatchesAnyPattern(request.getRequestURI())) {
            Map<String, Long> parametersMap = getParametersMap(request.getRequestURI(), request);
            setUpRequest(request, parametersMap);
        } else {
            errorOccurred = true;
            request.getRequestDispatcher("/").forward(request, response);
        }
    }

    protected boolean isUrlMatchesAnyPattern(String url) {
        return getUrlPatterns().stream()
                .anyMatch(pattern -> compareUrlWithPattern(pattern, url));
    }

    private List<String> getUrlPatterns() {
        URLPatterns[] values = URLPatterns.values();
        return Arrays.stream(values)
                .map(URLPatterns::getPattern)
                .collect(Collectors.toList());
    }

    private boolean compareUrlWithPattern(String urlPattern, String URL) {
        Pattern pattern = Pattern.compile(urlPattern);
        Matcher matcher = pattern.matcher(URL);
        return matcher.find() && matcher.group(0).equalsIgnoreCase(URL);
    }

    private Map<String, Long> getParametersMap(String url, HttpServletRequest request) {
        Map<String, Long> parameters = (Map<String, Long>) request.getAttribute(ServletUtils.getUrlParametersName());
        if (parameters == null) {
            Set<String> endPointsSet = getEndPointsSet();
            parameters = new HashMap<>();
            for (String endPointName : endPointsSet) {
                Long parameterValue = getParameterValue(endPointName, request);
                if (parameterValue != -1) {
                    parameters.put(endPointName, parameterValue);
                }
            }
        }
        return parameters;
    }

    private Long getParameterValue(String endPointName, HttpServletRequest request) {
        String idValue = request.getParameter(endPointName);
        Long result;
        try {
            result = Long.parseLong(idValue);
        } catch (IllegalArgumentException e) {
            result = -1L;
        }
        return result;
    }

    private void setUpRequest(HttpServletRequest request,
                              Map<String, Long> URLAttributes) {
        if (URLAttributes != null && URLAttributes.size() != 0) {
            request.setAttribute(ServletUtils.getUrlParametersName(), URLAttributes);
        }
    }

    private Set<String> getEndPointsSet() {
        return Arrays.stream(RequestIdentifierName.values())
                .map(RequestIdentifierName::getKeyName)
                .collect(Collectors.toSet());
    }



/*    private Long getEndPointValue(String endPointName, String url) {
        String[] splitUrlArray = url.split("/");
        String parameter = "";
        for (int i = 0; i < splitUrlArray.length; i++) {
            if (splitUrlArray[i].equals(endPointName)) {
                int requiredIndex = i + 1;
                if (requiredIndex < splitUrlArray.length) {
                    parameter = splitUrlArray[i + 1];
                }
            }
        }
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(parameter);
        if (!matcher.find()) {
            parameter = "-1";
        }
        return Long.parseLong(parameter);
    }*/


}
