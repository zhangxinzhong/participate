package com.participate.web.repository;


import com.participate.web.annotation.Repository;
import com.participate.web.model.User;

import java.io.Serializable;

/**
 * 实现 {@link CrudRepository} 的用户仓储
 *
 * @author: zhangxinzhong
 * @since: 1.0.0
 * @version: JDK8
 * @create: 2021-01-25 10:16
 **/
@Repository
public class UserRepository implements Comparable<UserRepository>, // getGenericInterfaces[0] = ParameterizedType ->
        // ParameterizedType.rawType = Comparable
        // ParameterizedType.getActualTypeArguments()[0] = UserRepository

        CrudRepository<User>,      // getGenericInterfaces[1] = ParameterizedType -> ParameterizedType.rawType = CrudRepository
        // ParameterizedType.rawType = CrudRepository
        // ParameterizedType.getActualTypeArguments()[0] = User

        Serializable {             // getGenericInterfaces[2] = Class -> isInterface() == true

    @Override
    public int compareTo(UserRepository o) {
        return 0;
    }

}
