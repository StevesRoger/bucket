
import com.chipmong.digx.app.common.any.Mixin;
import com.ofss.fc.infra.ws.SoapLogHandler;
import org.springframework.util.Assert;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import java.lang.reflect.Constructor;
import java.net.URL;

public final class WSDLFactory {

    public static final int _20SEC = 20000;

    public static <T> T createProxy(Class<?> serviceClass, Class<T> proxyClass) {
        return createProxy(serviceClass, proxyClass, serviceClass.getAnnotation(WebServiceClient.class).wsdlLocation(), null, null);
    }

    public static <T> T createProxy(Class<?> serviceClass, Class<T> proxyClass, String wsdlUrl, String username, String password) {
        return createProxy(serviceClass, proxyClass, wsdlUrl, _20SEC, username, password);
    }

    public static <T> T createProxy(Class<?> serviceClass, Class<T> proxyClass, String ip, int port) {
        return createProxy(serviceClass, proxyClass, ip, port, _20SEC);
    }

    public static <T> T createProxy(Class<?> serviceClass, Class<T> proxyClass, String ip, int port, int timeout) {
        return createProxy(serviceClass, proxyClass, "http", ip, port, timeout, null, null);
    }

    public static <T> T createProxy(Class<?> serviceClass, Class<T> proxyClass, String protocol, String ip, int port, int timeout, String username, String password) {
        return createProxy(serviceClass, proxyClass, buildLocation(serviceClass, protocol, ip, port), timeout, username, password);
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<?> serviceClass, Class<T> proxyClass, String wsdlUrl, int timeout, String username, String password) {
        try {
            WebServiceClient webServiceClient = serviceClass.getAnnotation(WebServiceClient.class);
            LUGGER.log("Service Name:" + serviceClass.getSimpleName());
            LUGGER.log("Namespace:" + webServiceClient.targetNamespace());
            LUGGER.log("WSDL:" + wsdlUrl);
            LUGGER.log("timeout:" + timeout);
            Constructor<?> serviceConstruct = serviceClass.getConstructor(URL.class, QName.class);
            Service service = (Service) serviceConstruct.newInstance(new Object[]{new URL(wsdlUrl),
                    new QName(webServiceClient.targetNamespace(), serviceClass.getSimpleName())});
            BindingProvider provider = (BindingProvider) service.getPort(service.getPorts().next(), proxyClass);
            WSDLHelper.addHandler(provider, new SoapLogHandler());
            WSDLHelper.setTimeout(provider, timeout);
            WSDLHelper.setBasicAuthCredentials(provider, username, password);
            WSDLHelper.setEndPointAddress(provider, wsdlUrl);
            return (T) provider;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static String buildLocation(Class<?> stubClass, String protocol, String ip, int port) {
        WebServiceClient webServiceClient = stubClass.getAnnotation(WebServiceClient.class);
        Assert.notNull(webServiceClient, "Cannot find annotation WebServiceClient on class " + stubClass.getSimpleName());
        String wsdl = webServiceClient.wsdlLocation();
        wsdl = wsdl.substring(wsdl.lastIndexOf(":"));
        wsdl = wsdl.substring(wsdl.indexOf("/"));
        return protocol + "://" + ip + ":" + port + wsdl;
    }
}


