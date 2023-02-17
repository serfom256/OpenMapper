package com.openmapper.core.loader;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class OpenMapperContextLoader {

    private final List<? extends ComponentLoader> loaders;

    public OpenMapperContextLoader(List<ComponentLoader> loaders) {
        this.loaders = loaders;
    }

    @PostConstruct
    public void loadAll() {
        loaders.forEach(ComponentLoader::load);
    }
}
