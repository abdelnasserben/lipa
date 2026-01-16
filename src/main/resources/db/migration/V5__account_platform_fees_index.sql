CREATE UNIQUE INDEX ux_account_platform_fees
ON account (display_name)
WHERE type = 'TECHNICAL'
  AND display_name = 'Platform Fees';
