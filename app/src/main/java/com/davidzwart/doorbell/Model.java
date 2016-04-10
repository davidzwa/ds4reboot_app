package com.davidzwart.doorbell;

public class Model{
    String name;
    int value = 1; /* 0 -&gt; checkbox disable, 1 -&gt; checkbox enable */

    Model(String name, int value){
        this.name = name;
        this.value = value;
    }

    Model(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    public int getValue(){
        return this.value;
    }

}