/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.plugin.kbrdemo.rest;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.HandledTransportAction;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

public class MyRestTransportAction extends HandledTransportAction<MyRestRequest, MyRestResponse> {
  final org.apache.logging.log4j.Logger logger = Loggers.getLogger(getClass());

  private final NodeClient client;

  @Inject
  public MyRestTransportAction(Settings settings, ThreadPool threadPool, TransportService transportService,
                               ActionFilters actionFilters, IndexNameExpressionResolver indexNameExpressionResolver,
                               NodeClient client) {
    super(settings, MyRestAction.NAME, threadPool, transportService, actionFilters, indexNameExpressionResolver, MyRestRequest::new);
    this.client = client;
  }

  @Override
  protected void doExecute(MyRestRequest request, ActionListener<MyRestResponse> listener) {
    logger.info("____________________: MyRestTransportAction, do execute, "+Thread.currentThread().getName());

    try {
      listener.onResponse(new MyRestResponse(null));
    } catch (Exception e) {
      listener.onResponse(new MyRestResponse(e));
    }
  }
}
