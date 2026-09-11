package com.usst.thumbs.common.constant;

public interface RabbitMQConstant {
    String BLOG_INDEX_EVENT_TYPE = "blog_index";
    String INTERACTION_EVENT_TYPE = "interaction_type";
    String DELAY_DELETE_EVENT_TYPE = "delay_delete";

    String BLOG_INDEX_CONSUMER_NAME = "blog_index_consumer";

    String INTERACTION_EXCHANGE = "interaction_exchange";
    String INTERACTION_QUEUE = "interaction_queue";
    String INTERACTION_ROUTING_KEY = "interaction_routing";

    String INTERACTION_DLX_EXCHANGE = "interaction_dlx_exchange";
    String INTERACTION_DLX_QUEUE = "interaction_dlx_queue";
    String INTERACTION_DLX_ROUTING_KEY = "interaction_dlx_routing";

    String BLOG_INDEX_EXCHANGE = "blog_index_exchange";
    String BLOG_INDEX_QUEUE = "blog_index_queue";
    String BLOG_INDEX_ROUTING_KEY = "blog_index_routing";

    String BLOG_INDEX_DLX_EXCHANGE = "blog_index_dlx_exchange";
    String BLOG_INDEX_DLX_QUEUE = "blog_index_dlx_queue";
    String BLOG_INDEX_DLX_ROUTING_KEY = "blog_index_dlx_routing";

    String DELAY_DELETE_QUEUE = "delay_delete_queue";
    String DELAY_DELETE_ROUTING_KEY = "delay_delete_routing";
    String DELAY_DELETE_EXCHANGE = "delay_delete_exchange";

    String RABBITMQ_HEADER_EVENT_ID = "x-event-id";
    String RABBITMQ_HEADER_EVENT_TYPE = "x-event-type";
}
