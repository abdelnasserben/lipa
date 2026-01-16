CREATE TABLE fee_configuration (
    id UUID PRIMARY KEY,
    percentage NUMERIC(5,2) NOT NULL,
    min_amount NUMERIC(19,2) NOT NULL,
    max_amount NUMERIC(19,2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    active BOOLEAN NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Optionnel mais fortement recommandé : une seule config active
CREATE UNIQUE INDEX ux_fee_configuration_active
ON fee_configuration (active)
WHERE active = true;
