package org.acme.quickstart;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import javax.enterprise.context.ApplicationScoped;

@Health
@ApplicationScoped
public class RuntimeHealthCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        Runtime r = Runtime.getRuntime();
        return HealthCheckResponse.named("runtime")
                .withData("usedMemory", format(r.totalMemory() - r.freeMemory()))
                .withData("totalMemory", format(r.totalMemory()))
                .withData("maxMemory", format(r.maxMemory()))
                .withData("freeMemory", format(r.freeMemory()))
                .withData("availableProcessors", r.availableProcessors())
                .up()
                .build();
    }

    public static String format(long v) {
        if (v < 1024) return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double) v / (1L << (z * 10)), " KMGTPE".charAt(z));
    }
}
