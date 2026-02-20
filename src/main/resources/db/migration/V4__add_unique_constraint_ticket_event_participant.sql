ALTER TABLE tickets
    ADD CONSTRAINT uq_tickets_event_participant UNIQUE (event_id, participant_id);
