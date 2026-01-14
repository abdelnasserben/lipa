-- V1__init.sql
-- LIPA - socle MVP
-- PostgreSQL

SET search_path TO public;

CREATE EXTENSION IF NOT EXISTS pgcrypto WITH SCHEMA public;

-- =========================
-- account
-- =========================
CREATE TABLE account (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  type            VARCHAR(30) NOT NULL,
  status          VARCHAR(30) NOT NULL,
  display_name    VARCHAR(120),
  phone           VARCHAR(32),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_account_type   ON account(type);
CREATE INDEX idx_account_status ON account(status);

-- =========================
-- card
-- =========================
CREATE TABLE card (
  id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  uid                VARCHAR(80) NOT NULL,
  account_id         UUID NOT NULL REFERENCES account(id),
  status             VARCHAR(30) NOT NULL,
  pin_hash           VARCHAR(255),
  pin_fail_count     INT NOT NULL DEFAULT 0,
  pin_blocked_until  TIMESTAMPTZ,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_card_uid ON card(uid);
CREATE INDEX idx_card_account_id ON card(account_id);

-- =========================
-- transaction
-- =========================
CREATE TABLE transaction (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  type             VARCHAR(30) NOT NULL,
  status           VARCHAR(30) NOT NULL,
  amount           NUMERIC(19,2) NOT NULL CHECK (amount > 0),
  currency         CHAR(3) NOT NULL DEFAULT 'KMF',
  idempotency_key  VARCHAR(120) NOT NULL,
  description      VARCHAR(280),
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX ux_transaction_idempotency_key ON transaction(idempotency_key);
CREATE INDEX idx_transaction_created_at ON transaction(created_at);

-- =========================
-- ledger_entry
-- =========================
CREATE TABLE ledger_entry (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  transaction_id  UUID NOT NULL REFERENCES transaction(id),
  account_id      UUID NOT NULL REFERENCES account(id),
  direction       VARCHAR(10) NOT NULL,
  amount          NUMERIC(19,2) NOT NULL CHECK (amount > 0),
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_ledger_entry_account_id ON ledger_entry(account_id);
CREATE INDEX idx_ledger_entry_transaction_id ON ledger_entry(transaction_id);

-- =========================
-- audit_event
-- =========================
CREATE TABLE audit_event (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  actor_type   VARCHAR(30) NOT NULL,
  actor_id     UUID,
  action       VARCHAR(80) NOT NULL,
  target_type  VARCHAR(30),
  target_id    UUID,
  metadata     JSONB NOT NULL DEFAULT '{}'::jsonb,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_event_created_at ON audit_event(created_at);
CREATE INDEX idx_audit_event_action ON audit_event(action);
