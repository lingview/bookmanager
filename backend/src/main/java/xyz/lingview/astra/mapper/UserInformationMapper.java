package xyz.lingview.astra.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import xyz.lingview.astra.domain.UserInformation;

@Mapper
@Repository
public interface UserInformationMapper {
    String selectUserUUID(@Param("username") String username);
    UserInformation selectUserByUUID(@Param("uuid") String uuid);
    String selectUsernameByUUID(@Param("uuid") String uuid);
    String getUsernameByUuid(@Param("uuid") String uuid);
    UserInformation getUserByUsername(@Param("username") String username);
    String getEmailByUsername(@Param("username") String username);
    int selectUserByPhone(@Param("phone") String phone);
    int selectUserByEmail(@Param("email") String email);
    UserInformation getUserByUsernameAndEmail(@Param("username") String username, @Param("email") String email);
    int updatePasswordByUsername(@Param("username") String username, @Param("password") String password);
    int updateUserByUUID(UserInformation userInformation);

    java.util.List<UserInformation> selectUsersPage(@Param("offset") int offset, @Param("limit") int limit);

    int countUsers();
}