/*
   Copyright 2015 codecentric AG

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   Author: Hendrik Saly <hendrik.saly@codecentric.de>
 */
package org.elasticsearch.plugin.kbrdemo.authentication;

import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.plugin.kbrdemo.validation.ValidatorsRepo;

import java.util.Objects;

public class KerberosAuthenticationToken {
    final static org.apache.logging.log4j.Logger logger = Loggers.getLogger(KerberosAuthenticationToken.class);
    private final String outToken;
    private final String principal;
    private final String username;
    private final String kerbType;

    public KerberosAuthenticationToken(final String outToken, final String principal, final String username, final String kbrtype) {
        this.outToken = Objects.requireNonNull(outToken);
        this.principal = Objects.requireNonNull(principal);
        this.username = Objects.requireNonNull(username);
        this.kerbType = kbrtype;
    }

    public void clearCredentials() {
        logger.debug("credentials cleared for {}", toString());
    }

    public String credentials() {
        return outToken;
    }

    public String principal() {
        return principal;
    }

    public String username() {
        return username;
    }

    public String kerbType() {
        return kerbType;
    }

    public String toString() {
        return "KerberosAuthenticationToken [principal=" + principal + ", credentials null?: " + (outToken == null) + "]";
    }

}
