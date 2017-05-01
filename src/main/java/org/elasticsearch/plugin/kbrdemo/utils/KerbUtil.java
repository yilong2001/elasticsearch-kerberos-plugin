package org.elasticsearch.plugin.kbrdemo.utils;

/**
 * Created by yilong on 2017/4/24.
 */
public class KerbUtil {
    public static final String TYPE = "kerberos";

    public String jaasPath;
    public String kerbType;
    public String keytabPath;
    public String kerbPrincipal;
    public String krb5FilePath;
    public String kerbRoles;
    public String shortName() {
        if (!kerbPrincipal.isEmpty()) {
            String[] tmps = kerbPrincipal.split("/");
            return tmps[0];
        }

        return null;
    }

    public KerbUtil() {}
    public String toString() {
        return "kerbType: "+kerbType+
                "; keytabPath: "+keytabPath+
                "; kerbPrincipal: "+kerbPrincipal+
                "; krb5FilePath: "+krb5FilePath+
                "; kerbRoles: "+kerbRoles;
    }
}
