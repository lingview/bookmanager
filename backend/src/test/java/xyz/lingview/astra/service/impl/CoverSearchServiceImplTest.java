package xyz.lingview.astra.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import xyz.lingview.astra.dto.response.CoverCandidateDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class CoverSearchServiceImplTest {

    @InjectMocks
    private CoverSearchServiceImpl coverSearchService;

    @Test
    void parseBaidu_shouldPreferMiddleUrl() {
        String json = """
                {"queryEnc":"","listNum":2,"data":[
                  {"thumbURL":"https://img0.baidu.com/it/thumb-a","middleURL":"https://img0.baidu.com/it/mid-a","objURL":"https://example.com/a.jpg"},
                  {"thumbURL":"https://img1.baidu.com/it/thumb-b","middleURL":"","objURL":"https://example.com/b.jpg"},
                  {}
                ]}
                """;

        List<CoverCandidateDTO> result = coverSearchService.parseBaidu(json);

        assertEquals(2, result.size());
        assertEquals("https://img0.baidu.com/it/mid-a", result.get(0).getImage_url());
        assertEquals("https://img0.baidu.com/it/thumb-a", result.get(0).getThumb_url());
        assertEquals("baidu", result.get(0).getSource());
        assertEquals("https://example.com/b.jpg", result.get(1).getImage_url());
    }

    @Test
    void parseBaidu_shouldReturnEmptyForEmptyData() {
        assertEquals(0, coverSearchService.parseBaidu("{\"data\":[{}]}").size());
        assertEquals(0, coverSearchService.parseBaidu("").size());
        assertEquals(0, coverSearchService.parseBaidu("not-json").size());
    }

    @Test
    void parseBing_shouldUnescapeAndFilterNonHttp() {
        String html = "murl&quot;:&quot;https:\\/\\/img.example.com\\/a.jpg&quot;"
                + " murl&quot;:&quot;ftp://bad&quot;"
                + " murl&quot;:&quot;https:\\/\\/img.example.com\\/b.jpg&quot;";

        List<CoverCandidateDTO> result = coverSearchService.parseBing(html);

        assertEquals(2, result.size());
        assertEquals("https://img.example.com/a.jpg", result.get(0).getImage_url());
        assertEquals("https://img.example.com/b.jpg", result.get(1).getImage_url());
        assertEquals("bing", result.get(0).getSource());
    }

    @Test
    void parseBing_shouldReturnEmptyForBlankHtml() {
        assertTrue(coverSearchService.parseBing(null).isEmpty());
        assertTrue(coverSearchService.parseBing("").isEmpty());
        assertTrue(coverSearchService.parseBing("<html>no images</html>").isEmpty());
    }
}