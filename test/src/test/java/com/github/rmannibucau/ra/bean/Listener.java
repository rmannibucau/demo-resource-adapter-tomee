package com.github.rmannibucau.ra.bean;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

@MessageDriven(
        activationConfig = {
                @ActivationConfigProperty(propertyName = "useJNDI", propertyValue = "false"),
                @ActivationConfigProperty(propertyName = "useLocalTx", propertyValue = "true"),
                @ActivationConfigProperty(propertyName = "maxSession", propertyValue = "1"),
                @ActivationConfigProperty(propertyName = "destination", propertyValue = "the-queue")
        }
)
public class Listener implements MessageListener {
    @Inject
    private Event<OnJmsMessage> onJmsMessageEvent;

    @Override
    public void onMessage(final Message message) {
        try {
            onJmsMessageEvent.fire(new OnJmsMessage(message.getBody(String.class)));
        } catch (final JMSException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
