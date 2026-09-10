package com.usst.thumbs.common.redis;

public interface InteractionStreamConstant {
    String STREAM_KEY = "interaction:stream";
    String DEAD_STREAM_KEY = "dead_stream";
    String GROUP = "interaction-persist-group";
    String DEAD_GROUP = "dead_group";
    String CONSUMER = "interaction-persist-worker";
    String PERSIST_CONSUMER_NAME = "interaction_stream_persist";
    int BATCH_SIZE = 100;
}
