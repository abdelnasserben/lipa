package com.lipa.application.usecase;

import com.lipa.application.dto.BackofficeTransactionSearchCriteria;
import com.lipa.application.dto.BackofficeTransactionSearchResult;
import com.lipa.application.dto.PageRequest;
import com.lipa.application.port.in.SearchTransactionsBackofficeUseCase;
import com.lipa.application.port.out.TransactionSearchQueryPort;
import org.springframework.stereotype.Service;

@Service
public class SearchTransactionsBackofficeService implements SearchTransactionsBackofficeUseCase {

    private final TransactionSearchQueryPort queryPort;

    public SearchTransactionsBackofficeService(TransactionSearchQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public BackofficeTransactionSearchResult search(
            BackofficeTransactionSearchCriteria criteria,
            int limit,
            int offset
    ) {
        PageRequest page = PageRequest.of(limit, offset);
        return queryPort.search(criteria, page.limit(), page.offset());
    }
}
