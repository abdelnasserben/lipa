package com.lipa.application.dto;

import com.lipa.domain.model.Account;
import com.lipa.domain.model.Card;

public record EnrollCardPersistCommand(
        Account account,
        Card card
) {
}
