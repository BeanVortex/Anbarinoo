package ir.darkdeveloper.anbarinoo.config;

import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.core.DecoratingProxy;
import org.springframework.security.authentication.AuthenticationManager;

public class SecurityHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.proxies().registerJdkProxy(AuthenticationManager.class, SpringProxy.class, Advised.class, DecoratingProxy.class);
    }
}
