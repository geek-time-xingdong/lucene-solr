package org.apache.lucene.queries.marry;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * @author chengzhengzheng
 * @date 2021/2/19
 */
public class TestDeque {
    public static void main(String[] args) {
        Deque<String> deque = new ArrayDeque<>();
        deque.addFirst("HELLO0");//数组结尾添加
        deque.addFirst("HELLO2");//数组结尾添加
        deque.addFirst("HELLO3");//数组结尾添加
        deque.addFirst("HELLO4");//数组结尾添加
        deque.addFirst("HELLO5");//数组结尾添加

        deque.addLast("HELLO6");
        Iterator<String> stringIterator = deque.descendingIterator();
        while (stringIterator.hasNext()){
            System.out.println(stringIterator.next());
        }

    }
}
