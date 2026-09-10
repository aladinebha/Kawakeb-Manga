INSERT INTO app_user (id, email, display_name)
VALUES ('00000000-0000-0000-0000-000000000001', 'creator@local.manga', 'Local Creator')
ON CONFLICT (id) DO NOTHING;
