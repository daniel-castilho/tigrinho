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

    private static final int BYTES_IN_KB = 1024;
    private static final int MILLIS_IN_SECOND = 1000;
    private static final int SECONDS_IN_MINUTE = 60;
    private static final int MINUTES_IN_HOUR = 60;
    private static final int HOURS_IN_DAY = 24;
    private static final int EXP_OFFSET = 1; // Used in formatBytes for 'KMiB' prefix array

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
        if (bytes < BYTES_IN_KB) {
            return bytes + " B";
        }
        final int exp = (int) (Math.log(bytes) / Math.log(BYTES_IN_KB));
        final String pre = "KMGTPE".charAt(exp - EXP_OFFSET) + "i";
        return String.format("%.1f %sB", (double) bytes / Math.pow(BYTES_IN_KB, exp), pre);
    }

    /**
     * Formats a given uptime in milliseconds into a human-readable string
     * (e.g., "1d 2h 3m 4s").
     *
     * @param uptime The uptime in milliseconds.
     * @return A formatted string representing the uptime.
     */
    private String formatUptime(final long uptime) {
        final long totalSeconds = uptime / MILLIS_IN_SECOND;
        final long days = totalSeconds / (SECONDS_IN_MINUTE * MINUTES_IN_HOUR * HOURS_IN_DAY);
        final long hours = totalSeconds %
                (SECONDS_IN_MINUTE * MINUTES_IN_HOUR * HOURS_IN_DAY) / (SECONDS_IN_MINUTE * MINUTES_IN_HOUR);
        final long minutes = totalSeconds % (SECONDS_IN_MINUTE * MINUTES_IN_HOUR) / SECONDS_IN_MINUTE;
        final long seconds = totalSeconds % SECONDS_IN_MINUTE;
        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }
}
