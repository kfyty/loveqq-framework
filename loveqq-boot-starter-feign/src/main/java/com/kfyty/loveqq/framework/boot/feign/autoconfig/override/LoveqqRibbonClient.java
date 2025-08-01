package com.kfyty.loveqq.framework.boot.feign.autoconfig.override;

import com.kfyty.loveqq.framework.boot.feign.autoconfig.factory.LoadBalancerClientFactory;
import com.netflix.client.ClientException;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.DefaultClientConfigImpl;
import feign.Client;
import feign.Request;
import feign.Response;

import java.io.IOException;
import java.net.URI;

/**
 * 完全复制自 {@link feign.ribbon.RibbonClient}
 * <p>
 * RibbonClient can be used in Feign builder to activate smart routing and resiliency capabilities
 * provided by Ribbon. Ex.
 *
 * <pre>
 * MyService api = Feign.builder.client(RibbonClient.create()).target(MyService.class,
 *     &quot;http://myAppProd&quot;);
 * </pre>
 * <p>
 * Where {@code myAppProd} is the ribbon client name and {@code myAppProd.ribbon.listOfServers}
 * configuration is set.
 */
public class LoveqqRibbonClient implements Client {
    private final Client delegate;
    private final LoadBalancerClientFactory lbClientFactory;

    public LoveqqRibbonClient(Client delegate, LoadBalancerClientFactory lbClientFactory) {
        this.delegate = delegate;
        this.lbClientFactory = lbClientFactory;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        try {
            URI asUri = URI.create(request.url());
            String clientName = asUri.getHost();
            URI uriWithoutHost = cleanUrl(request.url(), clientName);
            LoveqqLBClient.RibbonRequest ribbonRequest = new LoveqqLBClient.RibbonRequest(delegate, request, uriWithoutHost, clientName);
            return lbClient(clientName)
                    .executeWithLoadBalancer(ribbonRequest, new LoveqqRibbonClient.FeignOptionsClientConfig(options))
                    .toResponse();
        } catch (ClientException e) {
            propagateFirstIOException(e);
            throw new RuntimeException(e);
        }
    }

    static void propagateFirstIOException(Throwable throwable) throws IOException {
        while (throwable != null) {
            if (throwable instanceof IOException) {
                throw (IOException) throwable;
            }
            throwable = throwable.getCause();
        }
    }

    static URI cleanUrl(String originalUrl, String host) {
        return URI.create(originalUrl.replaceFirst(host, ""));
    }

    private LoveqqLBClient lbClient(String clientName) {
        return this.lbClientFactory.create(clientName);
    }

    static class FeignOptionsClientConfig extends DefaultClientConfigImpl {

        public FeignOptionsClientConfig(Request.Options options) {
            setProperty(CommonClientConfigKey.ConnectTimeout, options.connectTimeoutMillis());
            setProperty(CommonClientConfigKey.ReadTimeout, options.readTimeoutMillis());
            setProperty(CommonClientConfigKey.FollowRedirects, options.isFollowRedirects());
        }

        @Override
        public void loadProperties(String clientName) {

        }

        @Override
        public void loadDefaultValues() {

        }
    }
}
