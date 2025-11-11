package com.tigrinho.slot.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom {@link HealthIndicator} that provides detailed system information
 * as part of the application's health check. This includes JVM, memory,
 * OS details, and application uptime.
 */
@Component
public class AppHealthIndicator implements HealthIndicator {

    /**
     * Performs a health check and provides detailed system information.
     *
     * @return A {@link Health} object with status UP and system details.
     */
    @Override
    public Health health() {
        return Health.up()
                .withDetails(getSystemInfo())
                .build();
    }

    /**
     * Gathers various system and JVM information.
     *
     * @return A {@link Map} containing system information details.
     */
    private Map<String, Object> getSystemInfo() {
        final Map<String, Object> details = new HashMap<>();
        
        // JVM Information
        final Runtime runtime = Runtime.getRuntime();
        details.put("jvm.name", System.getProperty("java.vm.name"));
        details.put("jvm.version", System.getProperty("java.version"));
        details.put("jvm.vendor", System.getProperty("java.vendor"));
        
        // Memory Information
        details.put("memory.used", formatBytes(runtime.totalMemory() - runtime.freeMemory()));
        details.put("memory.free", formatBytes(runtime.freeMemory()));
        details.put("memory.total", formatBytes(runtime.totalMemory()));
        details.put("memory.max", formatBytes(runtime.maxMemory()));
        
        // OS Information
        final OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        details.put("os.name", os.getName());
        details.put("os.version", os.getVersion());
        details.put("os.arch", os.getArch());
        details.put("available.processors", os.getAvailableProcessors());
        details.put("system.load.average", os.getSystemLoadAverage());
        
        // Uptime
        final RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        details.put("jvm.uptime", formatUptime(rb.getUptime()));
        details.put("jvm.startTime", LocalDateTime.ofInstant(
                Instant.ofEpochMilli(rb.getStartTime()), ZoneId.systemDefault()));
        
        return details;
    }
    
    /**
     * Formats a given number of bytes into a human-readable string (e.g., "1.5 MiB").
     *
     * @param bytes The number of bytes to format.
     * @return A formatted string representing the byte size.
     */
    private String formatBytes(final long bytes) {
        if (bytes < 1024) return bytes + " B";
        final int exp = (int) (Math.log(bytes) / Math.log(1024));
        final String pre = "KMGTPE".charAt(exp-1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    /**
     * Formats a given uptime in milliseconds into a human-readable string
     * (e.g., "1d 2h 3m 4s").
     *
     * @param uptime The uptime in milliseconds.
     * @return A formatted string representing the uptime.
     */
    private String formatUptime(final long uptime) {
        final long days = uptime / (1000 * 60 * 60 * 24);
        final long hours = (uptime % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        final long minutes = (uptime % (1000 * 60 * 60)) / (1000 * 60);
        final long seconds = (uptime % (1000 * 60)) / 1000;
        
        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }
}
