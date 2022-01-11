package com.abitica.executors;

import com.abitica.server.HttpRequest;
import com.abitica.server.HttpResponse;

public interface RequestExecutor {
    HttpResponse execute(HttpRequest httpRequest);
}
