package com.lenovo.messageservice.controller;

import com.lenovo.messageservice.entity.Message;
import com.lenovo.messageservice.repository.MessageRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@RestController
@RequestMapping("/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messageRepository;
    private static final Sinks.Many<Message> chatSink = Sinks.many().multicast().directAllOrNothing();

    @GetMapping(value = "/stream/{channel}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Message> flux(@PathVariable String channel) {
        var userFlux = messageRepository.findAllByChannel(channel);
        return Flux.concat(userFlux, chatSink.asFlux().filter(message -> channel.equalsIgnoreCase(message.getChannel())))
            .distinct()
            .doOnNext(el -> System.out.println("Received: " + el));
    }

    @PostMapping
    public Mono<Message> toFlux(@RequestBody Message message) {
        return messageRepository.save(message)
            .doOnNext(chatSink::tryEmitNext);
    }

    @GetMapping(value = "/find/{channel}")
    public Flux<Message> fluxq(@PathVariable String channel) {
        return messageRepository.findAllByChannel(channel);
    }

    @DeleteMapping("{id}")
    public Mono<Void> delete(@PathVariable String id) {
        return messageRepository.deleteById(id);

    }

}
