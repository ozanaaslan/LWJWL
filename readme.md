
---

# Lightweight Java Web Library (LWJWL)

## Overview

LWJWL is a minimalist, lightweight web framework written in Java designed to facilitate the creation of RESTful web services. It supports HTTP requests using the `GET`, `POST`, `PUT`, `PATCH`, and `DELETE` methods, while allowing developers to define custom endpoints with minimal boilerplate code.

It leverages Java’s reflection capabilities to dynamically map methods to HTTP requests, making it easy to define custom endpoints and handle requests efficiently. LWJWL is optimized for simplicity and flexibility, providing a straightforward way to manage HTTP servers, endpoints, and responses.

## Features

- **Dynamic Endpoint Registration**: Define and register HTTP endpoints using annotations.
- **Support for HTTP Methods**: Supports the common HTTP methods such as `GET`, `POST`, `PUT`, `PATCH`, and `DELETE`.
- **Request Parameters**: Easily handle query parameters, body parameters, and cookies using annotations.
- **Custom Responses**: Return various types of responses including JSON, text, files, or redirect.
- **Minimal Dependencies**: Relies only on Java standard libraries, making it lightweight and easy to integrate into your projects.

## Requirements

- JDK 8 or higher.
- Basic knowledge of Java and HTTP protocols.

## What It Can Do

- **Create a Web Server**: You can run a lightweight HTTP server with just a few lines of code.
- **Handle Multiple Endpoints**: Easily handle multiple RESTful endpoints with different HTTP methods (GET, POST, PUT, PATCH, DELETE).
- **Flexible Parameter Handling**: Supports query parameters, body parameters, and cookies.
- **Custom Response Handling**: Return responses in JSON, plain text, file downloads, or redirects.
- **Logging**: Simple logging is provided to trace server activity and debug issues.

## What It Shouldn't Be Used For

- **Heavy Production Traffic**: LWJWL is designed for lightweight applications or educational purposes. It may not be suitable for large-scale production systems with heavy traffic.
- **Advanced Security Features**: While basic functionalities like cookies and query parameters are supported, LWJWL doesn't include advanced security features like authentication, authorization, or encryption out of the box.
- **Complex Routing or Middleware**: For more complex routing, middleware support, or integrations (e.g., database connections, third-party libraries), it might be better to use a full-fledged framework like Spring or Spark Java.

## Getting Started

### 1. Setup

First, include the LWJWL library in your project. If you're using Maven, add the following dependency:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.ozanaaslan</groupId>
        <artifactId>LWJWL</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>

```

### 2. Create a Simple Server

Create a class to start the server and register your endpoints:

```java
import com.github.ozanaaslan.lwjwl.LWJWL;
import com.github.ozanaaslan.lwjwl.web.endpoint.annotation.Endpoint;
import com.github.ozanaaslan.lwjwl.web.endpoint.annotation.Param;
import com.github.ozanaaslan.lwjwl.web.endpoint.response.Response;
import lombok.SneakyThrows;

public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        LWJWL server = new LWJWL(8080);
        server.register(Main.class);  // Register the class with the endpoints
    }

    // Example GET endpoint
    @Endpoint("/hello") @GET
    public static Response helloEndpoint(EndpointController endpointController) {
        return Response.json(200, "Hello, world!");
    }

    // Example POST endpoint
    @Endpoint("/postExample") @POST
    public static Response postEndpoint(EndpointController endpointController, @Param("id") String id) {
        return Response.json(200, "Received ID: " + id);
    }
}
```

### 3. Understanding the Code

- **LWJWL(8080)**: Initializes the server on port `8080`.
- **server.register(Main.class)**: Registers the `Main` class to handle HTTP requests.
- **@Endpoint("/hello")**: Defines the URL pattern for the endpoint. It supports various HTTP methods like `GET`, `POST`, `PUT`, etc.
- **@Param("id")**: Binds the `id` parameter from the request to the method argument.
- **Response.json()**: Returns a JSON response with a specified HTTP status code.


### 4. Stopping the Server

To stop the server, use the `stop()` method:

```java
server.stop();
```


## API Documentation

### `LWJWL`

- **Constructor**: `LWJWL(int port)`
    - Initializes the server on the specified port.

- **Method**: `register(Class reference)`
    - Registers the specified class with all the endpoint methods defined within it.

- **Method**: `stop()`
    - Stops the HTTP server.

### `EndpointController`

- **Method**: `respond(int responseCode, byte[] responseBytes)`
    - Sends the raw response bytes with the specified HTTP status code.

- **Method**: `respond(Response response)`
    - Sends a predefined response (e.g., JSON, file, redirect).

## Example Use Case

```java
public class MyApi {

    public static void main(String[] args) {
        LWJWL server = new LWJWL(8080);
        server.register(MyApi.class);
    }

    @Endpoint("/greet") @GET
    public static Response greetEndpoint(EndpointController endpointController) {
        return Response.json(200, "Welcome to LWJWL!");
    }

}
```

- **GET /greet**: Returns a "Welcome to LWJWL!" message.


## License

This project is licensed under the **Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License (CC BY-NC-SA 4.0)**.

### Terms:
- You are free to:
    - Share and adapt the material for non-commercial purposes.
    - Modify the code and use it in your own projects, as long as the modifications are also non-commercial.

- You may not:
    - Use the material for commercial purposes.

- Any modifications and forks must:
    - Be shared under the same **Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License (CC BY-NC-SA 4.0)**.

By using this project, you agree to these terms.

---

