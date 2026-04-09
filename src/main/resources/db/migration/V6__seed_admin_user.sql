-- Default admin password: 'admin123' (must be changed in production)
INSERT INTO users (name, email, password, role, participant_id)
SELECT
    'System Admin',
    'admin@eventhub.local',
    '$2a$10$6yPPd32ws.K7k.WtQHxA1Oel/iG4ytQBHy2YlbeLIaZI0giw3eoTG',
    'ADMIN',
    NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM users
    WHERE email = 'admin@eventhub.local'
);
