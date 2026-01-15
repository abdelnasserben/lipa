package com.lipa.application.port.out;

import com.lipa.application.dto.CardSnapshot;

import java.util.Optional;

public interface PaymentCardPort {

    Optional<CardSnapshot> findByUid(String uid);
}
