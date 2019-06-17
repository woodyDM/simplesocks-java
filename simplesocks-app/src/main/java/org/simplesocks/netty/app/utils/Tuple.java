package org.simplesocks.netty.app.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public final class Tuple<K,V> {

    private K key;
    private V value;



    public Tuple(@NonNull K key, @NonNull V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return "{" + key +", " + value +'}';
    }


}
