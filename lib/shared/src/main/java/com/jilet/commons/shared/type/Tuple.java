package com.jilet.commons.shared.type;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
@Setter
public class Tuple <T, U> {

    private T first;
    private U second;

     public static <T,U> Tuple<T,U> of(T first, U second){
        Tuple<T,U> tuple = new Tuple<>();
        tuple.first = first;
        tuple.second = second;
        return tuple;
    }

}
