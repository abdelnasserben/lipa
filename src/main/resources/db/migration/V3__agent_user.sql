-- V3__agent_user.sql
-- Users back-office (AGENT / ADMIN) pour sécuriser cash-in + futur back-office

CREATE TABLE agent_user (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  username       VARCHAR(80) NOT NULL,
  password_hash  VARCHAR(255) NOT NULL,
  role           VARCHAR(20) NOT NULL,   -- AGENT | ADMIN
  status         VARCHAR(20) NOT NULL,   -- ACTIVE | DISABLED
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_agent_user_username ON agent_user(username);
CREATE INDEX idx_agent_user_role ON agent_user(role);
CREATE INDEX idx_agent_user_status ON agent_user(status);
