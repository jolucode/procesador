package service.cloud.request.clientRequest.utils;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.Optional;

public class UsuarioCamposIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object o) throws HibernateException {
        Query namedQuery = session.createNamedQuery("Usuariocampos.findLastId");
        Optional optional = Optional.ofNullable(namedQuery.getSingleResult());
        if (optional.isPresent())
            return Integer.parseInt(String.valueOf(optional.get())) + 1;
        return 1;
    }

    @Override
    public boolean supportsJdbcBatchInserts() {
        return false;
    }
}
