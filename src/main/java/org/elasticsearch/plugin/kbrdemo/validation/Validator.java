package org.elasticsearch.plugin.kbrdemo.validation;

import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by yilong on 2017/4/23.
 */
public abstract class Validator {
    public Validator() {
    }

    public abstract String getKey();

    public boolean onOkResponse(RequestValidationContext rvc,
                                ActionRequest ar,
                                ActionResponse response,
                                ValidatorCheckResult vcr,
                                Map<String, String> headers) {
        return true;
    }

    public boolean onFailResponse(RequestValidationContext rvc, ActionRequest ar, Map<String, String> headers) {
        return true;
    }

    public boolean onExceptionFailure(RequestValidationContext rc, ActionRequest ar, Exception e, Map<String, String> headers) {
        return true;
    }

    public abstract CompletableFuture<Boolean> check(RequestValidationContext rvc, ValidatorCheckResult vcr);
}
