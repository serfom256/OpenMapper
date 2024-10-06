package com.openmapper.core.query.model;

import java.util.Map;

public class ModelSpecifications {

    private Map<String, Object> params = null;
    private Object generatedOptimisticLockValue = null;
    private Object previousOptimisticLockValue = null;
    private String optimisticLockName = null;

    public ModelSpecifications(Map<String, Object> params) {
        this.params = params;
    }

    public ModelSpecifications() {
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public Object getGeneratedOptimisticLockValue() {
        return generatedOptimisticLockValue;
    }

    public void setGeneratedOptimisticLockValue(Object generatedOptimisticLockValue) {
        this.generatedOptimisticLockValue = generatedOptimisticLockValue;
    }

    public String getOptimisticLockName() {
        return optimisticLockName;
    }

    public void setOptimisticLockName(String optimisticLockName) {
        this.optimisticLockName = optimisticLockName;
    }

    public Object getPreviousOptimisticLockValue() {
        return previousOptimisticLockValue;
    }

    public void setPreviousOptimisticLockValue(Object previousOptimisticLockValue) {
        this.previousOptimisticLockValue = previousOptimisticLockValue;
    }

    
}
