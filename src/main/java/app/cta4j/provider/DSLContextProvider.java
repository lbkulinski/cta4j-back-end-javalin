package app.cta4j.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.util.Objects;

public final class DSLContextProvider implements Provider<DSLContext> {
    private final HikariDataSource dataSource;

    @Inject
    public DSLContextProvider(HikariDataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource);
    }

    @Override
    public DSLContext get() {
        return DSL.using(this.dataSource, SQLDialect.POSTGRES);
    }
}
