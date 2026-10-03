package com.events.infrastructure.adapter.out.persistence;

import com.events.application.port.out.TransactionPort;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class SpringTransactionAdapter implements TransactionPort {
    private final TransactionTemplate template;
    public SpringTransactionAdapter(PlatformTransactionManager manager) { template = new TransactionTemplate(manager); }
    @Override
    public <T> T execute(Supplier<T> action) { return template.execute(status -> action.get()); }
}
