package com.github.rmannibucau.ra;

import com.github.rmannibucau.ra.bean.JMSMessages;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ArquillianExtension.class)
class ArtemisTest {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackage(JMSMessages.class.getPackage())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private JMSMessages accumulator;

    @Resource(name = "artemis-javax.jms.ConnectionFactory")
    private ConnectionFactory cf;

    @Resource(name = "artemis-javax.jms.Queue")
    private Queue queue;

    @Inject
    @JMSConnectionFactory("artemis-javax.jms.ConnectionFactory")
    private JMSContext context;

    @Test
    void validate() throws Exception {
        ensureInjectionsAreFromArtemisAndNotAMQ5();
        withAMQServer(() -> {
            try {
                sendMessageToEmbedBroker();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        });
    }

    private void withAMQServer(final Runnable task) throws Exception { // because we are embedded we can
        final var server = new EmbeddedActiveMQ();
        server.start();
        try {
            task.run();
        } finally {
            server.stop();
        }
    }

    private void sendMessageToEmbedBroker() throws InterruptedException {
        final var latch = new CountDownLatch(1);
        accumulator.reset(latch);
        try (final var ctx = context) {
            ctx.createProducer().send(queue, "arquillian test");
        }
        assertTrue(latch.await(20, SECONDS));
        assertEquals(List.of("arquillian test"), accumulator.getMessages());
        accumulator.reset(null);
    }

    private void ensureInjectionsAreFromArtemisAndNotAMQ5() {
        Stream.of(queue, cf)
                .map(Object::getClass)
                .forEach(it -> assertTrue(it.getName().startsWith("org.apache.activemq.artemis.jms.client.A"), it::getName));
    }
}
