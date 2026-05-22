package io.dataease.license.manage;

import io.dataease.license.bo.F2CLicResult;

public class F2CLicManage {
    public F2CLicResult updateLicense(String product, String license) {
        F2CLicResult result = new F2CLicResult();
        result.setStatus(F2CLicResult.Status.valid);
        return result;
    }

    public F2CLicResult validate() {
        F2CLicResult result = new F2CLicResult();
        result.setStatus(F2CLicResult.Status.valid);
        return result;
    }

    public F2CLicResult validate(String product, String license) {
        return validate();
    }

    public void revert() {
    }
}
