package com.lipa.application.port.out;

import com.lipa.application.dto.BackofficeTransactionSearchCriteria;
import com.lipa.application.dto.BackofficeTransactionSearchResult;

public interface TransactionSearchQueryPort {

    BackofficeTransactionSearchResult search(
            BackofficeTransactionSearchCriteria criteria,
            int limit,
            int offset
    );
}
