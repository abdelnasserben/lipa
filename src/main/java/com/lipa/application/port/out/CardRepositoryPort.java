package com.lipa.application.port.out;

import com.lipa.domain.model.Account;
import com.lipa.domain.model.Card;

import java.util.Optional;

public interface CardRepositoryPort {

    boolean existsByUid(String uid);

    Optional<Card> findByUid(String uid);

    /**
     * Persiste un nouvel account + une nouvelle carte (enroll).
     * (On garde cette granularité pour minimiser le changement ; extraction possible plus tard.)
     */
    void createAccountAndCard(Account account, Card card);

    /**
     * Persiste l'état de la carte (PIN state / status / etc.).
     */
    void save(Card card);
}
