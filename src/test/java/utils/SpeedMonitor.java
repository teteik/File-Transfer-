package utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class SpeedMonitor {
    private static final long MILLIS_IN_SECOND = 3000L;
    private static final double BYTES_IN_KB = 1024.0;
    private static final long MIN_REPORT_INTERVAL_MILLIS = MILLIS_IN_SECOND;
    private static final String THREAD_NAME_FORMAT = "SpeedMonitor-%s";
    private static final String INSTANT_SPEED_FORMAT = "[Клиент %s] Скорость: %.2f КБ/с%n";
    private static final String AVERAGE_SPEED_FORMAT = "[Клиент %s] Передача завершена. Средняя скорость: %.2f КБ/с%n";

    private final AtomicLong totalBytes = new AtomicLong(0);
    private final AtomicLong lastReportedBytes = new AtomicLong(0);
    private final AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
    private final String clientName;
    private final ScheduledExecutorService scheduler;

    public SpeedMonitor(String clientName) {
        this.clientName = clientName;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, String.format(THREAD_NAME_FORMAT, clientName));
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(this::printInstantSpeed, 1, 3, TimeUnit.SECONDS);
    }

    public void addBytes(long bytes) {
        totalBytes.addAndGet(bytes);
    }

    private void printInstantSpeed() {
        long now = System.currentTimeMillis();
        long elapsedSinceStart = now - startTime.get();
        if (elapsedSinceStart < MIN_REPORT_INTERVAL_MILLIS) {
            return;
        }

        long currentTotal = totalBytes.get();
        long diff = currentTotal - lastReportedBytes.get();

        System.out.printf(INSTANT_SPEED_FORMAT,
                clientName,
                (double) diff / BYTES_IN_KB);

        lastReportedBytes.set(currentTotal);
    }

    public void shutdown() {
        scheduler.shutdown();
        long total = totalBytes.get();
        long totalTimeMillis = System.currentTimeMillis() - startTime.get();

        if (totalTimeMillis > 0) {
            double totalTimeSeconds = totalTimeMillis / (double) MILLIS_IN_SECOND;
            double avgSpeedBytesPerSecond = total / totalTimeSeconds;

            System.out.printf(AVERAGE_SPEED_FORMAT,
                    clientName,
                    avgSpeedBytesPerSecond / BYTES_IN_KB);
        }
    }
}