package Server;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PollQueue implements Runnable{
    private ConcurrentLinkedQueue<Poll> pollQueue;
    public PollQueue(ConcurrentLinkedQueue<Poll> pollQueue2){
        this.pollQueue = pollQueue2;
    }


    @Override
    public void run() {
        
    }
}