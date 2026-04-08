package com.larissafalcao.eventhub_api.listener;

import com.larissafalcao.eventhub_api.event.TicketPurchasedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class TicketPurchasedListenerTest {

    private final TicketPurchasedListener listener = new TicketPurchasedListener();

    @Test
    @DisplayName("onTicketPurchased: logs mocked confirmation email details")
    void onTicketPurchasedLogsMockedConfirmationEmailDetails(CapturedOutput output) {
        TicketPurchasedEvent event = new TicketPurchasedEvent(
                123L,
                "Alice",
                "alice@email.com",
                "JavaConf",
                Instant.parse("2026-02-16T12:00:00Z"));

        listener.onTicketPurchased(event);

        assertThat(output.getOut())
                .contains("[EMAIL] Confirmation sent to Alice (alice@email.com) for event \"JavaConf\" - Ticket #123");
    }
}
