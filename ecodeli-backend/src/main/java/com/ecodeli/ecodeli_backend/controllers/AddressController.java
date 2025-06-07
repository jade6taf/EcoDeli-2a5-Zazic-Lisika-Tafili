package com.ecodeli.ecodeli_backend.controllers;

import com.ecodeli.ecodeli_backend.services.GeocodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
public class AddressController {

    @Autowired
    private GeocodingService geocodingService;

    @GetMapping("/search")
    public Mono<ResponseEntity<List<Map<String, Object>>>> searchAddresses(
            @RequestParam("q") String query) {
        if (query == null || query.trim().length() < 3) {
            return Mono.just(ResponseEntity.ok(List.of()));
        }
        return geocodingService.searchAddresses(query)
                .map(suggestions -> ResponseEntity.ok(suggestions))
                .onErrorResume(e -> {
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}
