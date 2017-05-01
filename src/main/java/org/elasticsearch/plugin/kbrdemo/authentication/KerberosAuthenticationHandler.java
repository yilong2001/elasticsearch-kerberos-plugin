package org.elasticsearch.plugin.kbrdemo.authentication;

import org.apache.commons.codec.binary.Base64;

import org.apache.hadoop.security.authentication.client.AuthenticationException;
import org.apache.hadoop.security.authentication.client.KerberosAuthenticator;
import org.apache.hadoop.security.authentication.util.KerberosUtil;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchSecurityException;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.env.Environment;
import org.elasticsearch.plugin.kbrdemo.validation.RequestValidationContext;
import org.elasticsearch.plugin.kbrdemo.validation.ValidatorCheckResult;
import org.ietf.jgss.*;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;

/**
 * Created by yilong on 2017/4/24.
 */
public class KerberosAuthenticationHandler {
    final static org.apache.logging.log4j.Logger logger = Loggers.getLogger(KerberosAuthenticationHandler.class);

    final private RequestValidationContext rc;
    private final Environment env;
    private GSSManager gssManager;
    Subject serverSubject;


    public KerberosAuthenticationHandler(RequestValidationContext rc) {
        this.rc = rc;
        this.env = new Environment(rc.getSettings());
        gssManager = GSSManager.getInstance();
        serverSubject = new Subject();
    }

    public boolean authenticate(ValidatorCheckResult vcr) {
        final String authorizationHeader = rc.getHeaders().get("Authorization");
        try {
            //init();
            final KerberosAuthenticationToken token = authenticate(authorizationHeader);
            vcr.addHeader(org.apache.hadoop.security.authentication.client.KerberosAuthenticator.WWW_AUTHENTICATE,
                    org.apache.hadoop.security.authentication.client.KerberosAuthenticator.NEGOTIATE + " " + token.credentials());
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
            logger.error(e.getMessage());
        }

        return false;
    }

    public KerberosAuthenticationToken authenticate(final String authorizationHeader)
            throws IOException, AuthenticationException {

        if (authorizationHeader == null || rc.getKerbUtil().keytabPath == null || rc.getKerbUtil().kerbPrincipal == null) {
            logger.error("authorizationHeader == null || kerbUtil.keytabPath == null || kerbUtil.kerbPrincipal == null");
            throw new ElasticsearchException("Bad 'Authorization' header or no kerberos config.");
        }

        if (!authorizationHeader.trim().toLowerCase(Locale.ENGLISH).startsWith("negotiate ")) {
            logger.info("token : "+"Bad 'Authorization' header");
            throw new ElasticsearchException("Bad 'Authorization' header");
        }

        //final Path keyTabPath = env.configFile().resolve(rc.getKerbUtil().keytabPath);

        KerberosAuthenticationToken token = null;

        final String authorization = authorizationHeader.substring(KerberosAuthenticator.NEGOTIATE.length()).trim();

        final Base64 base64 = new Base64(0);
        final byte[] clientToken = base64.decode(authorization);


        final String sprincipal = rc.getKerbUtil().kerbPrincipal;
        final Set<String> serverPrincipals = new HashSet<String>();
        serverPrincipals.add(sprincipal);

        try {
            token = Subject.doAsPrivileged(serverSubject,
                    new PrivilegedExceptionAction<KerberosAuthenticationToken>() {
                        @Override
                        public KerberosAuthenticationToken run() throws Exception {
                            if (logger.isTraceEnabled()) {
                                logger.trace("SPNEGO with server principals: {} for {}",
                                        serverPrincipals.toString());
                            }
                            KerberosAuthenticationToken token = null;
                            Exception lastException = null;
                            for (String serverPrincipal : serverPrincipals) {
                                try {
                                    token = runWithPrincipal(serverPrincipal, clientToken, base64);
                                } catch (Exception ex) {
                                    lastException = ex;
                                    logger.trace("Auth {} failed with {}", serverPrincipal, ex);
                                } finally {
                                    if (token != null) {
                                        logger.trace("Auth {} successfully", serverPrincipal);
                                        break;
                                    }
                                }
                            }
                            if (token != null) {
                                return token;
                            } else {
                                throw new AuthenticationException(lastException);
                            }
                        }
                    }, null);
        } catch (PrivilegedActionException ex) {
            ex.printStackTrace();
            if (ex.getException() instanceof IOException) {
                throw (IOException) ex.getException();
            } else {
                throw new AuthenticationException(ex.getException());
            }
        }

        return token;
    }

