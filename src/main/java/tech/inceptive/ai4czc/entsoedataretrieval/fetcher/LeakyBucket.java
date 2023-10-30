/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tech.inceptive.ai4czc.entsoedataretrieval.fetcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andres
 */
public class LeakyBucket {
    
    private static final Logger LOGGER = LogManager.getLogger(LeakyBucket.class);

    protected double maxRate;
    protected long minTime;
    // holds time of last action (past or future!)
    protected long lastSchedAction = System.currentTimeMillis();

    public LeakyBucket(double maxRate) {
        this.maxRate = maxRate;
        this.minTime = (long) (1000.0 / maxRate);
    }

    public void consume() throws InterruptedException {
        long curTime = System.currentTimeMillis();
        long timeLeft;

        // calculate when can we do the action
        synchronized (this) {
            timeLeft = lastSchedAction + minTime - curTime;
            if (timeLeft > 0) {
                lastSchedAction += minTime;
            } else {
                lastSchedAction = curTime;
            }
        }

        // If needed, wait for our time
        if (timeLeft <= 0) {
            return;
        } else {
            Thread.sleep(timeLeft);
        }
    }
}
