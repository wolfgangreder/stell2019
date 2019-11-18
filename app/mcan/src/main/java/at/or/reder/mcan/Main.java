/*
 * Copyright 2019 Wolfgang Reder.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.or.reder.mcan;

import de.entropia.can.CanSocket;
import de.entropia.can.CanSocket.CanFrame;
import de.entropia.can.CanSocket.CanId;
import de.entropia.can.CanSocket.CanInterface;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Wolfgang Reder
 */
public class Main
{

  private static final Logger LOGGER = Logger.getLogger("at.or.reder.mcan");
  private final InetAddress mcastAddress;
  private final int mcastPort;
  private volatile MulticastSocket mcastSocket;
  private volatile CanSocket canSocket;
  private final Thread mcastRThread;
  private final Thread mcastSThread;
  private final Thread canRThread;
  private final Thread canSThread;
  private final BlockingQueue<ByteBuffer> mcastQueue = new LinkedTransferQueue<>();
  private final BlockingQueue<ByteBuffer> canQueue = new LinkedTransferQueue<>();
  private final Map<Integer, String> ifMap = new HashMap<>();

  private Main(String[] args) throws UnknownHostException, IOException
  {
    discoverInterfaces();
    mcastAddress = InetAddress.getByName(args[0]);
    mcastPort = Integer.parseInt(args[1]);
    mcastRThread = new Thread(this::mcastReceiveLoop,
                              "mcast-receivethread");
    mcastSThread = new Thread(this::mcastSendLoop,
                              "mcast-sendthread");
    canRThread = new Thread(this::canReceiveLoop,
                            "can-receivethread");
    canSThread = new Thread(this::canSendLoop,
                            "can-sendthread");
  }

  private void createInterfaceEntry(Path p)
  {
    Path uevent = Paths.get(p.toString(),
                            "uevent");
    if (Files.isRegularFile(uevent) && Files.isReadable(p)) {
      try (LineNumberReader reader = new LineNumberReader(new FileReader(uevent.toFile()))) {
        String line;
        String name = null;
        int index = -1;
        while ((line = reader.readLine()) != null) {
          String[] parts = line.split("=");
          if (parts.length == 2) {
            switch (parts[0]) {
              case "INTERFACE":
                name = parts[1];
                break;
              case "IFINDEX":
                try {
                  index = Integer.parseInt(parts[1]);
                } catch (Throwable th) {
                }
                break;
            }
          }
        }
        if (name != null && !name.isBlank() && index >= 0) {
          ifMap.put(index,
                    name);
        }
      } catch (IOException ex) {
        LOGGER.log(Level.SEVERE,
                   ex,
                   () -> "createInterfaceEntry " + p.toString());
      }
    }
  }

  private boolean isCanInterface(Path p,
                                 BasicFileAttributes attr)
  {
    if (attr.isDirectory()) {
      String fileName = p.getFileName().toString();
      return fileName.startsWith("can");
    }
    return false;
  }

  private void discoverInterfaces() throws IOException
  {
    Path p = Paths.get("/sys/class/net");
    Files.find(p,
               1,
               this::isCanInterface,
               FileVisitOption.FOLLOW_LINKS).
            map((path) -> {
              try {
                return path.toRealPath();
              } catch (IOException ex) {
              }
              return null;
            }).
            filter((path) -> path != null).
            forEach(this::createInterfaceEntry);
  }

  private static StringBuilder byteBuffer2HexString(ByteBuffer buffer,
                                                    StringBuilder builder,
                                                    char interByteChar)
  {
    Objects.requireNonNull(buffer,
                           "input is null");
    StringBuilder result;
    if (builder == null) {
      result = new StringBuilder();
    } else {
      result = builder;
    }
    ByteBuffer tmp = buffer.slice();
    boolean oneAdded = tmp.hasRemaining();
    while (tmp.hasRemaining()) {
      String s = Integer.toHexString(tmp.get() & 0xff);
      if (s.length() == 1) {
        result.append("0");
      }
      result.append(s);
      if (interByteChar != 0) {
        result.append(interByteChar);
      }
    }
    if (oneAdded && interByteChar != 0) {
      result.setLength(result.length() - 1); // remove last interByteChar
    }
    return result;
  }

  private void run() throws IOException
  {
    startMCAST();
    startCAN();
    mcastRThread.start();
    canRThread.start();
    mcastSThread.start();
    canSThread.start();
  }

