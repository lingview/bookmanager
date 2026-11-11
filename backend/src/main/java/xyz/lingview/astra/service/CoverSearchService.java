package xyz.lingview.astra.service;

import xyz.lingview.astra.dto.response.CoverCandidateDTO;

import java.util.List;

public interface CoverSearchService {

    List<CoverCandidateDTO> search(String keyword, int limit);
}