package Server;

import java.util.concurrent.ConcurrentLinkedQueue;

public class PollQueue implements Runnable{
    private ConcurrentLinkedQueue<Poll> pollQueue;
    public PollQueue(ConcurrentLinkedQueue<Poll> pollQueue){
        this.pollQueue = pollQueue;
    }

    @Override
    public void run() {
        
    }
}