package model.executors;

import server.HttpRequest;
import server.HttpResponse;

public interface RequestExecutor {
    HttpResponse execute(HttpRequest httpRequest);
}
