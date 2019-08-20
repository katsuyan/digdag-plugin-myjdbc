package com.github.katsuyan.digdag.plugin.myjdbc;

import io.digdag.client.config.Config;
import io.digdag.spi.OperatorFactory;
import io.digdag.spi.OperatorProvider;
import io.digdag.spi.Plugin;
import io.digdag.spi.TemplateEngine;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

public class MyjdbcPlugin implements Plugin {
    @Override
    public <T> Class<? extends T> getServiceProvider(Class<T> type) {
        if (type == OperatorProvider.class) {
            return MyjdbcOperatorProvider.class.asSubclass(type);
        } else {
            return null;
        }
    }

    public static class MyjdbcOperatorProvider implements OperatorProvider {
        @Inject
        protected TemplateEngine templateEngine;

        @Inject
        protected Config systemConfig;

        @Override
        public List<OperatorFactory> get() {
            return Arrays.asList(new MyjdbcOperatorFactory(systemConfig,templateEngine));
        }
    }
}
