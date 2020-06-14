package test;

import java.lang.reflect.InvocationTargetException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.beanutils.BeanUtils;

public class Test {
    public static void main(String[] args) {
        Queue queue = new ConcurrentLinkedQueue();
        queue.add("a");
        queue.add("b");
        queue.add("c");
        queue.add("d");
        System.out.println(queue.poll());
        System.out.println(queue.poll());
    }
}
