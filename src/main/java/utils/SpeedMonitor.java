package utils;

import common.Protocol;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SpeedMonitor {
    private static final double MIN_REPORT_INTERVAL_NANOS = 3_000_000_000.0;
    private static final double MIN_REPORT_INTERVAL_SECONDS = MIN_REPORT_INTERVAL_NANOS / 1_000_000_000;
    private static final double BYTES_IN_KB = 1024.0;
    private static final String THREAD_NAME_FORMAT = "SpeedMonitor-%s";
    private static final String INSTANT_SPEED_FORMAT = "[Клиент %s] Скорость: %.2f КБ/с%n";
    private static final String AVERAGE_SPEED_FORMAT = "[Клиент %s] Передача завершена. Средняя скорость: %.2f КБ/с%n";

    private long lastReportedBytes = 0;
    private final long startTimeNanos = System.nanoTime();
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
        double now = System.nanoTime();
        double elapsedSinceStart = now - startTimeNanos;
        if (elapsedSinceStart < MIN_REPORT_INTERVAL_NANOS) {
            return;
        }

        long currentTotal = protocol.getTotalBytesRead();
        long diff = currentTotal - lastReportedBytes;

        System.out.printf(INSTANT_SPEED_FORMAT,
                clientName,
                (double) diff / (BYTES_IN_KB * MIN_REPORT_INTERVAL_SECONDS));

        lastReportedBytes = currentTotal;
    }

    public void shutdown() {
        scheduler.shutdown();
        double totalTimeNanos = System.nanoTime() - startTimeNanos;

        double NanosInSeconds = 1000_000_000;
        double avgSpeedBytesPerSecond = NanosInSeconds * protocol.getTotalBytesRead() / totalTimeNanos;

        System.out.printf(AVERAGE_SPEED_FORMAT,
                clientName,
                avgSpeedBytesPerSecond / BYTES_IN_KB);
    }
}