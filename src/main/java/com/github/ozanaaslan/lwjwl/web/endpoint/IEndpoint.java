package com.github.ozanaaslan.lwjwl.web.endpoint;

import com.github.ozanaaslan.lwjwl.web.endpoint.response.Response;

public interface IEndpoint {

    Response handle(EndpointController endpointController);

}
