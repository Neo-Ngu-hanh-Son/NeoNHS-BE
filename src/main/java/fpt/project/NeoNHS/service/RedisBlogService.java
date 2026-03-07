package fpt.project.NeoNHS.service;

import java.util.UUID;

public interface RedisBlogService {
    void incrementTempViewCount(UUID id);
}