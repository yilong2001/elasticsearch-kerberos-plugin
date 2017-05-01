package org.elasticsearch.plugin.kbrdemo.validation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by yilong on 2017/4/25.
 */
public class ValidatorCheckResult {
    private boolean result;
    private final ConcurrentMap<String, String> headResults = new ConcurrentHashMap<String, String>();

    public ValidatorCheckResult() {}

    public synchronized void addHeader(String k, String v) {
        headResults.putIfAbsent(k,v);
    }

    public synchronized Map<String, String> getHeaders() {
        Map<String, String> out = new HashMap<>();
        headResults.forEach((k,v)->out.put(k,v));
        return out;
    }

    public synchronized void setResult(boolean r) { result = r; }
    public synchronized boolean getResult() { return result; }
}
