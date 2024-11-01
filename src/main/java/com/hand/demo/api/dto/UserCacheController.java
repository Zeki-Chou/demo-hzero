package com.hand.demo.api.dto;


import com.hand.demo.api.dto.UserCacheDTO;
import com.hand.demo.app.service.UserCacheService;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.ApiOperation;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userCacheController.v1")
@RequestMapping("/v1/{organizationId}/userCache")
public class UserCacheController extends BaseController {
    @Autowired

    private UserCacheService service;

    @ApiOperation(value = "User Id")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{id}")
    public ResponseEntity<UserCacheDTO> getUserById(@PathVariable Long id, @PathVariable Long organizationId) {
        UserCacheDTO user = service.getUserFromRedis(id);
        return Results.success(user);
    }
}
