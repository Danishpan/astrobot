package kz.dair.astrobot.services;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class HoroscopeScheduler {

    private final HoroscopeService horoscopeService;
    private final ThreadsService threadsService;

    @Scheduled(cron = "0 0 * * * ?") // Запуск каждый день в 9 утра
    public void schedulePost() {
        threadsService.createMediaContainer(horoscopeService.generateHoroscope())
                .flatMap(threadsService::publishContainer)
                .subscribe(result -> System.out.println("Post result: " + result));
    }
}
