package ir.darkdeveloper.anbarinoo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Component
public class TestUtils {


    public HttpServletRequest setUpHeaderAndGetReqWithRes(HttpServletResponse res) {

        var headers = new HashMap<String, String>();
        headers.put(null, "HTTP/1.1 200 OK");
        headers.put("Content-Type", "text/html");

        headers.put("refresh_token", res.getHeader("refresh_token"));
        headers.put("access_token", res.getHeader("access_token"));

        var request = mock(HttpServletRequest.class);
        for (var key : headers.keySet())
            when(request.getHeader(key)).thenReturn(headers.get(key));

        return request;
    }

    public String getRefreshToken(HttpServletRequest req) {
        return req.getHeader("refresh_token");
    }

    public String getAccessToken(HttpServletRequest req) {
        return req.getHeader("refresh_token");
    }

    public <T> T getHeader(HttpServletRequest req, String headerName, Class<T> clazz) {
        return clazz.cast(req.getHeader(headerName));
    }

    public static String mapToJson(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }

    public HttpHeaders getAuthHeaders(HttpServletResponse response) {
        var req = setUpHeaderAndGetReqWithRes(response);
        var headers = new HttpHeaders();
        headers.add("refresh_token", getRefreshToken(req));
        headers.add("access_token", getAccessToken(req));
        return headers;
    }

}
