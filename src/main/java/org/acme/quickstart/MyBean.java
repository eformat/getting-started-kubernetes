package org.acme.quickstart;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.enterprise.context.ApplicationScoped;
import org.reactivestreams.Publisher;
import io.reactivex.Flowable;

@ApplicationScoped
public class MyBean {

    AtomicInteger counter = new AtomicInteger();

    public Publisher<String> stream() {
        return Flowable.interval(1, TimeUnit.SECONDS)        
        .map(x -> counter.incrementAndGet())
        .map(i -> Integer.toString(i));
    }
    
}