package xyz.lingview.astra.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import xyz.lingview.astra.domain.UploadAttachment;

@Mapper
@Repository
public interface UploadMapper {
    int insertUploadAttachment(UploadAttachment uploadAttachment);
    UploadAttachment selectByAccessKey(@Param("accessKey") String accessKey);
}