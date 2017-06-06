# elasticsearch-kerberos-plugin
A kerberos plugin for elasticsearch v5.3.0 
=====================

Kerberos/SPNEGO custom plugin for Elasticsearch v5.3.0
Authenticate HTTP and REST requests via Kerberos/SPNEGO.

###License
* Apache License 2.0 

###Features
* Kerberos/SPNEGO REST/HTTP authentication
* Both support REST/HTTP with Kerberos/SPNEGO and without Kerberos/SPNEGO at the same time
* Based On jaas.conf
* No external dependencies, no dependency for elasticsearch sercurity shield
* Based On elasticsearch.yaml configuration
* Client as a example

###support
* WeChat: yilong2001
* email: yilong2001@126.com

###Prerequisites
* Elasticsearch 5.3.0
* Kerberos Infrastructure (ActiveDirectory, MIT, Heimdal, ...)

###Install release
compile, copy to the plugin path of ElasticSearch 

###Build and install latest
*     $ git clone https://github.com/yilong2001/elasticsearch-kerberos-plugin.git
*     $ mvn clean package

###Configuration
*     export ES_JAVA_OPTS="${ES_JAVA_OPTS} -Djava.security.policy=/whole path/plugin-security.policy"

Configuration is done in elasticsearch.yml
* kbrdemo:
*     kerb.jaas_path: /whole path/jaas.conf
*     kerb.type: kerberos
*     kerb.keytab_path: /http serve keytab path/http.127.0.0.1.keytab
*     kerb.principal: HTTP/127.0.0.1@XXX.COM
*     kerb.file_path: /Library/Preferences/edu.mit.Kerberos or /whole path/krb5.conf
*     kerb.roles: admin

###REST/HTTP authentication with curl

    $ kinit
    $ curl --negotiate -u : "http://localhost:9200/_logininfo?pretty"

###REST/HTTP authentication with client

    $ kinit
    $ java -cp XXX.jar org.elasticsearch.plugin.kbrdemo.client.Client

Or with a browser that supports SPNEGO like Chrome or Firefox

###特性
*     支持kerberos认证
*     支持基于 rest api 的kerberos认证，即可以对部分 api 做kerberos认证；
*     使用jaas.conf文件
*     不依赖ES的扩展包
*     基于ES本身的配置文件，不需要额外增加配置文件
*     提供了client示例，可以执行ES的kerberos认证

如果需要帮助，联系邮箱：yilong2001@126.com, 或微信yilong2001

