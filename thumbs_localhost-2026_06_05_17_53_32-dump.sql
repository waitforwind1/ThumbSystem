-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: thumbs
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `blog`
--

DROP TABLE IF EXISTS `blog`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `blog` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文章id',
  `user_id` bigint NOT NULL COMMENT '作者id',
  `title` varchar(512) NOT NULL COMMENT '文章名称',
  `content` longtext NOT NULL COMMENT '文章内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `thumb_count` bigint NOT NULL DEFAULT '0' COMMENT '点赞数',
  `cover_image` varchar(512) NOT NULL COMMENT '封面',
  `comment_count` bigint NOT NULL DEFAULT '0' COMMENT '被评论数',
  `favorite_count` bigint NOT NULL DEFAULT '0' COMMENT '被收藏数',
  `share_count` bigint NOT NULL DEFAULT '0' COMMENT '被分享次数',
  `hot_score` double NOT NULL DEFAULT '0',
  `view_count` bigint NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '业务状态 0-未发布  1-已发布 2-审核中  3-审核不通过 4-已下架  ',
  `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除字段',
  `category` varchar(64) DEFAULT NULL COMMENT '分类',
  `tag` varchar(256) DEFAULT NULL COMMENT '标签',
  `summary` varchar(256) DEFAULT NULL COMMENT '摘要',
  PRIMARY KEY (`id`),
  UNIQUE KEY `blog_user_id_title_uindex` (`user_id`,`title`) COMMENT '唯一索引 同一个用户不能有多个相同的标题文章',
  KEY `status_create_time` (`status`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `blog`
--

LOCK TABLES `blog` WRITE;
/*!40000 ALTER TABLE `blog` DISABLE KEYS */;
INSERT INTO `blog` VALUES (5,1,'helo world','全世界谁倾听你','2026-05-31 16:38:24','2026-05-31 16:38:24',0,'evuwbhjnkl',4,1,2,-16,1,1,1,NULL,NULL,NULL),(6,1,'第二章博客','奶龙传说','2026-05-31 16:39:45','2026-05-31 16:39:45',2,'evuwbhjnkl',2,0,1,-28,2,1,1,NULL,NULL,NULL),(7,1,'小说第三章','莫欺奶龙穷','2026-05-31 16:40:16','2026-05-31 16:40:16',1,'23gywibjnkm',2,2,1,-20,3,1,1,NULL,NULL,NULL),(8,2,'cdx_blog_b9d7f740','http content','2026-05-31 22:10:15','2026-05-31 22:10:15',1,'https://example.com/cover.png',1,1,1,-28.5,1,4,0,NULL,NULL,NULL),(9,4,'cdx_blog_a791ceb1','http content','2026-05-31 22:15:03','2026-05-31 22:15:03',0,'https://example.com/cover.png',1,0,1,3,0,1,1,NULL,NULL,NULL),(10,7,'我是神','被v汝等所不及口碑v不是哦紧迫辅导课程本身‘【2维克多v还是科博会哦【工人金额VS的vi过后IP解耦【快乐v的哦hi的VS IP你看了吗','2026-06-02 00:26:08','2026-06-02 00:26:08',0,'',2,0,0,-25,1,1,1,NULL,NULL,NULL),(11,1,'詹姆斯下快攻啦','','2026-06-02 12:27:58','2026-06-02 12:27:58',0,'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRA3roEMMZRSNqs7celP7vX-9EBmZrYfdtnQPBFDM9b_GmmPKQuFAPt2aicJh5QxL5n1Gz3VAsYigO-kCbsBuBA1BblIQ6XV6FaD5oKlx32OA&s=10',0,0,0,-27.5,1,1,0,'1111111','redis','哈哈哈哈哈哈哈哈哈哈哈'),(12,7,'我是faker 入住此社区啦','我就是LPL的神','2026-06-02 12:30:06','2026-06-02 12:30:06',1,'https://images.pexels.com/photos/37537468/pexels-photo-37537468.jpeg',2,0,1,-7.5,4,1,0,'game','','wuwuwuwwuuuw'),(13,1,'这是一篇技术类别的文章博客','不要做成 AI 生成感很强的官网首页。\n不要使用大面积渐变背景。\n不要使用夸张玻璃拟态。\n不要把卡片做得过大。\n不要只做静态展示，要体现点赞、评论、收藏、热榜这些真实交互状态。','2026-06-02 15:41:31','2026-06-02 15:41:31',1,'https://images.pexels.com/photos/18193711/pexels-photo-18193711.jpeg',0,0,0,-23,1,1,0,'技术','redis,zset','需要解决热榜的一些问题 并且还有标签以及分类设置的东西'),(14,1,'写一篇真正能被读懂的内容','这是一个学习的博客写一篇真正能被读懂的内容写一篇真正能被读懂的内容写一篇真正能被读懂的内容写一篇真正能被读懂的内容','2026-06-02 15:46:44','2026-06-02 15:46:44',1,'https://images.pexels.com/photos/37475357/pexels-photo-37475357.jpeg',0,0,0,-23,1,1,0,'学习','vue,springboot','这是一个学习的博客'),(15,1,'redis缓存错误','本质是：\n\nRedis Hash 的 field 使用了 Long 类型，\n但 StringRedisSerializer 只能序列化 String，\n所以 Long 转 String 失败。\n\n你现在重点检查：\n\nThumbServiceImpl.hasThumb 第 123 行\n\n把里面的 blogId 改成：\n\nString.valueOf(blogId)\n\n同时把点赞、取消点赞、收藏、取消收藏里所有 Hash field 的 blogId 都统一转成字符串。','2026-06-02 15:49:32','2026-06-02 15:49:32',1,'https://images.pexels.com/photos/27364222/pexels-photo-27364222.jpeg',5,2,5,44,8,1,0,'技术','redis,java','解决 Redis Hash 的 field 类型传错导致的类型转换错误'),(16,1,'11','1111','2026-06-02 15:53:44','2026-06-02 15:53:44',0,'https://images.pexels.com/photos/29041985/pexels-photo-29041985.jpeg',0,0,0,-26,1,1,0,'111','','111'),(17,1,'2222','22222','2026-06-02 15:53:54','2026-06-02 15:53:54',0,'https://images.pexels.com/photos/29265319/pexels-photo-29265319.jpeg',0,0,0,-26,1,1,0,'2222','222','222'),(18,1,'3333','33333','2026-06-02 15:54:04','2026-06-02 15:54:04',0,'https://images.pexels.com/photos/37654010/pexels-photo-37654010.jpeg',0,0,0,-27,0,1,0,'333','3333','3333'),(19,1,'4444','44444','2026-06-02 15:54:17','2026-06-02 15:54:17',0,'https://images.pexels.com/photos/19888754/pexels-photo-19888754.jpeg',0,0,0,-27,0,1,0,'4444','444','4444'),(20,1,'555','55555','2026-06-02 15:54:27','2026-06-02 15:54:27',0,'https://images.pexels.com/photos/37759209/pexels-photo-37759209.jpeg',0,0,0,-26,1,1,0,'55555','555','55'),(21,1,'网络OSI模型和TCP/IP模型分别介绍一下','网络OSI模型和TCP/IP模型分别介绍一下','2026-06-02 15:57:33','2026-06-02 15:57:33',0,'https://images.pexels.com/photos/37523788/pexels-photo-37523788.jpeg',0,0,0,-24,3,1,0,'计算机网络','计算机网络,OSI','由于 OSI 模型实在太复杂，提出的也只是概念理论上的分层，并没有提供具体的实现方案。'),(22,1,'https://images.pexels.com/photos/37106270/pexels-photo-37106270.jpeg','111111','2026-06-02 20:06:55','2026-06-02 20:06:55',1,'https://images.pexels.com/photos/37106270/pexels-photo-37106270.jpeg',0,0,0,4,3,1,1,'','',''),(23,7,'我是gggggg','把中间的发现内容 刷新写文章删掉，左边的频道也删掉，把右下角的我的社区那个模块去掉，完全不需要，右上角的退出按钮太大了，前端设计成左边是每个文章模块 每个矩形是一个模块横排和竖排结合 一排可以有三四个矩形文章模块，底部有每个作者头像名字点赞收藏评论数目文章创建时间，整个文章模块按照分页方式查询，总体页面大小是固定的，大概一个页面显示9条左右 均匀排列，底部是多个页面；右边应该是是热榜模块，热榜下面是另一个多标签模块，以随机的位置再矩形模块里对这些标签组合，点击不同的标签可以查看多个属于该标签的文章，也就是根据标签查找文章，可以在主页展示  先修改这些内容','2026-06-02 21:12:19','2026-06-02 21:12:19',1,'https://images.pexels.com/photos/37895963/pexels-photo-37895963.jpeg',0,0,0,-18,3,1,0,'生活','标签','把中间的发现内容'),(24,8,'gwefsa','而网上查查v热动i内要二百多v不是不能离开','2026-06-02 22:25:20','2026-06-02 22:25:20',0,'https://images.pexels.com/photos/35922317/pexels-photo-35922317.jpeg',5,0,0,-0.5,3,1,0,'vsd','vSD','VDsX'),(25,9,'总决赛回忆录','最后两分钟，我能听见自己的呼吸，也能听见甲骨文球馆里那种压得人胸口发紧的嘈杂声。\n\n比分咬得太死了，每一个回合都像是在决定整个赛季的命运。我们从1比3落后追到第七场，已经没有退路了。那一刻，我脑子里没有什么复杂的战术分析，只有一个念头：必须把球打进，必须把这座城市带回家。\n\n我看着勒布朗。他已经拼到极限了，但眼神还是很坚定。我知道，他相信我。整个系列赛，我们承受了太多质疑：勇士73胜，他们是历史级球队；我们落后过，我们被看低过，可我们还站在这里。最后两分钟，所有声音都变得很远，场上只剩下我、球、篮筐，还有防在我面前的人。\n\n当我持球面对库里时，我没有犹豫。那一刻不是冲动，而是训练里重复过无数次的本能。我运球，找节奏，后撤，起跳。球离开手指的一瞬间，我心里异常安静。我知道这球有机会。它划过空中，时间像被拉长了一样。然后，球进了。\n\n那不是一个普通的三分。那一球里面有克利夫兰五十多年的等待，有我们整个赛季的挣扎，有所有人不相信我们时憋在心里的那口气。进球之后，我没有立刻狂喜，因为比赛还没结束。越是这种时候，越不能松。勇士还有库里，还有汤普森，还有他们改变比赛的能力。\n\n最后的防守、篮板、罚球，每一秒都很漫长。直到终场哨响，我才真正意识到：我们做到了。我们完成了别人认为不可能的事情。\n\n那一刻，我不是只为自己骄傲。我为勒布朗骄傲，为队友骄傲，也为克利夫兰骄傲。那座城市等了太久，而我们终于把冠军带回去了。2016年总决赛最后两分钟，是我职业生涯最清醒、最紧张，也最值得铭记的两分钟。','2026-06-04 22:01:59','2026-06-04 22:01:59',1,'https://images.pexels.com/photos/35357579/pexels-photo-35357579.png',1,1,0,11,1,1,0,'体育','NBA','2016Final最后两分钟');
/*!40000 ALTER TABLE `blog` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment`
--

