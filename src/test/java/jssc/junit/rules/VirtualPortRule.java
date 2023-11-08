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

package jssc.junit.rules;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import jssc.SerialNativeInterface;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualPortRule implements TestRule {

  private static final Logger LOG = LoggerFactory.getLogger(VirtualPortRule.class);

  private static final ExecutorService executor = Executors.newCachedThreadPool();

  private final List<Future<?>> processes = new ArrayList<Future<?>>();

  private OutputStream outputStream;

  private boolean isAvailable;

  private final Random random = new SecureRandom();

  private File virtualCom1;
  private File virtualCom2;

  public VirtualPortRule() {
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    // is windows
    if (SerialNativeInterface.getOsType() == SerialNativeInterface.OS_WINDOWS) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          base.evaluate();
        }
      };
    }

    // is *nix

    initUnix(description);

    return new Statement() {

      @Override
      public void evaluate() throws Throwable {
        try {
          final BackgroundProcess socatProcess = openSocat();
          final Future<?> socatFuture = executor.submit(socatProcess);
          VirtualPortRule.this.processes.add(socatFuture);

          // Java 8:
          // System.timemillis…
          // while (!socatProcess.getProcess().isAlive()) {
          //  Thread.sleep(50);
          // }

          // java 6 way… pretend it is running.
          Thread.sleep(500);

          VirtualPortRule.this.isAvailable = true;

          base.evaluate();
        } finally {
          // stop socat
          for (final Future<?> process : VirtualPortRule.this.processes) {
            process.cancel(false);
          }

          executor.shutdown();
        }
      }
    };
  }

  private void initUnix(final Description description) {
    final File testTargetDir = new File("./target").getAbsoluteFile();
    final File virtualPortsDir = new File(testTargetDir, "virtual-ports");

    virtualPortsDir.mkdirs();

    final String filename1 = String
        .format("virtualcom-%s-%s-%d", description.getClassName(), description.getMethodName(), this.random.nextInt(10000));
    this.virtualCom1 = new File(virtualPortsDir, filename1);
    final String fileName2 = String
        .format("virtualcom-%s-%s-%d", description.getClassName(), description.getMethodName(), this.random.nextInt(10000));
    this.virtualCom2 = new File(virtualPortsDir, fileName2);
  }

  private BackgroundProcess openSocat() {
    return new BackgroundProcess() {

      private Process process;

      @Override
      public Process getProcess() {
        return this.process;
      }

      @Override
      public void run() {

        try {
          List cmds = asList(
                  "socat",
                  "pty,link=" + VirtualPortRule.this.virtualCom1.getAbsolutePath() + ",rawer,echo=0",
                  "pty,link=" + VirtualPortRule.this.virtualCom2.getAbsolutePath() + ",rawer,echo=0"
          );
          final ProcessBuilder processBuilder = new ProcessBuilder(cmds);
          processBuilder.redirectErrorStream(true);

          LOG.info("Process starting: {}", cmds);
          this.process = processBuilder.start();
          LOG.info("Process started! [{}], ports: [{}] // [{}].", this.process, VirtualPortRule.this.virtualCom1.getName(),
              VirtualPortRule.this.virtualCom2.getName());
          VirtualPortRule.this.outputStream = this.process.getOutputStream();
          this.process.waitFor();
        } catch (final IOException ioEx) {
          throw new IllegalStateException("unable to start socat!", ioEx);
        } catch (final InterruptedException interruptEx) {
          Thread.currentThread().interrupt();
          LOG.debug("interrupted.", interruptEx);
          this.process.destroy();
        }
      }
    };
  }

  public OutputStream getOutputStream() {
    return this.outputStream;
  }

  public boolean isAvailable() {
    return this.isAvailable;
  }

  public File getVirtualCom1() {
    return this.virtualCom1;
  }

  public File getVirtualCom2() {
    return this.virtualCom2;
  }

  @Override
  public String toString() {
    return "VirtualPortRule{" + "processes=" + this.processes
        + ", outputStream=" + this.outputStream
        + ", isAvailable=" + this.isAvailable
        + ", random=" + this.random
        + ", virtualCom1=" + this.virtualCom1
        + ", virtualCom2=" + this.virtualCom2
        + '}';
  }
}
