package com.periferia.social.feed.api;

import java.util.UUID;

public record LikeResponse(UUID postId, int likeCount, boolean likedByMe) {}
