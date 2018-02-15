// automatically generated by the FlatBuffers compiler, do not modify

package com.daniel.FitTrackerApp;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class Weight extends Table {
  public static Weight getRootAsWeight(ByteBuffer _bb) { return getRootAsWeight(_bb, new Weight()); }
  public static Weight getRootAsWeight(ByteBuffer _bb, Weight obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public Weight __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public double weight() { int o = __offset(4); return o != 0 ? bb.getDouble(o + bb_pos) : 0.0; }
  public long date() { int o = __offset(6); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }
  public long lastModified() { int o = __offset(8); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }

  public static int createWeight(FlatBufferBuilder builder,
      double weight,
      long date,
      long last_modified) {
    builder.startObject(3);
    Weight.addLastModified(builder, last_modified);
    Weight.addDate(builder, date);
    Weight.addWeight(builder, weight);
    return Weight.endWeight(builder);
  }

  public static void startWeight(FlatBufferBuilder builder) { builder.startObject(3); }
  public static void addWeight(FlatBufferBuilder builder, double weight) { builder.addDouble(0, weight, 0.0); }
  public static void addDate(FlatBufferBuilder builder, long date) { builder.addLong(1, date, 0L); }
  public static void addLastModified(FlatBufferBuilder builder, long lastModified) { builder.addLong(2, lastModified, 0L); }
  public static int endWeight(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

