package com.larissafalcao.eventhub_api.controller;

import com.larissafalcao.eventhub_api.dto.response.TicketResponse;
import com.larissafalcao.eventhub_api.entity.User;
import com.larissafalcao.eventhub_api.service.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

@RestController
public class TicketController implements TicketControllerDocs {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/events/{eventId}/tickets")
    @Override
    public ResponseEntity<TicketResponse> purchaseTicket(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User authenticatedUser) {
        TicketResponse response = ticketService.purchaseTicket(eventId, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/participant/tickets")
    @Override
    public ResponseEntity<List<TicketResponse>> listMyTickets(@AuthenticationPrincipal User authenticatedUser) {
        List<TicketResponse> response = ticketService.listTicketsByAuthenticatedUser(authenticatedUser);
        return ResponseEntity.ok(response);
    }
}
