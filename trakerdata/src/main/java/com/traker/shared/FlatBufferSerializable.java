package com.traker.shared;

public interface FlatBufferSerializable<T>
{
    byte[] serialize();
    T deserialize(byte[] bytesRead);
}
