package com.lipa.application.port.out;

import com.lipa.application.dto.EnrollCardPersistCommand;
import com.lipa.application.dto.EnrollCardPersistResult;

public interface EnrollCardPersistencePort {

    EnrollCardPersistResult persist(EnrollCardPersistCommand command);
}
