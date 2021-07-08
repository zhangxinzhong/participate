package com.participate.web.repository;

import com.participate.web.annotation.Repository;
import com.participate.web.model.Hello;

/**
 * hello repository
 *
 * @author <a href="mailto:xinzhong.zhang@happy-seed.com">ZhangXinZhong</a>
 * Date: 2021/7/7
 * Time: 20:38
 * @since 1.0.0
 */
@Repository
public class HelloRepository implements  CrudRepository<Hello> {
}
