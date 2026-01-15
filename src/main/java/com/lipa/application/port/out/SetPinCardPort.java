package com.lipa.application.port.out;

import com.lipa.application.dto.SetPinCardSnapshot;
import com.lipa.application.dto.SetPinCardUpdate;

import java.util.Optional;

public interface SetPinCardPort {

    Optional<SetPinCardSnapshot> findByUid(String uid);

    void applyUpdate(SetPinCardUpdate update);
}
