package org.lskk.lumen.helpdesk;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

/**
 * Created by ceefour on 23/06/2016.
 */
@Configuration
@ConditionalOnProperty("helpdesk.telegram.enabled")
public class TelegramConfig {

    @Bean
    public JakartaCityBot jakartaCityBot() {
        return new JakartaCityBot();
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        final TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        telegramBotsApi.registerBot(jakartaCityBot());
//        telegramBotsApi.registerBot(new ChannelHandlers());
//        telegramBotsApi.registerBot(new DirectionsHandlers());
//        telegramBotsApi.registerBot(new RaeHandlers());
//        telegramBotsApi.registerBot(new WeatherHandlers());
//        telegramBotsApi.registerBot(new TransifexHandlers());
//        telegramBotsApi.registerBot(new FilesHandlers());
        return telegramBotsApi;
    }
}
