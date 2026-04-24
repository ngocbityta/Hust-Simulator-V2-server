package com.hustsimulator.social;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hustsimulator.social.entity.Post;
import com.hustsimulator.social.common.GeometryUtils;

public class PostSerializationTest {
    public static void main(String[] args) throws Exception {
        Post post = Post.builder()
            .content("Test")
            .location(GeometryUtils.createPoint(10.0, 20.0))
            .build();
            
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(post);
            System.out.println("Serialization successful: " + json);
        } catch (Exception e) {
            System.out.println("Serialization failed: " + e.getMessage());
        }
    }
}
