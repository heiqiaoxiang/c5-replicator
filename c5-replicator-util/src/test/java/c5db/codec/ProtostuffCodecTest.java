/*
 * Copyright 2014 WANdisco
 *
 *  WANdisco licenses this file to you under the Apache License,
 *  version 2.0 (the "License"); you may not use this file except in compliance
 *  with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations
 *  under the License.
 */

package c5db.codec;

import io.netty.buffer.ByteBuf;
import io.protostuff.Input;
import io.protostuff.Message;
import io.protostuff.Output;
import io.protostuff.Schema;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ProtostuffCodecTest {


  private static class SerObj implements Message<SerObj>, Schema<SerObj> {
    private int id;
    private String desc;
    private long timestamp;
    private double cost;

    public SerObj(int id, String desc, long timestamp, double cost) {
      this.id = id;
      this.desc = desc;
      this.timestamp = timestamp;
      this.cost = cost;
    }

    private SerObj() {

    }

    @Override
    public Schema<SerObj> cachedSchema() {
      return this;
    }

    @Override
    public String getFieldName(int number) {
      switch (number) {
        case 1:
          return "id";
        case 2:
          return "desc";
        case 3:
          return "timestamp";
        case 4:
          return "cost";
        default:
          return null;
      }
    }

    @Override
    public int getFieldNumber(String name) {
      switch (name) {
        case "id":
          return 1;
        case "desc":
          return 2;
        case "timestamp":
          return 3;
        case "cost":
          return 4;
        default:
          return 0;
      }
    }

    @Override
    public boolean isInitialized(SerObj message) {
      return true;
    }

    @Override
    public SerObj newMessage() {
      return new SerObj();
    }

    @Override
    public String messageName() {
      return getClass().getSimpleName();
    }

    @Override
    public String messageFullName() {
      return getClass().getName();
    }

    @Override
    public Class<? super SerObj> typeClass() {
      return SerObj.class;
    }

    @Override
    public void mergeFrom(Input input, SerObj message) throws IOException {
      for (int number = input.readFieldNumber(this); ; number = input.readFieldNumber(this)) {
        switch (number) {
          case 0:
            return;

          case 1:
            message.id = input.readInt32();
            break;

          case 2:
            message.desc = input.readString();
            break;

          case 3:
            message.timestamp = input.readInt64();
            break;

          case 4:
            message.cost = input.readDouble();
            break;
        }
      }
    }

    @Override
    public void writeTo(Output output, SerObj message) throws IOException {
      output.writeInt32(1, message.id, false);
      output.writeString(2, message.desc, false);
      output.writeInt64(3, message.timestamp, false);
      output.writeDouble(4, message.cost, false);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      SerObj serObj = (SerObj) o;

      if (Double.compare(serObj.cost, cost) != 0) {
        return false;
      }
      if (id != serObj.id) {
        return false;
      }
      if (timestamp != serObj.timestamp) {
        return false;
      }
      return desc.equals(serObj.desc);

    }

    @Override
    public int hashCode() {
      int result;
      long temp;
      result = id;
      result = 31 * result + desc.hashCode();
      result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
      temp = Double.doubleToLongBits(cost);
      result = 31 * result + (int) (temp ^ (temp >>> 32));
      return result;
    }
  }

  @Test
  public void testSerDe() throws Exception {

    SerObj o = new SerObj(11, "hello world", System.currentTimeMillis(), 4.455);
    ProtostuffEncoder<SerObj> enc = new ProtostuffEncoder<>();
    List<Object> objs = new ArrayList<>();
    enc.encode(null, o, objs);
    assertEquals(1, objs.size());

    ProtostuffDecoder<SerObj> dec = new ProtostuffDecoder<>(o);
    List<Object> results = new ArrayList<>();
    dec.decode(null, (ByteBuf) objs.get(0), results);

    assertEquals(1, results.size());

    SerObj aResult = (SerObj) results.get(0);
    assertEquals(o, aResult);

  }

}
