package com.example.notification.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 1. 클라이언트가 구독 요청을 보냄( subscribe())
 * 2. 클라이언트 id로 SseEmitter 객체 만들고 맵에다가 저장해둠 (여긴 다른 클라이언트의 SseEmitter 객체들도 있음)
 * 3. 서버가 클라이언트에게 알림 보낼 일이 생김
 * 4. 알림을 보낼 클라이언트의 id, 메시지를 body에 담고 sendNotification() 실행
 * 5. 클라이언트 id를 key 값으로 맵에서 해당하는 SseEmitter를 찾고, 그 SseEmitter에 메시지를 넣고 보냄
 * 6. const eventSource = new EventSource('http://localhost:8080/sse/subscribe/12345');를 통해 이 api가 반환한 emitter를 저장
 * 7. emitter안에 있는 메시지 출력
 */
@RestController
public class SseController {

    // 클라이언트 식별자와 SSE 연결을 관리하기 위한 맵
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // 클라이언트가 구독 요청을 보낼 엔드포인트
    @GetMapping("/sse/subscribe/{clientId}")
    public SseEmitter subscribe(@PathVariable String clientId) {
        SseEmitter emitter = new SseEmitter(0L); // 타임아웃을 무제한으로 설정
        emitters.put(clientId, emitter);

        // 연결이 완료되거나 타임아웃이 발생하면 맵에서 제거
        emitter.onCompletion(() -> emitters.remove(clientId));
        emitter.onTimeout(() -> emitters.remove(clientId));
        emitter.onError(e -> emitters.remove(clientId));

        return emitter;
    }

    // 특정 클라이언트에게 이벤트를 전송하는 메서드
    public void notifySubscribers(String clientId, String message) {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().data(message));
            } catch (IOException e) {
                emitters.remove(clientId);
            }
        }
    }
}
