package xyz.lingview.astra.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import xyz.lingview.astra.domain.Login;

@Mapper
@Repository
public interface LoginMapper {
    Login loginUser(Login login);
}