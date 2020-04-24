package threadSell;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.concurrent.*;

/**
 * @author LuckyBoy
 * @create 2020/4/24
 */

public class 多线程发送消息 {
    public static void main(String[] args) {
        LinkedList<String> msgList=new LinkedList<>();
        for (int i=1;i<11;i++){
            msgList.add(i+"");
        }
        for (int i=0;i<5;i++){
            System.out.println();
            send(msgList);
        }


    }


    //线程池最大线程数量
    private final static int MAXTHREAD = 10;

    //初始化线程数量
    private final static int THREADSZIE = 6;

    //固定长度线程池
    private final static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(THREADSZIE, MAXTHREAD, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    //重复发送次数
    private final static int sentNum = 5;


    /**
     * 推送消息的主方法
     * @param msgList
     */
    public static void send(LinkedList<String> msgList) {

        if (msgList == null) {
            throw new RuntimeException("无消息可推送");
        }
        LinkedList<String> successList = new LinkedList<>();//成功发送消息队列
        LinkedList<String> failList = new LinkedList<>();//失败发送消息队列

        int pushNum=THREADSZIE>msgList.size()?msgList.size():THREADSZIE;
        CountDownLatch countDownLatch= new CountDownLatch(pushNum);

        for (int i=0;i<pushNum;i++){
            String msg = msgList.removeFirst();
            //消息推送
            poolSend(msg,successList,failList,countDownLatch);
        }

        try {
            //阻塞主线程，等待所有子线程执行完
            countDownLatch.await();
            TimeUnit.SECONDS.sleep(1);

        }catch (InterruptedException e){
            throw new RuntimeException("线程中断"+e);
        }

        //发送成功的消息内容展示
        if (successList.size()!=0){
            System.out.print("消息成功列表数据有：");
            for (String msg : successList){
                System.out.print(msg+" ");
            }
            System.out.println();
            //清除发送成功的消息
            successList.clear();
            if (msgList.size()!=0){
                System.out.print("还未发送的消息列表数据有：");
                for (String msg : msgList){
                    System.out.print(msg+" ");
                }
            }
        }


    }

    /**
     * 多线程推送消息处理
     * @param msg
     * @param successList
     * @param failList
     * @param countDownLatch
     */
    private static void poolSend(String msg, LinkedList<String> successList, LinkedList<String> failList,CountDownLatch countDownLatch) {

        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean result = sendMessage(msg, successList);
                    //消息推送失败处理
                    if (!result) {
                        repeatSent(msg, successList);
                    }

                } catch (Exception e) {
                    throw new RuntimeException("多线程推送消息异常"+e);
                }
                countDownLatch.countDown();
            }
        });

    }


    /**
     * 推送消息,这里假设了消息推送失败的场景
     *
     * @param msg
     * @param successList
     * @return
     */
    private static boolean sendMessage(String msg, LinkedList<String> successList) {


        boolean isFlag = false;

        if (!isFlag) {
            System.out.println(Thread.currentThread().getName() + "推送消息" + msg);
            successList.add(msg);
            isFlag = true;
        }

        return isFlag;
    }


    /**
     * 推送失败，重复推送
     *
     * @param msg
     * @param successList
     */
    private static void repeatSent(String msg, LinkedList<String> successList) {
        //重复推送不大于5次
        boolean result = false;
        for (int i = 0; i < sentNum; i++) {
            result = sendMessage(msg, successList);
            if (!result) {
                break;
            }
        }
        //大于5次则直接推送失败，不再推送
        if (result) {
            System.out.println(Thread.currentThread().getName() + "消息推送失败" + msg);
        }


    }


}
