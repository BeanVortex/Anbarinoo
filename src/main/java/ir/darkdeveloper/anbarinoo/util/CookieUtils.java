package ir.darkdeveloper.anbarinoo.util;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.SerializationUtils;

public class CookieUtils {

    public static Optional<Cookie> getCookie(HttpServletRequest req, String name) {

        var foundCookie = new AtomicReference<>(Optional.<Cookie>empty());
        var cookies = Optional.ofNullable(req.getCookies()).map(List::of);
        cookies.ifPresent(cookies1 -> cookies1.forEach(cookie -> {
            if (cookie.getName().equals(name))
                foundCookie.set(Optional.of(cookie));
        }));

        return foundCookie.get();
    }

    public static void addCookie(HttpServletResponse res, String name, String value, int maxAge) {
        var cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setHttpOnly(true);
        res.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest req, HttpServletResponse res, String name) {
        var cookies = Optional.ofNullable(req.getCookies());
        cookies.map(cs -> cs.length)
                .filter(i -> i != 0)
                .ifPresent(i -> {
                    for (var cookie : cookies.get())
                        if (cookie.getName().equals(name)) {
                            cookie.setValue("");
                            cookie.setPath("/");
                            cookie.setMaxAge(0);
                            res.addCookie(cookie);
                        }
                });
    }

    public static String serialize(Object obj) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(obj));
    }

    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(SerializationUtils.deserialize(Base64.getUrlDecoder().decode(cookie.getValue())));
    }

}
