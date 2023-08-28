package com.vhc.ec.admin.controller;

import com.vhc.ec.admin.dto.*;
import com.vhc.ec.admin.service.ServicePackageService;
import com.vhc.ec.api.versioning.ApiVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@ApiVersion("1")
@RestController
@RequestMapping("/admin/service-package")
@RequiredArgsConstructor
public class ServicePackageController {
    private final ServicePackageService servicePackageService;

    @GetMapping
    public PageDto<ServicePackageView> getList(@RequestParam(required = false) String code,
                                               @RequestParam(required = false) String name,
                                               @RequestParam(required = false, defaultValue = "-1") Long totalBeforeVAT,
                                               @RequestParam(required = false, defaultValue = "-1") Long totalAfterVAT,
                                               @RequestParam(required = false, defaultValue = "-1") Integer duration,
                                               @RequestParam(required = false, defaultValue = "-1") Integer numberOfContracts,
                                               @RequestParam(required = false, defaultValue = "-1") Integer status,
                                               Pageable pageable) {

        return servicePackageService.getList(code, name, totalBeforeVAT, totalAfterVAT,
                duration, numberOfContracts, status, pageable);
    }

    @GetMapping("/{id}")
    public ServicePackageDetailDto getDetail(@PathVariable long id) {
        return servicePackageService.getDetail(id);
    }

    @GetMapping("/codes")
    public List<ServicePackageView> getAllCodes() {
        return servicePackageService.getAllCodes();
    }

    @PostMapping
    public ServicePackageView add(@Valid @RequestBody SaveServicePackageDto saveServicePackageDto) {
        return servicePackageService.add(saveServicePackageDto);
    }

    @PutMapping("/{id}")
    public ServicePackageView edit(@PathVariable long id,
            @Valid @RequestBody SaveServicePackageDto saveServicePackageDto) {

        return servicePackageService.edit(id, saveServicePackageDto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        servicePackageService.delete(id);
    }
}
