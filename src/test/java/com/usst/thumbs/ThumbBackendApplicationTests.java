package com.usst.thumbs;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.usst.thumbs.model.Blog;
import com.usst.thumbs.model.Comment;
import com.usst.thumbs.model.Favorite;
import com.usst.thumbs.model.InteractionEvent;
import com.usst.thumbs.model.Share;
import com.usst.thumbs.model.Thumb;
import com.usst.thumbs.model.User;
import com.usst.thumbs.service.BlogService;
import com.usst.thumbs.service.CommentService;
import com.usst.thumbs.service.FavoriteService;
import com.usst.thumbs.service.InteractionEventService;
import com.usst.thumbs.service.ShareService;
import com.usst.thumbs.service.ThumbService;
import com.usst.thumbs.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@SpringBootTest(properties = {
        "spring.task.scheduling.enabled=false",
        "thumbs.jobs.interaction-event-publish.enabled=false"
})
@AutoConfigureMockMvc
class ThumbBackendApplicationTests {

    @Resource
    private UserService userService;

    @Resource
    private BlogService blogService;

    @Resource
    private ThumbService thumbService;

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private CommentService commentService;

    @Resource
    private ShareService shareService;

    @Resource
    private InteractionEventService interactionEventService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
        
    @Resource
    private MockMvc mockMvc;

    @Resource
    private DataSource dataSource;

    @Resource
    private ObjectMapper objectMapper;
    
