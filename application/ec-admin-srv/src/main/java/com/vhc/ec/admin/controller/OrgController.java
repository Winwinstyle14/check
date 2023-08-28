package com.vhc.ec.admin.controller;

import com.vhc.ec.admin.dto.*;

import com.vhc.ec.admin.service.OrganizationService;
import com.vhc.ec.api.versioning.ApiVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@ApiVersion("1")
@RestController
@RequestMapping("/admin/organization")
@RequiredArgsConstructor
public class OrgController {
    private final OrganizationService orgService;

    @GetMapping
    public PageDto<OrgViewDto> search(@RequestParam(required = false) String name,
                                      @RequestParam(required = false) String address,
                                      @RequestParam(required = false) String representative,
                                      @RequestParam(required = false) String email,
                                      @RequestParam(required = false) String phone,
                                      @RequestParam(required = false) String code,
                                      @RequestParam(required = false, defaultValue = "-1") Integer status,
                                      Pageable pageable) {

        return orgService.search(name, address, representative, email, phone, status, code, pageable);

    }

    @GetMapping("/{id}")
    public OrgDetailDto getDetail(@PathVariable int id) {
        return orgService.getDetail(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrgViewDto add(@Valid @RequestBody SaveOrgReq saveOrgReq) {
        return orgService.add(saveOrgReq);
    }

    @PutMapping("/{id}")
    public OrgViewDto edit(@PathVariable int id, @Valid @RequestBody SaveOrgReq saveOrgReq) {
        return orgService.edit(id, saveOrgReq);
    }

    @PatchMapping("/{id}")
    public OrgViewDto active(@PathVariable int id) {
        return orgService.active(id);
    }

    @GetMapping("/{id}/service/detail/{serviceId}")
    public OrgServiceDetailDto getServiceDetail(@PathVariable int id,
                                                @PathVariable long serviceId) {

        return orgService.getServiceDetail(id, serviceId);
    }

    @PatchMapping("/{id}/service/register")
    public OrgDetailDto registerService(@PathVariable int id,
                                        @RequestBody OrgRegisterServiceDto registerServiceDto) {

        return orgService.registerService(id, registerServiceDto);
    }

    @PatchMapping("/{id}/service/cancel/{serviceId}")
    public void cancelService(@PathVariable int id, @PathVariable long serviceId) {
        orgService.cancelService(id, serviceId);
    }

    @DeleteMapping("/{id}")
    public void deleteOrganization(@PathVariable int id) {
        orgService.deleteOrganization(id);
    }

    // cap nhat thoi gian bat dau su dung dich vu
    @PatchMapping("/internal/{id}/service/update-start-date")
    public void updateStartTimeUsing(@PathVariable int id) {
        orgService.updateStartTimeUsing(id);
    }

}
