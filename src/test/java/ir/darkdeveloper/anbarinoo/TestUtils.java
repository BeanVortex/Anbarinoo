package ir.darkdeveloper.anbarinoo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.darkdeveloper.anbarinoo.util.JwtUtils;
import ir.darkdeveloper.anbarinoo.util.UserUtils.UserAuthUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Component
public class TestUtils {

    private final JwtUtils jwtUtils;

    @Autowired
    public TestUtils(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    //should return the object; data is being removed
    public HttpServletRequest setUpHeaderAndGetReq(String email, Long userId) {

        var headers = new HashMap<String, String>();
        headers.put(null, "HTTP/1.1 200 OK");
        headers.put("Content-Type", "text/html");

        var refresh = jwtUtils.generateRefreshToken(email, userId);
        var access = jwtUtils.generateAccessToken(email);
        var refreshDate = UserAuthUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(refresh));
        var accessDate = UserAuthUtils.TOKEN_EXPIRATION_FORMAT.format(jwtUtils.getExpirationDate(access));
        headers.put("refresh_token", refresh);
        headers.put("access_token", access);
        headers.put("refresh_expiration", refreshDate);
        headers.put("access_expiration", accessDate);

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

    public HttpHeaders getAuthHeaders(String email, Long userId) {
        var req = setUpHeaderAndGetReq(email, userId);
        var headers = new HttpHeaders();
        headers.add("refresh_token", getRefreshToken(req));
        headers.add("access_token", getAccessToken(req));
        return headers;
    }

}
