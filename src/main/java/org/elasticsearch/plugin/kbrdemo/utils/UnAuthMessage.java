package org.elasticsearch.plugin.kbrdemo.utils;

import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;

/**
 * Created by yilong on 2017/4/22.
 */
public class UnAuthMessage implements ToXContent {
    public String action;
    public UnAuthMessage() {}

    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        return builder.field("WWW-Authenticate", "Basic");
    }
}
