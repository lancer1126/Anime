package org.lance.core;

import lombok.extern.slf4j.Slf4j;
import org.lance.common.constrants.enums.HttpDownStatus;
import org.lance.common.constrants.enums.MessageType;
import org.lance.domain.Message;
import org.lance.domain.MessageData;
import org.lance.domain.entity.TaskInfo;
import org.lance.network.websocket.ClientCallback;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息发送类
 */
@Slf4j
public class MessageCore {

    private static final String SEPARATE = "_";
    private static final Object LOCK = new Object();
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);

    private static final Map<String, Map<String, Message>> taskIdMessageMap = new HashMap<>();
    private static final LinkedList<Message> callbackMessageList = new LinkedList<>();

    private static final MessageCore INSTANCE = new MessageCore();

    public static MessageCore getInstance() {
        return INSTANCE;
    }

    public void init() {
        // todo 初始化消息发送类
    }

    public void stop() {

    }

    public static void send(Message message) {
        ClientCallback.push(message);
    }

    public static void send(MessageType type, HttpDownStatus status, String msg, TaskInfo taskInfo) {
        Message message = createMessage(type, status, msg, taskInfo);
        if (message.getType() == MessageType.CALL_BACK.getType()) {
            String msgId = message.getData().getId() + SEPARATE + ID_GENERATOR.getAndIncrement();
            message.setId(msgId);

            synchronized (LOCK) {
                String taskId = message.getData().getId();
                Map<String, Message> map = taskIdMessageMap.get(taskId);
                if (map != null) {
                    // 删除原有的message
                    callbackMessageList.remove(map.values().stream().findFirst().get());
                    taskIdMessageMap.remove(taskId);
                }
                callbackMessageList.add(message);
                taskIdMessageMap.put(message.getData().getId(), Collections.singletonMap(message.getId(), message));
            }
        }
        ClientCallback.push(message);
    }

    private static Message createMessage(MessageType type, HttpDownStatus status, String msg, TaskInfo taskInfo) {
        Message message = new Message();
        message.setType(type.getType());
        message.setStatus(status.getStatus());
        message.setMsg(msg);

        MessageData msgData = new MessageData();
        msgData.setId(taskInfo.getId());
        msgData.setTitle(taskInfo.getName());
        msgData.setCurrentSize(taskInfo.getCurrentOffset());
        msgData.setTotalSize(taskInfo.getTotalSize());
        msgData.setSpeed(taskInfo.getSpeed());
        msgData.setFilePath(taskInfo.getFilePath());
        msgData.setCover(taskInfo.getCoverImg());
        message.setData(msgData);
        return message;
    }
}