DROP TABLE IF EXISTS `comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '评论用户ID',
  `blog_id` bigint NOT NULL COMMENT '博客id',
  `reply_user_id` bigint DEFAULT NULL COMMENT '被回复的用户的ID，用于快速查找当前用户的评论回复，主要是查询方便',
  `root_id` bigint NOT NULL DEFAULT '0' COMMENT '评论树的根部ID  ',
  `parent_id` bigint DEFAULT NULL COMMENT '直系父亲ID',
  `content` varchar(1024) NOT NULL COMMENT '评论内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建评论时间',
  `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除字段 0-保留 1-删除',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '增加状态避免评论数断裂 0-正常 1-删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='保存用户评论';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment`
--

LOCK TABLES `comment` WRITE;
/*!40000 ALTER TABLE `comment` DISABLE KEYS */;
INSERT INTO `comment` VALUES (7,1,5,NULL,0,0,'三十年河东 三十年河西','2026-05-31 16:41:05',0,0),(8,1,5,1,7,7,'三年之约已至，奶龙如约来到了云岚宗','2026-05-31 16:45:07',0,0),(9,1,5,1,7,7,'三年之约已至，奶龙如约来到了云岚宗','2026-05-31 16:45:19',0,0),(10,1,5,1,7,7,'三年之约已至，奶龙如约来到了云岚宗','2026-05-31 16:47:33',0,0),(11,1,6,NULL,0,0,'Bug 修复报告','2026-05-31 21:40:32',0,0),(12,1,6,NULL,0,0,'Bug 修复报告','2026-05-31 21:41:05',0,0),(13,3,8,NULL,0,0,'http comment','2026-05-31 22:10:16',0,0),(14,5,9,NULL,0,0,'http comment','2026-05-31 22:15:03',0,0),(15,7,10,NULL,0,0,'你是gb','2026-06-02 00:26:18',0,0),(16,1,10,NULL,0,0,'阿里阿里巴巴阿里巴巴是个快乐的青年','2026-06-02 00:28:24',0,0),(17,7,7,NULL,0,0,'为什么热度还有负值','2026-06-02 15:37:33',0,0),(18,7,7,7,17,17,'因为没有相应的数值校验','2026-06-02 15:37:49',0,0),(19,1,15,NULL,0,0,'为什么会出现这个错误呢？？？','2026-06-02 15:51:08',0,0),(20,1,15,1,19,19,'缓存字段类型设置错误','2026-06-02 15:51:21',0,0),(21,8,15,NULL,0,0,'tuvyibunokm;l','2026-06-02 22:22:59',0,0),(22,7,15,8,21,21,'nihaonihao','2026-06-04 20:59:44',0,0),(23,7,15,8,21,21,'你好','2026-06-04 20:59:49',0,0),(24,1,12,NULL,0,0,'怎么设计二级索引呢？？','2026-06-04 21:28:42',0,0),(25,1,12,1,24,24,'不是二级索引 而是二级评论  多级评论 但是redis只是缓存两级评论 一个根评论，一个次级评论','2026-06-04 21:29:48',0,0),(26,1,24,NULL,0,0,'你好','2026-06-04 21:56:00',0,0),(27,1,24,1,26,26,'你好我是詹姆斯','2026-06-04 21:56:10',0,0),(28,1,24,1,26,27,'詹姆斯是gb','2026-06-04 21:56:20',0,0),(29,8,24,1,26,28,'哈哈哈哈哈哈 nb','2026-06-04 21:56:46',0,0),(30,9,24,1,26,27,'詹姆斯 我是欧文','2026-06-04 21:58:02',0,0),(31,9,25,NULL,0,0,'MVP!','2026-06-04 22:02:20',0,0);
/*!40000 ALTER TABLE `comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `favorite`
--

