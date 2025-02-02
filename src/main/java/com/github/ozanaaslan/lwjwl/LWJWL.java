package com.github.ozanaaslan.lwjwl;

import com.github.ozanaaslan.lwjwl.web.endpoint.EndpointController;
import com.github.ozanaaslan.lwjwl.web.endpoint.annotation.Endpoint;
import com.github.ozanaaslan.lwjwl.web.endpoint.annotation.Param;
import com.github.ozanaaslan.lwjwl.web.endpoint.annotation.method.*;
import com.github.ozanaaslan.lwjwl.web.endpoint.response.ContentType;
import com.github.ozanaaslan.lwjwl.web.endpoint.response.GenericResponse;
import com.github.ozanaaslan.lwjwl.web.endpoint.response.Response;
import com.github.ozanaaslan.lwjwl.web.endpoint.response.Status;
import com.sun.net.httpserver.HttpServer;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LWJWL {

    @Getter
    public static LWJWL lwjwl;
    @Getter
    private final int port;
    @Getter
    private HttpServer ancestorServer;
    @Getter
    private EndpointController centralEndpointController;
    @Getter
    private ArrayList<Method> endpoints;
    @Getter
    private Logger logger;

    public LWJWL(int port) {
        this.port = port;
        lwjwl = this;
        init();
    }

    public static boolean hasAnyAnnotation(Method method, Class<?>... annotations) {
        return Arrays.stream(annotations)
                .anyMatch(annotation -> method.isAnnotationPresent(annotation.asSubclass(java.lang.annotation.Annotation.class)));
    }

    @SneakyThrows
    private void init() {
        this.logger = Logger.getLogger(LWJWL.class.getName());
        this.logger.info("Initializing server on port: " + port);
        this.endpoints = new ArrayList<>();
        this.ancestorServer = HttpServer.create(new InetSocketAddress(port), 0);

        (this.ancestorServer).setExecutor(Executors.newCachedThreadPool());
        this.centralEndpointController = new EndpointController() {
            @Override
            public Response handle(EndpointController endpointController) {

                try {
                    Method method = getEndpoint(endpointController.getExchange().getRequestURI().getPath(),
                            endpointController.getExchange().getRequestMethod());

                    if (method == null)
                        return new Response(Status.NOT_FOUND, ContentType.APPLICATION_JSON,
                                new GenericResponse(404, "No such Endpoint", "NOT FOUND", "There's no such endpoint").toJson());


                    HashMap<String, String> map = new HashMap<>();
                    Optional.ofNullable(endpointController.getBodyParameters()).ifPresent(map::putAll);
                    Optional.ofNullable(endpointController.getQueryParameters()).ifPresent(map::putAll);
                    Optional.ofNullable(endpointController.getCookies())
                            .ifPresent(cookies -> cookies.forEach(cookie -> map.put(cookie.getKey(), cookie.getValue())));

                    List<String> parameterValues = new ArrayList<>();
                    Parameter[] methodParams = method.getParameters();

                    for (Parameter parameter : methodParams) {
                        if (parameter.isAnnotationPresent(Param.class)) {
                            String paramValue = map.get(parameter.getAnnotation(Param.class).value());
                            if (paramValue == null)
                                return new Response(Status.BAD_REQUEST, ContentType.APPLICATION_JSON,
                                        new GenericResponse(400, "Request incomplete!", "BAD REQUEST",
                                                "Missing required parameter: " + parameter.getAnnotation(Param.class).value()).toJson());
                            parameterValues.add(paramValue);
                        }
                    }


                    Object[] params = new Object[method.getParameterCount()];
                    params[0] = endpointController;

                    for (int i = 0; i < parameterValues.size(); i++) {
                        params[i + 1] = parameterValues.get(i);
                    }
                    logger.info("Invoking method: " + method.getName() + " with parameters: " + parameterValues);

                    if(method.getReturnType() != Response.class)
                        return Response.json(200, method.invoke(this, params));
                    return (Response) method.invoke(this, params);

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    logger.severe("Method access error: " + e.getMessage());
                    return new Response(Status.INTERNAL_SERVER_ERROR, ContentType.APPLICATION_JSON,
                            new GenericResponse(500, "Couldn't access method", "INTERNAL SERVER ERROR", e.getMessage()).toJson());
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                    logger.severe("Method invocation error: " + e.getCause().getMessage());
                    logger.severe("Unexpected error: " + e.getMessage());
                    return new Response(Status.INTERNAL_SERVER_ERROR, ContentType.APPLICATION_JSON,
                            new GenericResponse(500, "Couldn't invoke method", "INTERNAL SERVER ERROR", e.getMessage()).toJson());
                } catch (Exception e) {
                    e.printStackTrace();
                    return new Response(Status.INTERNAL_SERVER_ERROR, ContentType.APPLICATION_JSON,
                            new GenericResponse(500, "Something went wrong!", "INTERNAL SERVER ERROR", e.getMessage()).toJson());
                }
            }
        };

        this.ancestorServer.createContext("/", this.centralEndpointController);
        this.ancestorServer.start();
    }

    public void stop() {
        this.ancestorServer.stop(0);
    }

    public Method getEndpoint(String path, String requestMethod) {
        this.logger.info("Searching for endpoint for path: " + path + " with method: " + requestMethod);
        Method result = endpoints.stream().filter(method -> method.getAnnotation(Endpoint.class)
                .value().equalsIgnoreCase(path) && (!hasAnyAnnotation(method, GET.class, POST.class, PUT.class, PATCH.class, DELETE.class) || (Arrays.stream(method.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().getSimpleName().equalsIgnoreCase(requestMethod))))).findFirst().orElse(null);

        if (result == null)
            logger.warning("No matching endpoint found for path: " + path + " with method: " + requestMethod);
        return result;
    }


    public void register(Method m) {
        Endpoint e = m.getDeclaredAnnotation(Endpoint.class);
        if (e == null) {
            logger.warning("No @Endpoint on " + m.getName());
            return;
        }
        String p = e.value().toLowerCase();
        Set<String> nm = Stream.of(m.isAnnotationPresent(GET.class) ? "GET" : null,
                        m.isAnnotationPresent(POST.class) ? "POST" : null,
                        m.isAnnotationPresent(PUT.class) ? "PUT" : null,
                        m.isAnnotationPresent(PATCH.class) ? "PATCH" : null,
                        m.isAnnotationPresent(DELETE.class) ? "DELETE" : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (endpoints.stream().anyMatch(r -> {
            Endpoint re = r.getDeclaredAnnotation(Endpoint.class);
            if (re == null || !re.value().equalsIgnoreCase(p)) return false;
            Set<String> rm = Stream.of(r.isAnnotationPresent(GET.class) ? "GET" : null,
                            r.isAnnotationPresent(POST.class) ? "POST" : null,
                            r.isAnnotationPresent(PUT.class) ? "PUT" : null,
                            r.isAnnotationPresent(PATCH.class) ? "PATCH" : null,
                            r.isAnnotationPresent(DELETE.class) ? "DELETE" : null)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            return (nm.isEmpty() && rm.isEmpty()) || nm.stream().anyMatch(rm::contains);
        })) {
            logger.warning("Redundant endpoint for '" + p + "'. Skipping " + m.getName());
            return;
        }
        endpoints.add(m);
        logger.info("Registered " + m.getName() + " for '" + p + "' with " + (nm.isEmpty() ? "generic" : nm));
    }

    public void register(Class reference){
        Arrays.stream(reference.getMethods())
                .filter(method -> method.isAnnotationPresent(Endpoint.class)).forEach(method -> register(method));
    }

}