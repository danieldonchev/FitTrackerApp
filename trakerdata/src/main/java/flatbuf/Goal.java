// automatically generated by the FlatBuffers compiler, do not modify

package flatbuf;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class Goal extends Table {
  public static Goal getRootAsGoal(ByteBuffer _bb) { return getRootAsGoal(_bb, new Goal()); }
  public static Goal getRootAsGoal(ByteBuffer _bb, Goal obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public Goal __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public String id() { int o = __offset(4); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer idAsByteBuffer() { return __vector_as_bytebuffer(4, 1); }
  public int type() { int o = __offset(6); return o != 0 ? bb.getInt(o + bb_pos) : 0; }
  public double distance() { int o = __offset(8); return o != 0 ? bb.getDouble(o + bb_pos) : 0.0; }
  public long duration() { int o = __offset(10); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }
  public long calories() { int o = __offset(12); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }
  public long steps() { int o = __offset(14); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }
  public long fromDate() { int o = __offset(16); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }
  public long toDate() { int o = __offset(18); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }
  public long lastModified() { int o = __offset(20); return o != 0 ? bb.getLong(o + bb_pos) : 0L; }

  public static int createGoal(FlatBufferBuilder builder,
      int idOffset,
      int type,
      double distance,
      long duration,
      long calories,
      long steps,
      long fromDate,
      long toDate,
      long last_modified) {
    builder.startObject(9);
    Goal.addLastModified(builder, last_modified);
    Goal.addToDate(builder, toDate);
    Goal.addFromDate(builder, fromDate);
    Goal.addSteps(builder, steps);
    Goal.addCalories(builder, calories);
    Goal.addDuration(builder, duration);
    Goal.addDistance(builder, distance);
    Goal.addType(builder, type);
    Goal.addId(builder, idOffset);
    return Goal.endGoal(builder);
  }

  public static void startGoal(FlatBufferBuilder builder) { builder.startObject(9); }
  public static void addId(FlatBufferBuilder builder, int idOffset) { builder.addOffset(0, idOffset, 0); }
  public static void addType(FlatBufferBuilder builder, int type) { builder.addInt(1, type, 0); }
  public static void addDistance(FlatBufferBuilder builder, double distance) { builder.addDouble(2, distance, 0.0); }
  public static void addDuration(FlatBufferBuilder builder, long duration) { builder.addLong(3, duration, 0L); }
  public static void addCalories(FlatBufferBuilder builder, long calories) { builder.addLong(4, calories, 0L); }
  public static void addSteps(FlatBufferBuilder builder, long steps) { builder.addLong(5, steps, 0L); }
  public static void addFromDate(FlatBufferBuilder builder, long fromDate) { builder.addLong(6, fromDate, 0L); }
  public static void addToDate(FlatBufferBuilder builder, long toDate) { builder.addLong(7, toDate, 0L); }
  public static void addLastModified(FlatBufferBuilder builder, long lastModified) { builder.addLong(8, lastModified, 0L); }
  public static int endGoal(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

