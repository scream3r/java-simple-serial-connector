/*
 * GNU Lesser General Public License v3.0
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package jssc;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import jssc.junit.rules.VirtualPortRule;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;

public class VirtualPortTest {

  private static final String HELLO_WORLD = "Hello, world!";

  private final byte[] bytes;

  @Rule
  public VirtualPortRule virtualPort = new VirtualPortRule();

  public VirtualPortTest() throws UnsupportedEncodingException {
    this.bytes = HELLO_WORLD.getBytes("UTF-8");
  }

  @Test
  public void testOpenPort() throws IOException {
    // given virtualcom port is available
    Assume.assumeTrue(this.virtualPort.isAvailable());

    // given two virtual ports (null modem)
    final SerialNativeInterface serial1 = new SerialNativeInterface();
    final long handle = serial1.openPort(this.virtualPort.getVirtualCom1().getAbsolutePath(), false);

    final SerialNativeInterface serial2 = new SerialNativeInterface();
    final long handle2 = serial2.openPort(this.virtualPort.getVirtualCom2().getAbsolutePath(), false);

    // when bytes are written to port 1
    serial1.writeBytes(handle, this.bytes);
    serial1.closePort(handle);

    // expect same output on port 2.
    final byte[] readBytes = serial2.readBytes(handle2, this.bytes.length);
    serial2.closePort(handle2);

    final String readString = new String(readBytes, "UTF-8");
    assertThat(readString, is(HELLO_WORLD));
  }

}