    private KerberosAuthenticationToken runWithPrincipal(String serverPrincipal, byte[] clientToken, Base64 base64) throws
            IOException, AuthenticationException, ClassNotFoundException,
            GSSException, IllegalAccessException, NoSuchFieldException {
        GSSContext gssContext = null;
        GSSCredential gssCreds = null;
        KerberosAuthenticationToken token = null;

        try {
            logger.trace("SPNEGO initiated with server principal [{}]", serverPrincipal);
            gssCreds = this.gssManager.createCredential(
                    this.gssManager.createName(serverPrincipal,
                            KerberosUtil.getOidInstance("NT_GSS_KRB5_PRINCIPAL")),
                    GSSCredential.INDEFINITE_LIFETIME,
                    new Oid[]{
                            KerberosUtil.getOidInstance("GSS_SPNEGO_MECH_OID"),
                            KerberosUtil.getOidInstance("GSS_KRB5_MECH_OID")},
                    GSSCredential.ACCEPT_ONLY);
            gssContext = this.gssManager.createContext(gssCreds);
            byte[] serverToken = gssContext.acceptSecContext(clientToken, 0,
                    clientToken.length);
            String authenticateToken = null;
            if (serverToken != null && serverToken.length > 0) {
                authenticateToken = base64.encodeToString(serverToken);
            }

            if (!gssContext.isEstablished()) {
                logger.trace("SPNEGO in progress");
            } else {
                String clientPrincipal = gssContext.getSrcName().toString();
                logger.info("------------------------------------------------");
                logger.info(clientPrincipal);
                String userName = rc.getKerbUtil().shortName();
                logger.info(userName);
                logger.info("------------------------------------------------");

                token = new KerberosAuthenticationToken(authenticateToken, userName, clientPrincipal, rc.getKerbUtil().kerbType);
                logger.trace("SPNEGO completed for client principal [{}]", clientPrincipal);
            }
        } finally {
            if (gssContext != null) {
                gssContext.dispose();
            }
            if (gssCreds != null) {
                gssCreds.dispose();
            }
        }
        return token;
    }

    public void init() throws Exception {
        Oid krb5Oid = new Oid( "1.2.840.113554.1.2.2");

        // 1.2 Set Kerberos Properties
        System.setProperty( "sun.security.krb5.debug", "true");
        System.setProperty( "java.security.auth.login.config", rc.getKerbUtil().jaasPath);
        System.setProperty( "javax.security.auth.useSubjectCredsOnly", "true");

        //final String principal = rc.getKerbUtil().kerbPrincipal;
        //final String keytab = rc.getKerbUtil().keytabPath;
        //final KerberosConfiguration kerberosConfiguration = new KerberosConfiguration(keytab, principal);

        // 2. Login to the KDC.
        LoginContext loginCtx = null;
        // "KerberizedServer" refers to a section of the JAAS configuration in the jaas.conf file.
        try {
            CallbackHandler handler = new MyCallbackHandler("HTTP");
            loginCtx = new LoginContext( "HttpServer", new Subject(), handler);

            logger.info("init login context ... ");
            loginCtx.login();
            logger.info("login context login over ... ");
            serverSubject = loginCtx.getSubject();
            logger.info(serverSubject.toString());
            logger.info("the subject is ... ");
        }
        catch (LoginException e) {
            System.err.println("Login failure : " + e);
            throw new ElasticsearchSecurityException(e.getMessage());
        }

        try {
            gssManager = Subject.doAs(serverSubject,
                    new PrivilegedExceptionAction<GSSManager>() {
                        @Override
                        public GSSManager run() throws Exception {
                            return GSSManager.getInstance();
                        }
                    });
        } catch (PrivilegedActionException ex) {
            ex.printStackTrace();
            throw ex.getException();
        }
    }

    private static class KerberosConfiguration extends Configuration {
        private String keytab;
        private String principal;

        public KerberosConfiguration(String keytab, String principal) {
            this.keytab = keytab;
            this.principal = principal;
        }

        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            Map<String, String> options = new HashMap<String, String>();

            options.put("keyTab", keytab);
            options.put("principal", principal);
            options.put("useKeyTab", "true");
            options.put("storeKey", "true");
            options.put("doNotPrompt", "true");
            options.put("useTicketCache", "true");
            options.put("renewTGT", "true");
            options.put("isInitiator", "false");

            options.put("refreshKrb5Config", "true");
            String ticketCache = System.getenv("KRB5CCNAME");
            if (ticketCache != null) {
                options.put("ticketCache", ticketCache);
            }

            if (logger.isDebugEnabled()) {
                options.put("debug", "true");
            }

            return new AppConfigurationEntry[]{
                    new AppConfigurationEntry(KerberosUtil.getKrb5LoginModuleName(),
                            AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                            options), };
        }
    }
}
