package org.elasticsearch.plugin.kbrdemo.utils;

import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;

import java.util.Map;

/**
 * Created by yilong on 2017/4/19.
 */
public class ReponseUtil {
    final static org.apache.logging.log4j.Logger logger = Loggers.getLogger(ReponseUtil.class);

    static public void sendNotAuthResponse(RestChannel channel, RestRequest request, Map<String, String> headers) {
        UnAuthMessage message = new UnAuthMessage();
        try {
            XContentBuilder builder = channel.newBuilder();
            builder.startObject();
            message.toXContent(builder, request);
            builder.endObject();

            BytesRestResponse rsp = new BytesRestResponse(RestStatus.UNAUTHORIZED, builder);
            if (headers!=null)  { headers.forEach((k,v)->rsp.addHeader(k,v)); }

            channel.sendResponse(rsp);
        } catch (Exception e) {
            channel.sendResponse(new BytesRestResponse(RestStatus.UNAUTHORIZED, "authentication failed!"));
            logger.error(e);
        }
    }
}
