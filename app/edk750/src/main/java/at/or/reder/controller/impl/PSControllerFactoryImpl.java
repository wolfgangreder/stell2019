/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package at.or.reder.controller.impl;

import at.or.reder.controller.PSController;
import at.or.reder.controller.PSControllerEvent;
import at.or.reder.controller.PSControllerEventListener;
import at.or.reder.controller.PSControllerEventType;
import at.or.reder.controller.PSControllerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.ControllerId;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = PSControllerFactory.class)
public final class PSControllerFactoryImpl implements PSControllerFactory
{

  public static final Logger LOGGER = Logger.getLogger("at.or.reder.pscontroller");
  public static final String VEND_SONY = "054c";
  public static final String COMP_PS = "09cc";
  private static final AtomicInteger threadCounter = new AtomicInteger();
  private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4,
                                                                                     this::createNewThread);
  private final Map<ControllerId, PSController> registeredController = new HashMap<>();
  private ScheduledFuture<?> scanFuture;
  private final Object lock = new Object();
  private int scanCounter;
  private long delay;
  private final Set<PSControllerEventListener> actionListener = new CopyOnWriteArraySet<>();

  public PSControllerFactoryImpl()
  {
    startScanning();
  }

  private Thread createNewThread(Runnable r)
  {
    return new Thread(r,
                      "PSControllerThread-" + threadCounter.incrementAndGet());
  }

  private void disconnect()
  {
    synchronized (lock) {
      registeredController.values().stream().forEach((c) -> {
        try {
          c.close();
        } catch (IOException ex) {
          LOGGER.log(Level.SEVERE,
                     null,
                     ex);
        }
      });
      registeredController.clear();
    }
  }

  void pollFail(PSControllerImpl controller)
  {
    startScanning();
  }

  @Override
  public void startScanning()
  {
    synchronized (lock) {
      disconnect();
      scanCounter = 0;
      switchScanFrequence(100);
    }
  }

  @Override
  public void stopScanning()
  {
    synchronized (lock) {
      if (scanFuture != null) {
        scanFuture.cancel(true);
        scanFuture = null;
        scanCounter = 0;
      }
    }
  }

  @Override
  public void scan()
  {
    innerScan(true);
  }

//  @SuppressWarnings("SleepWhileHoldingLock")
  private void switchScanFrequence(long time)
  {
    synchronized (lock) {
      if (scanFuture != null) {
        int waitCounter = 0;
        while (waitCounter < 10 && !scanFuture.isDone()) {
          LOGGER.log(Level.FINE,
                     "Canceling scanTast");
          scanFuture.cancel(true);
//        try {
//          Thread.sleep(100);
//        } catch (InterruptedException ex) {
//        }
          ++waitCounter;
        }
      }
      delay = time;
      LOGGER.log(Level.FINE,
                 () -> "Switching to " + time + " ms");
      scanFuture = executor.scheduleAtFixedRate(this::innerScan,
                                                time,
                                                time,
                                                TimeUnit.MILLISECONDS);
    }
  }

  private void innerScan()
  {
    innerScan(false);
  }

  private void innerScan(boolean oneShot)
  {
    synchronized (lock) {
      if (!oneShot) {
        LOGGER.log(Level.FINE,
                   () -> "Delay = " + delay);

        if ((scanCounter > 10) & (delay < 1000)) {
          switchScanFrequence(1000);
        }
        if ((scanCounter > 100) && (delay < 5000)) {
          switchScanFrequence(5_000);
        } else if ((scanCounter > 200) && (delay < 10_000)) {
          switchScanFrequence(10_000);
        }
      }
      ControllerEnvironment env = ControllerEnvironment.getDefaultEnvironment();
      env.rescanController();
      List<Controller> newController = env.getControllers();
      Set<ControllerId> oldController = new HashSet<>(registeredController.keySet());
      for (Controller c : newController) {
        if (!oldController.remove(c.getId()) && isPSController(c.getId())) {
          registerController(c);
        }
      }
      for (ControllerId id : oldController) { // übrig bleiben die nicht mehr verbundenen
        unregisterController(id);
      }
      ++scanCounter;
    }
  }

  private PSController registerController(Controller c)
  {
    PSController controller = registeredController.get(c.getId());
    if (controller == null) {
      controller = new PSControllerImpl(this,
                                        c,
                                        executor);
      registeredController.put(c.getId(),
                               controller);
      fireAction(controller,
                 PSControllerEventType.OPEN);
      return controller;
    }
    return null;
  }

  private PSController unregisterController(ControllerId id)
  {
    PSController controller = registeredController.remove(id);
    if (controller != null) {
      try {
        controller.close();
        fireAction(controller,
                   PSControllerEventType.CLOSE);
      } catch (IOException ex) {
        LOGGER.log(Level.SEVERE,
                   null,
                   ex);
      }
      return controller;
    }
    return null;

  }

  private boolean isPSController(ControllerId id)
  {
    return (id.getVendor().equals(VEND_SONY) && id.getDevice().equals(COMP_PS));
  }

  @Override
  public List<PSController> getConnectedController()
  {
    synchronized (lock) {
      return new ArrayList<>(registeredController.values());
    }
  }

  private void fireAction(PSController controller,
                          PSControllerEventType type)
  {
    final PSControllerEvent evt = new PSControllerEvent(this,
                                                        type,
                                                        controller);
    actionListener.forEach((l) -> l.onControllerEvent(evt));
  }

  @Override
  public void addPSControllerEventListener(PSControllerEventListener l)
  {
    if (l != null) {
      actionListener.add(l);
    }
  }

  @Override
  public void removePSControllerEventListener(PSControllerEventListener l)
  {
    actionListener.remove(l);
  }

}
