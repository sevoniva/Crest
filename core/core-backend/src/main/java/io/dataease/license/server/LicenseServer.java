package io.dataease.license.server;

import io.dataease.api.license.LicenseApi;
import io.dataease.api.license.dto.LicenseRequest;
import io.dataease.license.bo.F2CLicResult;
import io.dataease.license.manage.CoreLicManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/license")
public class LicenseServer implements LicenseApi {

    @Resource
    private CoreLicManage coreLicManage;

    @Override
    public F2CLicResult update(LicenseRequest request) {
        F2CLicResult result = new F2CLicResult();
        result.setStatus(F2CLicResult.Status.valid);
        return result;
    }

    @Override
    public F2CLicResult validate(LicenseRequest request) {
        F2CLicResult result = new F2CLicResult();
        result.setStatus(F2CLicResult.Status.valid);
        return result;
    }

    @Override
    public String version() {
        return coreLicManage.getVersion();
    }

    @Override
    public void revert() {
    }
}
