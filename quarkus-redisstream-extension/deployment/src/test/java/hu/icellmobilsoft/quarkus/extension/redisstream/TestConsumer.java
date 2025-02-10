package hu.icellmobilsoft.quarkus.extension.redisstream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import hu.icellmobilsoft.reactive.messaging.redis.streams.IncomingRedisStreamMetadata;
import io.smallrye.mutiny.Uni;

/**
 * Test bean for consuming redis messages.
 * 
 * @author mark.petrenyi
 * @since 1.0.0
 */
@ApplicationScoped
public class TestConsumer {
    private final List<String> messages = Collections.synchronizedList(new ArrayList<>());
    private final List<MessageWithMetadata> metadataMessages = Collections.synchronizedList(new ArrayList<>());

    public List<String> getMessages() {
        return messages;
    }

    public List<MessageWithMetadata> getMetadataMessages() {
        return metadataMessages;
    }

    @Incoming("in")
    public void consume(final String message) {
        messages.add(message);
    }

    @Incoming("in-reactive")
    public Uni<Void> consumeReactive(final Message<String> message) {
        return Uni.createFrom().item(message).invoke(m -> metadataMessages.add(MessageWithMetadata.of(m))).invoke(Message::ack).replaceWithVoid();
    }

    public static class MessageWithMetadata {

        private String message;
        private String id;
        private Map<String, String> metadata;

        public static MessageWithMetadata of(Message<String> message) {
            MessageWithMetadata m = new MessageWithMetadata();
            m.message = message.getPayload();
            Optional<IncomingRedisStreamMetadata> incomingRedisStreamMetadata = message.getMetadata().get(IncomingRedisStreamMetadata.class);
            if (incomingRedisStreamMetadata.isPresent()) {
                m.id = incomingRedisStreamMetadata.get().getId();
                m.metadata = incomingRedisStreamMetadata.get().getAdditionalFields();
            }
            return m;
        }

        /**
         * Gets message.
         *
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Gets id.
         *
         * @return the id
         */
        public String getId() {
            return id;
        }

        /**
         * Gets metadata.
         *
         * @return the metadata
         */
        public Map<String, String> getMetadata() {
            return metadata;
        }
    }
}
