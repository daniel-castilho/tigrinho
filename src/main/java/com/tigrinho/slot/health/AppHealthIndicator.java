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

@Component
public class AppHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.up()
                .withDetails(getSystemInfo())
                .build();
    }

    private Map<String, Object> getSystemInfo() {
        Map<String, Object> details = new HashMap<>();
        
        // JVM Information
        Runtime runtime = Runtime.getRuntime();
        details.put("jvm.name", System.getProperty("java.vm.name"));
        details.put("jvm.version", System.getProperty("java.version"));
        details.put("jvm.vendor", System.getProperty("java.vendor"));
        
        // Memory Information
        details.put("memory.used", formatBytes(runtime.totalMemory() - runtime.freeMemory()));
        details.put("memory.free", formatBytes(runtime.freeMemory()));
        details.put("memory.total", formatBytes(runtime.totalMemory()));
        details.put("memory.max", formatBytes(runtime.maxMemory()));
        
        // OS Information
        OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        details.put("os.name", os.getName());
        details.put("os.version", os.getVersion());
        details.put("os.arch", os.getArch());
        details.put("available.processors", os.getAvailableProcessors());
        details.put("system.load.average", os.getSystemLoadAverage());
        
        // Uptime
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        details.put("jvm.uptime", formatUptime(rb.getUptime()));
        details.put("jvm.startTime", LocalDateTime.ofInstant(
                Instant.ofEpochMilli(rb.getStartTime()), ZoneId.systemDefault()));
        
        return details;
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    private String formatUptime(long uptime) {
        long days = uptime / (1000 * 60 * 60 * 24);
        long hours = (uptime % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (uptime % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (uptime % (1000 * 60)) / 1000;
        
        return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
    }
}
