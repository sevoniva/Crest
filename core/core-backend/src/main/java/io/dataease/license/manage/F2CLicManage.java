package io.dataease.license.manage;

import io.dataease.license.bo.F2CLicResult;

public interface F2CLicManage {
    F2CLicResult updateLicense(String product, String license);

    F2CLicResult validate();

    F2CLicResult validate(String product, String license);

    void revert();
}
