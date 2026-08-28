package xyz.lingview.astra.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.lingview.astra.common.ApiResponse;
import xyz.lingview.astra.dto.request.RegisterDTO;
import xyz.lingview.astra.dto.response.RegisterResponseDTO;
import xyz.lingview.astra.service.RegisterService;

@RestController
@RequestMapping("/api")
@Slf4j
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    // 用户注册
    @PostMapping("/register")
    public ApiResponse<RegisterResponseDTO> register(@RequestBody @Valid RegisterDTO requestDTO) {
        try {
            RegisterResponseDTO data = registerService.register(requestDTO);
            return ApiResponse.success(data);
        } catch (RuntimeException e) {
            return ApiResponse.error(400, e.getMessage());
        } catch (Exception e) {
            log.error("注册过程中发生错误", e);
            return ApiResponse.error(500, "注册失败，请稍后再试");
        }
    }
}