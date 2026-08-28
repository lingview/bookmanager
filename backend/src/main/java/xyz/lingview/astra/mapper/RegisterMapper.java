package xyz.lingview.astra.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import xyz.lingview.astra.domain.Register;

@Mapper
@Repository
public interface RegisterMapper {
    int selectUser(String username);
    int insertUser(Register register);

}