package utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class SpeedMonitor {
    private final AtomicLong totalBytes = new AtomicLong(0);
    private final AtomicLong lastBytes = new AtomicLong(0);
    private final AtomicReference<Long> lastTime = new AtomicReference<>(System.currentTimeMillis());
    private final String clientName;

    public SpeedMonitor(String clientName) {
        this.clientName = clientName;
    }

    public void addBytes(long bytes) {
        totalBytes.addAndGet(bytes);
    }

    public void printSpeed() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastTime.get();
        if (elapsed >= 3000) { // каждые 3 секунды
            long currentTotal = totalBytes.get();
            long diff = currentTotal - lastBytes.get();
            double instantSpeed = (double) diff / (elapsed / 1000.0); // байт/сек
            double avgSpeed = (double) currentTotal / ((now - lastTime.get()) / 1000.0);

            System.out.printf("[Клиент %s] Мгновенная скорость: %.2f Б/с, Средняя скорость: %.2f Б/с%n",
                    clientName, instantSpeed, avgSpeed);

            lastBytes.set(currentTotal);
            lastTime.set(now);
        }
    }

    public long getTotalBytes() {
        return totalBytes.get();
    }
}