package com.lipa.application.port.in;

import com.lipa.application.dto.BackofficeTransactionSearchCriteria;
import com.lipa.application.dto.BackofficeTransactionSearchResult;

public interface SearchTransactionsBackofficeUseCase {

    BackofficeTransactionSearchResult search(
            BackofficeTransactionSearchCriteria criteria,
            int limit,
            int offset
    );
}
