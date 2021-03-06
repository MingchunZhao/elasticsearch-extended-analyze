/*
 * Copyright 2013 Jun Ohtani
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package info.johtani.elasticsearch.rest.action.admin.indices.analyze;

import info.johtani.elasticsearch.action.admin.indices.extended.analyze.ExtendedAnalyzeAction;
import info.johtani.elasticsearch.action.admin.indices.extended.analyze.ExtendedAnalyzeRequest;
import info.johtani.elasticsearch.action.admin.indices.extended.analyze.ExtendedAnalyzeResponse;
import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.support.RestToXContentListener;

import static org.elasticsearch.rest.RestRequest.Method.*;

/**
 * Extended _analyze for REST endpoint
 */
public class RestExtendedAnalyzeAction extends BaseRestHandler {

    @Inject
    public RestExtendedAnalyzeAction(Settings settings, Client client, RestController controller) {
        super(settings, controller, client);
        controller.registerHandler(GET, "/_extended_analyze", this);
        controller.registerHandler(GET, "/{index}/_extended_analyze", this);
        controller.registerHandler(POST, "/_extended_analyze", this);
        controller.registerHandler(POST, "/{index}/_extended_analyze", this);
    }

    @Override
    public void handleRequest(final RestRequest request, final RestChannel channel, final Client client) {
        String text = request.param("text");
        if (text == null && request.hasContent()) {
            text = request.content().toUtf8();
        }
        if (text == null) {
            throw new ElasticsearchIllegalArgumentException("text is missing");
        }

        ExtendedAnalyzeRequest analyzeRequest = new ExtendedAnalyzeRequest(request.param("index"), text);
        analyzeRequest.listenerThreaded(false);
        analyzeRequest.preferLocal(request.paramAsBoolean("prefer_local", analyzeRequest.preferLocalShard()));
        analyzeRequest.analyzer(request.param("analyzer"));
        analyzeRequest.field(request.param("field"));
        analyzeRequest.tokenizer(request.param("tokenizer"));
        analyzeRequest.tokenFilters(request.paramAsStringArray("token_filters", request.paramAsStringArray("filters", null)));
        analyzeRequest.charFilters(request.paramAsStringArray("char_filters", analyzeRequest.charFilters()));
        analyzeRequest.tokenChain(request.paramAsBoolean("token_chain", analyzeRequest.tokenChain()));
        analyzeRequest.attributes(request.paramAsStringArray("attributes", null));

        client.admin().indices().execute(ExtendedAnalyzeAction.INSTANCE, analyzeRequest, new RestToXContentListener<ExtendedAnalyzeResponse>(channel));
    }
}
