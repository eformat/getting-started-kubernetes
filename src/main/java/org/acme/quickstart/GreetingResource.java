package org.acme.quickstart;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;
import org.reactivestreams.Publisher;

@Path("/hello")
public class GreetingResource {

    @Inject
    GreetingService service;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/greeting/{name}")
    public String greeting(@PathParam("name") String name) {
        return service.greeting(name);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "hello";
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/async-rx")
    public Publisher<String> asyncStream() {
        return ReactiveStreams.of("h", "e", "l", "l", "o").map(s -> s.toUpperCase()).distinct()
                .buildRs();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/async")
    public CompletionStage<String> helloAysnc() {
        return CompletableFuture.supplyAsync(() -> {
            return "hello";
        });
    }

    @Inject
    MyBean bean;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @Path("/stream")
    public Publisher<String> stream() {
        return bean.stream();
    }

}
