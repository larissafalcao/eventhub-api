package com.larissafalcao.eventhub_api.controller;

import com.larissafalcao.eventhub_api.dto.request.PurchaseTicketRequest;
import com.larissafalcao.eventhub_api.dto.response.TicketResponse;
import com.larissafalcao.eventhub_api.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
            @Valid @RequestBody PurchaseTicketRequest request) {
        TicketResponse response = ticketService.purchaseTicket(eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/participants/{participantId}/tickets")
    @Override
    public ResponseEntity<List<TicketResponse>> listTicketsByParticipant(@PathVariable Long participantId) {
        List<TicketResponse> response = ticketService.listTicketsByParticipant(participantId);
        return ResponseEntity.ok(response);
    }
}
