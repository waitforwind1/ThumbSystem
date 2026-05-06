package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import generator.domain.Blog;
import generator.service.BlogService;
import generator.mapper.BlogMapper;
import org.springframework.stereotype.Service;

/**
* @author 22097
* @description 针对表【blog】的数据库操作Service实现
* @createDate 2026-05-03 18:52:07
*/
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
    implements BlogService{

}




