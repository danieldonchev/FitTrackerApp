// automatically generated by the FlatBuffers compiler, do not modify

package flatbuf;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class Split extends Struct {
  public void __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; }
  public Split __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public int id() { return bb.getInt(bb_pos + 0); }
  public double distance() { return bb.getDouble(bb_pos + 8); }
  public long duration() { return bb.getLong(bb_pos + 16); }

  public static int createSplit(FlatBufferBuilder builder, int id, double distance, long duration) {
    builder.prep(8, 24);
    builder.putLong(duration);
    builder.putDouble(distance);
    builder.pad(4);
    builder.putInt(id);
    return builder.offset();
  }
}

