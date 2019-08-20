package com.github.katsuyan.digdag.plugin.myjdbc;

import io.digdag.standards.operator.jdbc.AbstractJdbcResultSet;

import java.sql.ResultSet;

public class MyjdbcResultSet
        extends AbstractJdbcResultSet
{
    MyjdbcResultSet(ResultSet resultSet)
    {
        super(resultSet);
    }

    @Override
    protected Object serializableObject(Object raw)
    {
        // TODO add more conversion logics here. MySQL jdbc may return objects that are not serializable using Jackson.
        return raw;
    }

}
