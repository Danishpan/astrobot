package kz.dair.astrobot.controllers;

import kz.dair.astrobot.services.HoroscopeService;
import kz.dair.astrobot.services.ThreadsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/horoscope")
@RequiredArgsConstructor
public class HoroscopeController {

    private final HoroscopeService horoscopeService;
    private final ThreadsService threadsService;

    @GetMapping("/post")
    public Mono<String> postHoroscope() {
        return threadsService.createMediaContainer(horoscopeService.generateHoroscope())
                .flatMap(threadsService::publishContainer);
    }
}
