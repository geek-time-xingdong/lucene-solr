package org.apache.lucene.queries.marry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author chengzhengzheng
 * @date 2021/2/5
 */
public class MMapDirectoryTest {
    public static void main(String[] args) throws IOException {
        File file = new File("/Users/chengzheng/workspace/learn/lucene-solr/lucene/core/src/test/org/apache/lucene/marry/test.da");
        FileInputStream fileInputStream = new FileInputStream(file);
        FileChannel fc = fileInputStream.getChannel();
        long length = file.length();
        MappedByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0,length );
        buffer.load();
        System.out.println(buffer.isLoaded());

//        long begin = System.currentTimeMillis();
//        for (int offset = 0; offset < length; offset++) {
//            byte b = buffer.get();
//            System.out.println(b);
//        }
//        System.out.println(System.currentTimeMillis() - begin);
//        System.out.println(buffer.position());


//        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
//        long begin = System.currentTimeMillis();
//        for (int offset = 0; offset < length; offset++) {
//            byte b = buffer.get();
////            System.out.println(b);
//        }
//        System.out.println(System.currentTimeMillis() - begin);
//        System.out.println(buffer.position());
//




        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024);
        System.out.println(byteBuffer.position());
        byteBuffer.put("helo".getBytes());
        System.out.println(byteBuffer.position());
    }
}
