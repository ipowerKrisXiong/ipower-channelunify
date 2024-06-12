package com.ipower.cloud.channelunify.controller;


import cn.need.framework.common.support.api.Result;
import com.ipower.cloud.channelunify.application.SampleOrderManageService;
import com.ipower.cloud.channelunify.application.dto.SampleOrderDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "示例订单接口")
@RequestMapping("/api/order/order")
@Validated//加在这儿才能对Get的参数判断生效
@RestController
public class SampleController {

    @Autowired
    SampleOrderManageService sampleOrderManageService;


    @Operation(summary = "查询示例订单详情", description = "查询示例订单详情")
    @PostMapping(value = "/detail")
    public Result<SampleOrderDTO> detail(
            @Parameter(description = "订单ID", required = true)
            @RequestParam String orderId) {
        return Result.ok(sampleOrderManageService.detail(orderId));
    }

    @Operation(summary = "新增示例订单", description = "新增示例订单")
    @PostMapping(value = "/add-auto")
    public Result<SampleOrderDTO> addAuto() {
        return Result.ok(sampleOrderManageService.addAuto());
    }


}
