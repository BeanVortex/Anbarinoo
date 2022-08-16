package ir.darkdeveloper.anbarinoo.util;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CookieUtilsTest {
    private static MockHttpServletRequest req;
    private static MockHttpServletResponse res;
    private static javax.servlet.http.Cookie cookie;

    @Test
    @Order(1)
    void addCookie() {
        res = new MockHttpServletResponse();
        req = new MockHttpServletRequest();
        CookieUtils.addCookie(res, "cookie-key", "cookie-value", 1600);
        req.setCookies(res.getCookies());
    }

    @Test
    @Order(2)
    void getCookie() {
        var cookieOpt = CookieUtils.getCookie(req, "cookie-key");
        cookieOpt.ifPresent(cookie -> {
            assert cookie.getName().equals("cookie-key");
            assert cookie.getValue().equals("cookie-value");
            CookieUtilsTest.cookie = cookie;
        });
    }

    @Test
    @Order(3)
    void serialize() {
        System.out.println(CookieUtils.serialize(cookie));
    }


    @Test
    @Order(4)
    void deleteCookie() {
        CookieUtils.deleteCookie(req, res, "cookie-key");
    }
}