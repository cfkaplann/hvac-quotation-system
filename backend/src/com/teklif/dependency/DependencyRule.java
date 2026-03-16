package com.teklif.dependency;

import java.util.List;

import com.teklif.model.OlcuAlanTipi;

public class DependencyRule {

    private final OlcuAlanTipi source;
    private final String sourceValue;

    private final OlcuAlanTipi target;
    private final List<String> allowedValues;

    public DependencyRule(OlcuAlanTipi source,
                          String sourceValue,
                          OlcuAlanTipi target,
                          List<String> allowedValues){

        this.source = source;
        this.sourceValue = sourceValue;
        this.target = target;
        this.allowedValues = allowedValues;
    }

    public OlcuAlanTipi getSource(){ return source; }
    public String getSourceValue(){ return sourceValue; }

    public OlcuAlanTipi getTarget(){ return target; }
    public List<String> getAllowedValues(){ return allowedValues; }
}
