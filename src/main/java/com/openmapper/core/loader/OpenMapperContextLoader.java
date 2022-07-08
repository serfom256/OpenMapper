package com.openmapper.core.loader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class OpenMapperContextLoader {

    private final List<? extends ComponentLoader> loaders;

    @Autowired
    public OpenMapperContextLoader(List<ComponentLoader> loaders) {
        this.loaders = loaders;
    }

    @PostConstruct
    private void loadAll() {
        loaders.forEach(ComponentLoader::load);
    }
}
