package com.kfyty.loveqq.framework.boot.feign.autoconfig.override;

import com.netflix.client.AbstractLoadBalancerAwareClient;
import com.netflix.client.ClientException;
import com.netflix.client.ClientRequest;
import com.netflix.client.IResponse;
import com.netflix.client.RequestSpecificRetryHandler;
import com.netflix.client.config.CommonClientConfigKey;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.reactive.LoadBalancerCommand;
import feign.Client;
import feign.Request;
import feign.Request.HttpMethod;
import feign.Response;
import feign.Util;
import feign.ribbon.LBClientFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 完全复制自 {@link feign.ribbon.LBClient}
 * 去除了 final 修饰
 */
public class LoveqqLBClient extends AbstractLoadBalancerAwareClient<LoveqqLBClient.RibbonRequest, LoveqqLBClient.RibbonResponse> {
    private final int connectTimeout;
    private final int readTimeout;
    private final IClientConfig clientConfig;
    private final Set<Integer> retryableStatusCodes;
    private final Boolean followRedirects;

    public static LoveqqLBClient create(ILoadBalancer lb, IClientConfig clientConfig) {
        return new LoveqqLBClient(lb, clientConfig);
    }

    static Set<Integer> parseStatusCodes(String statusCodesString) {
        if (statusCodesString == null || statusCodesString.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Integer> codes = new LinkedHashSet<>();
        for (String codeString : statusCodesString.split(",")) {
            codes.add(Integer.parseInt(codeString));
        }
        return codes;
    }

    LoveqqLBClient(ILoadBalancer lb, IClientConfig clientConfig) {
        super(lb, clientConfig);
        this.clientConfig = clientConfig;
        connectTimeout = clientConfig.get(CommonClientConfigKey.ConnectTimeout);
        readTimeout = clientConfig.get(CommonClientConfigKey.ReadTimeout);
        retryableStatusCodes = parseStatusCodes(clientConfig.get(LBClientFactory.RetryableStatusCodes));
        followRedirects = clientConfig.get(CommonClientConfigKey.FollowRedirects);
    }

    @Override
    public LoveqqLBClient.RibbonResponse execute(LoveqqLBClient.RibbonRequest request, IClientConfig configOverride) throws IOException, ClientException {
        Request.Options options;
        if (configOverride != null) {
            options = new Request.Options(
                    configOverride.get(CommonClientConfigKey.ConnectTimeout, connectTimeout),
                    TimeUnit.MILLISECONDS,
                    configOverride.get(CommonClientConfigKey.ReadTimeout, readTimeout),
                    TimeUnit.MILLISECONDS,
                    configOverride.get(CommonClientConfigKey.FollowRedirects, followRedirects)
            );
        } else {
            options = new Request.Options(connectTimeout, TimeUnit.MILLISECONDS, readTimeout, TimeUnit.MILLISECONDS, true);
        }
        Response response = request.client().execute(request.toRequest(), options);
        if (retryableStatusCodes.contains(response.status())) {
            response.close();
            throw new ClientException(ClientException.ErrorType.SERVER_THROTTLED);
        }
        return new LoveqqLBClient.RibbonResponse(request.getUri(), response);
    }

    @Override
    public RequestSpecificRetryHandler getRequestSpecificRetryHandler(LoveqqLBClient.RibbonRequest request, IClientConfig requestConfig) {
        if (clientConfig.get(CommonClientConfigKey.OkToRetryOnAllOperations, false)) {
            return new RequestSpecificRetryHandler(true, true, this.getRetryHandler(), requestConfig);
        }
        if (request.toRequest().httpMethod() != HttpMethod.GET) {
            return new RequestSpecificRetryHandler(true, false, this.getRetryHandler(), requestConfig);
        } else {
            return new RequestSpecificRetryHandler(true, true, this.getRetryHandler(), requestConfig);
        }
    }

    @Override
    protected void customizeLoadBalancerCommandBuilder(RibbonRequest request, IClientConfig config, LoadBalancerCommand.Builder<RibbonResponse> builder) {
        builder.withServerLocator(request.getLoadBalancerKey());
    }

    public static class RibbonRequest extends ClientRequest implements Cloneable {
        private final Request request;
        private final Client client;

        RibbonRequest(Client client, Request request, URI uri, Object loadBalancerKey) {
            this.client = client;
            this.request = request;
            setUri(uri);
            setLoadBalancerKey(loadBalancerKey);
        }

        @SuppressWarnings("deprecation")
        Request toRequest() {
            // add header "Content-Length" according to the request body
            final byte[] body = request.body();
            final int bodyLength = body != null ? body.length : 0;
            // create a new Map to avoid side effect, not to change the old headers
            Map<String, Collection<String>> headers = new LinkedHashMap<>();
            headers.putAll(request.headers());
            headers.put(Util.CONTENT_LENGTH, Collections.singletonList(String.valueOf(bodyLength)));
            return Request.create(request.httpMethod(), getUri().toASCIIString(), headers, body, request.charset());
        }

        Client client() {
            return client;
        }

        public LoveqqLBClient.RibbonRequest clone() {
            return new LoveqqLBClient.RibbonRequest(client, request, getUri(), getLoadBalancerKey());
        }
    }

    public static class RibbonResponse implements IResponse {
        private final URI uri;
        private final Response response;

        RibbonResponse(URI uri, Response response) {
            this.uri = uri;
            this.response = response;
        }

        @Override
        public Object getPayload() throws ClientException {
            return response.body();
        }

        @Override
        public boolean hasPayload() {
            return response.body() != null;
        }

        @Override
        public boolean isSuccess() {
            return response.status() == 200;
        }

        @Override
        public URI getRequestedURI() {
            return uri;
        }

        @Override
        public Map<String, Collection<String>> getHeaders() {
            return response.headers();
        }

        Response toResponse() {
            return response;
        }

        @Override
        public void close() throws IOException {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }
    }
}
