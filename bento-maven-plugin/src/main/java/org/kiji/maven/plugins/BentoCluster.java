/**
 * (c) Copyright 2014 WibiData, Inc.
 *
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kiji.maven.plugins;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Preconditions;
import org.apache.maven.plugin.logging.Log;

/**
 * An in-process way to start and stop a Bento cluster running in a Docker container. This class
 * wraps commands sent to the bento script in the Bento cluster installation.
 */
public final class BentoCluster {
  /** The singleton instance. */
  private static BentoCluster instance = null;

  /**
   * Initializes the singleton.
   *
   * @param bentoName name of the Bento cluster container.
   * @param venvRoot to the python virtualenv to install bento-cluster to.
   * @param log the maven log.
   */
  public static void setInstance(final String bentoName, final File venvRoot, final Log log) {
    Preconditions.checkState(
        venvRoot.mkdirs(),
        "Failed to create venv root: %s",
        venvRoot.getAbsolutePath()
    );
    instance = new BentoCluster(bentoName, venvRoot, log);
  }

  /**
   * Returns true if the singleton has been initialized.
   *
   * @return true if the singleton has been initialized.
   */
  public static boolean isInstanceSet() {
    return instance != null;
  }

  /**
   * Gets the singleton instance.
   *
   * @return the singleton instance.
   */
  public static BentoCluster getInstance() {
    return instance;
  }

  /**
   * Bento commands.
   */
  private static final String BENTO_CREATE = "create --output-config-dir=%s";
  private static final String BENTO_START = "start --output-config-dir=%s";
  private static final String BENTO_STOP = "stop";
  private static final String BENTO_RM = "rm";
  private static final String BENTO_STATUS = "status";

  /** Hope the the bento cluster components all start in 120 seconds. */
  private static final long STARTUP_TIMEOUT_MS = 120000L;

  /** Name of the Bento cluster container. */
  private final String mBentoName;

  /** Path to the python virtualenv to install bento-cluster to. */
  private final File mVenvRoot;

  /** The maven log used to communicate with the maven user. */
  private final Log mLog;

  /**
   * Constructs a bento cluster.
   *
   * @param bentoName name of the Bento cluster container.
   * @param venvRoot to the python virtualenv to install bento-cluster to.
   * @param log the maven log.
   */
  private BentoCluster(final String bentoName, final File venvRoot, final Log log) {
    mBentoName = bentoName;
    mVenvRoot = venvRoot;
    mLog = log;
  }

  /**
   * Execute command to start the Bento cluster container within a timeout.
   *
   * @param configDir to write bento hadoop/hbase configuration files to.
   * @param createBento should be set to false to skip creating the bento container.
   * @throws Exception if the Bento cluster container could not be started in the specified timeout.
   */
  public void start(final File configDir, final boolean createBento) throws Exception {
    if (isRunning()) {
      throw new RuntimeException("Cluster already running.");
    }

    // Create a virtualenv for this cluster.
    ShellExecUtil.executeCommand(
        String.format("env python3 -m venv %s", mVenvRoot.getAbsolutePath())
    );
//    ShellExecUtil.executeCommand(
//        String.format("chmod +x %s/activate", mVenvRoot.getAbsolutePath())
//    );

    // Install bento-cluster in this virtualenv.
    ShellExecUtil.executeCommand(
        venvCommand("env python3 -m pip install kiji-bento-cluster -i http://localhost/simple"));

    if (createBento) {
      // Create Bento cluster by running the 'bento create' script.
      mLog.info(String.format("Starting the Bento cluster '%s'...", mBentoName));
      executeAndLogCommand(bentoCommand(String.format(BENTO_CREATE, configDir.getAbsolutePath())));
    }

    // Start Bento cluster by running the 'bento start' script.
    mLog.info(String.format("Creating the bento cluster '%s'...", mBentoName));
    executeAndLogCommand(bentoCommand(String.format(BENTO_START, configDir.getAbsolutePath())));

    // Has the container started as expected within the startup timeout?
    final long startTime = System.currentTimeMillis();
    while (!isRunning()) {
      if (System.currentTimeMillis() - startTime > STARTUP_TIMEOUT_MS) {
        throw new RuntimeException(
            String.format(
                "Could not start the Bento cluster '%s' within required timeout %d.",
                mBentoName,
                STARTUP_TIMEOUT_MS
            )
        );
      }
      Thread.sleep(1000);
    }
  }

  /**
   * Execute command to stop the Bento cluster container. Wait uninterruptibly until the shell
   * command returns.
   *
   * @param deleteBento should be set to false to skip deleting the bento container.
   * @throws Exception if the Bento cluster container could not be stopped.
   */
  public void stop(final boolean deleteBento) throws Exception {
    if (!isRunning()) {
      mLog.error("Attempting to shut down a Bento cluster container, but none running.");
      return;
    }

    mLog.info(String.format("Stopping the Bento cluster '%s'...", mBentoName));
    executeAndLogCommand(bentoCommand(BENTO_STOP));

    if (deleteBento) {
      mLog.info(String.format("Deleting the Bento cluster '%s'...", mBentoName));
      executeAndLogCommand(bentoCommand(BENTO_RM));
    }
  }

  /**
   * Check if the Bento cluster container is running, by querying the bento script.
   *
   * @return true if the bento script
   * @throws Exception if the bento script can not be uninterruptibly queried for whether a bento
   * container is running.
   */
  public boolean isRunning() throws Exception {
    return ShellExecUtil
        .executeCommand(bentoCommand(BENTO_STATUS))
        .getStderr()
        .contains("services started");
  }

  /**
   * Returns a shell command executed with a python venv setup.
   *
   * @param command to run with a python venv.
   * @return a formatted string command to execute.
   */
  private String venvCommand(final String command) {
    return String.format(
        "source %s/activate && %s",
        mVenvRoot.getAbsolutePath(),
        command
    );
  }

  /**
   * Returns a bento script shell command to execute.
   *
   * @param command to run in shell such as create, stop, rm, etc.
   * @return a formatted string command to execute.
   */
  private String bentoCommand(final String command) {
    return String.format(
        "bento -n %s %s",
        mBentoName,
        command
    );
  }

  /**
   * Executes a command and logs its stdout/stderr and exit code results to the maven log.
   *
   * @param command to run.
   * @throws IOException if there is an error running the command.
   * @throws ExecutionException if there is an error running the command.
   */
  private void executeAndLogCommand(final String command) throws IOException, ExecutionException {
    final ShellResult result = ShellExecUtil.executeCommand(command);
    mLog.info("Stdout:");
    mLog.info(result.getStdout());
    mLog.info("Stderr:");
    mLog.info(result.getStderr());
  }
}
