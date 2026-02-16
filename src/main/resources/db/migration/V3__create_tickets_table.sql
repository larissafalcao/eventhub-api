CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events (id),
    participant_id BIGINT NOT NULL REFERENCES participants (id),
    purchased_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_tickets_event_id ON tickets (event_id);
CREATE INDEX idx_tickets_participant_id ON tickets (participant_id);
