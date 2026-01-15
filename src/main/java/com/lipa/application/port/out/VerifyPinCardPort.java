package com.lipa.application.port.out;

import com.lipa.application.dto.CardPinSnapshot;
import com.lipa.application.dto.VerifyPinCardUpdate;

import java.util.Optional;

public interface VerifyPinCardPort {

    Optional<CardPinSnapshot> findByUid(String uid);

    void applyUpdate(VerifyPinCardUpdate update);
}
