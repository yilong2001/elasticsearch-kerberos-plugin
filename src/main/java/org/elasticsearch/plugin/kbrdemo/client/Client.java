package org.elasticsearch.plugin.kbrdemo.client;

import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.security.authentication.client.AuthenticatedURL;
import org.apache.hadoop.security.authentication.client.AuthenticationException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivilegedExceptionAction;

import static java.lang.System.exit;

/**
 * Created by yilong on 2017/4/26.
 */
public class Client {
    public static void main(String[] args) {
        String httpUrl = "http://127.0.0.1:9200/megacorp/employee/_search?q=last_name:Liu";
        URL url = null;
        try {
            url = new URL(httpUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            System.out.println(e);
            exit(-1);
        }

        //should kinit firstly
        try {
            final URL lurl = url;

            org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
            conf.set("hadoop.security.authentication", "Kerberos");
            UserGroupInformation.setConfiguration(conf);

            //here, keytab path should be correct!......
            UserGroupInformation.loginUserFromKeytab("mystorm@ZHIEASY.COM", "/wholepath/mystrom.keytab");
            UserGroupInformation ui = UserGroupInformation.getLoginUser();

            System.out.println(ui.getUserName());
            System.out.println(ui.getGroupNames().toString());

            HttpURLConnection connection = ui.doAs(
                    new PrivilegedExceptionAction<HttpURLConnection>() {
                        public HttpURLConnection run() throws Exception {
                            AuthenticatedURL.Token token = new AuthenticatedURL.Token();
                            return new AuthenticatedURL().openConnection(lurl, token);
                        }
                    });

            System.out.println(connection.getResponseCode());
            System.out.println(connection.getResponseMessage());

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
