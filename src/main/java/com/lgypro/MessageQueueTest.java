package com.lgypro;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MessageQueueTest {
    public static void main(String[] args) {
        MessageQueue messageQueue = new MessageQueue(2);
        for (int i = 0; i < 5; i++) {
            int id = i;
            new Thread(() -> {
                Message message = new Message(id, "message-" + id);
                log.debug("prepare to put {}", message);
                messageQueue.put(message);
                log.debug("put message {} succeed", message.getId());
            }, "producer-" + i).start();
        }
        new Thread(() -> {
            while (true) {
                Message message = messageQueue.take();
                log.debug("take {} succsssfully", message);
                try {
                    TimeUnit.MILLISECONDS.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, "consumer").start();
    }

    private static class MessageQueue {
        private final LinkedList<Message> list = new LinkedList<>();
        private final int capacity;

        public MessageQueue(int capacity) {
            this.capacity = capacity;
        }

        public synchronized Message take() {
            while (list.isEmpty()) {
                try {
                    log.debug("The queue is empty, waiting");
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (list.size() == capacity) {
                log.debug("current size is {}: The queue is not full, you can put a message", list.size() - 1);
                notify();
            }
            return list.removeFirst();
        }

        public synchronized void put(Message message) {
            while (list.size() == capacity) {
                try {
                    log.debug("The queue is full, waiting");
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (list.isEmpty()) {
                log.debug("current size is {}: The queue is not empty, you can take a message", list.size() + 1);
                notify();
            }
            list.addLast(message);
        }
    }

    private static class Message {
        private final int id;
        private final Object content;

        public Message(int id, Object message) {
            this.id = id;
            this.content = message;
        }

        public int getId() {
            return id;
        }

        public Object getMessage() {
            return content;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "id=" + id +
                    ", message=" + content +
                    '}';
        }
    }
}