    @Test
    void testRegisterLoginAndSessionCookie() throws Exception {
        Assumptions.assumeTrue(canConnectToDatabase(), "Local MySQL is not reachable with application-local.yml credentials");

        String account = "codex_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String password = "codex123";

        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/user/register")
                            .param("account", account)
                            .param("password", password)
                            .param("checkPassword", password)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/user/login")
                            .param("account", account)
                            .param("password", password)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            assertThat(result.getResponse().getHeaders("Set-Cookie"))
                    .anyMatch(cookie -> cookie.startsWith("SESSION"));
            System.out.println("Session smoke test passed at " + LocalDateTime.now());
        } finally {
            userService.remove(new LambdaQueryWrapper<User>().eq(User::getAccount, account));
        }
    }

    @Test
    void testCoreContentInteractionFlow() throws Exception {
        Assumptions.assumeTrue(canConnectToDatabase(), "Local MySQL is not reachable with application-local.yml credentials");

        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String authorAccount = "codex_a_" + suffix;
        String actorAccount = "codex_b_" + suffix;
        String password = "codex123";
        String blogTitle = "codex_blog_" + suffix;
        Long blogId = null;
        Long authorId = null;
        Long actorId = null;

        try {
            Cookie authorSession = registerAndLogin(authorAccount, password);
            Cookie actorSession = registerAndLogin(actorAccount, password);
            authorId = userService.lambdaQuery().eq(User::getAccount, authorAccount).one().getId();
            actorId = userService.lambdaQuery().eq(User::getAccount, actorAccount).one().getId();

            assertSuccess(mockMvc.perform(post("/blog/add")
                            .cookie(authorSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "%s",
                                      "content": "codex integration content",
                                      "coverImage": "https://example.com/cover.png"
                                    }
                                    """.formatted(blogTitle)))
                    .andReturn());
            blogId = blogService.lambdaQuery().eq(Blog::getTitle, blogTitle).one().getId();

            MvcResult blogListResult = mockMvc.perform(get("/blog/getBlog")
                            .cookie(actorSession)
                            .param("pageNo", "1")
                            .param("pageSize", "10"))
                    .andReturn();
            assertSuccess(blogListResult);
            JsonNode blogList = objectMapper.readTree(blogListResult.getResponse().getContentAsString()).get("data");
            JsonNode createdBlogJson = null;
            for (JsonNode blogJson : blogList) {
                if (blogTitle.equals(blogJson.get("title").asText())) {
                    createdBlogJson = blogJson;
                    break;
                }
            }
            assertThat(createdBlogJson).isNotNull();
            assertThat(createdBlogJson.get("userId").asLong()).isEqualTo(authorId);

            assertSuccess(mockMvc.perform(post("/thumb/do")
                            .cookie(actorSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"blogid\":%d}".formatted(blogId)))
                    .andReturn());
            assertBusinessCode(mockMvc.perform(post("/thumb/do")
                            .cookie(actorSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"blogid\":%d}".formatted(blogId)))
                    .andReturn(), 409);
            assertSuccess(mockMvc.perform(post("/thumb/undo")
                            .cookie(actorSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"blogid\":%d}".formatted(blogId)))
                    .andReturn());

            assertSuccess(mockMvc.perform(post("/favorite/do")
                            .cookie(actorSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"blogId\":%d}".formatted(blogId)))
                    .andReturn());
            assertBusinessCode(mockMvc.perform(post("/favorite/do")
                            .cookie(actorSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"blogId\":%d}".formatted(blogId)))
                    .andReturn(), 400);
            assertSuccess(mockMvc.perform(post("/favorite/undo")
                            .cookie(actorSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"blogId\":%d}".formatted(blogId)))
                    .andReturn());

            assertSuccess(mockMvc.perform(post("/comment/add")
                            .cookie(actorSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "blogId": %d,
                                      "content": "codex comment"
                                    }
                                    """.formatted(blogId)))
                    .andReturn());
            Comment comment = commentService.lambdaQuery()
                    .eq(Comment::getBlogId, blogId)
                    .eq(Comment::getUserId, actorId)
                    .one();
            assertThat(comment).isNotNull();

            assertSuccess(mockMvc.perform(post("/comment/reply")
                            .cookie(authorSession)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "blogId": %d,
                                      "parentId": %d,
                                      "content": "codex reply"
                                    }
                                    """.formatted(blogId, comment.getId())))
                    .andReturn());
            assertSuccess(mockMvc.perform(get("/comment/page")
                            .param("blogId", blogId.toString())
                            .param("pageNo", "1")
                            .param("pageSize", "10"))
                    .andReturn());

            assertSuccess(mockMvc.perform(get("/message/unread/count")
                            .cookie(authorSession))
                    .andReturn());
            assertSuccess(mockMvc.perform(get("/message/page")
                            .cookie(authorSession)
                            .param("pageNo", "1")
                            .param("pageSize", "10"))
                    .andReturn());

            assertSuccess(mockMvc.perform(get("/hot/list")
                            .cookie(actorSession))
                    .andReturn());
            assertSuccess(mockMvc.perform(post("/share/share")
                            .cookie(actorSession)
                            .param("blogId", blogId.toString())
                            .param("url", "https://example.com/blog/" + blogId)
                            .param("path", "test"))
                    .andReturn());

            assertSuccess(mockMvc.perform(delete("/comment/{commentId}", comment.getId())
                            .cookie(actorSession))
                    .andReturn());
            assertSuccess(mockMvc.perform(post("/blog/{blogId}", blogId)
                            .cookie(authorSession))
                    .andReturn());
        } finally {
            cleanup(authorAccount, actorAccount, blogTitle, blogId, authorId, actorId);
        }
    }

    private Cookie registerAndLogin(String account, String password) throws Exception {
        assertSuccess(mockMvc.perform(post("/user/register")
                        .param("account", account)
                        .param("password", password)
                        .param("checkPassword", password)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn());
        MvcResult login = mockMvc.perform(post("/user/login")
                        .param("account", account)
                        .param("password", password)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        assertSuccess(login);
        Cookie sessionCookie = login.getResponse().getCookie("SESSION");
        assertThat(sessionCookie).isNotNull();
        return sessionCookie;
    }

    private void assertSuccess(MvcResult result) throws Exception {
        assertBusinessCode(result, 200);
    }

    private void assertBusinessCode(MvcResult result, int expectedCode) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(json.get("code").asInt())
                .as(result.getRequest().getMethod() + " " + result.getRequest().getRequestURI() + " -> " + json)
                .isEqualTo(expectedCode);
    }

    private void cleanup(String authorAccount, String actorAccount, String blogTitle, Long blogId, Long authorId, Long actorId) {
        if (blogId != null) {
            commentService.remove(new LambdaQueryWrapper<Comment>().eq(Comment::getBlogId, blogId));
            favoriteService.remove(new LambdaQueryWrapper<Favorite>().eq(Favorite::getBlogId, blogId));
            thumbService.remove(new LambdaQueryWrapper<Thumb>().eq(Thumb::getBlogId, blogId));
            shareService.remove(new LambdaQueryWrapper<Share>().like(Share::getUrl, "/blog/" + blogId));
            interactionEventService.remove(new LambdaQueryWrapper<InteractionEvent>().eq(InteractionEvent::getBlogId, blogId));
            blogService.remove(new LambdaQueryWrapper<Blog>().eq(Blog::getId, blogId));
            redisTemplate.delete("blog:detail:" + blogId);
            redisTemplate.delete("blog:thumb:count:" + blogId);
            redisTemplate.delete("blog:favorite:count:" + blogId);
        }
        blogService.remove(new LambdaQueryWrapper<Blog>().eq(Blog::getTitle, blogTitle));
        if (authorId != null) {
            redisTemplate.delete("message:unread:" + authorId);
            redisTemplate.delete("user:thumb:" + authorId);
            redisTemplate.delete("user:favorite:" + authorId);
        }
        if (actorId != null) {
            redisTemplate.delete("message:unread:" + actorId);
            redisTemplate.delete("user:thumb:" + actorId);
            redisTemplate.delete("user:favorite:" + actorId);
        }
        userService.remove(new LambdaQueryWrapper<User>().in(User::getAccount, authorAccount, actorAccount));
    }

    private boolean canConnectToDatabase() {
        try (Connection ignored = dataSource.getConnection()) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
