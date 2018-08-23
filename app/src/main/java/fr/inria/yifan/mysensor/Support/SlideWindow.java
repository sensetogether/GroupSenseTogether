package fr.inria.yifan.mysensor.Support;

import java.util.Arrays;

public class SlideWindow {
    private float[] storage;
    private int ct;

    // Constructor requiring size and initial values
    public SlideWindow(int size, float initVal) {
        storage = new float[size];
        ct = 0;
        // Initial fill
        for (int i = 0; i < storage.length; i++) {
            storage[i] = initVal;
        }
        //System.out.println(Arrays.toString(storage));
    }

    // Adding a new value into the window
    public void putValue(float val) {
        // Initial update
        if (ct == 0) {
            for (int i = 0; i < storage.length; i++) {
                storage[i] = val;
            }
        }
        storage[ct % storage.length] = val;
        ct++;
        //System.out.println(Arrays.toString(storage));
    }

    // Return the mean value of the window
    public float getMean() {
        float sum = 0;
        for (float aStorage : storage) {
            sum += aStorage;
        }
        return sum / storage.length;
    }

    // Return the last value in the window
    private float getLast() {
        return ct == 0 ? storage[ct] : storage[(ct - 1) % storage.length];
    }

    // Update the window by adding the last value
    public void updateWindow(){
        this.putValue(this.getLast());
    }

}
