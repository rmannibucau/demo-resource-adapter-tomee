package com.github.rmannibucau.ra.bean;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

@ApplicationScoped
public class JMSMessages {
    private final List<String> messages = new CopyOnWriteArrayList<>();

    private volatile CountDownLatch latch;

    public List<String> getMessages() {
        return messages;
    }

    public void onMessage(@Observes final OnJmsMessage onJmsMessage) {
        final var latch = this.latch;
        if (latch == null) {
            return;
        }
        messages.add(onJmsMessage.getMessage());
        latch.countDown();
    }

    public void reset(final CountDownLatch latch) {
        this.messages.clear();
        this.latch = latch;
    }
}
