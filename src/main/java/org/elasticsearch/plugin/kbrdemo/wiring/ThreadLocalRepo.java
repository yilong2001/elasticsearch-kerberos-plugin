
package org.elasticsearch.plugin.kbrdemo.wiring;

import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestRequest;

/**
 * Created by sscarduzio on 25/11/2016.
 */
public class ThreadLocalRepo {
  public static ThreadLocal<RestRequest> request = new ThreadLocal<>();
  public static ThreadLocal<RestChannel> channel = new ThreadLocal<>();
}