  private void canSendLoop()
  {
    while (true) {
      try {
        ByteBuffer buffer = canQueue.poll(1,
                                          TimeUnit.SECONDS);
        if (buffer != null) {
          int nanos = buffer.getInt();
          long seconds = buffer.getLong();
          int ifIndex = buffer.get() & 0xff;
          int flags = buffer.get() & 0xff;
          String ifName = ifMap.get(ifIndex);
          CanInterface iface = new CanSocket.CanInterface(canSocket,
                                                          ifName);
          CanId canId = new CanId(buffer.getInt());
          if ((flags & 0x01) != 0) {
            canId.setEFFSFF();
          } else {
            canId.clearEFFSFF();
          }
          if ((flags & 0x80) != 0) {
            canId.setERR();
          } else {
            canId.clearERR();
          }
          if ((flags & 0x02) != 0) {
            canId.setRTR();
          } else {
            canId.clearRTR();
          }
          byte[] data;
          if (buffer.hasRemaining()) {
            data = new byte[buffer.remaining()];
            buffer.get(data);
          } else {
            data = new byte[0];
          }
          CanFrame frame = new CanFrame(iface,
                                        canId,
                                        data);
          canSocket.send(frame);
        }
      } catch (Throwable th) {
        LOGGER.log(Level.SEVERE,
                   th,
                   () -> "canSendLoop");
      }
    }

  }

  private void canReceiveLoop()
  {
    while (true) {
      try {
        CanFrame frame = canSocket.recv();
        Instant now = Instant.now();
        long seconds = now.getEpochSecond();
        int nanos = now.getNano();
        CanId id = frame.getCanId();
        byte[] data = frame.getData();
        int dlc = data != null ? data.length : 0;
        ByteBuffer buffer = ByteBuffer.allocate(18 + dlc);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(nanos);
        buffer.putLong(seconds);
        frame.getCanInterfacae().resolveIfName(canSocket);

        String ifName = frame.getCanInterfacae().getIfName();
        buffer.put((byte) frame.getCanInterfacae().getInterfaceIndex());
        byte tmp = 0;
        if (id.isSetEFFSFF()) {
          tmp |= 0x01;
        }
        if (id.isSetERR()) {
          tmp |= 0x80;
        }
        if (id.isSetRTR()) {
          tmp |= 0x02;
        }
        buffer.put(tmp);
        buffer.putInt(id.getId());
        if (data != null) {
          buffer.put(data);
        }
        buffer.flip();
        LOGGER.log(Level.FINEST,
                   byteBuffer2HexString(buffer,
                                        new StringBuilder(ifName).append("->"),
                                        ' ').toString());
        mcastQueue.put(buffer);
      } catch (Throwable th) {
        LOGGER.log(Level.SEVERE,
                   th,
                   () -> "canReceiveLoop");
      }
    }
  }

  private void mcastSendLoop()
  {
    DatagramPacket packet = new DatagramPacket(new byte[256],
                                               256);
    packet.setAddress(mcastAddress);
    packet.setPort(mcastPort);
    while (true) {
      try {
        ByteBuffer buffer = mcastQueue.poll(1,
                                            TimeUnit.SECONDS);
        if (buffer != null) {
          packet.setData(buffer.array(),
                         0,
                         buffer.remaining());
          mcastSocket.send(packet);
        }
      } catch (Throwable th) {
        LOGGER.log(Level.SEVERE,
                   th,
                   () -> "mcastSendLoop");
      }
    }
  }

  private void mcastReceiveLoop()
  {
    final DatagramPacket packet = new DatagramPacket(new byte[256],
                                                     256);
    while (true) {
      try {
        mcastSocket.receive(packet);
        ByteBuffer buffer = ByteBuffer.allocate(packet.getLength());
        buffer.put(packet.getData(),
                   packet.getOffset(),
                   packet.getLength());
        buffer.flip();
        LOGGER.log(Level.INFO,
                   byteBuffer2HexString(buffer,
                                        new StringBuilder("MCAS->"),
                                        ' ').toString());
        canQueue.put(buffer);
      } catch (SocketTimeoutException ex) {
        // Ignore
      } catch (Throwable th) {
        LOGGER.log(Level.SEVERE,
                   th,
                   () -> "mcastReceiveLoop");
      }
    }
  }

  private void startCAN() throws IOException
  {
    canSocket = new CanSocket(CanSocket.Mode.RAW);
    canSocket.bind(CanSocket.CAN_ALL_INTERFACES);
    LOGGER.log(Level.INFO,
               "Can socket created");
  }

  private void startMCAST() throws IOException
  {
    mcastSocket = new MulticastSocket(mcastPort);
    mcastSocket.setSoTimeout(1000);
    mcastSocket.joinGroup(mcastAddress);
    mcastSocket.setLoopbackMode(true);
    LOGGER.log(Level.INFO,
               "Multicast socket created");
  }

  public static void main(String[] args) throws UnknownHostException, IOException
  {
    new Main(args).run();
//    mcSocket.leaveGroup(mcIPAddress);
//    mcSocket.close();
  }

}