DROP TABLE IF EXISTS `favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `blog_id` bigint DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `favorite_blog_id_user_id_uindex` (`blog_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `favorite`
--

LOCK TABLES `favorite` WRITE;
/*!40000 ALTER TABLE `favorite` DISABLE KEYS */;
INSERT INTO `favorite` VALUES (16,1,7,'2026-05-31 14:18:29'),(17,7,5,'2026-06-01 16:29:10'),(18,7,8,'2026-06-01 16:29:16'),(20,7,7,'2026-06-02 07:37:18'),(21,1,15,'2026-06-02 07:50:56'),(22,7,15,'2026-06-02 13:26:45'),(23,9,25,'2026-06-04 14:02:31');
/*!40000 ALTER TABLE `favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interaction_event`
--

DROP TABLE IF EXISTS `interaction_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interaction_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `event_id` varchar(64) NOT NULL COMMENT '事件的ID',
  `user_id` bigint NOT NULL COMMENT '哪个用户触发的该事件',
  `blog_id` bigint DEFAULT NULL COMMENT '和哪个博客关联  应为几乎每个事件都和博客相关',
  `target_user_id` bigint DEFAULT NULL COMMENT '通知发给谁 这个主要用于生成本地消息',
  `comment_id` bigint DEFAULT NULL COMMENT '如果是评论时间 记录评论ID',
  `type` tinyint NOT NULL COMMENT '事件类型 1-点赞 2-评论 3-收藏 4-回复',
  `action` tinyint NOT NULL COMMENT '具体动作 比如新增还是删减 1-增加 0-取消',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '事件执行到哪一步了 0-待发送 1-已发送  2-已消费 3-发送失败 4-消费失败',
  `retry_count` tinyint DEFAULT '0' COMMENT '尝试发送的次数',
  `error_msg` varchar(512) DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `interaction_event_event_id_uindex` (`event_id`)
) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息事件 设置队列异步操作';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interaction_event`
--

LOCK TABLES `interaction_event` WRITE;
/*!40000 ALTER TABLE `interaction_event` DISABLE KEYS */;
INSERT INTO `interaction_event` VALUES (1,'86564a0c097841f49b89c7ffa4fad042',5,1,1,7,2,1,1,0,NULL,'2026-05-31 16:41:05','2026-05-31 16:41:05'),(2,'417ab7f12f5349e6988190069d7a4b1b',5,1,1,8,2,1,1,0,NULL,'2026-05-31 16:45:07','2026-05-31 16:45:07'),(3,'740f03edb931462aafe3303c31d7d79d',5,1,1,9,2,1,1,0,NULL,'2026-05-31 16:45:19','2026-05-31 16:45:19'),(4,'aa93de53b08c49e4becbc26b7d0f8849',5,1,1,10,2,1,1,0,NULL,'2026-05-31 16:47:33','2026-05-31 16:47:33'),(5,'b356d8d9a9044e44b64c6222e9aad6be',5,1,1,NULL,3,1,1,0,NULL,'2026-05-31 16:57:54','2026-05-31 16:57:54'),(6,'6d9cf148a5374732afb6fad05886f2e5',6,1,1,11,2,1,2,0,NULL,'2026-05-31 21:40:32','2026-05-31 21:40:32'),(7,'de6ceaa03c02471e904a41b27668f617',6,1,1,12,2,1,2,0,NULL,'2026-05-31 21:41:05','2026-05-31 21:41:05'),(8,'0bd42917f6984ad387d10e00525e7d1b',3,8,2,NULL,1,1,2,0,NULL,'2026-05-31 22:10:15','2026-05-31 22:10:15'),(9,'56b98d4049e84eaab66419f32837690a',3,8,2,NULL,1,0,2,0,NULL,'2026-05-31 22:10:15','2026-05-31 22:10:15'),(10,'559acb981792402d8ca874f21adb7396',3,8,2,NULL,3,1,2,0,NULL,'2026-05-31 22:10:15','2026-05-31 22:10:15'),(11,'b5756ed955f24807a4110cbb769cd170',3,8,2,NULL,3,0,2,0,NULL,'2026-05-31 22:10:15','2026-05-31 22:10:15'),(12,'916a441026264a59b78401ad744980f0',3,8,2,13,2,1,2,0,NULL,'2026-05-31 22:10:16','2026-05-31 22:10:16'),(13,'68025c78c40440b8b2ce6e4be861a9e4',5,9,4,NULL,1,1,2,0,NULL,'2026-05-31 22:15:03','2026-05-31 22:15:03'),(14,'9af11cebefba4ae8a0d51f6906abf39f',5,9,4,NULL,1,0,2,0,NULL,'2026-05-31 22:15:03','2026-05-31 22:15:03'),(15,'a05031fe9d2b4c4192b48a7215448be6',5,9,4,NULL,3,1,2,0,NULL,'2026-05-31 22:15:03','2026-05-31 22:15:03'),(16,'71dbde8fa5354cf4a66e5ad0e616a34f',5,9,4,NULL,3,0,2,0,NULL,'2026-05-31 22:15:03','2026-05-31 22:15:03'),(17,'434c116f4a024671956703eb8fa576bb',5,9,4,14,2,1,2,0,NULL,'2026-05-31 22:15:03','2026-05-31 22:15:03'),(18,'b4aa287b13424bb1a9c81df2569703ba',1,7,1,NULL,3,1,2,0,NULL,'2026-05-31 22:18:27','2026-05-31 22:18:27'),(19,'e11ac75f17eb46d0af733c029cd8bd6f',1,6,1,NULL,1,1,2,0,NULL,'2026-05-31 22:18:41','2026-05-31 22:18:41'),(20,'2d06aa6880e74adc9a73ad81c3ed337a',1,7,1,NULL,1,1,2,0,NULL,'2026-05-31 22:20:08','2026-05-31 22:20:08'),(21,'0c9a01b7a5d74d058a1e0f8bbe58d86f',6,7,1,NULL,1,1,2,0,NULL,'2026-05-31 22:21:35','2026-05-31 22:21:35'),(22,'35fb66aed43a49139fd5e09936589c82',6,6,1,NULL,1,1,2,0,NULL,'2026-05-31 22:21:39','2026-05-31 22:21:39'),(23,'29b855370454478096582a625a33292c',6,5,1,NULL,1,1,2,0,NULL,'2026-05-31 22:21:45','2026-05-31 22:21:45'),(24,'af5fc415ca87426a974bf4ace144ba6a',7,10,7,15,2,1,2,0,NULL,'2026-06-02 00:26:18','2026-06-02 00:26:18'),(25,'208ddbc7eb124f1f85c5c0ffe83bbfc6',1,10,7,16,2,1,2,0,NULL,'2026-06-02 00:28:25','2026-06-02 00:28:25'),(26,'dec31cd49c7e4eab8836b123eb540497',7,6,1,NULL,1,1,2,0,NULL,'2026-06-02 00:29:05','2026-06-02 00:29:05'),(27,'f8b33a1ec7ee4a50b7e2edcb8bbadcc4',7,5,1,NULL,1,1,2,0,NULL,'2026-06-02 00:29:06','2026-06-02 00:29:06'),(28,'4fae2330f9ef497b9cd98fcb59135b5e',7,5,1,NULL,3,1,2,0,NULL,'2026-06-02 00:29:07','2026-06-02 00:29:07'),(29,'a06d4798a51c44f58fb22bdef6a206b1',7,8,2,NULL,5,1,2,0,NULL,'2026-06-02 00:29:09','2026-06-02 00:29:09'),(30,'87a08b2768b54bb5851329a8f4f45033',7,8,2,NULL,3,1,2,0,NULL,'2026-06-02 00:29:14','2026-06-02 00:29:14'),(31,'4110b90007aa4a2793cdd2ac983a2e06',7,10,7,NULL,1,1,2,0,NULL,'2026-06-02 00:29:15','2026-06-02 00:29:15'),(32,'af2c6d2bea33431a84b1746471cf4fbc',7,8,2,NULL,1,1,2,0,NULL,'2026-06-02 11:36:36','2026-06-02 11:36:36'),(33,'045b5ab9234147e088b2dbfc0d673700',7,5,1,NULL,1,1,2,0,NULL,'2026-06-02 11:45:22','2026-06-02 11:45:22'),(34,'4a78928978a04667b6d27209ad96b8eb',7,5,1,NULL,5,1,2,0,NULL,'2026-06-02 11:45:34','2026-06-02 11:45:34'),(35,'c4e0b1ce6d9542429db3a6a2b46d91d8',7,5,1,NULL,5,1,2,0,NULL,'2026-06-02 11:45:35','2026-06-02 11:45:35'),(36,'05c783c64a934ee8b2e58daa8a909523',7,5,1,NULL,3,1,2,0,NULL,'2026-06-02 12:05:01','2026-06-02 12:05:01'),(37,'ab97ce67325c438cac5fb2d75563b874',7,10,7,NULL,1,1,2,0,NULL,'2026-06-02 12:05:18','2026-06-02 12:05:18'),(38,'1b3b03dd21ba469193ae3fbda07ba96f',7,10,7,NULL,1,0,2,0,NULL,'2026-06-02 12:05:19','2026-06-02 12:05:19'),(39,'2090751f7e7c46b183acb4e5acb32fa9',7,7,1,NULL,1,1,2,0,NULL,'2026-06-02 12:05:21','2026-06-02 12:05:21'),(40,'32c43cefcd4a4bf2828b46d0191d7d6e',7,7,1,NULL,1,0,2,0,NULL,'2026-06-02 12:05:28','2026-06-02 12:05:28'),(41,'eb58586efa3c43269e31b4f82ed0e284',7,7,1,NULL,1,1,2,0,NULL,'2026-06-02 12:05:29','2026-06-02 12:05:29'),(42,'3c90b510dd06457f98d63914636fbb9f',7,12,7,NULL,1,1,2,0,NULL,'2026-06-02 12:30:41','2026-06-02 12:30:41'),(43,'29e630583e474fd2b4ac7dba5ee2be04',7,12,7,NULL,5,1,2,0,NULL,'2026-06-02 12:30:42','2026-06-02 12:30:42'),(44,'6def1117cbfb4f6f86888989ea82a4b9',7,5,1,NULL,1,0,2,0,NULL,'2026-06-02 12:31:11','2026-06-02 12:31:11'),(45,'cd5ce32f256e44ee8ce6e0200cda3b71',7,5,1,NULL,1,1,2,0,NULL,'2026-06-02 12:31:12','2026-06-02 12:31:12'),(46,'ede664a28cae4ba78f39cc8cc233e276',7,6,1,NULL,1,1,2,0,NULL,'2026-06-02 12:31:14','2026-06-02 12:31:14'),(47,'11e4d0e527b849299b26a117cc554c7e',7,6,1,NULL,1,0,2,0,NULL,'2026-06-02 12:31:15','2026-06-02 12:31:15'),(48,'deb1e9baf7bc431ba8d4fbdd497c403a',7,6,1,NULL,5,1,2,0,NULL,'2026-06-02 12:31:22','2026-06-02 12:31:22'),(49,'7bfab5db105148e5969652481a1428c1',7,6,1,NULL,1,1,2,0,NULL,'2026-06-02 12:31:23','2026-06-02 12:31:23'),(50,'892824d7ea9b44a7844b836dd0f1e653',7,7,1,NULL,3,1,2,0,NULL,'2026-06-02 15:37:17','2026-06-02 15:37:17'),(51,'d0c69d5806e2467998604613b7d57bdf',7,7,1,NULL,5,1,2,0,NULL,'2026-06-02 15:37:17','2026-06-02 15:37:17'),(52,'dd676a0fca234909b8517fbd1a9fbf9d',7,7,1,17,2,1,2,0,NULL,'2026-06-02 15:37:33','2026-06-02 15:37:33'),(53,'e94c49d6fd154f5d80cd7be8daafb4a0',7,7,7,18,2,1,2,0,NULL,'2026-06-02 15:37:49','2026-06-02 15:37:49'),(54,'b83f308745404d859025aae42a6245d2',7,7,1,NULL,1,0,2,0,NULL,'2026-06-02 15:38:37','2026-06-02 15:38:37'),(55,'f8a66185abee49f6962867a86a1ff2a2',7,7,1,NULL,1,1,2,0,NULL,'2026-06-02 15:38:38','2026-06-02 15:38:38'),(56,'55af217e62aa4a00864bd78b519896c1',1,7,1,NULL,1,1,2,0,NULL,'2026-06-02 15:39:31','2026-06-02 15:39:31'),(57,'b3e61061ca6b4fea939ad2e2fc205884',1,13,1,NULL,1,1,2,0,NULL,'2026-06-02 15:41:47','2026-06-02 15:41:47'),(58,'20e325c411cc46aeaeaf2407133f55e9',1,14,1,NULL,1,1,2,0,NULL,'2026-06-02 15:46:53','2026-06-02 15:46:53'),(59,'a136c588b2e34ac39f495469626d6142',1,14,1,NULL,1,0,2,0,NULL,'2026-06-02 15:47:00','2026-06-02 15:47:00'),(60,'532d9f355e444df8841ae271697dc2a9',1,14,1,NULL,1,1,2,0,NULL,'2026-06-02 15:47:02','2026-06-02 15:47:02'),(61,'209c6d13b8164818a8a27c14c744c5e4',1,15,1,NULL,1,1,2,0,NULL,'2026-06-02 15:50:46','2026-06-02 15:50:46'),(62,'f1c1ab25f4e9463ba3c4c9bdbed147a1',1,15,1,NULL,1,0,2,0,NULL,'2026-06-02 15:50:50','2026-06-02 15:50:50'),(63,'e8749bcb838e433b9545e97b15b4e2f9',1,15,1,NULL,3,1,2,0,NULL,'2026-06-02 15:50:56','2026-06-02 15:50:56'),(64,'f92fbb09179f44a880a461791a6b01b0',1,15,1,19,2,1,2,0,NULL,'2026-06-02 15:51:08','2026-06-02 15:51:08'),(65,'a4ca34006109407aaa40b2e0afcfbd3c',1,15,1,20,2,1,2,0,NULL,'2026-06-02 15:51:21','2026-06-02 15:51:21'),(66,'457e31ba97b64d20908e29967b0e449d',1,15,1,NULL,1,1,2,0,NULL,'2026-06-02 20:08:30','2026-06-02 20:08:30'),(67,'4dd610a93dff4371851eac4bd1325089',1,5,1,NULL,1,1,2,0,NULL,'2026-06-02 20:08:34','2026-06-02 20:08:34'),(68,'c9853f6164dc48259b6b796fbb8388f1',1,5,1,NULL,1,0,2,0,NULL,'2026-06-02 20:08:36','2026-06-02 20:08:36'),(69,'c710da37467040a4b7f0995725e869d5',1,5,1,NULL,1,1,2,0,NULL,'2026-06-02 20:08:37','2026-06-02 20:08:37'),(70,'bbc0a6d004f74ac49c881032c31256b7',1,12,7,NULL,1,1,2,0,NULL,'2026-06-02 20:08:37','2026-06-02 20:08:37'),(71,'ac05100899ef425b8e8dc4533631e7e4',1,12,7,NULL,1,0,2,0,NULL,'2026-06-02 20:08:38','2026-06-02 20:08:38'),(72,'fa806d41986e4439beb8409249636b1b',7,15,1,NULL,1,1,2,0,NULL,'2026-06-02 21:26:41','2026-06-02 21:26:41'),(73,'5939bfd4438348238d5bb7047183293e',7,15,1,NULL,5,1,2,0,NULL,'2026-06-02 21:26:43','2026-06-02 21:26:43'),(74,'51defede2d6f47da8d98329d666bf8fc',7,15,1,NULL,3,1,2,0,NULL,'2026-06-02 21:26:44','2026-06-02 21:26:44'),(75,'6bed274568cf46a091de1a4ec419c724',1,23,7,NULL,1,1,2,0,NULL,'2026-06-02 21:28:23','2026-06-02 21:28:23'),(76,'45c46b2236314182a87591003fbb59da',1,22,1,NULL,1,1,2,0,NULL,'2026-06-02 22:10:01','2026-06-02 22:10:01'),(77,'61cde77b9bee4851afe04b423deacd2c',7,15,1,NULL,1,0,2,0,NULL,'2026-06-02 22:20:04','2026-06-02 22:20:04'),(78,'c9e8adbbf71e48d69b885abddd794868',7,15,1,NULL,1,1,2,0,NULL,'2026-06-02 22:20:05','2026-06-02 22:20:05'),(79,'2ce71c98360842d785fba9409a60b173',7,15,1,NULL,5,1,2,0,NULL,'2026-06-02 22:20:05','2026-06-02 22:20:05'),(80,'5c422de4885b405aa9403885642c5b7d',8,15,1,21,2,1,2,0,NULL,'2026-06-02 22:22:59','2026-06-02 22:22:59'),(81,'acbd49f810164d2bbeb33dfcb5e98cf9',1,21,1,NULL,1,1,2,0,NULL,'2026-06-02 22:58:52','2026-06-02 22:58:52'),(82,'05006954a13a49d99d68541558d87b93',1,21,1,NULL,1,0,2,0,NULL,'2026-06-02 22:59:30','2026-06-02 22:59:30'),(83,'61f71150830d4365a2514664ef6192fe',1,15,1,NULL,5,1,2,0,NULL,'2026-06-02 23:02:14','2026-06-02 23:02:14'),(84,'77defb899411498a836b93a30a00fbf0',1,15,1,NULL,1,0,2,0,NULL,'2026-06-02 23:02:14','2026-06-02 23:02:14'),(85,'38fb732d29be48a7a9784f291a76c6c3',1,15,1,NULL,1,1,2,0,NULL,'2026-06-02 23:02:15','2026-06-02 23:02:15'),(86,'63aab005b8294a599f338d48b1833a68',1,15,1,NULL,1,0,2,0,NULL,'2026-06-02 23:02:26','2026-06-02 23:02:26'),(87,'b4dcc3bdab714e8685ad5c3272f566b9',1,15,1,NULL,1,1,2,0,NULL,'2026-06-02 23:03:19','2026-06-02 23:03:19'),(88,'038c20d9f2ad45e0b5ed31bb695cd8ab',1,15,1,NULL,1,0,2,0,NULL,'2026-06-02 23:03:21','2026-06-02 23:03:21'),(89,'46ab3a428ebf48b6b788270bf3338935',1,15,1,NULL,1,1,2,0,NULL,'2026-06-02 23:03:22','2026-06-02 23:03:22'),(90,'a6fac0aeb3ff4f35a234f6abff7d8478',1,15,1,NULL,1,0,2,0,NULL,'2026-06-02 23:03:23','2026-06-02 23:03:23'),(91,'5be83575fc7640e1a94d8f9c669e4d3a',1,15,1,NULL,1,1,2,0,NULL,'2026-06-02 23:03:24','2026-06-02 23:03:24'),(92,'70a637237ccb42e0bd935fdfac1bbe7d',1,15,1,NULL,1,0,2,0,NULL,'2026-06-02 23:03:24','2026-06-02 23:03:24'),(93,'2a80abcf73674c07a04dd23cd2bddb9f',1,15,1,NULL,1,1,2,0,NULL,'2026-06-02 23:03:25','2026-06-02 23:03:25'),(94,'7994a0bc0b504e149debc68596ac4865',1,15,1,NULL,1,0,2,0,NULL,'2026-06-02 23:04:11','2026-06-02 23:04:11'),(95,'9826a7752517425a8ee2936fd92bdb0e',1,5,1,NULL,1,0,2,0,NULL,'2026-06-02 23:04:13','2026-06-02 23:04:13'),(96,'04db26229c154762b10a10339246a78e',1,5,1,NULL,1,1,2,0,NULL,'2026-06-02 23:04:16','2026-06-02 23:04:16'),(97,'7dfa1b93ef4d4ef9b59d8f024af7b2b3',1,15,1,NULL,5,1,2,0,NULL,'2026-06-02 23:04:18','2026-06-02 23:04:18'),(98,'25a84c027f5749b2a36243cdbdf02561',1,15,1,NULL,5,1,2,0,NULL,'2026-06-02 23:04:20','2026-06-02 23:04:20'),(99,'6a746ed085ff403194d71364d5471151',1,15,1,NULL,1,1,2,0,NULL,'2026-06-02 23:04:24','2026-06-02 23:04:24'),(100,'59b10667135744faa70a42896f015bb5',1,15,1,NULL,1,0,2,0,NULL,'2026-06-02 23:04:25','2026-06-02 23:04:25'),(101,'46fe5d7802ce412a946c48f64c8fd8ba',7,14,1,NULL,1,1,2,0,NULL,'2026-06-04 20:55:40','2026-06-04 20:55:40'),(102,'6f44f446c86e48f58577eaecda2c11bc',7,15,8,22,2,1,2,0,NULL,'2026-06-04 20:59:44','2026-06-04 20:59:44'),(103,'7d3f8517c2f44712998ccbf3c97c7dfb',7,15,8,23,2,1,2,0,NULL,'2026-06-04 20:59:49','2026-06-04 20:59:49'),(104,'4430e07e1b974e6eb112a5ffa47c2259',1,12,7,24,2,1,2,0,NULL,'2026-06-04 21:28:42','2026-06-04 21:28:42'),(105,'9dd2c19c135f4b8380984e482f6aab69',1,12,1,25,2,1,2,0,NULL,'2026-06-04 21:29:48','2026-06-04 21:29:48'),(106,'752b433ee3d7462daeb85f343608e7be',1,24,8,26,2,1,2,0,NULL,'2026-06-04 21:56:01','2026-06-04 21:56:01'),(107,'801e85afe8d64e368e70133d8b7e9252',1,24,1,27,2,1,2,0,NULL,'2026-06-04 21:56:10','2026-06-04 21:56:10'),(108,'98db882948104cf686a297afd0d1a38c',1,24,1,28,2,1,2,0,NULL,'2026-06-04 21:56:20','2026-06-04 21:56:20'),(109,'9ae8ca51ceac491a9ae2c7116098e5d1',8,24,1,29,2,1,2,0,NULL,'2026-06-04 21:56:46','2026-06-04 21:56:46'),(110,'9e213387707c4779b03aab4a04f9ae4f',9,24,1,30,2,1,2,0,NULL,'2026-06-04 21:58:02','2026-06-04 21:58:02'),(111,'49e5d910ecc541579fc75448feb48302',9,25,9,NULL,1,1,2,0,NULL,'2026-06-04 22:02:06','2026-06-04 22:02:06'),(112,'a73ebe437feb484eaefb737bae4561d4',9,25,9,31,2,1,2,0,NULL,'2026-06-04 22:02:20','2026-06-04 22:02:20'),(113,'dbcfe98a0cce4658a710562f48ffa605',9,25,9,NULL,3,1,2,0,NULL,'2026-06-04 22:02:30','2026-06-04 22:02:30');
/*!40000 ALTER TABLE `interaction_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint NOT NULL COMMENT '发送人的ID',
  `receiver_id` bigint NOT NULL COMMENT '站内收信人ID',
  `blog_id` bigint DEFAULT NULL COMMENT '  根据ID知道这条消息关联的是哪条评论',
  `comment_id` bigint DEFAULT NULL COMMENT '这条站内消息关联的是用户的哪个评论',
  `type` smallint NOT NULL COMMENT '消息类型 1-点赞  2-收藏 3-评论  4-回复通知  5-系统通知',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `is_read` smallint NOT NULL DEFAULT '0' COMMENT '是否已读取 0-未读取 1-已读取',
  `is_delete` smallint NOT NULL DEFAULT '0' COMMENT '逻辑删除字段  0-未删除  1-已删除',
  `content` varchar(512) DEFAULT NULL COMMENT '消息内容',
  `title` varchar(512) DEFAULT NULL COMMENT '消息名称',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='message站内消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
INSERT INTO `message` VALUES (1,6,1,1,11,2,'2026-05-31 21:40:36',1,0,'有人评论了你的内容','评论通知'),(2,6,1,1,12,2,'2026-05-31 21:41:06',1,0,'有人评论了你的内容','评论通知'),(3,3,2,8,NULL,1,'2026-05-31 22:10:17',0,0,'有人点赞了你的内容','点赞通知'),(4,3,2,8,NULL,4,'2026-05-31 22:10:17',0,0,'有人收藏了你的内容','收藏通知'),(5,3,2,8,13,2,'2026-05-31 22:10:17',0,0,'有人评论了你的内容','评论通知'),(6,5,4,9,NULL,1,'2026-05-31 22:15:04',0,0,'有人点赞了你的内容','点赞通知'),(7,5,4,9,NULL,4,'2026-05-31 22:15:04',0,0,'有人收藏了你的内容','收藏通知'),(8,5,4,9,14,2,'2026-05-31 22:15:04',0,0,'有人评论了你的内容','评论通知'),(9,6,1,7,NULL,1,'2026-05-31 22:21:36',1,0,'有人点赞了你的内容','点赞通知'),(10,6,1,6,NULL,1,'2026-05-31 22:21:39',1,0,'有人点赞了你的内容','点赞通知'),(11,6,1,5,NULL,1,'2026-05-31 22:21:48',1,0,'有人点赞了你的内容','点赞通知'),(12,1,7,10,16,2,'2026-06-02 00:28:28',1,0,'有人评论了你的内容','评论通知'),(13,7,1,6,NULL,1,'2026-06-02 00:29:07',1,0,'有人点赞了你的内容','点赞通知'),(14,7,1,5,NULL,1,'2026-06-02 00:29:07',1,0,'有人点赞了你的内容','点赞通知'),(15,7,1,5,NULL,4,'2026-06-02 00:29:10',1,0,'有人收藏了你的内容','收藏通知'),(16,7,2,8,NULL,5,'2026-06-02 00:29:10',0,0,'有人分享了你的博客','分享通知'),(17,7,2,8,NULL,4,'2026-06-02 00:29:16',0,0,'有人收藏了你的内容','收藏通知'),(18,7,2,8,NULL,1,'2026-06-02 11:36:37',0,0,'有人点赞了你的内容','点赞通知'),(19,7,1,5,NULL,5,'2026-06-02 11:45:36',1,0,'有人分享了你的博客','分享通知'),(20,7,1,5,NULL,5,'2026-06-02 11:45:36',1,0,'有人分享了你的博客','分享通知'),(21,7,1,7,NULL,1,'2026-06-02 12:05:24',1,0,'有人点赞了你的内容','点赞通知'),(22,7,1,6,NULL,5,'2026-06-02 12:31:23',1,0,'有人分享了你的博客','分享通知'),(23,7,1,7,NULL,4,'2026-06-02 15:37:18',1,0,'有人收藏了你的内容','收藏通知'),(24,7,1,7,NULL,5,'2026-06-02 15:37:18',1,0,'有人分享了你的博客','分享通知'),(25,7,1,7,17,2,'2026-06-02 15:37:36',1,0,'有人评论了你的内容','评论通知'),(26,1,7,12,NULL,1,'2026-06-02 20:08:39',1,0,'有人点赞了你的内容','点赞通知'),(27,7,1,15,NULL,1,'2026-06-02 21:26:42',1,0,'用户faker点赞了你的博客redis缓存错误','点赞通知'),(28,7,1,15,NULL,5,'2026-06-02 21:26:45',1,0,'用户faker分享了你的博客redis缓存错误','分享通知'),(29,7,1,15,NULL,4,'2026-06-02 21:26:45',1,0,'用户faker点赞了你的博客redis缓存错误','收藏通知'),(30,1,7,23,NULL,1,'2026-06-02 21:29:32',1,0,'用户我是詹姆斯点赞了你的博客我是gggggg','点赞通知'),(31,7,1,15,NULL,5,'2026-06-02 22:20:07',1,0,'用户faker分享了你的博客redis缓存错误','分享通知'),(32,8,1,15,21,2,'2026-06-02 22:23:01',1,0,'用户null评论了你的博客redis缓存错误','评论通知'),(33,7,1,14,NULL,1,'2026-06-04 20:55:42',1,0,'用户faker点赞了你的博客写一篇真正能被读懂的内容','点赞通知'),(34,7,8,15,22,2,'2026-06-04 20:59:46',1,0,'用户faker评论了你的博客redis缓存错误','评论通知'),(35,7,8,15,23,2,'2026-06-04 20:59:52',1,0,'用户faker评论了你的博客redis缓存错误','评论通知'),(36,1,7,12,24,2,'2026-06-04 21:28:44',0,0,'用户我是詹姆斯评论了你的博客我是faker 入住此社区啦','评论通知'),(37,1,8,24,26,2,'2026-06-04 21:56:02',0,0,'用户我是詹姆斯评论了你的博客gwefsa','评论通知'),(38,8,1,24,29,2,'2026-06-04 21:56:47',0,0,'用户null评论了你的博客gwefsa','评论通知'),(39,9,1,24,30,2,'2026-06-04 21:58:03',0,0,'用户凯里欧文评论了你的博客gwefsa','评论通知');
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `share`
--

DROP TABLE IF EXISTS `share`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `share` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `path` varchar(512) DEFAULT NULL COMMENT '分享路径，例如QQ 微信等',
  `url` varchar(512) DEFAULT NULL COMMENT '分享的内容url',
  `blog_id` bigint DEFAULT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='保存分享内容数据';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `share`
--

LOCK TABLES `share` WRITE;
/*!40000 ALTER TABLE `share` DISABLE KEYS */;
INSERT INTO `share` VALUES (1,'2026-05-17 10:15:12','QQ','http://localhost:8080/doc.html#/default/%E5%88%86%E4%BA%AB/share',NULL,NULL),(2,'2026-05-17 10:15:14','QQ','http://localhost:8080/doc.html#/default/%E5%88%86%E4%BA%AB/share',NULL,NULL),(3,'2026-05-17 10:15:15','QQ','http://localhost:8080/doc.html#/default/%E5%88%86%E4%BA%AB/share',NULL,NULL),(4,'2026-05-17 10:15:15','QQ','http://localhost:8080/doc.html#/default/%E5%88%86%E4%BA%AB/share',NULL,NULL),(5,'2026-05-25 04:04:42','QQ','k11_123',NULL,NULL),(6,'2026-05-25 04:04:42','QQ','k11_123',NULL,NULL),(7,'2026-05-25 04:04:44','QQ','k11_123',NULL,NULL),(8,'2026-05-25 04:04:44','QQ','k11_123',NULL,NULL),(9,'2026-05-25 04:04:44','QQ','k11_123',NULL,NULL),(10,'2026-05-25 04:04:45','QQ','k11_123',NULL,NULL),(11,'2026-05-25 04:04:45','QQ','k11_123',NULL,NULL),(12,'2026-05-31 14:15:04','test','https://example.com/blog/9',NULL,NULL),(13,'2026-05-31 14:18:10','QQ','k11_123',NULL,NULL),(14,'2026-06-01 16:29:09','web','http://localhost:5173/blog/8',8,7),(15,'2026-06-02 03:45:34','web','http://localhost:5173/blog/5',5,7),(16,'2026-06-02 03:45:35','web','http://localhost:5173/blog/5',5,7),(17,'2026-06-02 04:30:42','web','http://localhost:5173/blog/12',12,7),(18,'2026-06-02 04:31:22','web','http://localhost:5173/blog/6',6,7),(19,'2026-06-02 07:37:17','web','http://localhost:5173/blog/7',7,7),(20,'2026-06-02 13:26:43','web','http://localhost:5173/blog/15',15,7),(21,'2026-06-02 14:20:05','web','http://localhost:5173/blog/15',15,7),(22,'2026-06-02 15:02:14','web','http://localhost:5173/blog/15',15,1),(23,'2026-06-02 15:04:18','web','http://localhost:5173/blog/15',15,1),(24,'2026-06-02 15:04:20','web','http://localhost:5173/blog/15',15,1);
/*!40000 ALTER TABLE `share` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `thumb`
--

DROP TABLE IF EXISTS `thumb`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `thumb` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '点赞用户id',
  `blog_id` bigint NOT NULL COMMENT '文章id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `userId_blogId_uindex` (`user_id`,`blog_id`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `thumb`
--

LOCK TABLES `thumb` WRITE;
/*!40000 ALTER TABLE `thumb` DISABLE KEYS */;
INSERT INTO `thumb` VALUES (1,3,8,'2026-05-31 22:10:17'),(2,5,9,'2026-05-31 22:15:04'),(3,1,6,'2026-05-31 22:18:41'),(4,1,7,'2026-05-31 22:20:09'),(5,6,7,'2026-05-31 22:21:36'),(6,6,6,'2026-05-31 22:21:39'),(7,6,5,'2026-05-31 22:21:48'),(8,7,6,'2026-06-02 00:29:07'),(9,7,5,'2026-06-02 00:29:07'),(11,7,8,'2026-06-02 11:36:37'),(14,7,7,'2026-06-02 12:05:24'),(16,7,12,'2026-06-02 12:30:41'),(22,1,13,'2026-06-02 15:41:49'),(23,1,14,'2026-06-02 15:46:55'),(25,1,15,'2026-06-02 15:50:47'),(27,1,5,'2026-06-02 20:08:36'),(29,1,12,'2026-06-02 20:08:39'),(30,7,15,'2026-06-02 21:26:42'),(31,1,23,'2026-06-02 21:28:24'),(32,1,22,'2026-06-02 22:10:01'),(34,1,21,'2026-06-02 22:58:54'),(42,7,14,'2026-06-04 20:55:42'),(43,9,25,'2026-06-04 22:02:07');
/*!40000 ALTER TABLE `thumb` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户id',
  `password` varchar(256) NOT NULL COMMENT '密码',
  `username` varchar(256) DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '用户状态 0-正常 1-封禁',
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `avatar` varchar(512) DEFAULT NULL COMMENT '头像地址',
  `is_admin` tinyint NOT NULL DEFAULT '0' COMMENT '0---普通用户；1----管理员',
  `is_delete` tinyint NOT NULL DEFAULT '0' COMMENT ' 0-未删除 1-删除',
  `account` varchar(256) DEFAULT NULL COMMENT '账号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'0f41212c757ea9529da10ccfd51b7094','我是詹姆斯',0,NULL,NULL,'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRA3roEMMZRSNqs7celP7vX-9EBmZrYfdtnQPBFDM9b_GmmPKQuFAPt2aicJh5QxL5n1Gz3VAsYigO-kCbsBuBA1BblIQ6XV6FaD5oKlx32OA&s=10',1,0,'kyrie123'),(2,'93311cd0b0e213744fddac725e21eb5b',NULL,0,NULL,NULL,NULL,0,0,'cdxA_b9d7f740'),(3,'93311cd0b0e213744fddac725e21eb5b',NULL,0,NULL,NULL,NULL,0,0,'cdxB_b9d7f740'),(4,'93311cd0b0e213744fddac725e21eb5b',NULL,0,NULL,NULL,NULL,0,0,'cdxA_a791ceb1'),(5,'93311cd0b0e213744fddac725e21eb5b',NULL,0,NULL,NULL,NULL,0,0,'cdxB_a791ceb1'),(6,'0f41212c757ea9529da10ccfd51b7094',NULL,0,NULL,NULL,NULL,0,0,'maxmaxmax'),(7,'0f41212c757ea9529da10ccfd51b7094','faker',1,NULL,NULL,'https://images.pexels.com/photos/37866048/pexels-photo-37866048.jpeg',0,0,'kyrie11111'),(8,'0f41212c757ea9529da10ccfd51b7094',NULL,0,NULL,NULL,'https://images.pexels.com/photos/35922317/pexels-photo-35922317.jpeg',0,0,'cab123'),(9,'0f41212c757ea9529da10ccfd51b7094','凯里欧文',0,NULL,NULL,'https://images.pexels.com/photos/33712591/pexels-photo-33712591.jpeg',0,0,'123456');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-05 17:53:33
