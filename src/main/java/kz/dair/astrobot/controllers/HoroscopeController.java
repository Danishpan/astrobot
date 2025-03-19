package kz.dair.astrobot.controllers;

import kz.dair.astrobot.services.HoroscopeService;
import kz.dair.astrobot.services.ThreadsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/horoscope")
@RequiredArgsConstructor
public class HoroscopeController {

    private final HoroscopeService horoscopeService;
    private final ThreadsService threadsService;

    @GetMapping("/post")
    public String postHoroscope() {
        return threadsService.publishContainer(
                threadsService.createMediaContainer(
                        horoscopeService.generateHoroscope()
                )
        );
    }
}
