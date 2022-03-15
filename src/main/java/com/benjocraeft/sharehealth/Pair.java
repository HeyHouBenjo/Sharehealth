package com.benjocraeft.sharehealth;


public class Pair<F, S> {


    public final F first;
    public final S second;

    public Pair(F first, S second){
        this.first = first;
        this.second = second;
    }

    public static <F, S> Pair<F, S> pair(F first, S second){
        return new Pair<>(first, second);
    }
}
