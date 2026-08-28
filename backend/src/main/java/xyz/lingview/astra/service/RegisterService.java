package xyz.lingview.astra.service;

import xyz.lingview.astra.dto.request.RegisterDTO;
import xyz.lingview.astra.dto.response.RegisterResponseDTO;

public interface RegisterService {
    RegisterResponseDTO register(RegisterDTO requestDTO);
}