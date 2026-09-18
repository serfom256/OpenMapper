package io.github.serfom256.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Configuration;

import io.github.serfom256.benchmark.model.om.TicketCommentOM;
import io.github.serfom256.benchmark.model.om.TicketOM;
import io.github.serfom256.core.lifecycle.IdGenerator;

@Import(IdGeneratorTest.GeneratorConfig.class)
class IdGeneratorTest extends IntegrationTestBase {

    static final AtomicInteger SEQUENCE = new AtomicInteger();

    @Configuration
    static class GeneratorConfig {

        @Bean
        IdGenerator<TicketOM> ticketIds() {
            return new IdGenerator<>() {
                public Class<TicketOM> type() {
                    return TicketOM.class;
                }

                public Object generate(TicketOM ticket) {
                    return "TCK-%d".formatted(SEQUENCE.incrementAndGet());
                }
            };
        }

        @Bean
        IdGenerator<TicketCommentOM> commentIds() {
            return new IdGenerator<>() {
                public Class<TicketCommentOM> type() {
                    return TicketCommentOM.class;
                }

                public Object generate(TicketCommentOM comment) {
                    return "CMT-%d".formatted(SEQUENCE.incrementAndGet());
                }
            };
        }
    }

    private TicketOM ticket(String title) {
        TicketOM ticket = new TicketOM();
        ticket.setTitle(title);
        return ticket;
    }

    @Test
    @DisplayName("a key the database does not generate is assigned before the insert")
    void keyIsAssignedOnInsert() {
        TicketOM ticket = ticket("generated");
        ticketDao.insertTicket(ticket);

        assertNotNull(ticket.getId(), "the generator did not assign the key on the caller's object");
        assertTrue(ticket.getId().startsWith("TCK-"));
        assertEquals("generated", ticketDao.findTicketById(ticket.getId()).getTitle());
    }

    @Test
    @DisplayName("a key the caller already set is kept")
    void callerAssignedKeyIsKept() {
        TicketOM ticket = ticket("explicit");
        ticket.setId("TCK-mine");

        ticketDao.insertTicket(ticket);

        assertEquals("TCK-mine", ticket.getId());
        assertEquals("explicit", ticketDao.findTicketById("TCK-mine").getTitle());
    }

    @Test
    @DisplayName("every element of a batch gets its own key")
    void everyBatchElementGetsAKey() {
        List<TicketOM> tickets = List.of(ticket("b1"), ticket("b2"), ticket("b3"));

        ticketDao.insertTickets(tickets);

        for (TicketOM ticket : tickets) {
            assertNotNull(ticket.getId());
            assertEquals(ticket.getTitle(), ticketDao.findTicketById(ticket.getId()).getTitle());
        }
        assertNotEquals(tickets.get(0).getId(), tickets.get(1).getId());
        assertNotEquals(tickets.get(1).getId(), tickets.get(2).getId());
    }

    @Test
    @DisplayName("cascaded children get keys too")
    void cascadedChildrenGetKeys() {
        TicketOM ticket = ticket("with comments");
        TicketCommentOM first = new TicketCommentOM();
        first.setBody("first");
        TicketCommentOM second = new TicketCommentOM();
        second.setBody("second");
        ticket.setComments(List.of(first, second));

        ticketDao.insertTicket(ticket);

        assertNotNull(first.getId());
        assertNotNull(second.getId());
        assertNotEquals(first.getId(), second.getId());

        TicketOM loaded = ticketDao.findTicketById(ticket.getId());
        assertEquals(2, loaded.getComments().size());
    }
}
