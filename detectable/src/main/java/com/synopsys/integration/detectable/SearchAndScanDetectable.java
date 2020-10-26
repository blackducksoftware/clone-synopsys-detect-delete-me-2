package com.synopsys.integration.detectable;

import com.synopsys.integration.detectable.detectable.result.DetectableResult;

public abstract class SearchAndScanDetectable<T> extends Detectable {

    public SearchAndScanDetectable(final DetectableEnvironment environment) {
        super(environment);
    }

    public abstract T toMemento();

    public abstract DetectableResult fromMemento(T bucket);
}
