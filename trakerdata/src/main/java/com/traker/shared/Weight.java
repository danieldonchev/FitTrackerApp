package com.traker.shared;

import com.google.flatbuffers.FlatBufferBuilder;

import java.nio.ByteBuffer;
import java.util.UUID;


public class Weight implements FlatBufferSerializable {

    public double weight;
    public long date;
    public long lastModified;

    public Weight(){}

    public Weight(double weight, long date){
        this.weight = weight;
        this.date = date;
    }

    public Weight(double weight, long date, long lastModified){
        this(weight, date);
        this.lastModified = lastModified;
    }

    @Override
    public byte[] serialize() {
        FlatBufferBuilder builder = new FlatBufferBuilder(0);

        int finish = weightInt(builder);
        builder.finish(finish);

        ByteBuffer buf = builder.dataBuffer();
        byte[] array = new byte[buf.remaining()];
        buf.get(array);
        return array;
    }

    @Override
    public Weight deserialize(byte[] bytesRead) {
        ByteBuffer buf = ByteBuffer.wrap(bytesRead);
        flatbuf.Weight weightBufferer = flatbuf.Weight.getRootAsWeight(buf);

        weight = weightBufferer.weight();
        date = weightBufferer.date();
        lastModified = weightBufferer.lastModified();

        return this;
    }

    public int weightInt(FlatBufferBuilder builder){

        flatbuf.Weight.startWeight(builder);
        flatbuf.Weight.addWeight(builder, weight);
        flatbuf.Weight.addDate(builder, date);
        flatbuf.Weight.addLastModified(builder, lastModified);
        return flatbuf.Weight.endWeight(builder);
    }
}
