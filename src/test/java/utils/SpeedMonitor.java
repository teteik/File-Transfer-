package utils;

import common.Protocol;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpeedMonitor {
    private static final long MILLIS = 3000L;
    private static final long SECONDS = MILLIS / 1000;
    private static final double BYTES_IN_KB = 1024.0;
    private static final long MIN_REPORT_INTERVAL_MILLIS = MILLIS;
    private static final String THREAD_NAME_FORMAT = "SpeedMonitor-%s";
    private static final String INSTANT_SPEED_FORMAT = "[Клиент %s] Скорость: %.2f КБ/с%n";
    private static final String AVERAGE_SPEED_FORMAT = "[Клиент %s] Передача завершена. Средняя скорость: %.2f КБ/с%n";

    private long lastReportedBytes = 0;
    private final long startTime = System.currentTimeMillis();
    private final String clientName;
    private final ScheduledExecutorService scheduler;
    private final Protocol protocol;

    public SpeedMonitor(String clientName, Protocol protocol) {
        this.protocol = protocol;
        this.clientName = clientName;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, String.format(THREAD_NAME_FORMAT, clientName));
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::printInstantSpeed, 1, 3, TimeUnit.SECONDS);
    }

    private void printInstantSpeed() {
        long now = System.currentTimeMillis();
        long elapsedSinceStart = now - startTime;
        if (elapsedSinceStart < MIN_REPORT_INTERVAL_MILLIS) {
            return;
        }

        long currentTotal = protocol.getTotalBytesRead();
        long diff = currentTotal - lastReportedBytes;

        System.out.printf(INSTANT_SPEED_FORMAT,
                clientName,
                (double) diff / (BYTES_IN_KB * SECONDS));

        lastReportedBytes = currentTotal;
    }

    public void shutdown() {
        scheduler.shutdown();
        long totalTimeMillis = System.currentTimeMillis() - startTime;

        double millisInSeconds = 1000;
        double avgSpeedBytesPerMilli = (double) protocol.getTotalBytesRead() / totalTimeMillis;
        double avgSpeedBytesPerSecond = avgSpeedBytesPerMilli ;//* millisInSeconds;

        System.out.printf(AVERAGE_SPEED_FORMAT,
                clientName,
                avgSpeedBytesPerSecond / BYTES_IN_KB);
    }
}