package com.shopx.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private final RestClient restClient;

    public List<String> upload(List<MultipartFile> files) {

        List<String> urls = new ArrayList<>();

        for (MultipartFile file : files) {

            String fileName =
                    UUID.randomUUID() + "-" + file.getOriginalFilename();

            String uploadUrl =
                    supabaseUrl + "/storage/v1/object/" +
                            bucket + "/" + fileName;

            restClient.post()
                    .uri(uploadUrl)
                    .header("Authorization", "Bearer " + serviceKey)
                    .header("apikey", serviceKey)
                    .header("Content-Type", file.getContentType())
                    .body(file.getResource())
                    .retrieve()
                    .toBodilessEntity();

            urls.add(
                    supabaseUrl +
                            "/storage/v1/object/public/" +
                            bucket + "/" + fileName
            );
        }

        return urls;
    }
}