package org.elasticsearch.plugin.kbrdemo.validation;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugin.kbrdemo.utils.KerbUtil;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.threadpool.ThreadPool;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by yilong on 2017/4/20.
 */
public class RequestValidationContext {
    final private RestChannel channel;
    final private ActionRequest request;
    final private String action;
    private final Map<String, String> requestHeaders;
    private final ThreadPool threadPool;
    private final KerbUtil kerbUtil;
    private final String urlPath;
    private final Settings settings;

    public <Request extends ActionRequest> RequestValidationContext(RestChannel ch, String act, Request req, RestRequest rreq, ThreadPool tp, Settings set, KerbUtil ku) {
        channel = ch;
        action = act;
        request = req;
        threadPool = tp;
        kerbUtil = ku;
        settings = set;

        final Map<String, String> h = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        rreq.getHeaders().keySet().stream().forEach(k -> {
            if (rreq.getAllHeaderValues(k).isEmpty()) {
                return;
            }
            h.put(k, rreq.getAllHeaderValues(k).iterator().next());
        });

        requestHeaders = h;
        urlPath = rreq.path();
    }

    public boolean check() {
        return true;
    }

    public Map<String, String> getHeaders() {
        return this.requestHeaders;
    }

    public final String getUrlPath() {
        return urlPath;
    }

    public final Settings getSettings() { return settings; }

    public final KerbUtil getKerbUtil() { return kerbUtil; }

    public void addReponseHeader(Map<String, String> headers) {
        headers.forEach((k, v) -> threadPool.getThreadContext().addResponseHeader(k, v));
    }

    @Override
    public String toString() {
        return toString(false);
    }

    private String toString(boolean skipIndices) {
        return "RequestValidationContext: [ kerberos authention : " + " krb5" + " ]";
    }

}
