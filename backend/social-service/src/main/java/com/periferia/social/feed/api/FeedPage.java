package com.periferia.social.feed.api;

import java.util.List;

public record FeedPage(List<PostView> content, int page, int size, long totalElements) {}
