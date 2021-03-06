// automatically generated by the FlatBuffers compiler, do not modify

package com.daniel.FitTrackerApp;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class SportActivities extends Table {
  public static SportActivities getRootAsSportActivities(ByteBuffer _bb) { return getRootAsSportActivities(_bb, new SportActivities()); }
  public static SportActivities getRootAsSportActivities(ByteBuffer _bb, SportActivities obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__assign(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public SportActivities __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public SportActivity sportActivities(int j) { return sportActivities(new SportActivity(), j); }
  public SportActivity sportActivities(SportActivity obj, int j) { int o = __offset(4); return o != 0 ? obj.__assign(__indirect(__vector(o) + j * 4), bb) : null; }
  public int sportActivitiesLength() { int o = __offset(4); return o != 0 ? __vector_len(o) : 0; }

  public static int createSportActivities(FlatBufferBuilder builder,
      int sportActivitiesOffset) {
    builder.startObject(1);
    SportActivities.addSportActivities(builder, sportActivitiesOffset);
    return SportActivities.endSportActivities(builder);
  }

  public static void startSportActivities(FlatBufferBuilder builder) { builder.startObject(1); }
  public static void addSportActivities(FlatBufferBuilder builder, int sportActivitiesOffset) { builder.addOffset(0, sportActivitiesOffset, 0); }
  public static int createSportActivitiesVector(FlatBufferBuilder builder, int[] data) { builder.startVector(4, data.length, 4); for (int i = data.length - 1; i >= 0; i--) builder.addOffset(data[i]); return builder.endVector(); }
  public static void startSportActivitiesVector(FlatBufferBuilder builder, int numElems) { builder.startVector(4, numElems, 4); }
  public static int endSportActivities(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
}

