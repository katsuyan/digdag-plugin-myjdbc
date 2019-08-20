package com.github.katsuyan.digdag.plugin.myjdbc;

import io.digdag.client.config.Config;
import io.digdag.spi.Operator;
import io.digdag.spi.OperatorContext;
import io.digdag.spi.OperatorFactory;
import io.digdag.spi.SecretProvider;
import io.digdag.spi.TemplateEngine;
import io.digdag.standards.operator.jdbc.AbstractJdbcJobOperator;

public class MyjdbcOperatorFactory
        implements OperatorFactory
{
    private static final String OPERATOR_TYPE = "myjdbc";
    private final Config systemConfig;
    private final TemplateEngine templateEngine;

    public MyjdbcOperatorFactory(Config systemConfig,TemplateEngine templateEngine)
    {
        this.templateEngine = templateEngine;
        this.systemConfig = systemConfig;
    }

    @Override
    public String getType()
    {
        return OPERATOR_TYPE;
    }

    @Override
    public Operator newOperator(OperatorContext context)
    {
        return new MyjdbcOperator(systemConfig,context,templateEngine);
    }

    static class MyjdbcOperator
            extends AbstractJdbcJobOperator<MyjdbcConnectionConfig>
    {
        MyjdbcOperator(Config systemConfig,OperatorContext context, TemplateEngine templateEngine)
        {
            super(systemConfig,context, templateEngine);
        }

        @Override
        protected MyjdbcConnectionConfig configure(SecretProvider secrets, Config params)
        {
            return MyjdbcConnectionConfig.configure(secrets, params);
        }

        @Override
        protected MyjdbcConnection connect(MyjdbcConnectionConfig connectionConfig)
        {
            return MyjdbcConnection.open(connectionConfig);
        }

        @Override
        protected String type()
        {
            return OPERATOR_TYPE;
        }

        @Override
        protected SecretProvider getSecretsForConnectionConfig()
        {
            return context.getSecrets().getSecrets(type());
        }
    }
}
