// automatically generated by the FlatBuffers compiler, do not modify

package com.daniel.FitTrackerApp;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class SportActivity extends Table {
  public static SportActivity getRootAsSportActivity(ByteBuffer _bb) { return getRootAsSportActivity(_bb, new SportActivity()); }
  public static SportActivity getRootAsSportActivity(ByteBuffer _bb, SportActivity obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public SportActivity __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String id() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer idAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public String activity() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer activityAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }
  public double distance() { int o = __offset(8); return o != 0 ? bb.getDouble(o + bb_pos) : -1.0; }
  public long startTimestamp() { int o = __offset(10); return o != 0 ? bb.getLong(o + bb_pos) : -1L; }
  public long endTimestamp() { int o = __offset(12); return o != 0 ? bb.getLong(o + bb_pos) : -1L; }
  public long duration() { int o = __offset(14); return o != 0 ? bb.getLong(o + bb_pos) : -1L; }
  public int calories() { int o = __offset(16); return o != 0 ? bb.getInt(o + bb_pos) : -1; }
  public long steps() { int o = __offset(18); return o != 0 ? bb.getLong(o + bb_pos) : -1L; }
  public Splits splits() { return splits(new Splits()); }
  public Splits splits(Splits obj) { int o = __offset(20); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }
  public SportActivityMap sportActivityMap() { return sportActivityMap(new SportActivityMap()); }
  public SportActivityMap sportActivityMap(SportActivityMap obj) { int o = __offset(22); return o != 0 ? obj.__assign(__indirect(o + bb_pos), bb) : null; }
  public int type() { int o = __offset(24); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public long lastModified() { int o = __offset(26); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }

  public static int createSportActivity(FlatBufferBuilder builder,
      int idOffset,
      int activityOffset,
      double distance,
      long startTimestamp,
      long endTimestamp,
      long duration,
      int calories,
      long steps,
      int splitsOffset,
      int sportActivityMapOffset,
      int type,
      long last_modified) {
    builder.startObject(12);
    SportActivity.addLastModified(builder, last_modified);
    SportActivity.addSteps(builder, steps);
    SportActivity.addDuration(builder, duration);
    SportActivity.addEndTimestamp(builder, endTimestamp);
    SportActivity.addStartTimestamp(builder, startTimestamp);
    SportActivity.addDistance(builder, distance);
    SportActivity.addType(builder, type);
    SportActivity.addSportActivityMap(builder, sportActivityMapOffset);
    SportActivity.addSplits(builder, splitsOffset);
    SportActivity.addCalories(builder, calories);
    SportActivity.addActivity(builder, activityOffset);
    SportActivity.addId(builder, idOffset);
    return SportActivity.endSportActivity(builder);
  }

  public static void startSportActivity(FlatBufferBuilder builder) { builder.startObject(12); }
  public static void addId(FlatBufferBuilder builder, int idOffset) { builder.addOffset(0, idOffset, 0); }
  public static void addActivity(FlatBufferBuilder builder, int activityOffset) { builder.addOffset(1, activityOffset, 0); }
  public static void addDistance(FlatBufferBuilder builder, double distance) { builder.addDouble(2, distance, -1.0); }
  public static void addStartTimestamp(FlatBufferBuilder builder, long startTimestamp) { builder.addLong(3, startTimestamp, -1L); }
  public static void addEndTimestamp(FlatBufferBuilder builder, long endTimestamp) { builder.addLong(4, endTimestamp, -1L); }
  public static void addDuration(FlatBufferBuilder builder, long duration) { builder.addLong(5, duration, -1L); }
  public static void addCalories(FlatBufferBuilder builder, int calories) { builder.addInt(6, calories, -1); }
  public static void addSteps(FlatBufferBuilder builder, long steps) { builder.addLong(7, steps, -1L); }
  public static void addSplits(FlatBufferBuilder builder, int splitsOffset) { builder.addOffset(8, splitsOffset, 0); }
  public static void addSportActivityMap(FlatBufferBuilder builder, int sportActivityMapOffset) { builder.addOffset(9, sportActivityMapOffset, 0); }
  public static void addType(FlatBufferBuilder builder, int type) { builder.addInt(10, type, 0); }
  public static void addLastModified(FlatBufferBuilder builder, long lastModified) { builder.addLong(11, lastModified, 0L); }
  public static int endSportActivity(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishSportActivityBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
}

