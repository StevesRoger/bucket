
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WSDLHelper {

    public static void setEndPointAddress(BindingProvider provider, String endPointURL) {
        Assert.notNull(provider, "Stub service provider can not be null");
        if (StringUtils.isEmpty(endPointURL))
            throw new RuntimeException("Override end point url can not be null");
        Map<String, Object> context = provider.getRequestContext();
        context.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPointURL);
    }

    public static void addHandler(BindingProvider provider, Handler... handlers) {
        Assert.notNull(provider, "Stub service provider can not be null");
        List<Handler> list = new ArrayList<>(provider.getBinding().getHandlerChain());
        list.addAll(Arrays.asList(handlers));
        provider.getBinding().setHandlerChain(list);
    }

    public static void setTimeout(BindingProvider provider, int timeout) {
        if (timeout > 0) {
            Map<String, Object> context = provider.getRequestContext();
            context.put("com.sun.xml.internal.ws.connect.timeout", timeout);
            context.put("com.sun.xml.internal.ws.request.timeout", timeout);
            context.put("com.sun.xml.ws.connect.timeout", timeout);
            context.put("com.sun.xml.ws.request.timeout", timeout);
        }
    }

    public static void setBasicAuthCredentials(BindingProvider provider, String username, String password) {
        if (StringUtils.isNotEmpty(username) && StringUtils.isNotEmpty(password)) {
            Map<String, Object> context = provider.getRequestContext();
            context.put(BindingProvider.USERNAME_PROPERTY, username);
            context.put(BindingProvider.PASSWORD_PROPERTY, password);
        }
    }
}
