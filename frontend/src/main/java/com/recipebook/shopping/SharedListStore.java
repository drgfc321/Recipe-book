package com.recipebook.shopping;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SharedListStore {

    private record Entry(String text, Instant createdAt) {}

    private static final long TTL_HOURS = 24;
    private final ConcurrentHashMap<String, Entry> store = new ConcurrentHashMap<>();

    public String put(String text) {
        evictExpired();
        String id = UUID.randomUUID().toString();
        store.put(id, new Entry(text, Instant.now()));
        return id;
    }

    public Optional<String> get(String id) {
        Entry entry = store.get(id);
        if (entry == null) {
            return Optional.empty();
        }
        if (entry.createdAt().plus(TTL_HOURS, ChronoUnit.HOURS).isBefore(Instant.now())) {
            store.remove(id);
            return Optional.empty();
        }
        return Optional.of(entry.text());
    }

    private void evictExpired() {
        Instant cutoff = Instant.now().minus(TTL_HOURS, ChronoUnit.HOURS);
        store.entrySet().removeIf(e -> e.getValue().createdAt().isBefore(cutoff));
    }
}
