package io.crest.websocket.aop;

import io.crest.websocket.WsMessage;
import io.crest.websocket.WsService;
import org.apache.commons.lang3.ArrayUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
@SuppressWarnings({"unchecked", "rawtypes"})
public class WSTrigger {

    @Autowired
    private WsService wsService;

    @AfterReturning(value = "execution(* io.crest.service.message.service.strategy.SendStation.sendMsg(..))")
    public void after(JoinPoint point) {
        Object[] args = point.getArgs();
        Optional.ofNullable(args).ifPresent(objs -> {
            if (ArrayUtils.isEmpty(objs)) return;
            Object arg = args[0];
            Long userId = (Long) arg;
            WsMessage message = new WsMessage(userId, "/web-msg-topic", "refresh");
            wsService.releaseMessage(message);
        });

    }
}
