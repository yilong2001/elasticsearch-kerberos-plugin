package org.elasticsearch.plugin.kbrdemo.authentication;

import org.apache.hadoop.security.authentication.client.KerberosAuthenticator;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.plugin.kbrdemo.validation.RequestValidationContext;
import org.elasticsearch.plugin.kbrdemo.validation.Validator;
import org.elasticsearch.plugin.kbrdemo.validation.ValidatorCheckResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.elasticsearch.plugin.kbrdemo.utils.HttpConstants.WWW_AUTHENTICATE_HEADER;

/**
 * Created by yilong on 2017/4/23.
 */
public class KerberosValidator extends Validator {
    public static final String WWW_AUTHENTICATE = WWW_AUTHENTICATE_HEADER;
    public static final String KEY_TYPE = "kerberos";

    @Override
    public String getKey() {
        return "kerberos";
    }

    @Override
    public CompletableFuture<Boolean> check(RequestValidationContext requestValidationContext, ValidatorCheckResult validatorCheckResult) {
        KerberosAuthenticationHandler kerberosAuthenticationHandler = new KerberosAuthenticationHandler(requestValidationContext);
        try {
            kerberosAuthenticationHandler.init();
        } catch (Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(false);
        }

        boolean r = kerberosAuthenticationHandler.authenticate(validatorCheckResult);
        return CompletableFuture.completedFuture(r);
    }

    @Override
    public boolean onOkResponse(RequestValidationContext rvc, ActionRequest ar, ActionResponse response, ValidatorCheckResult vcr, Map<String, String> headers) {
        //here, should decide that response come from kerveros(@ getKey )
        vcr.getHeaders().forEach((k,v)->headers.put(k,v));
        return true;
    }

    @Override
    public boolean onFailResponse(RequestValidationContext rvc, ActionRequest ar, Map<String, String> headers) {
        headers.put(WWW_AUTHENTICATE, KerberosAuthenticator.NEGOTIATE);
        return true;
    }

    @Override
    public boolean onExceptionFailure(RequestValidationContext rc, ActionRequest ar, Exception e, Map<String, String> headers) {
        headers.put(WWW_AUTHENTICATE, KerberosAuthenticator.NEGOTIATE);
        return true;
    }
}
