package com.example.allrealmen.domain.category.dto;

import com.example.allrealmen.domain.category.entity.Category;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private String id;
    private String category;
    private String description;
    private List<FileInfo> images;
    private List<FileInfo> videos;

    @Getter @Setter
    public static class FileInfo {
        private String id;
        private String url;
    }

    public static CategoryResponse from(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setCategory(category.getCategory());
        response.setDescription(category.getDescription());
        
        if (category.getImages() != null) {
            response.setImages(category.getImages().stream()
                .map(img -> {
                    FileInfo info = new FileInfo();
                    info.setId(img.getId());
                    info.setUrl(img.getUrl());
                    return info;
                })
                .collect(Collectors.toList()));
        }
        
        if (category.getVideos() != null) {
            response.setVideos(category.getVideos().stream()
                .map(vid -> {
                    FileInfo info = new FileInfo();
                    info.setId(vid.getId());
                    info.setUrl(vid.getUrl());
                    return info;
                })
                .collect(Collectors.toList()));
        }
        
        return response;
    }
} 